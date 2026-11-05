package aisafe.flightplan.application;

import aisafe.aircraft.domain.AircraftRepository;
import aisafe.airport.domain.AirportRepository;
import aisafe.flightplan.domain.CreateFlightPlanFromFileService;
import aisafe.flightplan.domain.FlightPlanRepository;
import aisafe.flightroute.domain.FlightRouteRepository;
import aisafe.pilot.domain.Pilot;
import aisafe.pilot.domain.PilotID;
import aisafe.pilot.domain.PilotRepository;
import aisafe.usermanagement.domain.AiSafeRoles;
import aisafe.weather.domain.WeatherDataRepository;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.validations.Preconditions;

import java.io.IOException;

@UseCaseController
public class CreateFlightPlanFromFileController {

    private final AuthorizationService authz;
    private final PilotRepository pilotRepository;
    private final CreateFlightPlanFromFileService service;

    public CreateFlightPlanFromFileController(final FlightPlanRepository flightPlanRepository,
                                              final FlightRouteRepository flightRouteRepository,
                                              final AircraftRepository aircraftRepository,
                                              final AirportRepository airportRepository,
                                              final WeatherDataRepository weatherDataRepository,
                                              final PilotRepository pilotRepository) {
        this(
                AuthzRegistry.authorizationService(),
                flightPlanRepository,
                flightRouteRepository,
                aircraftRepository,
                airportRepository,
                weatherDataRepository,
                pilotRepository
        );
    }

    CreateFlightPlanFromFileController(final AuthorizationService authz,
                                       final FlightPlanRepository flightPlanRepository,
                                       final FlightRouteRepository flightRouteRepository,
                                       final AircraftRepository aircraftRepository,
                                       final AirportRepository airportRepository,
                                       final WeatherDataRepository weatherDataRepository,
                                       final PilotRepository pilotRepository) {
        Preconditions.noneNull(authz, "Authorization service must not be null.");
        Preconditions.noneNull(pilotRepository, "Pilot repository must not be null.");

        this.authz = authz;
        this.pilotRepository = pilotRepository;
        this.service = new CreateFlightPlanFromFileService(
                flightPlanRepository,
                flightRouteRepository,
                aircraftRepository,
                airportRepository,
                weatherDataRepository
        );
    }

    public String pilotDisplayNameFor(final FlightPlanDTO flightPlan) {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.PILOT);

        Preconditions.nonNull(flightPlan, "Flight plan must not be null.");

        final PilotID pilotID = PilotID.of(flightPlan.pilotID());

        return pilotRepository.findByPilotID(pilotID)
                .map(pilot -> String.format("%s <%s>",
                        pilot.systemUser().name(),
                        pilot.systemUser().email()))
                .orElse(flightPlan.pilotID());
    }

    public CreateFlightPlanFromFileService.ImportFlightPlanResult createFromFile(
            final String filePath) throws IOException {

        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.PILOT);

        final Pilot pilot = currentPilot();

        return service.createFromFile(pilot, filePath);
    }

    private Pilot currentPilot() {
        final SystemUser systemUser = authz.loggedinUserWithPermissions(AiSafeRoles.PILOT)
                .orElseThrow(() -> new IllegalStateException("No authenticated pilot found."));

        return pilotRepository.findBySystemUser(systemUser)
                .orElseThrow(() -> new IllegalStateException(
                        "No Pilot was found for the authenticated user."));
    }
}