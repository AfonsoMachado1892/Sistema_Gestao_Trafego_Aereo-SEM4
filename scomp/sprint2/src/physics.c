#define _GNU_SOURCE
#include <math.h>
#include <string.h>
#include "../include/physics.h"
#include "../include/simulation.h"

#define KNOTS_TO_MS     0.514444   /* 1 knot in m/s                          */
#define EARTH_RADIUS_M  6371000.0  /* mean Earth radius in metres            */
#define DEG_TO_RAD      (M_PI / 180.0)


/* Great-circle distance in metres between two lat/lon points.               */
double haversine(double lat1, double lon1, double lat2, double lon2) {
    double dlat = (lat2 - lat1) * DEG_TO_RAD;
    double dlon = (lon2 - lon1) * DEG_TO_RAD;
    double rlat1 = lat1 * DEG_TO_RAD;
    double rlat2 = lat2 * DEG_TO_RAD;

    double a = sin(dlat / 2) * sin(dlat / 2)
             + cos(rlat1) * cos(rlat2)
             * sin(dlon / 2) * sin(dlon / 2);

    double c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a));
    return EARTH_RADIUS_M * c;
}

/* Interpolates vertical rate (m/s) from a climb or descend profile          */
/* at the given altitude. Returns the rate of the last point if alt_m        */
/* exceeds the highest entry.                                                 */
double get_profile_rate(ProfilePoint *profile, int n, double alt_m) {
    if (n <= 0) return 0.0;
    if (alt_m <= profile[0].altitude_m) return profile[0].rate_ms;
    if (alt_m >= profile[n - 1].altitude_m) return profile[n - 1].rate_ms;

    for (int i = 0; i < n - 1; i++) {
        if (alt_m >= profile[i].altitude_m && alt_m < profile[i + 1].altitude_m) {
            double t = (alt_m - profile[i].altitude_m)
                     / (profile[i + 1].altitude_m - profile[i].altitude_m);
            return profile[i].rate_ms + t * (profile[i + 1].rate_ms - profile[i].rate_ms);
        }
    }
    return profile[n - 1].rate_ms;
}

/* Interpolates airspeed (knots) from a climb or descend profile             */
/* at the given altitude.                                                     */
double get_profile_speed(ProfilePoint *profile, int n, double alt_m) {
    if (n <= 0) return 0.0;
    if (alt_m <= profile[0].altitude_m) return profile[0].speed_knots;
    if (alt_m >= profile[n - 1].altitude_m) return profile[n - 1].speed_knots;

    for (int i = 0; i < n - 1; i++) {
        if (alt_m >= profile[i].altitude_m && alt_m < profile[i + 1].altitude_m) {
            double t = (alt_m - profile[i].altitude_m)
                     / (profile[i + 1].altitude_m - profile[i].altitude_m);
            return profile[i].speed_knots + t * (profile[i + 1].speed_knots - profile[i].speed_knots);
        }
    }
    return profile[n - 1].speed_knots;
}

/* Moves current lat/lon towards target by dist_m metres along the great     */
/* circle. Updates pos->lat and pos->lon in place.                           */
static void move_along_segment(AircraftPosition *pos,
                                double target_lat, double target_lon,
                                double dist_m) {
    double total = haversine(pos->lat, pos->lon, target_lat, target_lon);
    if (total < 1.0) return; /* already at destination                       */

    double fraction = dist_m / total;
    if (fraction > 1.0) fraction = 1.0;

    pos->lat = pos->lat + fraction * (target_lat - pos->lat);
    pos->lon = pos->lon + fraction * (target_lon - pos->lon);
}

/* Computes the next AircraftPosition after one TIME_STEP_S interval.        */
/* Updates *current_segment when the flight transitions to the next phase.   */
AircraftPosition next_position(FlightPlan *plan,
                               AircraftPosition *current,
                               int *current_segment) {
    AircraftPosition next = *current;
    next.step = current->step + 1;

    Leg     *leg = &plan->legs[0];
    Segment *seg = &leg->segments[*current_segment];

    if (*current_segment == 0) {
        /*  Climb  */
        double speed_ms = get_profile_speed(leg->climb, leg->n_climb,
                                            current->alt_m) * KNOTS_TO_MS;
        double rate_ms  = get_profile_rate(leg->climb, leg->n_climb,
                                           current->alt_m);

        double dist_m   = speed_ms * TIME_STEP_S;
        move_along_segment(&next, seg->end.lat, seg->end.lon, dist_m);
        next.alt_m      = current->alt_m + rate_ms * TIME_STEP_S;
        next.speed_knots = get_profile_speed(leg->climb, leg->n_climb,
                                             current->alt_m);

        /* transition to cruise when cruise altitude is reached           */
        if (next.alt_m >= seg->end.alt_m) {
            next.alt_m = seg->end.alt_m;
            (*current_segment)++;
        }

    } else if (*current_segment == 1) {
        /*  Cruise  */
        double speed_ms  = leg->cruise_speed_knots * KNOTS_TO_MS;
        double dist_m    = speed_ms * TIME_STEP_S;
        double remaining = haversine(current->lat, current->lon,
                                     seg->end.lat, seg->end.lon);

        move_along_segment(&next, seg->end.lat, seg->end.lon, dist_m);
        next.alt_m       = seg->start.alt_m; /* constant altitude          */
        next.speed_knots = leg->cruise_speed_knots;

        /* transition to descend when end of cruise segment is reached    */
        if (dist_m >= remaining) {
            next.lat = seg->end.lat;
            next.lon = seg->end.lon;
            (*current_segment)++;
        }

    } else if (*current_segment == 2) {
        /*  Descend  */
        double speed_ms = get_profile_speed(leg->descend, leg->n_descend,
                                            current->alt_m) * KNOTS_TO_MS;
        double rate_ms  = get_profile_rate(leg->descend, leg->n_descend,
                                           current->alt_m); /* negative     */

        double dist_m   = speed_ms * TIME_STEP_S;
        move_along_segment(&next, seg->end.lat, seg->end.lon, dist_m);
        next.alt_m      = current->alt_m + rate_ms * TIME_STEP_S;
        next.speed_knots = get_profile_speed(leg->descend, leg->n_descend,
                                             current->alt_m);

        /* clamp altitude to destination elevation                         */
        if (next.alt_m <= seg->end.alt_m) {
            next.alt_m = seg->end.alt_m;
            next.lat   = seg->end.lat;
            next.lon   = seg->end.lon;
            (*current_segment)++;  /* segment > 2 signals flight complete  */
        }
    }

    return next;
}