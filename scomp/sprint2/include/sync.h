#ifndef SYNC_H
#define SYNC_H

#include "simulation.h"




// create 1 control pipe per flight
int sync_init(int ctrl_pipes[][2], int n_flights);

// writes 1 byte to parameter to authorise flight
void step_signal(int ctrl_write_fd);

//block on read until parent signals
int step_wait(int ctrl_read_fd);

// child closes all control pipe ends except its own read end
void sync_close_child(int ctrl_pipes[][2], int n_flights, int flight_index);

void sync_cleanup(int ctrl_pipes[][2], int n_flights);

#endif