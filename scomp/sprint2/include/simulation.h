#ifndef SIMULATION_H
#define SIMULATION_H

#include <sys/types.h>

#define MAX_FLIGHTS     32
extern int TIME_STEP_S;

typedef enum {
    FLIGHT_RUNNING,
    FLIGHT_COMPLETED,
    FLIGHT_TERMINATED
} FlightExecutionStatus;

typedef enum {
    SIM_PASSED,
    SIM_FAILED
} SimulationGlobalResult;

// Sent from each child process to parent through pipe every time step
typedef struct {
    int    flight_id;
    double lat;
    double lon;
    double alt_m;
    double speed_knots;
    int    step;
} AircraftPosition;

typedef struct {
    int    step;
    double lat;
    double lon;
    double alt_m;
    int    flight_id_a;
    int    flight_id_b;
    double speed_a;
    double speed_b;
    double heading_a;     //0 = N 90 = E
    double heading_b;
} SafetyViolationEvent;

#define MAX_VIOLATIONS         50
#define TERMINATION_THRESHOLD  100
#define MAX_HISTORY_STEPS 500

// history per flight
typedef struct {
    AircraftPosition entries[MAX_FLIGHTS][MAX_HISTORY_STEPS];
    int              count[MAX_FLIGHTS];
} FlightHistory;


typedef struct {
    AircraftPosition      positions[MAX_FLIGHTS];
    pid_t                 pids[MAX_FLIGHTS];           // pid cada filho
    int                   start_step[MAX_FLIGHTS];     // step de cada voo, 0 ou + se for atrasado
    FlightHistory         history;
    FlightExecutionStatus status[MAX_FLIGHTS];
    SafetyViolationEvent  violations[MAX_VIOLATIONS];
    int                   n_flights;
    int                   violation_count;
    int                   current_step;
    SimulationGlobalResult global_result;
} SimState;

#endif