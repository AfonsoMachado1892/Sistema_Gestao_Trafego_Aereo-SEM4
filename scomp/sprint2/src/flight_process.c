#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <string.h>
#include "../include/flight_process.h"
#include "../include/physics.h"
#include "../include/sync.h"
#include "../include/simulation.h"

// flags

static volatile sig_atomic_t got_violation = 0;
static volatile sig_atomic_t got_terminate = 0;

// handlers

static void handle_violation(int signo, siginfo_t *info, void *ctx) {
    (void)signo; (void)info; (void)ctx;
    write(STDOUT_FILENO, "[FLIGHT] Safety violation detected!\n", 36);
    got_violation = 1;
}

static void handle_terminate(int signo, siginfo_t *info, void *ctx) {
    (void)signo; (void)info; (void)ctx;
    write(STDOUT_FILENO, "[FLIGHT] Termination signal received.\n", 38);
    got_terminate = 1;
}

// setup handlers

static void setup_signal_handlers(void) {
    struct sigaction act;

    memset(&act, 0, sizeof(act));
    act.sa_sigaction = handle_violation;
    act.sa_flags     = SA_SIGINFO;
    sigfillset(&act.sa_mask); //bloquear sinais
    sigaction(SIGUSR1, &act, NULL);

    memset(&act, 0, sizeof(act));
    act.sa_sigaction = handle_terminate;
    act.sa_flags     = SA_SIGINFO;
    sigemptyset(&act.sa_mask);
    sigaction(SIGTERM, &act, NULL);
}



void run_flight(FlightPlan *plan, int data_pipe_fd, int ctrl_pipe_fd) {
    setup_signal_handlers();

    // start at beginning of climb
    AircraftPosition pos;
    pos.flight_id   = plan->id;
    pos.lat         = plan->legs[0].segments[0].start.lat;
    pos.lon         = plan->legs[0].segments[0].start.lon;
    pos.alt_m       = plan->legs[0].segments[0].start.alt_m;
    pos.speed_knots = plan->legs[0].climb[0].speed_knots;
    pos.step        = 0;
    int current_segment = 0;

    while (!got_terminate) {
        // wait for parent to authorise
        if (step_wait(ctrl_pipe_fd) != 0) break;

        if (got_terminate) break;

        pos = next_position(plan, &pos, &current_segment);

        // send position
        write(data_pipe_fd, &pos, sizeof(AircraftPosition));

        // wait for parent to send barrier confirmation
        if (step_wait(ctrl_pipe_fd) != 0) break;

        // check flag set by SIGUSR1 handler
        if (got_violation) {
            got_violation = 0;
        }

        //finish
        if (current_segment > 2) break;
    }

    close(data_pipe_fd);
    close(ctrl_pipe_fd);
    exit(0);
}