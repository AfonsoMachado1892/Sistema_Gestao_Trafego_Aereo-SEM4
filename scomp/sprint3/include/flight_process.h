#ifndef FLIGHT_PROCESS_H
#define FLIGHT_PROCESS_H

#include "flight_plan.h"
#include "simulation.h"
#include "semaphore_sync.h"

/*
 * Loop de simulação para um processo filho.
 *
 * plan        — plano de voo deste filho
 * state       — ponteiro para a shared memory
 * step_sems   — array de semáforos step (filho espera em step_sems[idx])
 * done_sems   — array de semáforos done (filho faz post em done_sems[idx])
 * flight_idx  — índice deste voo em state->positions[]
 */
void run_flight(FlightPlan *plan, SimState *state,
                sem_t **step_sems, sem_t **done_sems,
                int flight_idx);

#endif
