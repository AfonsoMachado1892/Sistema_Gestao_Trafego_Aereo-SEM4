#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../include/json_parser.h"
#include "../include/simulation.h"

#define LINE_BUF 256

// Returns 1 if line contains the given key
static int json_line_has_key(const char *line, const char *key) {
    return strstr(line, key) != NULL;
}

// Used to detect top-level flight object openers in the JSON array.
static int is_top_level_object(const char *line) {
    const char *p = line;
    while (*p == ' ' || *p == '\t') p++;
    if (*p != '{') return 0;
    p++;
    while (*p == ' ' || *p == '\t' || *p == '\r' || *p == '\n') p++;
    return *p == '\0';
}

// Extracts the numeric value after "Value": in line, writes to *out.
static int extract_value(const char *line, double *out) {
    const char *p = strstr(line, "\"Value\"");
    if (p == NULL) return 0;
    p = strstr(p, ":");
    if (p == NULL) return 0;
    p++;
    return sscanf(p, " %lf", out) == 1;
}

// Extracts the numeric quantity after "Quantity": in line, writes to *out
static int extract_quantity(const char *line, double *out) {
    const char *p = strstr(line, "\"Quantity\"");
    if (p == NULL) return 0;
    p = strstr(p, ":");
    if (p == NULL) return 0;
    p++;
    return sscanf(p, " %lf", out) == 1;
}

// Extracts a string value after the first ":" in line
static int extract_string(const char *line, char *out, int len) {
    const char *p = strstr(line, ":");
    if (p == NULL) return 0;
    p++;
    while (*p == ' ' || *p == '\t') p++;
    if (*p != '"') return 0;
    p++;
    int i = 0;
    while (*p && *p != '"' && i < len - 1) {
        out[i++] = *p++;
    }
    out[i] = '\0';
    return i > 0;
}

// Reads ProfilePoint entries from f until the closing ']' of the array
static int parse_profile(FILE *f, ProfilePoint *profile, int *n, int is_climb) {
    char line[LINE_BUF];
    *n = 0;
    double alt = 0, speed = 0, rate = 0;
    int has_alt = 0, has_speed = 0, has_rate = 0;
    int reading_rate = 0;

    while (fgets(line, sizeof(line), f)) {
        if (json_line_has_key(line, "]")) break;

        if (json_line_has_key(line, "\"Altitude\"")) {
            has_alt = 0;
            has_speed = 0;
            has_rate = 0;
        }

        const char *rate_key = is_climb ? "\"RateClimb\"" : "\"RateDescent\"";
        if (json_line_has_key(line, rate_key)) {
            reading_rate = 1;
        }

        if (json_line_has_key(line, "\"Value\"")) {
            double v;
            if (!extract_value(line, &v)) continue;

            if (!has_alt) {
                alt = v;
                has_alt = 1;
            } else if (!has_speed) {
                speed = v;
                has_speed = 1;
            } else if (reading_rate) {
                rate = v;
                has_rate = 1;
                reading_rate = 0;
            }
        }

        if (has_alt && has_speed && has_rate && *n < MAX_PROFILE_POINTS) {
            profile[*n].altitude_m   = alt;
            profile[*n].speed_knots  = speed;
            profile[*n].rate_ms      = rate;
            (*n)++;
            has_alt = has_speed = has_rate = 0;
        }
    }
    return 0;
}

// Reads one segment object from f until its closing '}'

static int parse_segment(FILE *f, Segment *seg) {
    char line[LINE_BUF];
    int has_start = 0;
    int depth = 1;

    while (fgets(line, sizeof(line), f)) {
        for (const char *p = line; *p; p++) {
            if (*p == '{') depth++;
            if (*p == '}') depth--;
        }
        if (depth <= 0) break;

        if (json_line_has_key(line, "\"Mode\"")) {
            extract_string(line, seg->mode, MAX_CODE_LEN);
        }
        if (json_line_has_key(line, "\"Start\"")) {
            has_start = 1;
        }
        if (json_line_has_key(line, "\"End\"")) {
            has_start = 0;
        }
        if (json_line_has_key(line, "\"Latitude\"")) {
            double v;
            const char *p = strstr(line, ":");
            if (p && sscanf(p + 1, " %lf", &v) == 1) {
                if (has_start) seg->start.lat = v;
                else           seg->end.lat   = v;
            }
        }
        if (json_line_has_key(line, "\"Longitude\"")) {
            double v;
            const char *p = strstr(line, ":");
            if (p && sscanf(p + 1, " %lf", &v) == 1) {
                if (has_start) seg->start.lon = v;
                else           seg->end.lon   = v;
            }
        }
        if (json_line_has_key(line, "\"Quantity\"")) {
            double v;
            if (extract_quantity(line, &v)) {
                if (has_start) seg->start.alt_m = v;
                else           seg->end.alt_m   = v;
            }
        }
    }
    return 0;
}

