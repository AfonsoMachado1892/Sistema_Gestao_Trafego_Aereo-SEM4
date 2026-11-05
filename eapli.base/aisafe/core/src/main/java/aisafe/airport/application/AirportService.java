package aisafe.airport.application;

import aisafe.aircontrolarea.domain.AirControlArea;
import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.aircontrolarea.domain.AirControlAreaRepository;
import aisafe.airport.domain.*;
import aisafe.sharedkernel.Coordinates;
import eapli.framework.validations.Preconditions;

public class AirportService {

    private final AirportRepository airportRepository;
    private final AirControlAreaRepository airControlAreaRepository;

    public AirportService(
            final AirportRepository airportRepository,
            final AirControlAreaRepository airControlAreaRepository
    ) {
        Preconditions.noneNull(airportRepository, "Airport repository must be provided.");
        Preconditions.noneNull(airControlAreaRepository, "Air control area repository must be provided.");

        this.airportRepository = airportRepository;
        this.airControlAreaRepository = airControlAreaRepository;
    }

    public Airport registerAirport(
            final AirportIataCode iataCode,
            final AirportIcaoCode icaoCode,
            final Locality locality,
            final Coordinates coordinates,
            final AirportName name,
            final Elevation elevation,
            final AirControlArea controlArea
    ) {

        if (airportRepository.findByIataCode(iataCode).isPresent()) {
            throw new IllegalArgumentException("Airport IATA code already exists.");
        }

        if (airportRepository.findByIcaoCode(icaoCode).isPresent()) {
            throw new IllegalArgumentException("Airport ICAO code already exists.");
        }

        if (!controlArea.contains(coordinates)) {
            throw new IllegalArgumentException("Airport coordinates are outside the selected air control area.");
        }

        final AirControlArea airControlArea = airControlAreaRepository
                .findByCode(controlArea.code())
                .orElseThrow(() -> new IllegalArgumentException("Air control area does not exist."));

        if (!airControlArea.contains(coordinates)) {
            throw new IllegalArgumentException("Airport coordinates are outside the selected air control area.");
        }

        final Airport airport = new AirportFactory()
                .withIataCode(iataCode)
                .withIcaoCode(icaoCode)
                .withLocality(locality)
                .withCoordinates(coordinates)
                .withName(name)
                .withElevation(elevation)
                .inAirControlArea(controlArea)
                .build();

        return airportRepository.save(airport);
    }

    public Iterable<Airport> listAirports() {
        return airportRepository.findAll();
    }
}