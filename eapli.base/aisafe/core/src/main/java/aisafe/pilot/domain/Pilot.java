package aisafe.pilot.domain;

import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Aggregate root for a Pilot
 *
 * <p>Invariants enforced:
 * <ul>
 *   <li>A pilot must have at least one certification.</li>
 *   <li>An inactive pilot cannot be deactivated again.</li>
 * </ul>
 */
@Entity
@Table(name = "PILOT")
public class Pilot implements AggregateRoot<PilotID>, Serializable {

    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @EmbeddedId
    private PilotID pilotID;

    @OneToOne
    private SystemUser systemUser;

    @Embedded
    @AttributeOverride(name = "iataCode", column = @Column(name = "company_iata"))
    private AirTransportCompanyCode currentCompanyIataCode;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "PILOT_CERTIFICATIONS",
            joinColumns = @JoinColumn(name = "pilot_id")
    )
    private Set<Certification> certifications = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PilotStatus status;

    protected Pilot() {
        // for ORM
    }

    Pilot(final PilotID pilotID,
          final SystemUser systemUser,
          final AirTransportCompanyCode currentCompanyIataCode,
          final Set<Certification> certifications) {

        Preconditions.noneNull(pilotID, "Pilot ID must not be null.");
        Preconditions.noneNull(systemUser, "System user must not be null.");
        Preconditions.noneNull(currentCompanyIataCode, "Company IATA code must not be null.");
        Preconditions.nonEmpty(certifications, "A pilot must have at least one certification.");

        this.pilotID = pilotID;
        this.systemUser = systemUser;
        this.currentCompanyIataCode = currentCompanyIataCode;
        this.certifications = new HashSet<>(certifications);
        this.status = PilotStatus.ACTIVE;
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public PilotID pilotID() {
        return pilotID;
    }

    public SystemUser systemUser() {
        return systemUser;
    }

    public AirTransportCompanyCode currentCompanyIataCode() {
        return currentCompanyIataCode;
    }

    public Set<Certification> certifications() {
        return Collections.unmodifiableSet(certifications);
    }

    public PilotStatus status() {
        return status;
    }

    public boolean isActive() {
        return status == PilotStatus.ACTIVE;
    }

    public boolean belongsTo(final AirTransportCompanyCode companyCode) {
        Preconditions.noneNull(companyCode, "Company code must not be null.");
        return currentCompanyIataCode.equals(companyCode);
    }

    /**
     * Returns true if this pilot is certified to fly the given aircraft model on the given date.
     * Delegates to Certification.coversModel() — Tell, Don't Ask.
     */
    public boolean isCertifiedFor(final AircraftModel aircraftModel, final LocalDate onDate) {
        Preconditions.noneNull(aircraftModel, "Aircraft model must not be null.");
        Preconditions.noneNull(onDate, "Date must not be null.");
        return certifications.stream()
                .anyMatch(c -> c.coversModel(aircraftModel, onDate));
    }

    // ── Commands ─────────────────────────────────────────────────────────────

    /**
     * Marks this pilot as inactive.
     *
     * @throws IllegalArgumentException if already inactive
     */
    public void deactivate() {
        Preconditions.ensure(isActive(), "Pilot is already inactive.");
        this.status = PilotStatus.INACTIVE;
    }

    // ── AggregateRoot ────────────────────────────────────────────────────────

    @Override
    public PilotID identity() {
        return pilotID;
    }

    @Override
    public boolean sameAs(final Object other) {
        if (!(other instanceof Pilot)) return false;
        return this.identity().equals(((Pilot) other).identity());
    }

    @Override
    public boolean equals(final Object o) {
        return DomainEntities.areEqual(this, o);
    }

    @Override
    public int hashCode() {
        return DomainEntities.hashCode(this);
    }

    @Override
    public String toString() {
        return systemUser.name() + " [" + currentCompanyIataCode + "] (" + status + ")";
    }
}
