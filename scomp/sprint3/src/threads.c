#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <signal.h>
#include <math.h>
#include <unistd.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <semaphore.h>
#include <errno.h>
#include "../include/threads.h"
#include "../include/simulation.h"
#include "../include/physics.h"
#include "../include/report.h"
#include "../include/safety.h"

static int load_weather_csv(const char *filename, WeatherCell *cells, int *count) {
    FILE *f = fopen(filename, "r");
    if (f == NULL) {
        perror("fopen weather");
        return -1;
    }

    char line[512];
    int n = 0;

    /* Skip header */
    if (fgets(line, sizeof(line), f) == NULL) {
        fclose(f);
        *count = 0;
        return 0;
    }

    while (fgets(line, sizeof(line), f) != NULL && n < MAX_WEATHER_CELLS) {
        char *fields[12];
        int nf = 0;

        char *tok = strtok(line, ",");
        while (tok != NULL && nf < 12) {
            fields[nf++] = tok;
            tok = strtok(NULL, ",");
        }

        if (nf < 9) continue;

        cells[n].aca           = atoi(fields[0]);
        cells[n].lat1          = atof(fields[1]);
        cells[n].lon1          = atof(fields[2]);
        cells[n].lat2          = atof(fields[3]);
        cells[n].lon2          = atof(fields[4]);
        cells[n].alt_inf_ft    = atof(fields[5]);
        cells[n].alt_sup_ft    = atof(fields[6]);
        cells[n].direction_deg = atof(fields[7]);
        cells[n].value_knot    = atof(fields[8]);
        n++;
    }

    fclose(f);
    *count = n;
    return 0;
}

/* ------------------------------------------------------------------ */
/* Thread environment (US110)                                          */
/*                                                                     */
/* Carrega os dados ambientais de um CSV para a shared memory.          */
/* Depois mantém-se activa e actualiza last_update_step quando o pai    */
/* avança o current_step    */

void *thread_environment_fn(void *arg) {
    ThreadContext *ctx   = (ThreadContext *)arg;
    SimState      *state = ctx->state;

    if (!state->environment.enabled) {
        return NULL;
    }

    WeatherCell local_cells[MAX_WEATHER_CELLS];
    int local_count = 0;

    if (ctx->weather_filename == NULL ||
        load_weather_csv(ctx->weather_filename, local_cells, &local_count) != 0) {
        pthread_mutex_lock(ctx->mutex);
        state->environment.enabled = 0;
        state->environment.ready = 0;
        pthread_mutex_unlock(ctx->mutex);
        return NULL;
    }

    pthread_mutex_lock(ctx->mutex);     // guardar dados, lock para evitar conflitos
    state->environment.count = local_count;
    for (int i = 0; i < local_count; i++) {
        state->environment.cells[i] = local_cells[i];
    }
    state->environment.ready = 1;
    state->environment.last_update_step = state->current_step;
    pthread_mutex_unlock(ctx->mutex);

    int last_step = -1;

    while (1) {
        pthread_mutex_lock(ctx->mutex);

        if (state->sim_done) {
            pthread_mutex_unlock(ctx->mutex);
            break;
        }

        if (state->current_step != last_step) {
            state->environment.last_update_step = state->current_step;
            last_step = state->current_step;
        }

        pthread_mutex_unlock(ctx->mutex);

        usleep(1000); // adormece 1 milissegundo entre cada verificação
    }

    return NULL;
}


