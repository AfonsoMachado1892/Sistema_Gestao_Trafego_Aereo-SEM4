package aisafe.persistence.impl.inmemory;

import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.aircraft.domain.AircraftRepository;
import aisafe.airtransportcompany.domain.AirTransportCompany;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryAircraftRepository
        extends InMemoryDomainRepository<Aircraft, AircraftRegistration>
        implements AircraftRepository {

    @Override
    public Optional<Aircraft> findByRegistration(final AircraftRegistration registration) {
        return matchOne(a -> a.identity().equals(registration));
    }

    @Override
    public Iterable<Aircraft> findByAirTransportCompany(final AirTransportCompany airTransportCompany) {
        final List<Aircraft> result = new ArrayList<>();
        for (final Aircraft aircraft : findAll()) {
            if (aircraft.airCompany().equals(airTransportCompany)) {
                result.add(aircraft);
            }
        }
        return result;
    }
}