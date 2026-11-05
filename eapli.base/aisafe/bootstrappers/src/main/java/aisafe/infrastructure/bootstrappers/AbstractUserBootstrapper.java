package aisafe.infrastructure.bootstrappers;

import aisafe.persistence.PersistenceContext;
import aisafe.usermanagement.domain.AiSafeUser;
import aisafe.usermanagement.domain.AiSafeUserFactory;
import aisafe.usermanagement.domain.AiSafeUserRepository;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserManagementService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.time.util.CurrentTimeCalendars;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public abstract class AbstractUserBootstrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractUserBootstrapper.class);

    private final UserManagementService userSvc = AuthzRegistry.userService();
    private final AiSafeUserRepository aiSafeUserRepository =
            PersistenceContext.repositories().aiSafeUsers();

    protected SystemUser registerUser(final String username, final String password,
                                      final String firstName, final String lastName,
                                      final String email, final int countryCode,
                                      final String phoneNumber, final Set<Role> roles) {

        final Username userName = Username.valueOf(username);

        final var existingUser = userSvc.userOfIdentity(userName);
        if (existingUser.isPresent()) {
            LOGGER.info("»»» Utilizador já existe no bootstrap: {}", username);
            return existingUser.get();
        }

        final SystemUser systemUser = userSvc.registerNewUser(
                username,
                password,
                firstName,
                lastName,
                email,
                roles,
                CurrentTimeCalendars.now()
        );

        final AiSafeUser aiSafeUser = new AiSafeUserFactory()
                .withPhoneNumber(countryCode, phoneNumber)
                .withSystemUser(systemUser)
                .build();

        aiSafeUserRepository.save(aiSafeUser);

        LOGGER.debug("»»» Utilizador registado no bootstrap: {}", username);

        return systemUser;
    }
}