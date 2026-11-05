package aisafe.pilot.application;

import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.aircraftmodel.domain.AircraftModelRepository;
import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.pilot.domain.PilotRepository;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.util.List;

/**
 * Controller for US076 — List Pilot Roster.
 *
 * <p>The ATCC lists all pilots (active and inactive) of their own company.
 * The roster is always scoped to the collaborator's company.
 */
@UseCaseController
public class ListPilotRosterController {

    private final AuthorizationService authz;
    private final PilotService pilotService;
    private final CustomerCollaboratorRepository customerCollaboratorRepository;

    /**
     * Production constructor — resolves AuthorizationService from AuthzRegistry.
     */
    public ListPilotRosterController(
            final PilotRepository pilotRepository,
            final AircraftModelRepository aircraftModelRepository,
            final CustomerCollaboratorRepository customerCollaboratorRepository) {
        this(AuthzRegistry.authorizationService(),
                pilotRepository, aircraftModelRepository, customerCollaboratorRepository);
    }

    /**
     * Package-private constructor for unit tests — allows injecting an
     * {@link AuthorizationService} stub without touching AuthzRegistry.
     */
    ListPilotRosterController(
            final AuthorizationService authz,
            final PilotRepository pilotRepository,
            final AircraftModelRepository aircraftModelRepository,
            final CustomerCollaboratorRepository customerCollaboratorRepository) {
        this.authz = authz;
        this.pilotService = new PilotService(pilotRepository, aircraftModelRepository);
        this.customerCollaboratorRepository = customerCollaboratorRepository;
    }

    /**
     * Returns the roster (all pilots, active and inactive) of the
     * authenticated collaborator's company.
     */
    public List<PilotDTO> listPilotRoster() {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

        final SystemUser systemUser = authz.loggedinUserWithPermissions(
                        AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)
                .orElseThrow(() -> new IllegalStateException("No authenticated user found."));

        final AirTransportCompanyCode companyCode = resolveCompanyCode(systemUser);
        return pilotService.listPilotsByCompany(companyCode);
    }

    /**
     * Package-private — used in tests to bypass auth and pass company code directly.
     */
    List<PilotDTO> listPilotRoster(final AirTransportCompanyCode companyCode) {
        return pilotService.listPilotsByCompany(companyCode);
    }

    private AirTransportCompanyCode resolveCompanyCode(final SystemUser systemUser) {
        final CustomerCollaborator collaborator = customerCollaboratorRepository
                .findBySystemUser(systemUser)
                .orElseThrow(() -> new IllegalStateException(
                        "No collaborator found for this user."));
        return AirTransportCompanyCode.of(collaborator.customerCode());
    }
}
