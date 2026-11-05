package aisafe.flightroute.application;

import aisafe.airport.domain.AirportRepository;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.flightplan.domain.FlightPlanRepository;
import aisafe.flightroute.domain.FlightRoute;
import aisafe.flightroute.domain.FlightRouteRepository;
import aisafe.flightroute.domain.FlightRouteService;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.time.LocalDate;

@UseCaseController
public class DeactivateFlightRouteController {

    private final AuthorizationService authz;
    private final FlightRouteService flightRouteService;
    private final CustomerCollaboratorRepository customerCollaboratorRepository;

    public DeactivateFlightRouteController(
            final FlightRouteRepository flightRouteRepository,
            final AirportRepository airportRepository,
            final FlightPlanRepository flightPlanRepository,
            final CustomerCollaboratorRepository customerCollaboratorRepository) {
        this(AuthzRegistry.authorizationService(),
                flightRouteRepository,
                airportRepository,
                flightPlanRepository,
                customerCollaboratorRepository);
    }

    /** Package-private constructor for tests. */
    DeactivateFlightRouteController(
            final AuthorizationService authz,
            final FlightRouteRepository flightRouteRepository,
            final AirportRepository airportRepository,
            final FlightPlanRepository flightPlanRepository,
            final CustomerCollaboratorRepository customerCollaboratorRepository) {
        this.authz = authz;
        this.flightRouteService = new FlightRouteService(
                flightRouteRepository,
                airportRepository,
                flightPlanRepository
        );
        this.customerCollaboratorRepository = customerCollaboratorRepository;
    }

    public Iterable<FlightRouteDTO> listActiveRoutes() {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        final AirTransportCompanyCode companyCode =
                AirTransportCompanyCode.of(currentCompanyIataCode());

        return flightRouteService.listActiveRoutesByCompany(companyCode);
    }

    public Iterable<FlightRouteDTO> listCompanyRoutes() {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        final AirTransportCompanyCode companyCode =
                AirTransportCompanyCode.of(currentCompanyIataCode());

        return flightRouteService.listCompanyRoutes(companyCode);
    }

    public FlightRouteDTO deactivateRoute(final String routeCode,
                                          final LocalDate deactivationDate) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        final FlightRoute route = flightRouteService.deactivateRoute(
                routeCode,
                deactivationDate,
                AirTransportCompanyCode.of(currentCompanyIataCode())
        );

        return flightRouteService.toDTO(route);
    }

    /** Package-private for tests — bypasses authentication. */
    FlightRouteDTO deactivateRoute(final String routeCode,
                                   final LocalDate deactivationDate,
                                   final String companyIataCode) {
        final FlightRoute route = flightRouteService.deactivateRoute(
                routeCode,
                deactivationDate,
                AirTransportCompanyCode.of(companyIataCode)
        );

        return flightRouteService.toDTO(route);
    }

    private String currentCompanyIataCode() {
        final SystemUser systemUser = authz.loggedinUserWithPermissions(
                        AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)
                .orElseThrow(() -> new IllegalStateException(
                        "No authenticated user found."));

        final CustomerCollaborator collaborator = customerCollaboratorRepository
                .findBySystemUser(systemUser)
                .orElseThrow(() -> new IllegalStateException(
                        "No collaborator found for the authenticated user."));

        return collaborator.customerCode();
    }
}