#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <sys/wait.h>
#include "../include/flight_plan.h"
#include "../include/simulation.h"
#include "../include/json_parser.h"
#include "../include/flight_process.h"
#include "../include/sync.h"
#include "../include/safety.h"
#include "../include/report.h"

#define REPORT_FILENAME "simulation_report.txt"

int TIME_STEP_S = 60;

static int parse_time_minutes(const char *time_str) {
    int h = 0, m = 0;
    sscanf(time_str, "%d:%d", &h, &m);
    return h * 60 + m;
}

// delay one step
static void resolve_departure_conflicts(FlightPlan *plans, int n, int *start_steps) {
    int min_time = parse_time_minutes(plans[0].departure_time);
    for (int i = 1; i < n; i++) {
        int t = parse_time_minutes(plans[i].departure_time);
        if (t < min_time) min_time = t;
    }
    for (int i = 0; i < n; i++) {
        int t = parse_time_minutes(plans[i].departure_time);
        start_steps[i] = t - min_time;
    }
    for (int i = 0; i < n; i++) {
        for (int j = i + 1; j < n; j++) {
            if (strcmp(plans[i].legs[0].departure,
                       plans[j].legs[0].departure) == 0 &&
                start_steps[i] == start_steps[j]) {
                start_steps[j]++;
            }
        }
    }
}

int main(int argc, char *argv[]) {
    signal(SIGPIPE, SIG_IGN);
    if (argc < 2 || argc > 4) {
            fprintf(stderr, "Usage: %s <flights.json> [--timestep <seconds>]\n", argv[0]);
            return 1;
        }
        for (int i = 2; i < argc; i++) {
            if (strcmp(argv[i], "--timestep") == 0 && i + 1 < argc) {
                int ts = atoi(argv[i + 1]);
                if (ts <= 0) {
                    fprintf(stderr, "Error: timestep must be a positive integer\n");
                    return 1;
                }
                TIME_STEP_S = ts;
                i++;
            } else {
                fprintf(stderr, "Error: unknown argument '%s'\n", argv[i]);
                return 1;
            }
        }

    // parse flight plan
    FlightPlan plans[MAX_FLIGHTS];
    int n_flights = 0;

    if (parse_flight_plans(argv[1], plans, &n_flights) != 0) {
        fprintf(stderr, "Failed to parse flight plans.\n");
        return 1;
    }
    if (n_flights == 0) {
        fprintf(stderr, "No flight plans found in %s.\n", argv[1]);
        return 1;
    }
    printf("Loaded %d flight(s).\n", n_flights);

    // validations
    for (int i = 0; i < n_flights; i++) {
        FlightPlan *p = &plans[i];

        if (p->id == 0) {
            fprintf(stderr, "Error: flight %d has invalid ID\n", i);
            return 1;
        }
        if (p->n_legs == 0) {
            fprintf(stderr, "Error: flight %d has no legs\n", p->id);
            return 1;
        }
        if (p->legs[0].n_segments != 3) {
            fprintf(stderr, "Error: flight %d must have exactly 3 segments (climb, cruise, descend)\n", p->id);
            return 1;
        }
        if (p->legs[0].n_climb == 0) {
            fprintf(stderr, "Error: flight %d has no climb profile\n", p->id);
            return 1;
        }
        if (p->legs[0].n_descend == 0) {
            fprintf(stderr, "Error: flight %d has no descend profile\n", p->id);
            return 1;
        }
        if (p->legs[0].cruise_speed_knots <= 0) {
            fprintf(stderr, "Error: flight %d has invalid cruise speed\n", p->id);
            return 1;
        }
        if (p->legs[0].segments[0].end.alt_m <= 0) {
            fprintf(stderr, "Error: flight %d has invalid climb end altitude\n", p->id);
            return 1;
        }
    }

    // resolve conflitos
    int start_steps[MAX_FLIGHTS];
    resolve_departure_conflicts(plans, n_flights, start_steps);


    SimState state;
    memset(&state, 0, sizeof(SimState));
    state.n_flights     = n_flights;
    state.global_result = SIM_PASSED;
    for (int i = 0; i < n_flights; i++) {
        state.status[i]     = FLIGHT_RUNNING;
        state.start_step[i] = start_steps[i];
    }

    // create data and ctrl pipes then fork
    int data_pipes[MAX_FLIGHTS][2];
    int ctrl_pipes[MAX_FLIGHTS][2];

    for (int i = 0; i < n_flights; i++) {
        if (pipe(data_pipes[i]) == -1) { perror("pipe data"); return 1; }
    }
    if (sync_init(ctrl_pipes, n_flights) != 0) return 1;

    for (int i = 0; i < n_flights; i++) {
        pid_t pid = fork();
        if (pid == -1) { perror("fork"); return 1; }

        if (pid == 0) {
            // close data pipe read and unused write
            for (int j = 0; j < n_flights; j++) {
                close(data_pipes[j][0]);
                if (j != i) close(data_pipes[j][1]);
            }
            //close all control pipe except own read end
            sync_close_child(ctrl_pipes, n_flights, i);

            run_flight(&plans[i], data_pipes[i][1], ctrl_pipes[i][0]);
        }

        state.pids[i] = pid;
        close(ctrl_pipes[i][0]);
    }

    for (int i = 0; i < n_flights; i++) {
        close(data_pipes[i][1]);
    }

    int active = n_flights;

    while (active > 0) {
        state.current_step++;

        // signal compute the next step
        for (int i = 0; i < n_flights; i++) {
            if (state.status[i] == FLIGHT_RUNNING &&
                state.current_step > state.start_step[i]) {
                step_signal(ctrl_pipes[i][1]);
            }
        }

        // read position from flight
        for (int i = 0; i < n_flights; i++) {
            if (state.status[i] != FLIGHT_RUNNING) continue;
            if (state.current_step <= state.start_step[i]) continue;

            AircraftPosition pos;

            ssize_t n = read(data_pipes[i][0], &pos, sizeof(AircraftPosition));

            if (n == 0) {
                state.status[i] = FLIGHT_COMPLETED;
                close(data_pipes[i][0]);
                close(ctrl_pipes[i][1]);
                active--;
            } else if (n == (ssize_t)sizeof(AircraftPosition)) {
                state.positions[i] = pos;

                if (state.history.count[i] < MAX_HISTORY_STEPS) {
                    state.history.entries[i][state.history.count[i]++] = pos;
                }
            }
        }


        check_violations(&state);

        // signal barrier release
        for (int i = 0; i < n_flights; i++) {
            if (state.status[i] == FLIGHT_RUNNING &&
                state.current_step > state.start_step[i]) {
                step_signal(ctrl_pipes[i][1]);
            }
        }

        // terminar se >limite
        if (state.violation_count >= TERMINATION_THRESHOLD) {
            printf("Violation threshold reached. Terminating all flights.\n");
            for (int i = 0; i < n_flights; i++) {
                if (state.status[i] == FLIGHT_RUNNING) {
                    kill(state.pids[i], SIGTERM);
                    state.status[i] = FLIGHT_TERMINATED;
                    close(data_pipes[i][0]);
                    close(ctrl_pipes[i][1]);
                    active--;
                }
            }
            break;
        }
    }

    // wait for children
    for (int i = 0; i < n_flights; i++) {
        waitpid(state.pids[i], NULL, 0);
    }

    if (state.violation_count > 0) state.global_result = SIM_FAILED;

    if (generate_report(&state, REPORT_FILENAME) == 0)
        printf("Report written to %s\n", REPORT_FILENAME);

    return 0;
}