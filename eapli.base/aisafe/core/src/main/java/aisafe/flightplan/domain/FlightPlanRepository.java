package aisafe.flightplan.domain;

import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.flightroute.domain.RouteCode;
import aisafe.pilot.domain.PilotID;
import aisafe.weather.domain.WeatherDataID;
import eapli.framework.infrastructure.repositories.Repository;

import java.time.LocalDate;
import java.util.Optional;

public interface FlightPlanRepository extends Repository<FlightPlan, FlightPlanID> {

    Iterable<FlightPlan> findByRouteCode(RouteCode routeCode);

    Iterable<FlightPlan> findByAircraftRegistration(AircraftRegistration aircraftRegistration);

    Iterable<FlightPlan> findByPilotID(PilotID pilotID);

    Iterable<FlightPlan> findByStatus(FlightPlanStatus status);

    boolean existsActiveByRouteCodeAfter(RouteCode routeCode, LocalDate date);

    Optional<FlightPlan> findById(FlightPlanID flightPlanID);

}