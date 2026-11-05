#ifndef JSON_PARSER_H
#define JSON_PARSER_H

#include "flight_plan.h"

// plans is parsed data and n_plans is the number of plans
int parse_flight_plans(const char *filename, FlightPlan *plans, int *n_plans);

#endif