package aisafe.persistence.impl.jpa;

import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.flight.domain.Flight;
import aisafe.flight.domain.FlightRepository;
import jakarta.persistence.TypedQuery;

import java.util.Map;

public class JpaFlightRepository
        extends AiSafeJpaRepositoryBase<Flight, Long, Long>
        implements FlightRepository {

    public JpaFlightRepository(final String persistenceUnitName, final Map extendedProperties) {
        super(persistenceUnitName, extendedProperties, "id");
    }

    @Override
    public boolean existsPendingFlightsFor(final AircraftRegistration registration) {
        final TypedQuery<Long> query = entityManager().createQuery(
                "SELECT COUNT(f) FROM Flight f WHERE f.aircraftRegistration = :reg " +
                        "AND (f.status = 'PENDING' OR f.status = 'DRAFT')",
                Long.class);
        query.setParameter("reg", registration);
        return query.getSingleResult() > 0;
    }
}