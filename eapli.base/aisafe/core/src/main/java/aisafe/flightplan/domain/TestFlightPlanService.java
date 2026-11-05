package aisafe.flightplan.domain;

import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRepository;
import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.airport.domain.Airport;
import aisafe.airport.domain.AirportRepository;
import aisafe.flightplan.application.FlightPlanDTO;
import aisafe.flightplan.application.FlightPlanTestBoard;
import aisafe.flightplan.domain.FlightPlan;
import aisafe.flightplan.domain.FlightPlanID;
import aisafe.flightplan.domain.FlightPlanRepository;
import aisafe.flightplan.domain.FlightPlanStatus;
import aisafe.flightplan.domain.FlightPlanTestResult;
import aisafe.flightplan.domain.simulation.FlightPlanTester;
import aisafe.flightplan.domain.simulation.SimulatedFlightPlan;
import aisafe.flightplan.domain.simulation.SimulatedFlightPlanFactory;
import aisafe.flightroute.domain.FlightRoute;
import aisafe.flightroute.domain.FlightRouteRepository;
import aisafe.pilot.domain.Pilot;
import aisafe.weather.domain.WeatherData;
import aisafe.weather.domain.WeatherDataRepository;
import eapli.framework.validations.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestFlightPlanService {

    private final FlightPlanRepository flightPlanRepository;
    private final FlightRouteRepository flightRouteRepository;
    private final AircraftRepository aircraftRepository;
    private final AirportRepository airportRepository;
    private final WeatherDataRepository weatherDataRepository;
    private final SimulatedFlightPlanFactory simulatedFlightPlanFactory;
    private final FlightPlanTester flightPlanTester;

    public TestFlightPlanService(final FlightPlanRepository flightPlanRepository,
                                 final FlightRouteRepository flightRouteRepository,
                                 final AircraftRepository aircraftRepository,
                                 final AirportRepository airportRepository,
                                 final WeatherDataRepository weatherDataRepository) {
        Preconditions.noneNull(flightPlanRepository, "Flight plan repository must not be null.");
        Preconditions.noneNull(flightRouteRepository, "Flight route repository must not be null.");
        Preconditions.noneNull(aircraftRepository, "Aircraft repository must not be null.");
        Preconditions.noneNull(airportRepository, "Airport repository must not be null.");
        Preconditions.noneNull(weatherDataRepository, "Weather data repository must not be null.");

        this.flightPlanRepository = flightPlanRepository;
        this.flightRouteRepository = flightRouteRepository;
        this.aircraftRepository = aircraftRepository;
        this.airportRepository = airportRepository;
        this.weatherDataRepository = weatherDataRepository;
        this.simulatedFlightPlanFactory = new SimulatedFlightPlanFactory();
        this.flightPlanTester = new FlightPlanTester();
    }

    public Iterable<FlightPlanDTO> listDraftFlightPlansFor(final Pilot pilot) {
        Preconditions.ensure(pilot.isActive(), "Pilot must be active.");

        final List<FlightPlanDTO> draftPlans = new ArrayList<>();

        for (final FlightPlan flightPlan : flightPlanRepository.findByPilotID(pilot.identity())) {
            if (flightPlan.status() == FlightPlanStatus.DRAFT) {
                draftPlans.add(FlightPlanDTO.from(flightPlan));
            }
        }

        return draftPlans;
    }

    public FlightPlanTestBoard testAndValidate(final Pilot authenticatedPilot,
                                               final FlightPlanID flightPlanID) {

        final FlightPlan flightPlan = flightPlanRepository.findById(flightPlanID)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Flight plan not found: " + flightPlanID
                ));

        validateOwnership(flightPlan, authenticatedPilot);
        validateDraftStatus(flightPlan);

        final FlightRoute route = flightRouteRepository.findByRouteCode(flightPlan.routeCode())
                .orElse(null);

        if (route == null) {
            return failAndReject(
                    flightPlan,
                    List.of("Route not found: " + flightPlan.routeCode())
            );
        }

        final Aircraft aircraft = aircraftRepository.findByRegistration(flightPlan.aircraftRegistration())
                .orElse(null);

        if (aircraft == null) {
            return failAndReject(
                    flightPlan,
                    List.of("Aircraft not found: " + flightPlan.aircraftRegistration().code())
            );
        }

        final AircraftModel aircraftModel = aircraft.aircraftModel();

        if (aircraftModel == null) {
            return failAndReject(
                    flightPlan,
                    List.of("Aircraft model is missing for aircraft: " + aircraft.identity())
            );
        }

        final Airport departureAirport = airportRepository.findByIataCode(route.departureAirportIataCode())
                .orElse(null);

        if (departureAirport == null) {
            return failAndReject(
                    flightPlan,
                    List.of("Departure airport not found: " + route.departureAirportIataCode())
            );
        }

        final Airport arrivalAirport = airportRepository.findByIataCode(route.arrivalAirportIataCode())
                .orElse(null);

        if (arrivalAirport == null) {
            return failAndReject(
                    flightPlan,
                    List.of("Arrival airport not found: " + route.arrivalAirportIataCode())
            );
        }

        final List<String> domainErrors = validateDomainRules(
                authenticatedPilot,
                flightPlan,
                route,
                aircraft,
                aircraftModel
        );

        if (!domainErrors.isEmpty()) {
            return failAndReject(flightPlan, domainErrors);
        }

        final Optional<WeatherData> weatherData = loadWeatherDataFor(flightPlan);

        if (flightPlan.weatherDataID() != null && weatherData.isEmpty()) {
            return failAndReject(
                    flightPlan,
                    List.of("Associated weather data was not found: " + flightPlan.weatherDataID())
            );
        }

        final SimulatedFlightPlan simulatedFlightPlan =
                simulatedFlightPlanFactory.build(
                        flightPlan,
                        route,
                        departureAirport,
                        arrivalAirport,
                        aircraftModel
                );

        final FlightPlanTestResult testResult =
                flightPlanTester.test(simulatedFlightPlan, aircraftModel, weatherData);

        flightPlan.recordTestResult(testResult);

        if (testResult.passed()) {
            flightPlan.markTested();
            flightPlan.markValidated();
        } else {
            flightPlan.reject();
        }

        final FlightPlan savedFlightPlan = flightPlanRepository.save(flightPlan);

        return FlightPlanTestBoard.from(savedFlightPlan, testResult);
    }

    private Optional<WeatherData> loadWeatherDataFor(final FlightPlan flightPlan) {
        if (flightPlan.weatherDataID() == null) {
            return Optional.empty();
        }

        return weatherDataRepository.findByID(flightPlan.weatherDataID());
    }

    private void validateOwnership(final FlightPlan flightPlan,
                                   final Pilot authenticatedPilot) {
        Preconditions.ensure(
                flightPlan.pilotID().equals(authenticatedPilot.identity()),
                "The selected flight plan does not belong to the authenticated pilot."
        );
    }

    private void validateDraftStatus(final FlightPlan flightPlan) {
        Preconditions.ensure(
                flightPlan.status() == FlightPlanStatus.DRAFT,
                "Only DRAFT flight plans can be tested and validated."
        );
    }

    private List<String> validateDomainRules(final Pilot authenticatedPilot,
                                             final FlightPlan flightPlan,
                                             final FlightRoute route,
                                             final Aircraft aircraft,
                                             final AircraftModel aircraftModel) {
        final List<String> errors = new ArrayList<>();

        if (!route.isActiveOn(flightPlan.plannedDepartureDateTime().toLocalDate())) {
            errors.add("Route is not active on the planned departure date.");
        }

        if (!route.allowsDepartureAt(flightPlan.plannedDepartureDateTime())) {
            errors.add("Route schedule does not allow the planned departure date/time.");
        }

        if (!aircraft.isOperational()) {
            errors.add("Aircraft is not operational.");
        }

        if (!aircraft.belongsTo(route.airTransportCompanyIataCode())) {
            errors.add("Aircraft does not belong to the route's air transport company.");
        }

        if (!authenticatedPilot.belongsTo(route.airTransportCompanyIataCode())) {
            errors.add("Pilot does not belong to the route's air transport company.");
        }

        if (!authenticatedPilot.isCertifiedFor(
                aircraftModel,
                flightPlan.plannedDepartureDateTime().toLocalDate())) {
            errors.add("Pilot is not certified for the aircraft model on the planned departure date.");
        }

        return errors;
    }

    private FlightPlanTestBoard failAndReject(final FlightPlan flightPlan,
                                              final List<String> messages) {
        final FlightPlanTestResult result = FlightPlanTestResult.failed(
                "Flight plan validation failed.",
                messages,
                0.0,
                0.0,
                0.0,
                0.0
        );

        flightPlan.recordTestResult(result);
        flightPlan.reject();

        final FlightPlan savedFlightPlan = flightPlanRepository.save(flightPlan);

        return FlightPlanTestBoard.from(savedFlightPlan, result);
    }
}