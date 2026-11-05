package aisafe.enginemodel.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EngineModelID implements ValueObject, Serializable, Comparable<EngineModelID> {

    private static final long serialVersionUID = 1L;

    private String number;

    protected EngineModelID() {
        // for ORM
    }

    public EngineModelID(final String number) {
        Preconditions.nonEmpty(number, "EngineModelID cannot be empty");
        this.number = number;
    }

    public static EngineModelID of(final String number) {
        return new EngineModelID(number);
    }

    @Override
    public int compareTo(final EngineModelID other) {
        return this.number.compareTo(other.number);
    }

    @Override
    public String toString() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EngineModelID)) return false;
        EngineModelID that = (EngineModelID) o;
        return Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}