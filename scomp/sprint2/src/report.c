#include <stdio.h>
#include "../include/report.h"
#include "../include/simulation.h"


static const char *flight_status_str(FlightExecutionStatus s) {
    switch (s) {
        case FLIGHT_RUNNING:    return "RUNNING";
        case FLIGHT_COMPLETED:  return "COMPLETED";
        case FLIGHT_TERMINATED: return "TERMINATED";
        default:                return "UNKNOWN";
    }
}


int generate_report(SimState *state, const char *filename) {
    FILE *f = fopen(filename, "w");
    if (f == NULL) {
        perror("fopen report");
        return -1;
    }

    fprintf(f, "========================================\n");
    fprintf(f, "       FLIGHT SIMULATION REPORT         \n");
    fprintf(f, "========================================\n\n");

    /* summary */
    fprintf(f, "Total flights    : %d\n", state->n_flights);
    fprintf(f, "Total steps      : %d\n", state->current_step);
    fprintf(f, "Total violations : %d\n", state->violation_count);
    fprintf(f, "Global result    : %s\n\n",
            state->global_result == SIM_PASSED ? "PASSED" : "FAILED");

    fprintf(f, "----------------------------------------\n");
    fprintf(f, "Flight Execution Status\n");
    fprintf(f, "----------------------------------------\n");
    for (int i = 0; i < state->n_flights; i++) {
        fprintf(f, "  Flight ID %d : %s\n",
                state->positions[i].flight_id,
                flight_status_str(state->status[i]));
    }
    fprintf(f, "\n");

    fprintf(f, "----------------------------------------\n");
    fprintf(f, "Safety Violation Events\n");
    fprintf(f, "----------------------------------------\n");

    int recorded = state->violation_count < MAX_VIOLATIONS
                 ? state->violation_count
                 : MAX_VIOLATIONS;

    if (recorded == 0) {
        fprintf(f, "  No violations recorded.\n");
    } else {
        for (int i = 0; i < recorded; i++) {
            SafetyViolationEvent *ev = &state->violations[i];
            fprintf(f, "  Violation %d:\n", i + 1);
            fprintf(f, "    Step         : %d\n",  ev->step);
            fprintf(f, "    Position     : lat=%.4f lon=%.4f alt=%.1fm\n",
                    ev->lat, ev->lon, ev->alt_m);
            fprintf(f, "    Flights      : %d and %d\n",
                    ev->flight_id_a, ev->flight_id_b);
            fprintf(f, "    Velocity %d  : %.1f knots heading %.1f deg\n",
                    ev->flight_id_a, ev->speed_a, ev->heading_a);
            fprintf(f, "    Velocity %d  : %.1f knots heading %.1f deg\n\n",
                    ev->flight_id_b, ev->speed_b, ev->heading_b);
        }
        if (state->violation_count > MAX_VIOLATIONS) {
            fprintf(f, "  ... and %d more violations not recorded.\n",
                    state->violation_count - MAX_VIOLATIONS);
        }
    }

    fprintf(f, "\n========================================\n");
    fprintf(f, "End of report\n");
    fprintf(f, "========================================\n");

    fclose(f);
    return 0;
}