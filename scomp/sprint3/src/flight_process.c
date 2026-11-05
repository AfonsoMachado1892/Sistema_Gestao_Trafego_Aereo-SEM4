#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <string.h>
#include <errno.h>
#include <semaphore.h>
#include "../include/flight_process.h"
#include "../include/physics.h"
#include "../include/semaphore_sync.h"
#include "../include/simulation.h"

static volatile sig_atomic_t got_violation = 0;
static volatile sig_atomic_t got_terminate = 0;

static sem_t **g_done_sems = NULL;
static int     g_idx       = -1;
static volatile sig_atomic_t g_done_posted = 0;

static void handle_violation(int signo, siginfo_t *info, void *ctx) {
    (void)signo;
    (void)info;
    (void)ctx;

    got_violation = 1;
}

static void handle_terminate(int signo, siginfo_t *info, void *ctx) {
    (void)signo;
    (void)info;
    (void)ctx;

    got_terminate = 1;


    if (g_done_sems != NULL && g_idx >= 0 && !g_done_posted) {
        g_done_posted = 1;      // evitar mandar duplos posts ao pai
        sem_post(g_done_sems[g_idx]);   // para o pai não bloquear
    }
}

static void setup_signal_handlers(void) {
    struct sigaction act;

    memset(&act, 0, sizeof(act));
    act.sa_sigaction = handle_violation;
    act.sa_flags     = SA_SIGINFO;
    sigfillset(&act.sa_mask);
    sigdelset(&act.sa_mask, SIGTERM);
    sigaction(SIGUSR1, &act, NULL);

    memset(&act, 0, sizeof(act));
    act.sa_sigaction = handle_terminate;
    act.sa_flags     = SA_SIGINFO;
    sigemptyset(&act.sa_mask);
    sigaction(SIGTERM, &act, NULL);
}

void run_flight(FlightPlan *plan, SimState *state,
                sem_t **step_sems, sem_t **done_sems,
                int flight_idx) {
    g_done_sems   = done_sems;
    g_idx         = flight_idx;
    g_done_posted = 0;

    setup_signal_handlers();



    sem_done_signal(done_sems, flight_idx);

    AircraftPosition pos;
    pos.flight_id   = plan->id;
    pos.lat         = plan->legs[0].segments[0].start.lat;
    pos.lon         = plan->legs[0].segments[0].start.lon;
    pos.alt_m       = plan->legs[0].segments[0].start.alt_m;
    pos.speed_knots = plan->legs[0].climb[0].speed_knots;
    pos.step        = 0;

    int current_segment = 0;

    while (1) {
        int sw;

        do {
            sw = sem_wait(step_sems[flight_idx]);   // espera pelo sem_post(step)
        } while (sw != 0 && errno == EINTR && !got_terminate);

        if (got_terminate || sw != 0) {
            if (!g_done_posted) {   // evitar mandar post além do post do handler sigterm
                g_done_posted = 1;
                sem_done_signal(done_sems, flight_idx);
            }
            break;      // sair por sigterm, antes calcular
        }

        pos = next_position(plan, &pos, &current_segment);      // próxima posição
        apply_environment_wind(state, &pos);
        state->positions[flight_idx] = pos;

        if (state->history.count[flight_idx] < MAX_HISTORY_STEPS) {
            state->history.entries[flight_idx]
                                  [state->history.count[flight_idx]++] = pos;
        }

        state->pos_ready[flight_idx] = 1;

        if (current_segment > 2) {
            state->status[flight_idx] = FLIGHT_COMPLETED;
        }

        if (!g_done_posted) {   // caso normal
            sem_done_signal(done_sems, flight_idx);
        } else {     // recebeu sigterm depois de calcular, sair
            break;
        }

        if (current_segment > 2) {
            break;
        }

        if (got_violation) {
            got_violation = 0;
        }
    }

    state->pos_ready[flight_idx] = 0;
    sem_sync_close(step_sems, done_sems, state->n_flights);
    exit(0);
}
