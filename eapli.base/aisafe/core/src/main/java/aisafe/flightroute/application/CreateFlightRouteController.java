package aisafe.flightroute.application;

import aisafe.airport.domain.AirportRepository;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.flightplan.domain.FlightPlanRepository;
import aisafe.flightroute.domain.*;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

@UseCaseController
public class CreateFlightRouteController {

    private final AuthorizationService authz;
    private final FlightRouteService flightRouteService;
    private final CustomerCollaboratorRepository customerCollaboratorRepository;

    public CreateFlightRouteController(
            final FlightRouteRepository flightRouteRepository,
            final AirportRepository airportRepository,
            final FlightPlanRepository flightPlanRepository,
            final CustomerCollaboratorRepository customerCollaboratorRepository) {
        this(
                AuthzRegistry.authorizationService(),
                flightRouteRepository,
                airportRepository,
                flightPlanRepository,
                customerCollaboratorRepository
        );
    }

    /** Package-private constructor for tests. */
    CreateFlightRouteController(
            final AuthorizationService authz,
            final FlightRouteRepository flightRouteRepository,
            final AirportRepository airportRepository,
            final FlightPlanRepository flightPlanRepository,
            final CustomerCollaboratorRepository customerCollaboratorRepository) {
        this.authz = authz;
        this.flightRouteService = new FlightRouteService(
                flightRouteRepository, airportRepository, flightPlanRepository);
        this.customerCollaboratorRepository = customerCollaboratorRepository;
    }

    public Iterable<AirportDTO> listAirports() {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        return flightRouteService.listAirports();
    }

    public FlightRouteDTO createRegularFlightRoute(final String routeCode,
                                                   final String departureIataCode,
                                                   final String arrivalIataCode,
                                                   final RegularSchedule regularSchedule) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        final String companyIataCode = currentCompanyIataCode();

        final FlightRoute route = flightRouteService.createRegularFlightRoute(
                routeCode,
                departureIataCode,
                arrivalIataCode,
                AirTransportCompanyCode.of(companyIataCode),
                regularSchedule
        );

        return flightRouteService.toDTO(route);
    }

    public FlightRouteDTO createCharterFlightRoute(final String routeCode,
                                                   final String departureIataCode,
                                                   final String arrivalIataCode,
                                                   final CharterSchedule charterSchedule) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        final String companyIataCode = currentCompanyIataCode();

        final FlightRoute route = flightRouteService.createCharterFlightRoute(
                routeCode,
                departureIataCode,
                arrivalIataCode,
                AirTransportCompanyCode.of(companyIataCode),
                charterSchedule
        );

        return flightRouteService.toDTO(route);
    }

    /** Package-private method for tests. */
    FlightRouteDTO createRegularFlightRoute(final String routeCode,
                                            final String departureIataCode,
                                            final String arrivalIataCode,
                                            final String companyIataCode,
                                            final RegularSchedule regularSchedule) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        final FlightRoute route = flightRouteService.createRegularFlightRoute(
                routeCode,
                departureIataCode,
                arrivalIataCode,
                AirTransportCompanyCode.of(companyIataCode),
                regularSchedule
        );

        return flightRouteService.toDTO(route);
    }

    /** Package-private method for tests. */
    FlightRouteDTO createCharterFlightRoute(final String routeCode,
                                            final String departureIataCode,
                                            final String arrivalIataCode,
                                            final String companyIataCode,
                                            final CharterSchedule charterSchedule) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        final FlightRoute route = flightRouteService.createCharterFlightRoute(
                routeCode,
                departureIataCode,
                arrivalIataCode,
                AirTransportCompanyCode.of(companyIataCode),
                charterSchedule
        );

        return flightRouteService.toDTO(route);
    }

    public String currentCompanyIataCode() {
        final SystemUser systemUser = authz.loggedinUserWithPermissions(
                        AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)
                .orElseThrow(() -> new IllegalStateException("No authenticated user found."));

        final CustomerCollaborator collaborator = customerCollaboratorRepository
                .findBySystemUser(systemUser)
                .orElseThrow(() -> new IllegalStateException(
                        "No collaborator found for the authenticated user."));

        return collaborator.customerCode();
    }
}