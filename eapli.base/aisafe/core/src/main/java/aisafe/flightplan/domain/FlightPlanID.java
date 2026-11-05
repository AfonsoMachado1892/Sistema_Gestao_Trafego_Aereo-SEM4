package aisafe.flightplan.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class FlightPlanID implements ValueObject, Serializable, Comparable<FlightPlanID> {

    private static final long serialVersionUID = 1L;

    private String number;

    protected FlightPlanID() {
        // for ORM
    }

    private FlightPlanID(final String number) {
        Preconditions.nonEmpty(number, "Flight plan ID cannot be empty.");
        this.number = number;
    }

    public static FlightPlanID of(final String number) {
        return new FlightPlanID(number);
    }

    public static FlightPlanID newID() {
        return new FlightPlanID(UUID.randomUUID().toString());
    }

    public String number() {
        return number;
    }

    @Override
    public int compareTo(final FlightPlanID other) {
        return this.number.compareTo(other.number);
    }

    @Override
    public String toString() {
        return number;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof FlightPlanID)) return false;
        final FlightPlanID that = (FlightPlanID) o;
        return Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}