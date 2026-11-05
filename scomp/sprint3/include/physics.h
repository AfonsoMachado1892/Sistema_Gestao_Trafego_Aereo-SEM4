#ifndef PHYSICS_H
#define PHYSICS_H

#include "flight_plan.h"
#include "simulation.h"

// distance between two points
double haversine(double lat1, double lon1, double lat2, double lon2);

// vertical rate from a climb or descend profile
// profile is the array and n is the number of entries
double get_profile_rate(ProfilePoint *profile, int n, double alt_m);

//profile is the array, n is the number of entries
double get_profile_speed(ProfilePoint *profile, int n, double alt_m);

// current_segment is the index in leg->segments[] 0 climb,1=cruise,2=descend
AircraftPosition next_position(FlightPlan *plan, AircraftPosition *current, int *current_segment);

// applies wind drift from the environment data in shared memory
void apply_environment_wind(SimState *state, AircraftPosition *pos);

#endif