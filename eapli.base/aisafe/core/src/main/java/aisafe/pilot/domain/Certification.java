package aisafe.pilot.domain;

import aisafe.aircraftmodel.domain.AircraftModel;
import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Certification of a pilot for a specific aircraft model.
 *
 * <p>References AircraftModel by object (not by ID) as per project conventions.
 * A certification is valid between issuedAt and validUntil.
 */
@Embeddable
public class Certification implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    @ManyToOne
    private AircraftModel aircraftModel;

    @Column(name = "cert_issued_at")
    private LocalDate issuedAt;

    @Column(name = "cert_valid_until")
    private LocalDate validUntil;

    @Column(name = "flight_hours")
    private FlightHours flightHours;

    protected Certification() {
        // for ORM
    }

    private Certification(final AircraftModel aircraftModel,
                          final FlightHours flightHours,
                          final LocalDate issuedAt,
                          final LocalDate validUntil) {
        Preconditions.noneNull(aircraftModel, "Aircraft model must not be null.");
        Preconditions.noneNull(flightHours, "Flight hours must not be null.");
        Preconditions.noneNull(issuedAt, "Issued date must not be null.");
        Preconditions.noneNull(validUntil, "Valid until date must not be null.");
        Preconditions.ensure(validUntil.isAfter(issuedAt),
                "Valid until date must be after issued date.");

        this.aircraftModel = aircraftModel;
        this.flightHours = flightHours;
        this.issuedAt = issuedAt;
        this.validUntil = validUntil;
    }

    public static Certification of(final AircraftModel aircraftModel,
                                   final FlightHours flightHours,
                                   final LocalDate issuedAt,
                                   final LocalDate validUntil) {
        return new Certification(aircraftModel, flightHours, issuedAt, validUntil);
    }

    public AircraftModel aircraftModel() {
        return aircraftModel;
    }

    public FlightHours flightHours() {
        return flightHours;
    }

    public LocalDate issuedAt() {
        return issuedAt;
    }

    public LocalDate validUntil() {
        return validUntil;
    }

    /**
     * Returns true if this certification is valid on the given date.
     */
    public boolean isValidOn(final LocalDate date) {
        Preconditions.noneNull(date, "Date must not be null.");
        return !date.isBefore(issuedAt) && !date.isAfter(validUntil);
    }

    /**
     * Returns true if this certification covers the given aircraft model on the given date.
     * Follows Tell, Don't Ask — the caller sends a message, the certification decides.
     */
    public boolean coversModel(final AircraftModel model, final LocalDate onDate) {
        Preconditions.noneNull(model, "Aircraft model must not be null.");
        Preconditions.noneNull(onDate, "Date must not be null.");
        return aircraftModel.sameAs(model) && isValidOn(onDate);
    }

    @Override
    public String toString() {
        return "Certification{model=" + aircraftModel
                + ", hours=" + flightHours
                + ", issued=" + issuedAt
                + ", until=" + validUntil + "}";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Certification)) return false;
        final Certification that = (Certification) o;
        return Objects.equals(aircraftModel, that.aircraftModel)
                && Objects.equals(flightHours, that.flightHours)
                && Objects.equals(issuedAt, that.issuedAt)
                && Objects.equals(validUntil, that.validUntil);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aircraftModel, flightHours, issuedAt, validUntil);
    }
}
