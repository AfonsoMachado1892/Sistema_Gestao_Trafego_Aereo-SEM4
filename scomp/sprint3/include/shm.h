#ifndef SHM_H
#define SHM_H

#include "simulation.h"

#define SHM_NAME "/aisafe_sim"

/*
 * Cria a shared memory, define o tamanho e mapeia-a.
 * Deve ser chamada pelo pai ANTES do fork().
 * Devolve ponteiro para SimState ou NULL em caso de erro.
 */
SimState *shm_create(void);

/* Desmapeia a shared memory do espaço de endereçamento do processo. */
void shm_detach(SimState *state);

/* Remove o objeto de shared memory do sistema (chamada apenas pelo pai). */
void shm_destroy(void);

#endif