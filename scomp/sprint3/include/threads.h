#ifndef THREADS_H
#define THREADS_H

#include <pthread.h>
#include <semaphore.h>
#include "simulation.h"
#include "semaphore_sync.h"

/*
 * Semáforos de sincronização pai ↔ thread safety (US108):
 *
 *   sem_safety_check  — pai faz post depois da barreira;
 *                       thread safety espera neste para verificar.
 *   sem_safety_done   — thread safety faz post depois de verificar;
 *                       pai espera neste antes de limpar pos_ready.
 *
 * Nomes POSIX: "/aisafe_safety_check" e "/aisafe_safety_done"
 */
#define SEM_SAFETY_CHECK "/aisafe_safety_check"
#define SEM_SAFETY_DONE  "/aisafe_safety_done"

/*
 * Contexto partilhado entre as threads do processo pai.
 * Passado como argumento às funções de thread.
 */
typedef struct {
    SimState  *state;

    /* Semáforos — a thread safety precisa de sinalizar os filhos */
    sem_t    **step_sems;
    sem_t    **done_sems;

    /* Mutex que protege o SimState nas secções críticas */
    pthread_mutex_t *mutex;

    /* Condition variable: thread safety notifica thread report */
    pthread_cond_t  *cond_violation;

    /* Flag: thread safety escreve 1; thread report lê e processa */
    volatile int    *new_violation;

    /* Ficheiro de relatório */
    const char      *report_filename;

    /* Ficheiro CSV de weather/environment da US110 */
    const char      *weather_filename;
} ThreadContext;

/*
 * Cria e lança as threads safety e report.
 * Devolve 0 em sucesso, -1 em erro.
 */
int threads_launch(pthread_t *t_safety, pthread_t *t_report,
                   ThreadContext *ctx);

/*
 * Cria a thread environment da US110.
 * Devolve 0 em sucesso, -1 em erro.
 */
int thread_environment_launch(pthread_t *t_environment, ThreadContext *ctx);

/*
 * Aguarda o fim das duas threads.
 */
void threads_join(pthread_t t_safety, pthread_t t_report);

/* Funções de thread (internas, mas expostas para facilitar testes) */
void *thread_safety_fn(void *arg);
void *thread_report_fn(void *arg);
void *thread_environment_fn(void *arg);

#endif