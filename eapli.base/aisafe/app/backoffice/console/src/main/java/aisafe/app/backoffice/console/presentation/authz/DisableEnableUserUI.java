package aisafe.app.backoffice.console.presentation.authz;

import aisafe.usermanagement.application.DisableEnableUserController;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.ArrayList;
import java.util.List;

public class DisableEnableUserUI extends AbstractUI {

    private final DisableEnableUserController controller = new DisableEnableUserController();

    @Override
    protected boolean doShow() {
        final List<SystemUser> users = new ArrayList<>();
        controller.allUsers().forEach(users::add);

        if (users.isEmpty()) {
            System.out.println("No users found.");
            return false;
        }

        // Listar utilizadores com índice
        System.out.println("\n--- Users ---");
        for (int i = 0; i < users.size(); i++) {
            final SystemUser u = users.get(i);
            final String status = u.isActive() ? "ACTIVE" : "DISABLED";
            System.out.printf("%d. %-20s [%s]%n", i + 1, u.username(), status);
        }

        final int idx = Console.readInteger("Select user number (0 to cancel):") - 1;
        if (idx < 0 || idx >= users.size()) {
            System.out.println("Cancelled.");
            return false;
        }

        final SystemUser selected = users.get(idx);

        if (selected.isActive()) {
            controller.disableUser(selected);
            System.out.printf("User '%s' has been DISABLED.%n", selected.username());
        } else {
            controller.enableUser(selected);
            System.out.printf("User '%s' has been ENABLED.%n", selected.username());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Disable / Enable Backoffice User";
    }
}