package aisafe.persistence.impl.inmemory;

import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.pilot.domain.Pilot;
import aisafe.pilot.domain.PilotID;
import aisafe.pilot.domain.PilotRepository;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryPilotRepository
        extends InMemoryDomainRepository<Pilot, PilotID>
        implements PilotRepository {

    @Override
    public Optional<Pilot> findBySystemUser(final SystemUser systemUser) {
        return matchOne(p -> p.systemUser().equals(systemUser));
    }

    @Override
    public Iterable<Pilot> findByCompany(final AirTransportCompanyCode companyIataCode) {
        return match(p -> p.currentCompanyIataCode().equals(companyIataCode));
    }

    @Override
    public Iterable<Pilot> findActiveByCompany(final AirTransportCompanyCode companyIataCode) {
        return match(p -> p.currentCompanyIataCode().equals(companyIataCode) && p.isActive());
    }

    @Override
    public Optional<Pilot> findByPilotID(final PilotID pilotID) {
        return matchOne(p -> p.pilotID().equals(pilotID));
    }
}
