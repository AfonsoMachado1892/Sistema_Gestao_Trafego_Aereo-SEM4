package aisafe.pilot.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class PilotID implements ValueObject, Serializable, Comparable<PilotID> {

    private static final long serialVersionUID = 1L;

    private String number;

    protected PilotID() {
        // for ORM
    }

    private PilotID(final String number) {
        Preconditions.nonEmpty(number, "Pilot ID cannot be empty.");
        this.number = number;
    }

    public static PilotID of(final String number) {
        return new PilotID(number);
    }

    public static PilotID newID() {
        return new PilotID(UUID.randomUUID().toString());
    }

    public String number() {
        return number;
    }

    @Override
    public int compareTo(final PilotID other) {
        return this.number.compareTo(other.number);
    }

    @Override
    public String toString() {
        return number;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof PilotID)) return false;
        return Objects.equals(number, ((PilotID) o).number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}
