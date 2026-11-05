package aisafe.aircraftmodel.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AircraftModelID implements ValueObject, Serializable, Comparable<AircraftModelID> {

    private static final long serialVersionUID = 1L;

    private String number;

    protected AircraftModelID() {
        // for ORM
    }

    public AircraftModelID(final String number) {
        Preconditions.nonEmpty(number, "AircraftModelID cannot be empty");
        this.number = number;
    }

    public static AircraftModelID of(final String number) {
        return new AircraftModelID(number);
    }

    @Override
    public int compareTo(final AircraftModelID other) {
        return this.number.compareTo(other.number);
    }

    @Override
    public String toString() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AircraftModelID)) return false;
        AircraftModelID that = (AircraftModelID) o;
        return Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}
