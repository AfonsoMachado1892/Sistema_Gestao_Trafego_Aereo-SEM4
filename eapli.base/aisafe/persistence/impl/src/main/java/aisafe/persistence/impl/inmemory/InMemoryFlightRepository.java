package aisafe.persistence.impl.inmemory;

import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.flight.domain.Flight;
import aisafe.flight.domain.FlightRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

public class InMemoryFlightRepository
        extends InMemoryDomainRepository<Flight, Long>
        implements FlightRepository {

    @Override
    public boolean existsPendingFlightsFor(final AircraftRegistration registration) {
        return match(f -> registration.equals(f.aircraftRegistration())
                && f.isPendingOrDraft())
                .iterator()
                .hasNext();
    }
}