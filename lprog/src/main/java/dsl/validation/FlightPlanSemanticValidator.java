package dsl.validation;

import dsl.model.ImportedFlightPlan;
import dsl.model.ImportedLeg;
import dsl.model.ImportedSegment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FlightPlanSemanticValidator {

    public List<SemanticError> validate(ImportedFlightPlan flightPlan) {
        List<SemanticError> errors = new ArrayList<>();

        validateFlight(flightPlan, errors);
        validateLegs(flightPlan, errors);
        validateRouteCoherence(flightPlan, errors);
        validateAirportRepetition(flightPlan, errors);

        return errors;
    }

    private void validateFlight(ImportedFlightPlan flightPlan, List<SemanticError> errors) {
        if (flightPlan.flightId() == null || flightPlan.flightId().isBlank()) {
            errors.add(new SemanticError(
                    "FLIGHT_ERROR",
                    1,
                    0,
                    "Flight identifier cannot be empty."
            ));
        }

        String type = flightPlan.flightType();

        if (!"regular".equalsIgnoreCase(type) && !"charter".equalsIgnoreCase(type)) {
            errors.add(new SemanticError(
                    "FLIGHT_ERROR",
                    1,
                    0,
                    "Flight type must be regular or charter."
            ));
        }

        if (flightPlan.legs().isEmpty()) {
            errors.add(new SemanticError(
                    "LEG_ERROR",
                    1,
                    0,
                    "Flight must contain at least one leg."
            ));
        }
    }

    private void validateLegs(ImportedFlightPlan flightPlan, List<SemanticError> errors) {
        for (int i = 0; i < flightPlan.legs().size(); i++) {
            ImportedLeg leg = flightPlan.legs().get(i);
            int legNumber = i + 1;

            validateLegDateTimes(leg, legNumber, errors);
            validateFuel(leg, legNumber, errors);
            validateSegments(leg, legNumber, errors);
        }

        validateLegSequence(flightPlan, errors);
    }

    private void validateLegDateTimes(ImportedLeg leg, int legNumber, List<SemanticError> errors) {
        if (leg.departureDateTime() == null) {
            errors.add(new SemanticError(
                    "DATE_TIME_ERROR",
                    leg.departureLine(),
                    leg.departureColumn(),
                    "Leg " + legNumber + " departure date/time is invalid."
            ));
        }

        if (leg.arrivalDateTime() == null) {
            errors.add(new SemanticError(
                    "DATE_TIME_ERROR",
                    leg.arrivalLine(),
                    leg.arrivalColumn(),
                    "Leg " + legNumber + " arrival date/time is invalid."
            ));
        }

        if (leg.departureDateTime() != null
                && leg.arrivalDateTime() != null
                && !leg.departureDateTime().isBefore(leg.arrivalDateTime())) {

            errors.add(new SemanticError(
                    "TIME_ERROR",
                    leg.departureLine(),
                    leg.departureColumn(),
                    "Leg " + legNumber + " departure time must be before arrival time."
            ));
        }
    }

    private void validateFuel(ImportedLeg leg, int legNumber, List<SemanticError> errors) {
        if (leg.fuel().quantity() <= 0) {
            errors.add(new SemanticError(
                    "FUEL_ERROR",
                    leg.fuel().line(),
                    leg.fuel().column(),
                    "Leg " + legNumber + " fuel quantity must be positive."
            ));
        }
    }

    private void validateSegments(ImportedLeg leg, int legNumber, List<SemanticError> errors) {
        if (leg.segments().isEmpty()) {
            errors.add(new SemanticError(
                    "SEGMENT_ERROR",
                    leg.departureLine(),
                    leg.departureColumn(),
                    "Leg " + legNumber + " must contain at least one segment."
            ));
        }

        for (int i = 0; i < leg.segments().size(); i++) {
            ImportedSegment segment = leg.segments().get(i);
            int segmentNumber = i + 1;

            if (segment.start().equals(segment.end())) {
                errors.add(new SemanticError(
                        "SEGMENT_ERROR",
                        segment.line(),
                        segment.column(),
                        "Leg " + legNumber + ", segment " + segmentNumber + " start and end coordinates must be different."
                ));
            }

            if (segment.altitude() <= 0) {
                errors.add(new SemanticError(
                        "SEGMENT_ERROR",
                        segment.line(),
                        segment.column(),
                        "Leg " + legNumber + ", segment " + segmentNumber + " altitude must be positive."
                ));
            }

            if (segment.width() <= 0) {
                errors.add(new SemanticError(
                        "SEGMENT_ERROR",
                        segment.line(),
                        segment.column(),
                        "Leg " + legNumber + ", segment " + segmentNumber + " width must be positive."
                ));
            }

            if (segment.windSpeed() < 0) {
                errors.add(new SemanticError(
                        "SEGMENT_ERROR",
                        segment.line(),
                        segment.column(),
                        "Leg " + legNumber + ", segment " + segmentNumber + " wind speed cannot be negative."
                ));
            }
        }
    }

    private void validateLegSequence(ImportedFlightPlan flightPlan, List<SemanticError> errors) {
        List<ImportedLeg> legs = flightPlan.legs();

        for (int i = 0; i < legs.size() - 1; i++) {
            ImportedLeg current = legs.get(i);
            ImportedLeg next = legs.get(i + 1);

            if (!current.arrivalAirportCode().equals(next.departureAirportCode())) {
                errors.add(new SemanticError(
                        "LEG_SEQUENCE_ERROR",
                        next.departureLine(),
                        next.departureColumn(),
                        "Arrival airport of leg " + (i + 1) + " must match departure airport of leg " + (i + 2) + "."
                ));
            }

            LocalDateTime currentArrival = current.arrivalDateTime();
            LocalDateTime nextDeparture = next.departureDateTime();

            if (currentArrival != null
                    && nextDeparture != null
                    && !currentArrival.isBefore(nextDeparture)) {

                errors.add(new SemanticError(
                        "LEG_SEQUENCE_ERROR",
                        next.departureLine(),
                        next.departureColumn(),
                        "Arrival time of leg " + (i + 1) + " must be before departure time of leg " + (i + 2) + "."
                ));
            }
        }
    }

    private void validateRouteCoherence(ImportedFlightPlan flightPlan, List<SemanticError> errors) {
        if (flightPlan.legs().isEmpty()) {
            return;
        }

        ImportedLeg firstLeg = flightPlan.legs().get(0);
        ImportedLeg lastLeg = flightPlan.legs().get(flightPlan.legs().size() - 1);

        if (!flightPlan.route().originAirportCode().equals(firstLeg.departureAirportCode())) {
            errors.add(new SemanticError(
                    "ROUTE_ERROR",
                    firstLeg.departureLine(),
                    firstLeg.departureColumn(),
                    "Route origin must match the departure airport of the first leg."
            ));
        }

        if (!flightPlan.route().destinationAirportCode().equals(lastLeg.arrivalAirportCode())) {
            errors.add(new SemanticError(
                    "ROUTE_ERROR",
                    lastLeg.arrivalLine(),
                    lastLeg.arrivalColumn(),
                    "Route destination must match the arrival airport of the last leg."
            ));
        }
    }

    private void validateAirportRepetition(ImportedFlightPlan flightPlan, List<SemanticError> errors) {
        Set<String> visitedAirports = new HashSet<>();

        for (ImportedLeg leg : flightPlan.legs()) {
            if (!visitedAirports.add(leg.departureAirportCode())) {
                errors.add(new SemanticError(
                        "ROUTE_ERROR",
                        leg.departureLine(),
                        leg.departureColumn(),
                        "Airport " + leg.departureAirportCode() + " is visited more than once."
                ));
            }
        }

        if (!flightPlan.legs().isEmpty()) {
            ImportedLeg lastLeg = flightPlan.legs().get(flightPlan.legs().size() - 1);
            String finalArrival = lastLeg.arrivalAirportCode();

            if (!visitedAirports.add(finalArrival)) {
                errors.add(new SemanticError(
                        "ROUTE_ERROR",
                        lastLeg.arrivalLine(),
                        lastLeg.arrivalColumn(),
                        "Airport " + finalArrival + " is visited more than once."
                ));
            }
        }
    }
}