package aisafe.persistence.impl.inmemory;

import aisafe.airport.domain.Airport;
import aisafe.airport.domain.AirportIataCode;
import aisafe.airport.domain.AirportIcaoCode;
import aisafe.airport.domain.AirportRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryAirportRepository
        extends InMemoryDomainRepository<Airport, AirportIataCode>
        implements AirportRepository {

    @Override
    public Optional<Airport> findByIataCode(final AirportIataCode iataCode) {
        return matchOne(airport -> airport.iataCode().equals(iataCode));
    }

    @Override
    public Optional<Airport> findByIcaoCode(final AirportIcaoCode icaoCode) {
        return matchOne(airport -> airport.icaoCode().equals(icaoCode));
    }
}