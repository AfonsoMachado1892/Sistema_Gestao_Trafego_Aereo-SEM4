package aisafe.pilot.domain;

import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import eapli.framework.domain.model.DomainFactory;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.validations.Preconditions;

import java.util.Set;

/**
 * Factory for creating {@link Pilot} instances.
 * Used because pilot creation involves a SystemUser (external aggregate)
 * and a set of certifications.
 */
public class PilotFactory implements DomainFactory<Pilot> {

    private SystemUser systemUser;
    private AirTransportCompanyCode companyIataCode;
    private Set<Certification> certifications;

    public PilotFactory withSystemUser(final SystemUser systemUser) {
        this.systemUser = systemUser;
        return this;
    }

    public PilotFactory withCompany(final AirTransportCompanyCode companyIataCode) {
        this.companyIataCode = companyIataCode;
        return this;
    }

    public PilotFactory withCertifications(final Set<Certification> certifications) {
        this.certifications = certifications;
        return this;
    }

    @Override
    public Pilot build() {
        Preconditions.noneNull(systemUser, companyIataCode, certifications);
        return new Pilot(PilotID.newID(), systemUser, companyIataCode, certifications);
    }
}
