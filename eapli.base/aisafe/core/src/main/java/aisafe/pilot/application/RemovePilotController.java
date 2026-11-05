package aisafe.pilot.application;

import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.aircraftmodel.domain.AircraftModelRepository;
import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.flightplan.domain.FlightPlanRepository;
import aisafe.pilot.domain.PilotID;
import aisafe.pilot.domain.PilotRepository;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.util.List;

/**
 * Controller for US077 — Remove (deactivate) a Pilot.
 *
 * <p>Business rules enforced via {@link PilotService#removePilot}:
 * <ul>
 *   <li>The pilot must exist.</li>
 *   <li>The pilot must belong to the requesting collaborator's company.</li>
 *   <li>The pilot must have no flight plans assigned (DRAFT, TESTED or VALIDATED).</li>
 * </ul>
 */
@UseCaseController
public class RemovePilotController {

    private final AuthorizationService authz;
    private final PilotService pilotService;
    private final FlightPlanRepository flightPlanRepository;
    private final CustomerCollaboratorRepository customerCollaboratorRepository;

    /**
     * Production constructor — resolves AuthorizationService from AuthzRegistry.
     */
    public RemovePilotController(
            final PilotRepository pilotRepository,
            final AircraftModelRepository aircraftModelRepository,
            final FlightPlanRepository flightPlanRepository,
            final CustomerCollaboratorRepository customerCollaboratorRepository) {
        this(AuthzRegistry.authorizationService(),
                pilotRepository, aircraftModelRepository,
                flightPlanRepository, customerCollaboratorRepository);
    }

    /**
     * Package-private constructor for unit tests — mirrors the pattern of
     * AddPilotController and DeactivateFlightRouteController.
     */
    RemovePilotController(
            final AuthorizationService authz,
            final PilotRepository pilotRepository,
            final AircraftModelRepository aircraftModelRepository,
            final FlightPlanRepository flightPlanRepository,
            final CustomerCollaboratorRepository customerCollaboratorRepository) {
        this.authz = authz;
        this.pilotService = new PilotService(pilotRepository, aircraftModelRepository);
        this.flightPlanRepository = flightPlanRepository;
        this.customerCollaboratorRepository = customerCollaboratorRepository;
    }

    /**
     * Returns the active pilots of the authenticated collaborator's company,
     * used to populate the selection list in the UI. Only active pilots can
     * be selected for removal.
     */
    public List<PilotDTO> getActivePilots() {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        final AirTransportCompanyCode companyCode =
                resolveCompanyCode(authenticatedUser());
        return pilotService.listActivePilotsByCompany(companyCode);
    }

    /**
     * Deactivates the pilot identified by {@code pilotID}.
     * The company code is resolved from the authenticated collaborator.
     */
    public PilotDTO removePilot(final String pilotID) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        final AirTransportCompanyCode companyCode =
                resolveCompanyCode(authenticatedUser());
        return removePilot(pilotID, companyCode.toString());
    }

    /**
     * Package-private variant used in unit tests to pass company code directly,
     * bypassing auth-based resolution — mirrors DeactivateFlightRouteController.
     */
    PilotDTO removePilot(final String pilotID, final String companyIataCode) {
        return pilotService.removePilot(
                PilotID.of(pilotID),
                AirTransportCompanyCode.of(companyIataCode),
                flightPlanRepository);
    }

    private SystemUser authenticatedUser() {
        return authz.loggedinUserWithPermissions(
                        AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)
                .orElseThrow(() -> new IllegalStateException("No authenticated user found."));
    }

    private AirTransportCompanyCode resolveCompanyCode(final SystemUser systemUser) {
        final CustomerCollaborator collaborator = customerCollaboratorRepository
                .findBySystemUser(systemUser)
                .orElseThrow(() -> new IllegalStateException(
                        "No collaborator found for this user."));
        return AirTransportCompanyCode.of(collaborator.customerCode());
    }
}