/* ------------------------------------------------------------------ */
/* Thread safety (US106)                                               */
/*                                                                     */
/* ------------------------------------------------------------------ */
void *thread_safety_fn(void *arg) {
    ThreadContext *ctx   = (ThreadContext *)arg;
    SimState      *state = ctx->state;

    /* Abre os semáforos de sincronização safety-pai */
    sem_t *sem_check = sem_open(SEM_SAFETY_CHECK, 0);
    sem_t *sem_done  = sem_open(SEM_SAFETY_DONE,  0);
    if (sem_check == SEM_FAILED || sem_done == SEM_FAILED) {
        perror("sem_open safety barrier");
        return NULL;
    }

    while (1) {
        /* Espera que o pai sinalize "posições prontas para verificar" */
        int r;
        do { r = sem_wait(sem_check); } while (r == -1 && errno == EINTR);

        pthread_mutex_lock(ctx->mutex);

        if (state->sim_done) {
            pthread_mutex_unlock(ctx->mutex);
            sem_post(sem_done);
            break;
        }

        /*
         * regra de seguranca: percorre os pares de voos com pos_ready,
         * envia SIGUSR1 aos envolvidos e regista o evento em
         * state->violations[]/violation_count usando
         * SAFETY_THRESHOLD_H_M / SAFETY_THRESHOLD_V_M.
         *
         * Comparamos violation_count antes/depois para saber se houve
         * pelo menos uma violacao nova neste step, e so nesse caso
         * notificamos a thread report via condition variable (US107).
         */
        int before = state->violation_count;
        check_violations(state);

        if (state->violation_count > before) {
            *ctx->new_violation = 1;
            pthread_cond_signal(ctx->cond_violation);
        }

        pthread_mutex_unlock(ctx->mutex);

        /* Sinaliza ao pai que a verificação terminou */
        sem_post(sem_done);
    }

    sem_close(sem_check);
    sem_close(sem_done);
    return NULL;
}

/* ------------------------------------------------------------------ */
/* Thread report (US109 / US111)                                       */
/* ------------------------------------------------------------------ */
void *thread_report_fn(void *arg) {
    ThreadContext *ctx   = (ThreadContext *)arg;
    SimState      *state = ctx->state;

    int last_reported = 0;

    while (1) {
        pthread_mutex_lock(ctx->mutex); // para evitar outras threads alterar parametros

        while (last_reported >= state->violation_count && !state->sim_done)
            pthread_cond_wait(ctx->cond_violation, ctx->mutex);

        int total = state->violation_count;
        int done  = state->sim_done;

        /*
         * Imprime todas as violacoes novas desde a ultima vez que a
         * thread report acordou. Antes imprimia apenas a ultima violacao,
         * o que causava saltos no terminal: 1, 3, 6, 10, ...
         */
        int limit = total;
        if (limit > MAX_VIOLATIONS) limit = MAX_VIOLATIONS;

        for (int i = last_reported; i < limit; i++) {
            SafetyViolationEvent *ev = &state->violations[i];
            printf("[REPORT] Violation %d at step %d: flights %d and %d\n",
                   i + 1, ev->step, ev->flight_id_a, ev->flight_id_b);
            fflush(stdout);
        }

        last_reported = total;
        *ctx->new_violation = 0;

        pthread_mutex_unlock(ctx->mutex);

        if (done) break;
    }

    /* Relatorio final da simulacao (US109) */
    if (generate_report(state, ctx->report_filename) != 0) {
        fprintf(stderr, "Error writing report to %s\n", ctx->report_filename);
        return NULL;
    }

    printf("Report written to %s\n", ctx->report_filename);
    return NULL;
}

/* ------------------------------------------------------------------ */
int thread_environment_launch(pthread_t *t_environment, ThreadContext *ctx) {
    if (pthread_create(t_environment, NULL, thread_environment_fn, ctx) != 0) {
        perror("pthread_create environment");
        return -1;
    }
    return 0;
}

/* ------------------------------------------------------------------ */
int threads_launch(pthread_t *t_safety, pthread_t *t_report,
                   ThreadContext *ctx) {
    /* Cria os semáforos de barreira safety-pai */
    sem_unlink(SEM_SAFETY_CHECK);
    sem_unlink(SEM_SAFETY_DONE);
    sem_t *sc = sem_open(SEM_SAFETY_CHECK, O_CREAT | O_EXCL, 0644, 0);
    sem_t *sd = sem_open(SEM_SAFETY_DONE,  O_CREAT | O_EXCL, 0644, 0);
    if (sc == SEM_FAILED || sd == SEM_FAILED) {
        perror("sem_open safety barrier (create)");
        return -1;
    }
    sem_close(sc);
    sem_close(sd);      // fechar porque não vão ser usados

    if (pthread_create(t_safety, NULL, thread_safety_fn, ctx) != 0) {
        perror("pthread_create safety"); return -1;
    }
    if (pthread_create(t_report, NULL, thread_report_fn, ctx) != 0) {
        perror("pthread_create report"); return -1;
    }
    return 0;
}

void threads_join(pthread_t t_safety, pthread_t t_report) {
    pthread_join(t_safety, NULL);
    pthread_join(t_report, NULL);
    sem_unlink(SEM_SAFETY_CHECK);
    sem_unlink(SEM_SAFETY_DONE);
}