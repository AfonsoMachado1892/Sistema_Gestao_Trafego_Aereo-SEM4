package aisafe.aircraft.domain;


import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CabinConfiguration implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    private int economySeats;
    private int businessSeats;
    private int firstSeats;

    protected CabinConfiguration() {
        // for ORM
    }

    public CabinConfiguration(final int economySeats, final int businessSeats, final int firstSeats) {
        Preconditions.ensure(economySeats >= 0, "Economy seats must be non-negative.");
        Preconditions.ensure(businessSeats >= 0, "Business seats must be non-negative.");
        Preconditions.ensure(firstSeats >= 0, "First seats must be non-negative.");
        Preconditions.ensure(economySeats + businessSeats + firstSeats > 0,
                "Aircraft must have at least one seat.");

        this.economySeats = economySeats;
        this.businessSeats = businessSeats;
        this.firstSeats = firstSeats;
    }

    public static CabinConfiguration of(final int economySeats, final int businessSeats, final int firstSeats) {
        return new CabinConfiguration(economySeats, businessSeats, firstSeats);
    }

    public int economySeats() { return economySeats; }
    public int businessSeats() { return businessSeats; }
    public int firstSeats() { return firstSeats; }

    public int totalSeats() {
        return economySeats + businessSeats + firstSeats;
    }

    public boolean exceedsCapacity(final int maxCapacity) {
        return totalSeats() > maxCapacity;
    }

    @Override
    public String toString() {
        return String.format("Economy: %d | Business: %d | First: %d | Total: %d",
                economySeats, businessSeats, firstSeats, totalSeats());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CabinConfiguration)) return false;
        final CabinConfiguration that = (CabinConfiguration) o;
        return economySeats == that.economySeats
                && businessSeats == that.businessSeats
                && firstSeats == that.firstSeats;
    }

    @Override
    public int hashCode() {
        return Objects.hash(economySeats, businessSeats, firstSeats);
    }


}
