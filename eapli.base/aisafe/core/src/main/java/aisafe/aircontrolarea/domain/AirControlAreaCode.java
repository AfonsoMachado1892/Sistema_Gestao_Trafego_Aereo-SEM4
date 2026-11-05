package aisafe.aircontrolarea.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AirControlAreaCode implements ValueObject, Serializable, Comparable<AirControlAreaCode> {

    private static final long serialVersionUID = 1L;

    private String code;

    protected AirControlAreaCode() {
        // for ORM
    }

    public AirControlAreaCode(final String code) {
        Preconditions.nonEmpty(code, "Air Control Area Code cannot be empty.");
        this.code = code.trim().toUpperCase();
    }

    public static AirControlAreaCode of(final String code) {
        return new AirControlAreaCode(code);
    }

    public String code() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AirControlAreaCode)) return false;
        final AirControlAreaCode that = (AirControlAreaCode) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int compareTo(final AirControlAreaCode other) {
        return this.code.compareTo(other.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}