package aisafe.flightplan.application;

import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRepository;
import aisafe.flightplan.application.FlightPlanDTO;
import aisafe.flightplan.domain.FlightPlanRepository;
import aisafe.flightplan.domain.FlightPlanService;
import aisafe.flightplan.domain.FuelUnit;
import aisafe.flightroute.application.FlightRouteDTO;
import aisafe.flightroute.domain.FlightRouteRepository;
import aisafe.pilot.domain.Pilot;
import aisafe.pilot.domain.PilotID;
import aisafe.pilot.domain.PilotRepository;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.validations.Preconditions;

import java.time.LocalDateTime;

@UseCaseController
public class CreateFlightPlanController {

    private final AuthorizationService authz;
    private final PilotRepository pilotRepository;
    private final FlightPlanService flightPlanService;

    public CreateFlightPlanController(final FlightPlanRepository flightPlanRepository,
                                      final FlightRouteRepository flightRouteRepository,
                                      final AircraftRepository aircraftRepository,
                                      final PilotRepository pilotRepository) {
        this(
                AuthzRegistry.authorizationService(),
                flightPlanRepository,
                flightRouteRepository,
                aircraftRepository,
                pilotRepository
        );
    }

    /** Package-private constructor for tests. */
    CreateFlightPlanController(final AuthorizationService authz,
                               final FlightPlanRepository flightPlanRepository,
                               final FlightRouteRepository flightRouteRepository,
                               final AircraftRepository aircraftRepository,
                               final PilotRepository pilotRepository) {
        Preconditions.noneNull(authz, "Authorization service must not be null.");
        Preconditions.noneNull(pilotRepository, "Pilot repository must not be null.");

        this.authz = authz;
        this.pilotRepository = pilotRepository;
        this.flightPlanService = new FlightPlanService(
                flightPlanRepository,
                flightRouteRepository,
                aircraftRepository
        );
    }

    public Iterable<FlightRouteDTO> listAvailableRoutes() {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.PILOT);

        final Pilot pilot = currentPilot();

        Preconditions.ensure(pilot.isActive(), "Pilot must be active.");

        return flightPlanService.listAvailableRoutesFor(pilot);
    }

    public Iterable<Aircraft> listAvailableAircrafts() {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.PILOT);

        final Pilot pilot = currentPilot();

        Preconditions.ensure(pilot.isActive(), "Pilot must be active.");

        return flightPlanService.listAvailableAircraftFor(pilot);
    }

    public boolean routeAllowsDepartureAt(final String routeCodeValue,
                                          final LocalDateTime plannedDepartureDateTime) {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.PILOT);

        final Pilot pilot = currentPilot();

        Preconditions.ensure(pilot.isActive(), "Pilot must be active.");

        return flightPlanService.routeAllowsDepartureAtFor(
                pilot,
                routeCodeValue,
                plannedDepartureDateTime
        );
    }

    public FlightPlanDTO createFlightPlan(final String routeCodeValue,
                                          final String aircraftRegistrationValue,
                                          final LocalDateTime plannedDepartureDateTime,
                                          final LocalDateTime plannedArrivalDateTime,
                                          final double fuelAmount,
                                          final FuelUnit fuelUnit,
                                          final String descriptionText) {

        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.PILOT);

        final Pilot pilot = currentPilot();

        return flightPlanService.createDraftFlightPlan(
                pilot,
                routeCodeValue,
                aircraftRegistrationValue,
                plannedDepartureDateTime,
                plannedArrivalDateTime,
                fuelAmount,
                fuelUnit,
                descriptionText
        );
    }

    public Iterable<FlightPlanDTO> listMyCreatedFlightPlans() {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.PILOT);

        final Pilot pilot = currentPilot();

        Preconditions.ensure(pilot.isActive(), "Pilot must be active.");

        return flightPlanService.listFlightPlansFor(pilot);
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

    private Pilot currentPilot() {
        final SystemUser systemUser = authz.loggedinUserWithPermissions(AiSafeRoles.PILOT)
                .orElseThrow(() -> new IllegalStateException("No authenticated pilot found."));

        return pilotRepository.findBySystemUser(systemUser)
                .orElseThrow(() -> new IllegalStateException(
                        "No Pilot was found for the authenticated user."));
    }
}