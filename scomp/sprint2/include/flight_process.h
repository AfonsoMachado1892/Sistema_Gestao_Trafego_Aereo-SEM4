#ifndef FLIGHT_PROCESS_H
#define FLIGHT_PROCESS_H

#include "flight_plan.h"

//simulation loop for one child
// data_pipe_fd is write end and ctrl_pipe_fd is read end
void run_flight(FlightPlan *plan, int data_pipe_fd, int ctrl_pipe_fd);

#endif