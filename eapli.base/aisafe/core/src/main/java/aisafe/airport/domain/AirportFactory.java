package aisafe.airport.domain;

import aisafe.aircontrolarea.domain.AirControlArea;
import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.sharedkernel.Coordinates;
import eapli.framework.domain.model.DomainFactory;
import eapli.framework.validations.Preconditions;

public class AirportFactory implements DomainFactory<Airport> {

    private AirportIataCode iataCode;
    private AirportIcaoCode icaoCode;
    private Locality locality;
    private Coordinates coordinates;
    private AirportName name;
    private Elevation elevation;
    private AirControlArea airControlArea;

    public AirportFactory withIataCode(final AirportIataCode iataCode) {
        this.iataCode = iataCode;
        return this;
    }

    public AirportFactory withIcaoCode(final AirportIcaoCode icaoCode) {
        this.icaoCode = icaoCode;
        return this;
    }

    public AirportFactory withLocality(final Locality locality) {
        this.locality = locality;
        return this;
    }

    public AirportFactory withCoordinates(final Coordinates coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    public AirportFactory withName(final AirportName name) {
        this.name = name;
        return this;
    }

    public AirportFactory withElevation(final Elevation elevation) {
        this.elevation = elevation;
        return this;
    }

    public AirportFactory inAirControlArea(final AirControlArea airControlArea) {
        this.airControlArea = airControlArea;
        return this;
    }

    @Override
    public Airport build() {

        return new Airport(
                iataCode,
                icaoCode,
                locality,
                coordinates,
                name,
                elevation,
                airControlArea
        );
    }
}