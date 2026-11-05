package aisafe.flightplan.domain.simulation;

import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.airport.domain.Airport;
import aisafe.flightplan.domain.FlightPlan;
import aisafe.flightroute.domain.FlightRoute;
import eapli.framework.validations.Preconditions;

import java.util.List;

public class SimulatedFlightPlanFactory {

    private static final double DEFAULT_CRUISE_ALTITUDE_METERS = 10_000.0;

    public SimulatedFlightPlan build(final FlightPlan flightPlan,
                                     final FlightRoute route,
                                     final Airport departureAirport,
                                     final Airport arrivalAirport,
                                     final AircraftModel aircraftModel) {
        Preconditions.noneNull(flightPlan, "Flight plan must not be null.");
        Preconditions.noneNull(route, "Flight route must not be null.");
        Preconditions.noneNull(departureAirport, "Departure airport must not be null.");
        Preconditions.noneNull(arrivalAirport, "Arrival airport must not be null.");
        Preconditions.noneNull(aircraftModel, "Aircraft model must not be null.");

        final double simulatedAltitude = DEFAULT_CRUISE_ALTITUDE_METERS;

        final SimulatedFlightSegment segment = SimulatedFlightSegment.of(
                departureAirport.coordinates(),
                arrivalAirport.coordinates(),
                simulatedAltitude
        );

        return SimulatedFlightPlan.of(
                flightPlan.identity(),
                route.routeCode(),
                flightPlan.plannedDepartureDateTime(),
                flightPlan.plannedArrivalDateTime(),
                flightPlan.fuelQuantity(),
                List.of(segment)
        );
    }
}