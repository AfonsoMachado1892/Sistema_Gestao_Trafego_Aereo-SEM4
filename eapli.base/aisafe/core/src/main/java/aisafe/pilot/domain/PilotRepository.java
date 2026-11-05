package aisafe.pilot.domain;

import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.repositories.Repository;

import java.util.Optional;

public interface PilotRepository extends Repository<Pilot, PilotID> {

    Optional<Pilot> findBySystemUser(SystemUser systemUser);

    Iterable<Pilot> findByCompany(AirTransportCompanyCode companyIataCode);

    Iterable<Pilot> findActiveByCompany(AirTransportCompanyCode companyIataCode);

    Optional<Pilot> findByPilotID(PilotID pilotID);
}