// Reads one leg object from f until its closing '}'
static int parse_leg(FILE *f, Leg *leg) {
    char line[LINE_BUF];
    memset(leg, 0, sizeof(Leg));
    int depth = 1;

    while (fgets(line, sizeof(line), f)) {
        for (const char *p = line; *p; p++) {
            if (*p == '{') depth++;
            if (*p == '}') depth--;
        }
        if (depth <= 0) break;

        if (json_line_has_key(line, "\"Departure\"")) {
            extract_string(line, leg->departure, MAX_CODE_LEN);
        }

        if (json_line_has_key(line, "\"Arrival\"")) {
            extract_string(line, leg->arrival, MAX_CODE_LEN);
        }

        if (json_line_has_key(line, "\"Fuel\"")) {
            char next[LINE_BUF];
            if (fgets(next, sizeof(next), f)) {
                for (const char *p = next; *p; p++) {
                    if (*p == '{') depth++;
                    if (*p == '}') depth--;
                }
                extract_quantity(next, &leg->fuel_kg);
            }
        }

        if (json_line_has_key(line, "\"Climb\"")) {
            parse_profile(f, leg->climb, &leg->n_climb, 1);
        }

        if (json_line_has_key(line, "\"Descend\"")) {
            parse_profile(f, leg->descend, &leg->n_descend, 0);
        }

        if (json_line_has_key(line, "\"Cruise\"")) {
            char next[LINE_BUF];
            while (fgets(next, sizeof(next), f)) {
                for (const char *p = next; *p; p++) {
                    if (*p == '{') depth++;
                    if (*p == '}') depth--;
                }
                if (extract_value(next, &leg->cruise_speed_knots)) break;
                if (json_line_has_key(next, "}")) break;
            }
        }

        if (json_line_has_key(line, "\"Segments\"")) {
            while (fgets(line, sizeof(line), f)) {
                if (json_line_has_key(line, "]")) break;
                if (json_line_has_key(line, "{") && leg->n_segments < MAX_SEGMENTS) {
                    parse_segment(f, &leg->segments[leg->n_segments]);
                    leg->n_segments++;
                }
            }
        }
    }
    return 0;
}

static int is_top_level_close(const char *line) {
    if (line[0] != ' ' || line[1] != ' ' || line[2] != '}') return 0;
    const char *p = line + 3;
    if (*p == ',') p++;
    while (*p == ' ' || *p == '\t' || *p == '\r' || *p == '\n') p++;
    return *p == '\0';
}

// Reads one flight plan object from f until its closing '}'.

static int parse_flight_plan(FILE *f, FlightPlan *plan) {
    char line[LINE_BUF];
    memset(plan, 0, sizeof(FlightPlan));

    while (fgets(line, sizeof(line), f)) {
        if (is_top_level_close(line)) {
            break;
        }
        if (json_line_has_key(line, "\"ID\"")) {
            const char *p = strstr(line, ":");
            if (p) sscanf(p + 1, " %d", &plan->id);
        }
        if (json_line_has_key(line, "\"Type\"")) {
            extract_string(line, plan->type, MAX_TYPE_LEN);
        }
        if (json_line_has_key(line, "\"Route\"")) {
            extract_string(line, plan->route, MAX_CODE_LEN);
        }
        if (json_line_has_key(line, "\"DepartureTime\"")) {
            extract_string(line, plan->departure_time, MAX_TIME_LEN);
        }
        if (json_line_has_key(line, "\"Leg\"")) {
            while (fgets(line, sizeof(line), f)) {
                if (json_line_has_key(line, "]")) break;
                if (is_top_level_close(line)) break;
                if (json_line_has_key(line, "{") && plan->n_legs < MAX_LEGS) {
                    parse_leg(f, &plan->legs[plan->n_legs]);
                    plan->n_legs++;
                }
            }
        }
    }
    return 0;
}



int parse_flight_plans(const char *filename, FlightPlan *plans, int *n_plans) {
    FILE *f = fopen(filename, "r");
    if (f == NULL) {
        perror("fopen");
        return -1;
    }

    *n_plans = 0;
    char line[LINE_BUF];

    while (fgets(line, sizeof(line), f)) {
        if (is_top_level_object(line) && *n_plans < MAX_FLIGHTS) {
            if (parse_flight_plan(f, &plans[*n_plans]) == 0) {
                (*n_plans)++;
            }
        }
        if (json_line_has_key(line, "]")) break;
    }
    fclose(f);
    return 0;
}