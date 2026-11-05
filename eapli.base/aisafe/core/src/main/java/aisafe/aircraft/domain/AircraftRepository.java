package aisafe.aircraft.domain;

import aisafe.airtransportcompany.domain.AirTransportCompany;
import eapli.framework.infrastructure.repositories.Repository;

import java.util.Optional;

public interface AircraftRepository extends Repository<Aircraft, AircraftRegistration> {

    Optional<Aircraft> findByRegistration(AircraftRegistration registration);

    Iterable<Aircraft> findByAirTransportCompany(AirTransportCompany airTransportCompany);

}