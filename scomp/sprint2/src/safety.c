#define _GNU_SOURCE
#include <math.h>
#include <signal.h>
#include <string.h>
#include "../include/safety.h"
#include "../include/physics.h"

/* Calculates heading in degrees (0=North, 90=East) from two positions.      */
static double calc_heading(AircraftPosition *prev, AircraftPosition *curr) {
    double dlat = curr->lat - prev->lat;
    double dlon = curr->lon - prev->lon;
    double heading = atan2(dlon, dlat) * 180.0 / M_PI;
    if (heading < 0.0) heading += 360.0;
    return heading;
}


void check_violations(SimState *state) {
    for (int i = 0; i < state->n_flights; i++) {
        if (state->status[i] != FLIGHT_RUNNING) continue;

        for (int j = i + 1; j < state->n_flights; j++) {
            if (state->status[j] != FLIGHT_RUNNING) continue;

            AircraftPosition *a = &state->positions[i];
            AircraftPosition *b = &state->positions[j];

            /* skip flights that have not yet sent a position update         */
            if (a->flight_id == 0 || b->flight_id == 0) continue;

            double dist_h = haversine(a->lat, a->lon, b->lat, b->lon);
            double dist_v = a->alt_m - b->alt_m;
            if (dist_v < 0.0) dist_v = -dist_v;

            if (dist_h < SAFETY_THRESHOLD_H_M && dist_v < SAFETY_THRESHOLD_V_M) {

                kill(state->pids[i], SIGUSR1);
                kill(state->pids[j], SIGUSR1);

                if (state->violation_count < MAX_VIOLATIONS) {
                    SafetyViolationEvent *ev =
                        &state->violations[state->violation_count];

                    ev->step        = state->current_step;
                    ev->lat         = (a->lat + b->lat) / 2.0;
                    ev->lon         = (a->lon + b->lon) / 2.0;
                    ev->alt_m       = (a->alt_m + b->alt_m) / 2.0;
                    ev->flight_id_a = a->flight_id;
                    ev->flight_id_b = b->flight_id;
                    ev->speed_a     = a->speed_knots;
                    ev->speed_b     = b->speed_knots;

                    /* calculate headings from history if available          */
                    int hi = state->history.count[i];
                    int hj = state->history.count[j];

                    if (hi >= 2) {
                        ev->heading_a = calc_heading(
                            &state->history.entries[i][hi - 2],
                            &state->history.entries[i][hi - 1]);
                    } else {
                        ev->heading_a = 0.0;
                    }

                    if (hj >= 2) {
                        ev->heading_b = calc_heading(
                            &state->history.entries[j][hj - 2],
                            &state->history.entries[j][hj - 1]);
                    } else {
                        ev->heading_b = 0.0;
                    }
                }

                state->violation_count++;
            }
        }
    }
}