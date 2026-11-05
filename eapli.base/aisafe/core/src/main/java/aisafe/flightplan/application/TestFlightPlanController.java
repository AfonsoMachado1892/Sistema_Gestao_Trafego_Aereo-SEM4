package aisafe.flightplan.application;

import aisafe.aircraft.domain.AircraftRepository;
import aisafe.airport.domain.AirportRepository;
import aisafe.flightplan.domain.FlightPlanID;
import aisafe.flightplan.domain.FlightPlanRepository;
import aisafe.flightplan.domain.TestFlightPlanService;
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

@UseCaseController
public class TestFlightPlanController {

    private final AuthorizationService authz;
    private final PilotRepository pilotRepository;
    private final TestFlightPlanService service;

    public TestFlightPlanController(final FlightPlanRepository flightPlanRepository,
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

    TestFlightPlanController(final AuthorizationService authz,
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
        this.service = new TestFlightPlanService(
                flightPlanRepository,
                flightRouteRepository,
                aircraftRepository,
                airportRepository,
                weatherDataRepository
        );
    }

    public Iterable<FlightPlanDTO> listDraftFlightPlans() {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.PILOT);

        final Pilot pilot = currentPilot();

        return service.listDraftFlightPlansFor(pilot);
    }

    public FlightPlanTestBoard testAndValidate(final String flightPlanIDValue) {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.PILOT);

        Preconditions.nonEmpty(flightPlanIDValue, "Flight plan ID must not be empty.");

        final Pilot pilot = currentPilot();

        final FlightPlanID flightPlanID = FlightPlanID.of(flightPlanIDValue.trim());

        return service.testAndValidate(pilot, flightPlanID);
    }

    private Pilot currentPilot() {
        final SystemUser systemUser = authz.loggedinUserWithPermissions(AiSafeRoles.PILOT)
                .orElseThrow(() -> new IllegalStateException("No authenticated pilot found."));

        return pilotRepository.findBySystemUser(systemUser)
                .orElseThrow(() -> new IllegalStateException(
                        "No Pilot was found for the authenticated user."
                ));
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
}