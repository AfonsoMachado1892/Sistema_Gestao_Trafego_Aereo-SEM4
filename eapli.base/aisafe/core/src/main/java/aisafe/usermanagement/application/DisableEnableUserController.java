package aisafe.usermanagement.application;

import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserManagementService;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

@UseCaseController
public class DisableEnableUserController {

    private final AuthorizationService authz;
    private final UserManagementService userSvc;

    /** Production constructor — uses the global registry. */
    public DisableEnableUserController() {
        this(AuthzRegistry.authorizationService(), AuthzRegistry.userService());
    }

    /** Package-private constructor for tests — allows injecting stubs. */
    DisableEnableUserController(final AuthorizationService authz,
                                final UserManagementService userSvc) {
        this.authz = authz;
        this.userSvc = userSvc;
    }

    public Iterable<SystemUser> allUsers() {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.ADMIN);
        return userSvc.allUsers();
    }

    public SystemUser disableUser(final SystemUser user) {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.ADMIN);
        return userSvc.deactivateUser(user);
    }

    public SystemUser enableUser(final SystemUser user) {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.ADMIN);
        return userSvc.activateUser(user);
    }
}