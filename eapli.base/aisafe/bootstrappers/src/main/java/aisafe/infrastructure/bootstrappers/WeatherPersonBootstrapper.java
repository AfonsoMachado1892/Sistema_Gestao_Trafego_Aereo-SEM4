package aisafe.infrastructure.bootstrappers;

import aisafe.persistence.PersistenceContext;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.actions.Action;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class WeatherPersonBootstrapper extends AbstractUserBootstrapper implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherPersonBootstrapper.class);

    @Override
    public boolean execute() {
        register(
                "weather1", "Password1", "Maria", "Santos", "maria.santos@aisafe.pt",
                351, "912333444"
        );

        return true;
    }

    private void register(final String username,
                          final String password,
                          final String firstName,
                          final String lastName,
                          final String email,
                          final int countryCode,
                          final String phoneNumber) {
        try {
            final Set<Role> roles = new HashSet<>();
            roles.add(AiSafeRoles.WEATHER_PERSON);

            final SystemUser systemUser = registerUser(
                    username,
                    password,
                    firstName,
                    lastName,
                    email,
                    countryCode,
                    phoneNumber,
                    roles
            );

            LOGGER.debug("»»» Weather Person registered: {}", username);

        } catch (final Exception e) {
            LOGGER.warn("Weather Person skipped [{}]: {}", username, e.getMessage());
        }

    }


}