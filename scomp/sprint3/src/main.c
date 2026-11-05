#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <sys/wait.h>
#include <pthread.h>
#include <fcntl.h>
#include <sys/stat.h>
#include <semaphore.h>
#include <errno.h>

#include "../include/flight_plan.h"
#include "../include/simulation.h"
#include "../include/json_parser.h"
#include "../include/flight_process.h"
#include "../include/semaphore_sync.h"
#include "../include/shm.h"
#include "../include/threads.h"

#define REPORT_FILENAME "simulation_report.txt"

int TIME_STEP_S = 60;

static int parse_time_minutes(const char *s) {
    int h = 0, m = 0; sscanf(s, "%d:%d", &h, &m); return h * 60 + m;
}

static void resolve_departure_conflicts(FlightPlan *plans, int n, int *steps) { // Se 2 partirem ao mesmo tempo, incrementa o step do segundo
    int min = parse_time_minutes(plans[0].departure_time);
    for (int i = 1; i < n; i++) {
        int t = parse_time_minutes(plans[i].departure_time);
        if (t < min) min = t;
    }
    for (int i = 0; i < n; i++)
        steps[i] = parse_time_minutes(plans[i].departure_time) - min;
    for (int i = 0; i < n; i++)
        for (int j = i + 1; j < n; j++)
            if (strcmp(plans[i].legs[0].departure,
                       plans[j].legs[0].departure) == 0 &&
                steps[i] == steps[j])
                steps[j]++;
}


