package aisafe.persistence.impl.jpa;

import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.aircraft.domain.AircraftRepository;
import aisafe.airtransportcompany.domain.AirTransportCompany;
import jakarta.persistence.TypedQuery;

import java.util.Map;
import java.util.Optional;

public class JpaAircraftRepository
        extends AiSafeJpaRepositoryBase<Aircraft, AircraftRegistration, AircraftRegistration>
        implements AircraftRepository {

    public JpaAircraftRepository(final String persistenceUnitName, final Map extendedProperties) {
        super(persistenceUnitName, extendedProperties, "aircraftCode");
    }

    @Override
    public Optional<Aircraft> findByRegistration(final AircraftRegistration registration) {
        final TypedQuery<Aircraft> query = entityManager().createQuery(
                "SELECT a FROM Aircraft a WHERE a.aircraftCode = :code",
                Aircraft.class);
        query.setParameter("code", registration);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Iterable<Aircraft> findByAirTransportCompany(final AirTransportCompany airTransportCompany) {
        final TypedQuery<Aircraft> query = entityManager().createQuery(
                "SELECT a FROM Aircraft a WHERE a.airCompany = :company",
                Aircraft.class);
        query.setParameter("company", airTransportCompany);
        return query.getResultList();
    }
}