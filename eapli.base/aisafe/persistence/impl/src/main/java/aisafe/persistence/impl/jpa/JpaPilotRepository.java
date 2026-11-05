package aisafe.persistence.impl.jpa;

import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.pilot.domain.Pilot;
import aisafe.pilot.domain.PilotID;
import aisafe.pilot.domain.PilotRepository;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class JpaPilotRepository
        extends AiSafeJpaRepositoryBase<Pilot, PilotID, PilotID>
        implements PilotRepository {

    public JpaPilotRepository(final String persistenceUnitName,
                              final Map extendedProperties) {
        super(persistenceUnitName, extendedProperties, "pilotID");
    }

    @Override
    public Optional<Pilot> findBySystemUser(final SystemUser systemUser) {
        return entityManager()
                .createQuery("SELECT p FROM Pilot p WHERE p.systemUser = :user", Pilot.class)
                .setParameter("user", systemUser)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public Iterable<Pilot> findByCompany(final AirTransportCompanyCode companyIataCode) {
        return entityManager()
                .createQuery(
                        "SELECT p FROM Pilot p WHERE p.currentCompanyIataCode = :code",
                        Pilot.class)
                .setParameter("code", companyIataCode)
                .getResultList();
    }

    @Override
    public Iterable<Pilot> findActiveByCompany(final AirTransportCompanyCode companyIataCode) {
        return entityManager()
                .createQuery(
                        "SELECT p FROM Pilot p WHERE p.currentCompanyIataCode = :code "
                                + "AND p.status = 'ACTIVE'",
                        Pilot.class)
                .setParameter("code", companyIataCode)
                .getResultList();
    }

    @Override
    public Optional<Pilot> findByPilotID(final PilotID pilotID) {
        return entityManager()
                .createQuery("SELECT p FROM Pilot p WHERE p.pilotID = :pilotID", Pilot.class)
                .setParameter("pilotID", pilotID)
                .getResultList()
                .stream()
                .findFirst();
    }
}
