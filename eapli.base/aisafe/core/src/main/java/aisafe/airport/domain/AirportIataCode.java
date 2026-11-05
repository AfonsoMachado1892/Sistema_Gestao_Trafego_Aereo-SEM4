package aisafe.airport.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AirportIataCode implements Serializable, ValueObject, Comparable<AirportIataCode> {

    private static final long serialVersionUID = 1L;

    private String code;

    protected AirportIataCode() {
        // for ORM
    }

    public AirportIataCode(final String code) {
        Preconditions.nonEmpty(code, "IATA code must be filled.");

        final String normalizedCode = code.trim().toUpperCase();

        Preconditions.ensure(
                normalizedCode.matches("[A-Z]{3}"),
                "Airport IATA code must have exactly three letters."
        );

        this.code = normalizedCode;
    }

    public static AirportIataCode of(final String code) {
        return new AirportIataCode(code);
    }

    public String code() {
        return code;
    }

    @Override
    public int compareTo(final AirportIataCode other) {
        return this.code.compareTo(other.code);
    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AirportIataCode)) return false;
        final AirportIataCode that = (AirportIataCode) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}