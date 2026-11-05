#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <fcntl.h>
#include <sys/stat.h>
#include "../include/semaphore_sync.h"
#include "../include/simulation.h"

/* Constrói o nome POSIX do semáforo dado o prefixo e o índice */
static void build_sem_name(char *buf, size_t size, const char *prefix, int i) {
    snprintf(buf, size, "%s%d", prefix, i);
}

int sem_sync_init(sem_t **step_sems, sem_t **done_sems, int n_flights) {
    char name[64];

    for (int i = 0; i < n_flights; i++) {
        /* Remove semáforos órfãos de execuções anteriores */
        build_sem_name(name, sizeof(name), SEM_STEP_PREFIX, i);
        sem_unlink(name);
        build_sem_name(name, sizeof(name), SEM_DONE_PREFIX, i);
        sem_unlink(name);
    }

    for (int i = 0; i < n_flights; i++) {
        build_sem_name(name, sizeof(name), SEM_STEP_PREFIX, i);
        step_sems[i] = sem_open(name, O_CREAT | O_EXCL, 0644, 0);   // criar o semáforo step
        if (step_sems[i] == SEM_FAILED) {
            perror("sem_open step");
            /* limpa os que já foram criados */
            for (int j = 0; j < i; j++) {
                sem_close(step_sems[j]);
                sem_close(done_sems[j]);
            }
            return -1;
        }

        build_sem_name(name, sizeof(name), SEM_DONE_PREFIX, i);
        done_sems[i] = sem_open(name, O_CREAT | O_EXCL, 0644, 0);   // criar o semáforo done
        if (done_sems[i] == SEM_FAILED) {
            perror("sem_open done");
            sem_close(step_sems[i]);
            for (int j = 0; j < i; j++) {
                sem_close(step_sems[j]);
                sem_close(done_sems[j]);
            }
            return -1;
        }
    }
    return 0;
}

void sem_step_signal(sem_t **step_sems, int i) {
    sem_post(step_sems[i]);
}

void sem_done_signal(sem_t **done_sems, int i) {
    sem_post(done_sems[i]);
}

int sem_done_wait(sem_t **done_sems, int i) {   // o sem_step_wait é feito no código em si
    int r;
    do {
        r = sem_wait(done_sems[i]);
    } while (r == -1 && errno == EINTR);
    return r;
}

void sem_sync_close(sem_t **step_sems, sem_t **done_sems, int n_flights) {
    for (int i = 0; i < n_flights; i++) {
        if (step_sems[i] && step_sems[i] != SEM_FAILED)
            sem_close(step_sems[i]);
        if (done_sems[i] && done_sems[i] != SEM_FAILED)
            sem_close(done_sems[i]);
    }
}

void sem_sync_destroy(int n_flights) {
    char name[64];
    for (int i = 0; i < n_flights; i++) {
        build_sem_name(name, sizeof(name), SEM_STEP_PREFIX, i);
        sem_unlink(name);
        build_sem_name(name, sizeof(name), SEM_DONE_PREFIX, i);
        sem_unlink(name);
    }
}