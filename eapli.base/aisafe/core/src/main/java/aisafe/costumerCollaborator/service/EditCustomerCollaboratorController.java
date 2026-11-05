package aisafe.costumerCollaborator.service;

import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.domain.CustomerCollaboratorID;
import aisafe.usermanagement.domain.AiSafeUser;
import aisafe.usermanagement.domain.AiSafeUserRepository;
import aisafe.usermanagement.domain.AiSafeRoles;
import aisafe.usermanagement.domain.PhoneNumber;
import aisafe.usermanagement.domain.legacyFiles.Email;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.validations.Preconditions;

@UseCaseController
public class EditCustomerCollaboratorController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    private final CustomerCollaboratorRepository collaboratorRepository;
    private final AiSafeUserRepository aiSafeUserRepository;

    public EditCustomerCollaboratorController(
            final CustomerCollaboratorRepository collaboratorRepository,
            final AiSafeUserRepository aiSafeUserRepository
    ) {
        this.collaboratorRepository = collaboratorRepository;
        this.aiSafeUserRepository   = aiSafeUserRepository;
    }

    /**
     * Loads a collaborator by ID for display.
     */
    public CustomerCollaborator findCollaborator(final String collaboratorId) {
        ensureBackofficeOperatorOrAdmin();

        return collaboratorRepository
                .findById(CustomerCollaboratorID.of(collaboratorId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Collaborator not found: " + collaboratorId));
    }

    /**
     * Lists all active collaborators (for selection in the UI).
     */
    public Iterable<CustomerCollaborator> activeCollaborators() {
        ensureBackofficeOperatorOrAdmin();
        return collaboratorRepository.findAllActive();
    }

    /**
     * Updates the email and phone number of a collaborator.
     * All other fields remain unchanged.
     *
     * @param collaboratorId the collaborator's ID string
     * @param newEmail       new email address
     * @param newPhone       new phone number (e.g. "+351 910000000")
     * @return the updated CustomerCollaborator
     */
    public CustomerCollaborator editCollaborator(
            final String collaboratorId,
            final String newEmail,
            final String newPhone,
            final int countryCode
    ) {
        ensureBackofficeOperatorOrAdmin();

        Preconditions.nonEmpty(newEmail, "Email cannot be empty.");

        final CustomerCollaborator collaborator = collaboratorRepository
                .findById(CustomerCollaboratorID.of(collaboratorId))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Collaborator not found: " + collaboratorId));

        collaborator.changeEmail(Email.of(newEmail));
        collaboratorRepository.save(collaborator);

        final AiSafeUser aiSafeUser = aiSafeUserRepository
                .findByUsername(collaborator.systemUser().username())
                .orElseThrow(() -> new IllegalStateException(
                        "AiSafeUser not found for collaborator: " + collaboratorId));

        aiSafeUser.changePhoneNumber(PhoneNumber.of(countryCode, newPhone));
        aiSafeUserRepository.save(aiSafeUser);

        return collaborator;
    }

    private void ensureBackofficeOperatorOrAdmin() {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN
        );
    }
}