package aisafe.flightroute.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Route code for a flight route.
 * Format: exactly 2 uppercase letters (matching the company IATA code)
 * followed by 1 to 4 digits. E.g. "TP123", "FR42".
 */
@Embeddable
public class RouteCode implements ValueObject, Serializable, Comparable<RouteCode> {

    private static final long serialVersionUID = 1L;

    private static final String ROUTE_CODE_PATTERN = "^[A-Z]{2}\\d{1,4}$";

    // "value" is a reserved word in H2 — use an explicit column name
    @Column(name = "route_code")
    private String value;

    protected RouteCode() {
        // for ORM
    }

    private RouteCode(final String value) {
        Preconditions.nonEmpty(value, "Route code must not be empty.");
        final String normalized = value.trim().toUpperCase();
        Preconditions.ensure(
                normalized.matches(ROUTE_CODE_PATTERN),
                "Route code must be 2 uppercase letters followed by 1 to 4 digits (e.g. TP123)."
        );
        this.value = normalized;
    }

    public static RouteCode of(final String value) {
        return new RouteCode(value);
    }

    /**
     * Returns the 2-letter company prefix of this route code.
     * E.g. for "TP123" returns "TP".
     */
    public String companyPrefix() {
        return value.substring(0, 2);
    }

    public String value() {
        return value;
    }

    @Override
    public int compareTo(final RouteCode other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof RouteCode)) return false;
        return Objects.equals(value, ((RouteCode) o).value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}