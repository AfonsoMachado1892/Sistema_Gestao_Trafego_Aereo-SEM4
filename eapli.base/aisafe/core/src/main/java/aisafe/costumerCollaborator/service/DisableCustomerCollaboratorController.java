package aisafe.costumerCollaborator.service;

import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.domain.CustomerCollaboratorID;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserManagementService;

@UseCaseController
public class DisableCustomerCollaboratorController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final UserManagementService userSvc = AuthzRegistry.userService();

    private final CustomerCollaboratorRepository collaboratorRepository;

    public DisableCustomerCollaboratorController(
            final CustomerCollaboratorRepository collaboratorRepository) {
        this.collaboratorRepository = collaboratorRepository;
    }

    /**
     * Returns all active collaborators for selection in the UI.
     */
    public Iterable<CustomerCollaborator> activeCollaborators() {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.BACKOFFICE_OPERATOR, AiSafeRoles.ADMIN);
        return collaboratorRepository.findAllActive();
    }

    /**
     * Disables a collaborator and deactivates the associated SystemUser
     * so they may not use the system anymore.
     *
     * @param collaboratorId the collaborator's ID string
     * @return the updated CustomerCollaborator
     */
    public CustomerCollaborator disableCollaborator(final String collaboratorId) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.BACKOFFICE_OPERATOR, AiSafeRoles.ADMIN);

        final CustomerCollaborator collaborator = collaboratorRepository
                .findById(CustomerCollaboratorID.of(collaboratorId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Collaborator not found: " + collaboratorId));

        // Disable the collaborator (sets status = DISABLED)
        collaborator.disable();
        collaboratorRepository.save(collaborator);

        // Deactivate the SystemUser so they cannot login anymore
        userSvc.deactivateUser(collaborator.systemUser());

        return collaborator;
    }
}