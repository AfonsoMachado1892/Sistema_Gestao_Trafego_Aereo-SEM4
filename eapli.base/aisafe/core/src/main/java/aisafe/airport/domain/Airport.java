package aisafe.airport.domain;


import aisafe.aircontrolarea.domain.AirControlArea;
import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.sharedkernel.Coordinates;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "AIRPORT")
public class Airport implements AggregateRoot<AirportIataCode>, Serializable {

    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @EmbeddedId
    private AirportIataCode iataCode;

    @Embedded
    @AttributeOverride(name = "code", column = @Column(name = "icao_code"))
    private AirportIcaoCode icaoCode;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "cityName", column = @Column(name = "city_name")),
            @AttributeOverride(name = "country.name", column = @Column(name = "country_name")),
            @AttributeOverride(name = "country.isoCode.code", column = @Column(name = "country_iso_code"))
    })
    private Locality locality;

    @Embedded
    private Coordinates coordinates;

    @Embedded
    private Elevation elevation;

    @ManyToOne
    private AirControlArea airControl;

    @Embedded
    private AirportName name;

    protected Airport(){

        //

    }

    public Airport(AirportIataCode iataCode, AirportIcaoCode icaoCode, Locality locality, Coordinates coordinates, AirportName name, Elevation elevation, AirControlArea airControl){

        Preconditions.noneNull(iataCode, "IATA code must be filled.");
        Preconditions.noneNull(icaoCode, "ICAO code must be filled.");
        Preconditions.noneNull(locality, "Locality must be filled.");
        Preconditions.noneNull(coordinates, "Coordinates must be filled.");
        Preconditions.noneNull(elevation, "Elevation must be filled.");
        Preconditions.noneNull(airControl, "Air control area must be filled.");
        Preconditions.noneNull(name, "Airport name must be filled.");

        this.iataCode = iataCode;
        this.icaoCode = icaoCode;
        this.locality = locality;
        this.coordinates = coordinates;
        this.elevation = elevation;
        this.name = name;
        this.airControl = airControl;
    }

    public static Airport of(AirportIataCode iataCode, AirportIcaoCode icaoCode, Locality locality, Coordinates coordinates, AirportName name, Elevation elevation, AirControlArea airControl){

        return new Airport(iataCode, icaoCode, locality, coordinates, name, elevation, airControl);

    }

    public AirportIataCode iataCode() {
        return iataCode;
    }

    public AirportIcaoCode icaoCode() {
        return icaoCode;
    }

    public Locality locality() {
        return locality;
    }

    public Coordinates coordinates() {
        return coordinates;
    }

    public Elevation elevation() {
        return elevation;
    }

    public AirportName name() {
        return name;
    }

    public AirControlArea airControlArea() {
        return airControl;
    }

    @Override
    public boolean equals(final Object o) {
        return DomainEntities.areEqual(this, o);
    }

    @Override
    public int hashCode() {
        return DomainEntities.hashCode(this);
    }

    @Override
    public AirportIataCode identity() {
        return iataCode;
    }

    @Override
    public boolean sameAs(final Object other) {
        if (!(other instanceof Airport)) {
            return false;
        }

        final Airport that = (Airport) other;
        return this.identity().equals(that.identity());
    }

    @Override
    public String toString() {
        return iataCode + " - " + name + " (" + locality + ")";
    }

}
