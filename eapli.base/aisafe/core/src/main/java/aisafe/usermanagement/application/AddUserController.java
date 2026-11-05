package aisafe.usermanagement.application;

import aisafe.usermanagement.domain.*;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserManagementService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.time.util.CurrentTimeCalendars;
import eapli.framework.validations.Preconditions;

import java.util.Calendar;
import java.util.Set;

@UseCaseController
public class AddUserController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final UserManagementService userSvc = AuthzRegistry.userService();

    private final AiSafeUserRepository aiSafeUserRepository;

    public AddUserController(final AiSafeUserRepository aiSafeUserRepository) {
        Preconditions.nonNull(aiSafeUserRepository);
        this.aiSafeUserRepository = aiSafeUserRepository;
    }

    public Role[] getRoleTypes() {
        return AiSafeRoles.nonUserValues();
    }

    public SystemUser addUser(final String username, final String password,
                              final String firstName, final String lastName,
                              final String email, final int countryCode,
                              final String phoneNumber, final Set<Role> roles,
                              final Calendar createdOn) {

        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.ADMIN);

        final SystemUser systemUser = userSvc.registerNewUser(
                username, password, firstName, lastName, email, roles, createdOn
        );

        final PhoneNumber number = PhoneNumber.of(countryCode, phoneNumber);
        final AiSafeUser aiSafeUser = new AiSafeUserFactory().withSystemUser(systemUser).withPhoneNumber(countryCode, phoneNumber).build();

        aiSafeUserRepository.save(aiSafeUser);

        return systemUser;
    }

    public SystemUser addUser(final String username, final String password,
                              final String firstName, final String lastName,
                              final String email, final int countryCode,
                              final String phoneNumber, final Set<Role> roles) {

        return addUser(username, password, firstName, lastName, email,
                countryCode, phoneNumber, roles, CurrentTimeCalendars.now());
    }
}