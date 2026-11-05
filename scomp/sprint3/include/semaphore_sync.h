#ifndef SEMAPHORE_SYNC_H
#define SEMAPHORE_SYNC_H

#include <semaphore.h>

/*
 * Dois semáforos por voo:
 *
 *   sem_step[i]  (init 0) — pai faz sem_post para autorizar filho i a
 *                            calcular o próximo step.
 *   sem_done[i]  (init 0) — filho faz sem_post quando acabou de escrever
 *                            a posição em shared memory; pai espera neste
 *                            semáforo para garantir a barreira.
 *
 * Os nomes POSIX têm a forma "/aisafe_step_<i>" e "/aisafe_done_<i>".
 */

#define SEM_STEP_PREFIX "/aisafe_step_"
#define SEM_DONE_PREFIX "/aisafe_done_"

/*
 * Cria todos os semáforos para n_flights voos.
 * step_sems e done_sems são arrays de tamanho n_flights.
 * Devolve 0 em sucesso, -1 em erro.
 */
int sem_sync_init(sem_t **step_sems, sem_t **done_sems, int n_flights);

/* sem_post em step_sems[i] — autoriza filho i a avançar */
void sem_step_signal(sem_t **step_sems, int i);

/* sem_post em done_sems[i] — filho i assinala que escreveu posição */
void sem_done_signal(sem_t **done_sems, int i);

/* sem_wait em done_sems[i] — pai espera que filho i tenha escrito */
int  sem_done_wait(sem_t **done_sems, int i);

/* Fecha todos os semáforos (sem remover do sistema) */
void sem_sync_close(sem_t **step_sems, sem_t **done_sems, int n_flights);

/* Remove todos os semáforos do sistema (chamar só no pai) */
void sem_sync_destroy(int n_flights);

#endif