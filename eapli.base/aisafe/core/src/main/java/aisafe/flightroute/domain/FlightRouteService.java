package aisafe.flightroute.domain;

import aisafe.airport.domain.Airport;
import aisafe.airport.domain.AirportIataCode;
import aisafe.airport.domain.AirportRepository;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.flightplan.domain.FlightPlanRepository;
import aisafe.flightroute.application.AirportDTO;
import aisafe.flightroute.application.FlightRouteDTO;
import eapli.framework.validations.Preconditions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FlightRouteService {

    private final FlightRouteRepository flightRouteRepository;
    private final AirportRepository airportRepository;
    private final FlightPlanRepository flightPlanRepository;

    public FlightRouteService(
            final FlightRouteRepository flightRouteRepository,
            final AirportRepository airportRepository,
            final FlightPlanRepository flightPlanRepository) {

        Preconditions.noneNull(flightRouteRepository,
                "FlightRoute repository must be provided.");
        Preconditions.noneNull(airportRepository,
                "Airport repository must be provided.");
        Preconditions.noneNull(flightPlanRepository,
                "FlightPlan repository must be provided.");

        this.flightRouteRepository = flightRouteRepository;
        this.airportRepository = airportRepository;
        this.flightPlanRepository = flightPlanRepository;
    }

    public FlightRoute createRegularFlightRoute(final String routeCodeValue,
                                                final String departureIataCode,
                                                final String arrivalIataCode,
                                                final AirTransportCompanyCode companyIataCode,
                                                final RegularSchedule regularSchedule) {

        Preconditions.noneNull(companyIataCode, "Company IATA code must not be null.");
        Preconditions.noneNull(regularSchedule, "Regular schedule must not be null.");

        final RouteCode routeCode = RouteCode.of(routeCodeValue);

        ensureRouteCodeUnique(routeCode);

        final Airport departure = findAirportByIata(departureIataCode, "Departure");
        final Airport arrival = findAirportByIata(arrivalIataCode, "Arrival");

        final FlightRoute route = new FlightRouteFactory()
                .withRouteCode(routeCode)
                .withDepartureAirport(departure.iataCode())
                .withArrivalAirport(arrival.iataCode())
                .withCompany(companyIataCode)
                .asRegular(regularSchedule)
                .build();

        return flightRouteRepository.save(route);
    }

    public Iterable<FlightRouteDTO> listCompanyRoutes(
            final AirTransportCompanyCode companyIataCode) {

        Preconditions.noneNull(companyIataCode, "Company IATA code must not be null.");

        final List<FlightRouteDTO> dtos = new ArrayList<>();

        for (final FlightRoute route : flightRouteRepository.findByCompany(companyIataCode)) {
            dtos.add(toDTO(route));
        }

        return dtos;
    }

    public FlightRoute createCharterFlightRoute(final String routeCodeValue,
                                                final String departureIataCode,
                                                final String arrivalIataCode,
                                                final AirTransportCompanyCode companyIataCode,
                                                final CharterSchedule charterSchedule) {

        Preconditions.noneNull(companyIataCode, "Company IATA code must not be null.");
        Preconditions.noneNull(charterSchedule, "Charter schedule must not be null.");

        final RouteCode routeCode = RouteCode.of(routeCodeValue);

        ensureRouteCodeUnique(routeCode);

        final Airport departure = findAirportByIata(departureIataCode, "Departure");
        final Airport arrival = findAirportByIata(arrivalIataCode, "Arrival");

        final FlightRoute route = new FlightRouteFactory()
                .withRouteCode(routeCode)
                .withDepartureAirport(departure.iataCode())
                .withArrivalAirport(arrival.iataCode())
                .withCompany(companyIataCode)
                .asCharter(charterSchedule)
                .build();

        return flightRouteRepository.save(route);
    }

    public Iterable<AirportDTO> listAirports() {
        final List<AirportDTO> dtos = new ArrayList<>();

        for (final Airport airport : airportRepository.findAll()) {
            dtos.add(new AirportDTO(
                    airport.iataCode().toString(),
                    airport.icaoCode().toString(),
                    airport.name().toString(),
                    airport.locality().cityName()
            ));
        }

        return dtos;
    }

    public FlightRoute deactivateRoute(final String routeCodeValue,
                                       final LocalDate deactivationDate,
                                       final AirTransportCompanyCode companyIataCode) {

        final RouteCode routeCode = RouteCode.of(routeCodeValue);

        final FlightRoute route = flightRouteRepository
                .findByRouteCode(routeCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Flight route not found: " + routeCodeValue));

        Preconditions.ensure(
                route.airTransportCompanyIataCode().equals(companyIataCode),
                "Route does not belong to your company.");

        Preconditions.ensure(
                !flightPlanRepository.existsActiveByRouteCodeAfter(
                        routeCode, deactivationDate),
                "Cannot deactivate route: there are active flight plans " +
                        "scheduled after " + deactivationDate + ".");

        route.deactivate(deactivationDate);
        return flightRouteRepository.save(route);
    }

    public Iterable<FlightRouteDTO> listActiveRoutesByCompany(
            final AirTransportCompanyCode companyIataCode) {

        Preconditions.noneNull(companyIataCode, "Company IATA code must not be null.");

        final List<FlightRouteDTO> dtos = new ArrayList<>();

        for (final FlightRoute route : flightRouteRepository.findByCompany(companyIataCode)) {
            if (route.isActive()) {
                dtos.add(toDTO(route));
            }
        }

        return dtos;
    }

    private Airport findAirportByIata(final String iataCode, final String label) {
        return airportRepository
                .findByIataCode(AirportIataCode.of(iataCode))
                .orElseThrow(() -> new IllegalArgumentException(
                        label + " airport not found: " + iataCode));
    }

    private void ensureRouteCodeUnique(final RouteCode routeCode) {
        flightRouteRepository.findByRouteCode(routeCode).ifPresent(r -> {
            throw new IllegalStateException(
                    "A flight route with code '" + routeCode + "' already exists.");
        });
    }

    public FlightRouteDTO toDTO(final FlightRoute route) {
        return new FlightRouteDTO(
                route.routeCode().toString(),
                route.effectiveStatus().name(),
                route.departureAirportIataCode().toString(),
                route.arrivalAirportIataCode().toString(),
                route.airTransportCompanyIataCode().toString(),
                route.flightType().name(),
                route.deactivationDate() == null ? "-" : route.deactivationDate().toString(),
                route.schedule().toString()
        );
    }
}