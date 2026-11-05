package aisafe.app.common.console.presentation.authz;

import aisafe.persistence.PersistenceContext;
import aisafe.usermanagement.domain.AiSafeUser;
import aisafe.usermanagement.domain.AiSafeUserRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.presentation.console.AbstractUI;

import java.text.SimpleDateFormat;
import java.util.Optional;

@SuppressWarnings("squid:S106")
public class CheckPersonalInformationUI extends AbstractUI {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    private final AiSafeUserRepository aiSafeUserRepository =
            PersistenceContext.repositories().aiSafeUsers();

    @Override
    protected boolean doShow() {
        final Optional<SystemUser> maybeUser = authz.session()
                .map(session -> session.authenticatedUser());

        if (maybeUser.isEmpty()) {
            System.out.println("No authenticated user.");
            return false;
        }

        final SystemUser systemUser = maybeUser.get();
        final Optional<AiSafeUser> maybeAiSafeUser =
                aiSafeUserRepository.findByUsername(systemUser.username());

        System.out.println();
        System.out.println("=== Personal Information ===");
        System.out.printf("Username: %s%n", systemUser.username());
        System.out.printf("Name: %s%n", systemUser.name());
        System.out.printf("Email: %s%n", systemUser.email());
        System.out.printf("Roles: %s%n", systemUser.roleTypes());
        System.out.printf("Active: %s%n", systemUser.isActive() ? "Yes" : "No");

        if (systemUser.createdOn() != null) {
            final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            System.out.printf("Created on: %s%n", formatter.format(systemUser.createdOn().getTime()));
        }

        if (maybeAiSafeUser.isPresent()) {
            final AiSafeUser aiSafeUser = maybeAiSafeUser.get();
            System.out.printf("Phone number: %s%n", aiSafeUser.phoneNumber());
        } else {
            System.out.println("Phone number: not available");
        }

        System.out.println("============================");
        System.out.println();

        return true;
    }

    @Override
    public String headline() {
        return "Check Personal Information";
    }
}