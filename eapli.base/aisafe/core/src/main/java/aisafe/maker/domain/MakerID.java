package aisafe.maker.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MakerID implements ValueObject, Serializable, Comparable<MakerID> {

    private static final long serialVersionUID = 1L;

    private String number;

    protected MakerID() {
        // for ORM
    }

    public MakerID(final String number) {
        Preconditions.nonEmpty(number, "MakerID cannot be empty");
        this.number = number;
    }

    public static MakerID of(final String number) {
        return new MakerID(number);
    }

    @Override
    public String toString() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MakerID)) return false;
        MakerID makerID = (MakerID) o;
        return Objects.equals(number, makerID.number);
    }

    @Override
    public int compareTo(final MakerID other) {
        return this.number.compareTo(other.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}