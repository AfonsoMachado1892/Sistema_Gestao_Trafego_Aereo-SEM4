package aisafe.persistence.impl.inmemory;

import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.flightplan.domain.FlightPlan;
import aisafe.flightplan.domain.FlightPlanID;
import aisafe.flightplan.domain.FlightPlanRepository;
import aisafe.flightplan.domain.FlightPlanStatus;
import aisafe.flightroute.domain.RouteCode;
import aisafe.pilot.domain.PilotID;
import aisafe.weather.domain.WeatherDataID;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.time.LocalDate;

public class InMemoryFlightPlanRepository
        extends InMemoryDomainRepository<FlightPlan, FlightPlanID>
        implements FlightPlanRepository {

    @Override
    public Iterable<FlightPlan> findByRouteCode(final RouteCode routeCode) {
        return match(flightPlan -> flightPlan.routeCode().equals(routeCode));
    }

    @Override
    public Iterable<FlightPlan> findByAircraftRegistration(
            final AircraftRegistration aircraftRegistration) {
        return match(flightPlan ->
                flightPlan.aircraftRegistration().equals(aircraftRegistration));
    }

    @Override
    public Iterable<FlightPlan> findByPilotID(final PilotID pilotID) {
        return match(flightPlan -> flightPlan.pilotID().equals(pilotID));
    }

    @Override
    public Iterable<FlightPlan> findByStatus(final FlightPlanStatus status) {
        return match(flightPlan -> flightPlan.status() == status);
    }

    @Override
    public boolean existsActiveByRouteCodeAfter(
            final RouteCode routeCode, final LocalDate date) {
        return matchOne(fp ->
                fp.routeCode().equals(routeCode)
                        && isActive(fp.status())
                        && fp.plannedDepartureDateTime().toLocalDate().isAfter(date)
        ).isPresent();
    }

    private boolean isActive(final FlightPlanStatus status) {
        return status == FlightPlanStatus.DRAFT
                || status == FlightPlanStatus.TESTED
                || status == FlightPlanStatus.VALIDATED;
    }
}