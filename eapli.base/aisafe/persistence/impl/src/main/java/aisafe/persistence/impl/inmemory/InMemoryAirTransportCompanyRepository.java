package aisafe.persistence.impl.inmemory;

import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.airtransportcompany.domain.AirTransportCompanyRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryAirTransportCompanyRepository
        extends InMemoryDomainRepository<AirTransportCompany, AirTransportCompanyCode>
        implements AirTransportCompanyRepository {

    @Override
    public Optional<AirTransportCompany> findByIcaoCode(final String icaoCode) {
        return matchOne(c -> c.icaoCode().equalsIgnoreCase(icaoCode));
    }

    @Override
    public Optional<AirTransportCompany> findByIataCode(final AirTransportCompanyCode iataCode) {
        return matchOne(c -> c.iataCode().equals(iataCode));
    }
}