package aisafe.app.backoffice.console.presentation.authz;

import aisafe.usermanagement.application.ListUsersController;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.presentation.console.AbstractUI;

public class ListUsersUI extends AbstractUI {

    private final ListUsersController theController = new ListUsersController();

    @Override
    protected boolean doShow() {
        final Iterable<SystemUser> users = theController.allUsers();

        System.out.println("\n+--------------------------------+------------------------------+----------+");
        System.out.printf( "| %-30s | %-28s | %-8s |%n", "Name", "Email", "Status");
        System.out.println("+--------------------------------+------------------------------+----------+");

        int count = 0;
        for (final SystemUser user : users) {
            count++;
            System.out.printf("| %-30s | %-28s | %-8s |%n",
                    user.name().toString(),
                    user.email().toString(),
                    user.isActive() ? "ACTIVE" : "DISABLED");
        }

        System.out.println("+--------------------------------+------------------------------+----------+");
        System.out.printf("  Total: %d user(s)%n%n", count);

        return false;
    }

    @Override
    public String headline() {
        return "List Backoffice Users";
    }
}