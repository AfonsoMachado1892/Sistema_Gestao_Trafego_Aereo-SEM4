package aisafe.persistence.impl.jpa;

import aisafe.airport.domain.Airport;
import aisafe.airport.domain.AirportIataCode;
import aisafe.airport.domain.AirportIcaoCode;
import aisafe.airport.domain.AirportRepository;
import jakarta.persistence.TypedQuery;

import java.util.Map;
import java.util.Optional;

public class JpaAirportRepository
        extends AiSafeJpaRepositoryBase<Airport, AirportIataCode, AirportIataCode>
        implements AirportRepository {

    public JpaAirportRepository(final String persistenceUnitName, final Map extendedProperties) {
        super(persistenceUnitName, extendedProperties, "iataCode");
    }

    @Override
    public Optional<Airport> findByIataCode(final AirportIataCode iataCode) {
        final TypedQuery<Airport> query = entityManager().createQuery(
                "SELECT a FROM Airport a WHERE a.iataCode = :iataCode",
                Airport.class);
        query.setParameter("iataCode", iataCode);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Optional<Airport> findByIcaoCode(final AirportIcaoCode icaoCode) {
        final TypedQuery<Airport> query = entityManager().createQuery(
                "SELECT a FROM Airport a WHERE a.icaoCode = :icaoCode",
                Airport.class);
        query.setParameter("icaoCode", icaoCode);
        return query.getResultList().stream().findFirst();
    }
}