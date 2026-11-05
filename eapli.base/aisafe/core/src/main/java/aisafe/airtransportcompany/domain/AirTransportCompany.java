// aisafe/core/src/main/java/aisafe/airtransportcompany/domain/AirTransportCompany.java
package aisafe.airtransportcompany.domain;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Version;

import java.io.Serializable;

@Entity
public class AirTransportCompany implements Serializable, AggregateRoot<AirTransportCompanyCode> {

    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @EmbeddedId
    private AirTransportCompanyCode iataCode;

    private String icaoCode;
    private String name;

    protected AirTransportCompany() {
        // for ORM
    }

    public AirTransportCompany(
            final AirTransportCompanyCode iataCode,
            final String icaoCode,
            final String name
    ) {
        Preconditions.noneNull(iataCode, "Provide a valid IATA code.");
        Preconditions.nonEmpty(icaoCode, "Provide a valid ICAO code.");
        Preconditions.nonEmpty(name, "Provide a valid company name.");
        Preconditions.ensure(icaoCode.trim().length() >= 2 && icaoCode.trim().length() <= 3,
                "ICAO code must be 2-3 letters.");

        this.iataCode = iataCode;
        this.icaoCode = icaoCode.trim().toUpperCase();
        this.name = name.trim();
    }

    public String name() { return name; }
    public AirTransportCompanyCode iataCode() { return iataCode; }
    public String icaoCode() { return icaoCode; }

    @Override
    public AirTransportCompanyCode identity() { return iataCode; }

    @Override
    public boolean sameAs(final Object other) {
        if (!(other instanceof AirTransportCompany)) return false;
        final AirTransportCompany that = (AirTransportCompany) other;
        return this.identity().equals(that.identity());
    }

    @Override
    public String toString() {
        return iataCode + " / " + icaoCode + " - " + name;
    }
}