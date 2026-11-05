package aisafe.airport.domain;

import eapli.framework.infrastructure.repositories.Repository;

import java.util.Optional;

public interface AirportRepository extends Repository<Airport, AirportIataCode> {

    Optional<Airport> findByIataCode(AirportIataCode iataCode);

    Optional<Airport> findByIcaoCode(AirportIcaoCode icaoCode);
}