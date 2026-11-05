package aisafe.flightplan.domain.simulation;

import aisafe.flightplan.domain.FlightPlanID;
import aisafe.flightplan.domain.FuelQuantity;
import aisafe.flightroute.domain.RouteCode;
import eapli.framework.validations.Preconditions;

import java.time.LocalDateTime;
import java.util.List;

public final class SimulatedFlightPlan {

    private final FlightPlanID flightPlanID;
    private final RouteCode routeCode;
    private final LocalDateTime plannedDepartureDateTime;
    private final LocalDateTime plannedArrivalDateTime;
    private final FuelQuantity loadedFuel;
    private final List<SimulatedFlightSegment> segments;

    private SimulatedFlightPlan(final FlightPlanID flightPlanID,
                               final RouteCode routeCode,
                               final LocalDateTime plannedDepartureDateTime,
                               final LocalDateTime plannedArrivalDateTime,
                               final FuelQuantity loadedFuel,
                               final List<SimulatedFlightSegment> segments) {
        Preconditions.noneNull(flightPlanID, "Flight plan ID must not be null.");
        Preconditions.noneNull(routeCode, "Route code must not be null.");
        Preconditions.noneNull(plannedDepartureDateTime, "Planned departure date/time must not be null.");
        Preconditions.noneNull(plannedArrivalDateTime, "Planned arrival date/time must not be null.");
        Preconditions.noneNull(loadedFuel, "Loaded fuel must not be null.");
        Preconditions.nonEmpty(segments, "A simulated flight plan must have at least one segment.");

        this.flightPlanID = flightPlanID;
        this.routeCode = routeCode;
        this.plannedDepartureDateTime = plannedDepartureDateTime;
        this.plannedArrivalDateTime = plannedArrivalDateTime;
        this.loadedFuel = loadedFuel;
        this.segments = List.copyOf(segments);
    }

    public static SimulatedFlightPlan of(final FlightPlanID flightPlanID,
                                         final RouteCode routeCode,
                                         final LocalDateTime plannedDepartureDateTime,
                                         final LocalDateTime plannedArrivalDateTime,
                                         final FuelQuantity loadedFuel,
                                         final List<SimulatedFlightSegment> segments) {
        return new SimulatedFlightPlan(
                flightPlanID,
                routeCode,
                plannedDepartureDateTime,
                plannedArrivalDateTime,
                loadedFuel,
                segments
        );
    }

    public FlightPlanID flightPlanID() {
        return flightPlanID;
    }

    public RouteCode routeCode() {
        return routeCode;
    }

    public LocalDateTime plannedDepartureDateTime() {
        return plannedDepartureDateTime;
    }

    public LocalDateTime plannedArrivalDateTime() {
        return plannedArrivalDateTime;
    }

    public FuelQuantity loadedFuel() {
        return loadedFuel;
    }

    public List<SimulatedFlightSegment> segments() {
        return segments;
    }

    public double totalDistanceKm() {
        return segments.stream()
                .mapToDouble(SimulatedFlightSegment::distanceKm)
                .sum();
    }

    public double highestAltitudeMeters() {
        return segments.stream()
                .mapToDouble(SimulatedFlightSegment::altitudeMeters)
                .max()
                .orElse(0.0);
    }
}