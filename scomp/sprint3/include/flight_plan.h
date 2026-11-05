#ifndef FLIGHT_PLAN_H
#define FLIGHT_PLAN_H

#define MAX_PROFILE_POINTS  15
#define MAX_SEGMENTS        10
#define MAX_LEGS             1
#define MAX_CODE_LEN        16
#define MAX_TYPE_LEN        16
#define MAX_TIME_LEN        10

// For climb rate_ms is positive and for descend is negative
typedef struct {
    double altitude_m;
    double speed_knots;     // indicated airspeed at this altitude in knots
    double rate_ms;
} ProfilePoint;

// geographic point
typedef struct {
    double lat;
    double lon;
    double alt_m;
} Waypoint;

// linear path between two waypoints
typedef struct {
    char     mode[MAX_CODE_LEN]; // climb cruise ou descend
    Waypoint start;
    Waypoint end;
} Segment;

// single non-stop journey between two airports.
typedef struct {
    char departure[MAX_CODE_LEN];
    char arrival[MAX_CODE_LEN];
    double fuel_kg;

    ProfilePoint climb[MAX_PROFILE_POINTS];
    int          n_climb;
    ProfilePoint descend[MAX_PROFILE_POINTS];
    int          n_descend;
    double cruise_speed_knots;

    // climb, cruise, descend
    Segment segments[MAX_SEGMENTS];
    int     n_segments;
} Leg;

// One complete flight
typedef struct {
    int  id;
    char type[MAX_TYPE_LEN];
    char route[MAX_CODE_LEN];
    char departure_time[MAX_TIME_LEN];

    Leg  legs[MAX_LEGS];
    int  n_legs;
} FlightPlan;

#endif