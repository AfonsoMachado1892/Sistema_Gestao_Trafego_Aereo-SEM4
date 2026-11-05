package aisafe.pilot.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value Object representing the number of flight hours a pilot has
 * on a specific aircraft model.
 */
@Embeddable
public class FlightHours implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    private int hours;

    protected FlightHours() {
        // for ORM
    }

    private FlightHours(final int hours) {
        Preconditions.ensure(hours >= 0, "Flight hours must not be negative.");
        this.hours = hours;
    }

    public static FlightHours of(final int hours) {
        return new FlightHours(hours);
    }

    public int hours() {
        return hours;
    }

    @Override
    public String toString() {
        return hours + "h";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof FlightHours)) return false;
        return hours == ((FlightHours) o).hours;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hours);
    }
}
