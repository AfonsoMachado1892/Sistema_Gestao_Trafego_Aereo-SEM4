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
    double heading_a;
    double heading_b;
} SafetyViolationEvent;

#define MAX_VIOLATIONS        50
#define TERMINATION_THRESHOLD 100
#define MAX_HISTORY_STEPS     500

#define MAX_WEATHER_CELLS     128
#define WEATHER_FILE_SIZE     256

typedef struct {
    int    aca;
    double lat1;
    double lon1;
    double lat2;
    double lon2;
    double alt_inf_ft;
    double alt_sup_ft;
    double direction_deg;
    double value_knot;
} WeatherCell;

typedef struct {
    WeatherCell cells[MAX_WEATHER_CELLS];
    int         count;
    int         enabled;
    int         ready;
    int         last_update_step;
    char        source_file[WEATHER_FILE_SIZE];
} EnvironmentData;

typedef struct {
    AircraftPosition entries[MAX_FLIGHTS][MAX_HISTORY_STEPS];
    int              count[MAX_FLIGHTS];
} FlightHistory;

/*
 * SimState vive inteiramente em shared memory (sprint 3).
 * Todos os campos são acedidos pelo processo pai, pelas threads
 * safety, report e environment, e escritos pelos processos filho.
 *
 * Campos novos face ao sprint 2:
 *   sim_done   — pai escreve 1 quando a simulação termina;
 *                as threads usam-no para sair do seu loop.
 *   pos_ready  — filho escreve 1 depois de atualizar positions[i];
 *                thread safety usa-o como flag de leitura válida.
 *   environment — dados de vento da US110, carregados pela thread
 *                 environment e usados pelos processos de voo.
 */
typedef struct {
    AircraftPosition       positions[MAX_FLIGHTS];
    pid_t                  pids[MAX_FLIGHTS];
    int                    start_step[MAX_FLIGHTS];
    FlightHistory          history;
    FlightExecutionStatus  status[MAX_FLIGHTS];
    SafetyViolationEvent   violations[MAX_VIOLATIONS];
    EnvironmentData        environment;
    int                    n_flights;
    int                    violation_count;
    int                    current_step;
    SimulationGlobalResult global_result;
    volatile int           sim_done;
    volatile int           pos_ready[MAX_FLIGHTS];
} SimState;

#endif
