package aisafe.flight.domain;

import eapli.framework.domain.repositories.DomainRepository;
import aisafe.aircraft.domain.AircraftRegistration;

public interface FlightRepository extends DomainRepository<Long, Flight> {

    /**
     * Checks if there are any pending or draft flights for a given aircraft.
     * * @param registration the aircraft registration to check
     * @return true if pending flights exist, false otherwise
     */
    boolean existsPendingFlightsFor(AircraftRegistration registration);
}
