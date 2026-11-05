package aisafe.persistence.impl.jpa;

import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.flightplan.domain.FlightPlan;
import aisafe.flightplan.domain.FlightPlanID;
import aisafe.flightplan.domain.FlightPlanRepository;
import aisafe.flightplan.domain.FlightPlanStatus;
import aisafe.flightroute.domain.RouteCode;
import aisafe.pilot.domain.PilotID;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

public class JpaFlightPlanRepository
        extends AiSafeJpaRepositoryBase<FlightPlan, FlightPlanID, FlightPlanID>
        implements FlightPlanRepository {

    public JpaFlightPlanRepository(final String persistenceUnitName,
                                   final Map extendedProperties) {
        super(persistenceUnitName, extendedProperties, "flightPlanID");
    }

    @Override
    public Iterable<FlightPlan> findByRouteCode(final RouteCode routeCode) {
        final TypedQuery<FlightPlan> query = entityManager().createQuery(
                "SELECT fp FROM FlightPlan fp WHERE fp.route.routeCode = :routeCode",
                FlightPlan.class);
        query.setParameter("routeCode", routeCode);
        return query.getResultList();
    }

    @Override
    public Iterable<FlightPlan> findByAircraftRegistration(
            final AircraftRegistration aircraftRegistration) {
        final TypedQuery<FlightPlan> query = entityManager().createQuery(
                "SELECT fp FROM FlightPlan fp WHERE fp.aircraft.aircraftCode = :aircraftRegistration",
                FlightPlan.class);
        query.setParameter("aircraftRegistration", aircraftRegistration);
        return query.getResultList();
    }

    @Override
    public Iterable<FlightPlan> findByPilotID(final PilotID pilotID) {
        final TypedQuery<FlightPlan> query = entityManager().createQuery(
                "SELECT fp FROM FlightPlan fp WHERE fp.pilot.pilotID = :pilotID",
                FlightPlan.class);
        query.setParameter("pilotID", pilotID);
        return query.getResultList();
    }

    @Override
    public Iterable<FlightPlan> findByStatus(final FlightPlanStatus status) {
        final TypedQuery<FlightPlan> query = entityManager().createQuery(
                "SELECT fp FROM FlightPlan fp WHERE fp.status = :status",
                FlightPlan.class);
        query.setParameter("status", status);
        return query.getResultList();
    }

    @Override
    public boolean existsActiveByRouteCodeAfter(
            final RouteCode routeCode, final LocalDate date) {
        final Long count = entityManager().createQuery(
                        "SELECT COUNT(fp) FROM FlightPlan fp " +
                                "WHERE fp.route.routeCode = :routeCode " +
                                "AND fp.status IN :statuses " +
                                "AND FUNCTION('DATE', fp.plannedDepartureDateTime) > :date",
                        Long.class)
                .setParameter("routeCode", routeCode)
                .setParameter("statuses", java.util.List.of(
                        FlightPlanStatus.DRAFT,
                        FlightPlanStatus.TESTED,
                        FlightPlanStatus.VALIDATED
                ))
                .setParameter("date", date)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public Optional<FlightPlan> findById(final FlightPlanID flightPlanID) {
        final TypedQuery<FlightPlan> query = entityManager().createQuery(
                "SELECT fp FROM FlightPlan fp WHERE fp.flightPlanID = :id",
                FlightPlan.class);
        query.setParameter("id", flightPlanID);
        return query.getResultStream().findFirst();
    }
}