int main(int argc, char *argv[]) {
    char weather_file[WEATHER_FILE_SIZE] = "";
    int weather_enabled = 0;

    if (argc < 2 || argc > 6) {
        fprintf(stderr, "Usage: %s <flights.json> [--timestep <s>] [--weather <csv>]\n", argv[0]);
        return 1;
    }
    for (int i = 2; i < argc; i++) {
        if (strcmp(argv[i], "--timestep") == 0 && i + 1 < argc) {
            int ts = atoi(argv[++i]);
            if (ts <= 0) { fprintf(stderr, "Error: timestep must be positive\n"); return 1; }
            TIME_STEP_S = ts;
        } else if (strcmp(argv[i], "--weather") == 0 && i + 1 < argc) {
            strncpy(weather_file, argv[++i], sizeof(weather_file) - 1);
            weather_file[sizeof(weather_file) - 1] = '\0';
            weather_enabled = 1;
        } else { fprintf(stderr, "Error: unknown argument '%s'\n", argv[i]); return 1; }
    }

    FlightPlan plans[MAX_FLIGHTS];
    int n_flights = 0;
    if (parse_flight_plans(argv[1], plans, &n_flights) != 0 || n_flights == 0) {
        fprintf(stderr, "Failed to load flight plans from %s\n", argv[1]); return 1;
    }
    printf("Loaded %d flight(s).\n", n_flights);
    fflush(stdout);

    for (int i = 0; i < n_flights; i++) {
        FlightPlan *p = &plans[i];
        if (!p->id || !p->n_legs || p->legs[0].n_segments != 3 ||
            !p->legs[0].n_climb || !p->legs[0].n_descend ||
            p->legs[0].cruise_speed_knots <= 0 ||
            p->legs[0].segments[0].end.alt_m <= 0) {
            fprintf(stderr, "Error: flight %d failed validation\n", p->id);
            return 1;
        }
    }

    int start_steps[MAX_FLIGHTS];
    resolve_departure_conflicts(plans, n_flights, start_steps);

    SimState *state = shm_create();
    if (!state) return 1;
    state->n_flights     = n_flights;
    state->global_result = SIM_PASSED;
    state->sim_done      = 0;


    state->environment.enabled = weather_enabled;   //define os dados state para iniciar a simulacao
    state->environment.ready = 0;
    state->environment.count = 0;
    state->environment.last_update_step = 0;
    if (weather_enabled) {
        strncpy(state->environment.source_file, weather_file, WEATHER_FILE_SIZE - 1);
        state->environment.source_file[WEATHER_FILE_SIZE - 1] = '\0';
    } else {
        state->environment.source_file[0] = '\0';
    }


    for (int i = 0; i < n_flights; i++) {   //define os dados dos voos
        state->status[i]     = FLIGHT_RUNNING;
        state->start_step[i] = start_steps[i];
        state->pos_ready[i]  = 0;
    }

    sem_t *step_sems[MAX_FLIGHTS], *done_sems[MAX_FLIGHTS];     // declara dois arrays de ponteiros para semáforos
    if (sem_sync_init(step_sems, done_sems, n_flights) != 0) { // Cria os semáforos POSIX reais. Para cada voo cria dois semáforos step e done
        shm_destroy(); return 1;
    }

    pthread_mutex_t mutex;
    pthread_cond_t  cond_v;
    volatile int    new_v = 0;
    pthread_mutex_init(&mutex, NULL);   //iniciar mutex, passar os endereços & para poder sincronizar, não o valor (cópia)
    pthread_cond_init(&cond_v, NULL);   //iniciar condition variable

    ThreadContext ctx = {   // inicia ThreadContext
        .state = state, .step_sems = step_sems, .done_sems = done_sems,
        .mutex = &mutex, .cond_violation = &cond_v,
        .new_violation = &new_v, .report_filename = REPORT_FILENAME,
        .weather_filename = weather_enabled ? weather_file : NULL
    };

    pthread_t t_safety, t_report, t_environment;
    int environment_started = 0;    // flag para saber se foi criada thread the environment

    /* create the child processes before launching threads */
    for (int i = 0; i < n_flights; i++) {
        pid_t pid = fork();
        if (pid == -1) { perror("fork"); return 1; }
        if (pid == 0) {
            run_flight(&plans[i], state, step_sems, done_sems, i);
        }
        state->pids[i] = pid;
    }

    /* Barreira inicial para meter handlers nos filhos. Evita SIGUSR1 com handler default */
    for (int i = 0; i < n_flights; i++) {
        sem_done_wait(done_sems, i);
    }

    if (weather_enabled) {
        if (thread_environment_launch(&t_environment, &ctx) != 0) {
            sem_sync_destroy(n_flights); shm_destroy(); return 1;
        }
        environment_started = 1;

        while (!state->environment.ready && state->environment.enabled) {   // carrega dados de tempo
            usleep(1000);
        }

        if (!state->environment.enabled) {
            fprintf(stderr, "Error: failed to load weather file '%s'\n", weather_file);
            state->sim_done = 1;
            pthread_join(t_environment, NULL);

            for (int i = 0; i < n_flights; i++) {
                if (state->pids[i] > 0) {
                    kill(state->pids[i], SIGTERM);
                    sem_step_signal(step_sems, i);
                    waitpid(state->pids[i], NULL, 0);
                    state->pids[i] = 0;
                }
            }

            sem_sync_close(step_sems, done_sems, n_flights);
            sem_sync_destroy(n_flights);
            shm_detach(state);
            shm_destroy();
            return 1;
        }
    }

    /*
     * mandar safety/report threads.
     */
    if (threads_launch(&t_safety, &t_report, &ctx) != 0) {  // cria as threads e os semáforos
        sem_sync_destroy(n_flights); shm_destroy(); return 1;
    }

    sem_t *sem_sc = sem_open(SEM_SAFETY_CHECK, 0);
    sem_t *sem_sd = sem_open(SEM_SAFETY_DONE,  0);
    if (sem_sc == SEM_FAILED || sem_sd == SEM_FAILED) {
        perror("sem_open safety (main)"); return 1;
    }


    int active = n_flights;
    int threshold_hit = 0;

    while (active > 0 && !threshold_hit) {
        state->current_step++;

        /* Guarda quais filhos foram realmente autorizados neste step.*/
        int signalled[MAX_FLIGHTS] = {0};

        for (int i = 0; i < n_flights; i++) {
            if (state->status[i] == FLIGHT_RUNNING &&
                state->current_step > state->start_step[i]) {
                sem_step_signal(step_sems, i);      // Autorizar step
                signalled[i] = 1;       // marcar como autorizado
            }
        }

        for (int i = 0; i < n_flights; i++) {
            if (!signalled[i]) continue;    // se não foi autorizado (step seguinte, p.e) avança, por não ter começado ou ter terminado

            sem_done_wait(done_sems, i);    // esperar que filho acabe o step

            if (state->status[i] == FLIGHT_COMPLETED) {
                waitpid(state->pids[i], NULL, 0);       // se terminou naturalemente o voo recolher filho
                state->pids[i] = 0;
                active--;
            } else {
                int ws;
                if (waitpid(state->pids[i], &ws, WNOHANG) == state->pids[i]) {  // verifica se terminou não naturalmente não meteu status
                    state->status[i] = FLIGHT_COMPLETED;
                    state->pids[i]   = 0;
                    active--;
                }
            }
        }

        sem_post(sem_sc);       // depois de receber as posições todas, chama o safety para ver se há violações
        int r;
        do { r = sem_wait(sem_sd); } while (r == -1 && errno == EINTR); // espera que termine de verificar violações. EINTR para voltar para o wait se for interrompido por um sinal

        pthread_mutex_lock(&mutex);
        for (int i = 0; i < n_flights; i++) state->pos_ready[i] = 0;
        int vcount = state->violation_count;
        pthread_mutex_unlock(&mutex);

        if (vcount >= TERMINATION_THRESHOLD) {
            printf("Violation threshold reached. Terminating all flights.\n");
            threshold_hit = 1;

            int terminated_by_parent[MAX_FLIGHTS] = {0};

            for (int i = 0; i < n_flights; i++) {
                if (state->status[i] == FLIGHT_RUNNING && state->pids[i] > 0) {
                    int ws;
                    pid_t wr = waitpid(state->pids[i], &ws, WNOHANG);   // verifica se terminou naturalmente, se sim recolhe

                    if (wr == state->pids[i]) {
                        /* O filho ja acabou naturalmente. Nao vai fazer
                         * outro sem_post(done), por isso nao podemos esperar
                         * em done_sems[i]. */
                        state->status[i] = FLIGHT_COMPLETED;
                        state->pids[i] = 0;
                        active--;
                    } else {
                        kill(state->pids[i], SIGTERM);

                        /* Garante que o filho acorda mesmo se o SIGTERM
                         * chegar antes de ele bloquear no sem_wait(step). */
                        sem_step_signal(step_sems, i);

                        state->status[i] = FLIGHT_TERMINATED;
                        terminated_by_parent[i] = 1;
                        active--;
                    }
                }
            }

            for (int i = 0; i < n_flights; i++) {
                if (terminated_by_parent[i]) {  // recolhe filhos terminados com sigterm
                    /* recolher o processo.*/
                    waitpid(state->pids[i], NULL, 0);
                    state->pids[i] = 0;
                }
            }
        }
    }

    /* Filhos nunca activados*/
    for (int i = 0; i < n_flights; i++) {
        if (state->status[i] == FLIGHT_RUNNING && state->pids[i] > 0) {
            sem_step_signal(step_sems, i);
            kill(state->pids[i], SIGTERM);
            state->status[i] = FLIGHT_TERMINATED;
            sem_done_wait(done_sems, i);
            waitpid(state->pids[i], NULL, 0);
            state->pids[i] = 0;
        }
    }

    /* Filhos completados naturalmente ainda nao recolhidos */
    for (int i = 0; i < n_flights; i++)
        if (state->pids[i] > 0) {
            waitpid(state->pids[i], NULL, 0);
            state->pids[i] = 0;
        }

    pthread_mutex_lock(&mutex);
    if (state->violation_count > 0) state->global_result = SIM_FAILED;
    state->sim_done = 1;
    pthread_cond_broadcast(&cond_v);    // acordar todas as threads bloqueadas
    pthread_mutex_unlock(&mutex);

    sem_post(sem_sc);   // acordar thread safety check sc

    if (environment_started) {
        pthread_join(t_environment, NULL);
    }

    threads_join(t_safety, t_report);

    sem_close(sem_sc);
    sem_close(sem_sd);
    pthread_mutex_destroy(&mutex);
    pthread_cond_destroy(&cond_v);
    sem_sync_close(step_sems, done_sems, n_flights);
    sem_sync_destroy(n_flights);
    shm_detach(state);
    shm_destroy();
    return 0;
}