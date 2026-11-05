package aisafe.flightplan.application;

import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.airport.domain.Airport;
import aisafe.airport.domain.AirportRepository;
import aisafe.flightplan.domain.FlightPlan;
import aisafe.flightplan.domain.FlightPlanID;
import aisafe.flightplan.domain.FlightPlanRepository;
import aisafe.flightroute.domain.FlightRoute;
import aisafe.flightroute.domain.FlightRouteRepository;
import aisafe.pilot.domain.Pilot;
import aisafe.pilot.domain.PilotID;
import aisafe.pilot.domain.PilotRepository;
import aisafe.usermanagement.domain.AiSafeRoles;
import aisafe.weather.domain.WeatherData;
import aisafe.weather.domain.WeatherDataID;
import aisafe.weather.domain.WeatherDataRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.validations.Preconditions;

import java.time.LocalDate;
import java.util.Optional;

public class InsertWeatherDataInFlightController {

    private final AuthorizationService authz;
    private final FlightPlanRepository flightPlanRepository;
    private final WeatherDataRepository weatherDataRepository;
    private final PilotRepository pilotRepository;
    private final FlightRouteRepository flightRouteRepository;
    private final AirportRepository airportRepository;

    public InsertWeatherDataInFlightController(final FlightPlanRepository flightPlanRepository,
                                               final WeatherDataRepository weatherDataRepository,
                                               final PilotRepository pilotRepository,
                                               final FlightRouteRepository flightRouteRepository,
                                               final AirportRepository airportRepository) {
        this(
                AuthzRegistry.authorizationService(),
                flightPlanRepository,
                weatherDataRepository,
                pilotRepository,
                flightRouteRepository,
                airportRepository
        );
    }

    public InsertWeatherDataInFlightController(final AuthorizationService authz,
                                               final FlightPlanRepository flightPlanRepository,
                                               final WeatherDataRepository weatherDataRepository,
                                               final PilotRepository pilotRepository,
                                               final FlightRouteRepository flightRouteRepository,
                                               final AirportRepository airportRepository) {
        Preconditions.noneNull(authz, "Authorization service must not be null.");
        Preconditions.noneNull(flightPlanRepository, "Flight plan repository must not be null.");
        Preconditions.noneNull(weatherDataRepository, "Weather data repository must not be null.");
        Preconditions.noneNull(pilotRepository, "Pilot repository must not be null.");
        Preconditions.noneNull(flightRouteRepository, "Flight route repository must not be null.");
        Preconditions.noneNull(airportRepository, "Airport repository must not be null.");

        this.authz = authz;
        this.flightPlanRepository = flightPlanRepository;
        this.weatherDataRepository = weatherDataRepository;
        this.pilotRepository = pilotRepository;
        this.flightRouteRepository = flightRouteRepository;
        this.airportRepository = airportRepository;
    }

    public Pilot getAuthenticatedPilot() {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.PILOT, AiSafeRoles.ADMIN);

        final SystemUser currentUser = authz.session()
                .orElseThrow(() -> new IllegalStateException("No authenticated session found."))
                .authenticatedUser();

        return pilotRepository.findBySystemUser(currentUser)
                .orElseThrow(() -> new IllegalStateException("Pilot not found for current user."));
    }

    public Iterable<FlightPlan> getMyFlightPlans(final PilotID pilotID) {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.PILOT, AiSafeRoles.ADMIN);
        return flightPlanRepository.findByPilotID(pilotID);
    }

    public WeatherSearchSuggestion suggestWeatherSearchFor(final FlightPlan flightPlan) {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.PILOT, AiSafeRoles.ADMIN);

        Preconditions.nonNull(flightPlan, "Flight plan must not be null.");

        final FlightRoute route = flightRouteRepository.findByRouteCode(flightPlan.routeCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Flight route not found: " + flightPlan.routeCode()));

        final Airport arrivalAirport = airportRepository.findByIataCode(route.arrivalAirportIataCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Arrival airport not found: " + route.arrivalAirportIataCode()));

        final AirControlAreaCode areaCode = arrivalAirport.airControlArea().identity();

        return new WeatherSearchSuggestion(
                route.routeCode().toString(),
                route.departureAirportIataCode().toString(),
                route.arrivalAirportIataCode().toString(),
                arrivalAirport.name().toString(),
                areaCode.toString(),
                flightPlan.plannedDepartureDateTime().toLocalDate()
        );
    }

    public Iterable<WeatherData> getWeatherDataByAreaAndDate(final String areaCode,
                                                             final LocalDate date) {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.PILOT, AiSafeRoles.ADMIN);

        return weatherDataRepository.findByAreaAndDate(
                AirControlAreaCode.of(areaCode),
                date
        );
    }

    public Optional<WeatherData> getWeatherDataById(final WeatherDataID weatherDataID) {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.PILOT, AiSafeRoles.ADMIN);
        return weatherDataRepository.findByID(weatherDataID);
    }

    public void insertWeatherData(final FlightPlanID flightPlanID,
                                  final WeatherDataID weatherDataID) {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.PILOT, AiSafeRoles.ADMIN);

        WeatherData weather = weatherDataRepository.findByID(weatherDataID)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Weather data not found for ID: " + weatherDataID));

        final FlightPlan flightPlan = flightPlanRepository.findById(flightPlanID)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Flight plan not found for ID: " + flightPlanID));

        flightPlan.insertWeatherData(weather);

        flightPlanRepository.save(flightPlan);
    }

    public static final class WeatherSearchSuggestion {
        private final String routeCode;
        private final String departureAirport;
        private final String arrivalAirport;
        private final String arrivalAirportName;
        private final String areaCode;
        private final LocalDate flightDate;

        private WeatherSearchSuggestion(final String routeCode,
                                        final String departureAirport,
                                        final String arrivalAirport,
                                        final String arrivalAirportName,
                                        final String areaCode,
                                        final LocalDate flightDate) {
            this.routeCode = routeCode;
            this.departureAirport = departureAirport;
            this.arrivalAirport = arrivalAirport;
            this.arrivalAirportName = arrivalAirportName;
            this.areaCode = areaCode;
            this.flightDate = flightDate;
        }

        public String routeCode() {
            return routeCode;
        }

        public String departureAirport() {
            return departureAirport;
        }

        public String arrivalAirport() {
            return arrivalAirport;
        }

        public String arrivalAirportName() {
            return arrivalAirportName;
        }

        public String areaCode() {
            return areaCode;
        }

        public LocalDate flightDate() {
            return flightDate;
        }
    }
}