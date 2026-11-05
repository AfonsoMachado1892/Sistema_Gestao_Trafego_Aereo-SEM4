#ifndef SAFETY_H
#define SAFETY_H

#include <sys/types.h>
#include "simulation.h"

// minimum horizontal distance
#define SAFETY_THRESHOLD_H_M  5000.0

// minimum vertical distance
#define SAFETY_THRESHOLD_V_M   300.0


// send SIGUSR1 to involved child processes, records the event, increments violation_count
void check_violations(SimState *state);

#endif