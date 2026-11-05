// aisafe/core/src/main/java/aisafe/airtransportcompany/domain/AirTransportCompanyCode.java
package aisafe.airtransportcompany.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AirTransportCompanyCode implements ValueObject, Serializable, Comparable<AirTransportCompanyCode> {

    private static final long serialVersionUID = 1L;

    private String iataCode;

    protected AirTransportCompanyCode() {
        // for ORM
    }

    private AirTransportCompanyCode(final String iataCode) {
        Preconditions.nonEmpty(iataCode, "IATA code cannot be empty.");
        Preconditions.ensure(iataCode.trim().length() == 2,
                "IATA code must be exactly 2 letters.");
        this.iataCode = iataCode.trim().toUpperCase();
    }

    public static AirTransportCompanyCode of(final String iataCode) {
        return new AirTransportCompanyCode(iataCode);
    }

    @Override
    public String toString() { return iataCode; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AirTransportCompanyCode)) return false;
        return iataCode.equals(((AirTransportCompanyCode) o).iataCode);
    }

    @Override
    public int hashCode() { return Objects.hash(iataCode); }

    @Override
    public int compareTo(AirTransportCompanyCode other) {
        return this.iataCode.compareTo(other.iataCode);
    }
}