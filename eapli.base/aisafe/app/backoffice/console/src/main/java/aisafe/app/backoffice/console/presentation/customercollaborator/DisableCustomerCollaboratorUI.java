package aisafe.app.backoffice.console.presentation.customercollaborator;

import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.service.DisableCustomerCollaboratorController;
import aisafe.persistence.PersistenceContext;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.io.util.Console;

import java.util.ArrayList;
import java.util.List;

public class DisableCustomerCollaboratorUI extends AbstractUI {

    private final DisableCustomerCollaboratorController theController =
            new DisableCustomerCollaboratorController(
                    PersistenceContext.repositories().customerCollaborators()
            );

    @Override
    protected boolean doShow() {

        // Step 1: list active collaborators
        final List<CustomerCollaborator> list = new ArrayList<>();
        theController.activeCollaborators().forEach(list::add);

        if (list.isEmpty()) {
            System.out.println("No active collaborators found.");
            return false;
        }

        System.out.println("\nActive collaborators:");
        for (int i = 0; i < list.size(); i++) {
            final CustomerCollaborator c = list.get(i);
            System.out.printf("  %d. %s | %s | %s | %s%n",
                    i + 1,
                    c.systemUser().name(),
                    c.email(),
                    c.position(),
                    c.customerReference());
        }

        // Step 2: select collaborator
        final int choice = Console.readInteger("\nSelect collaborator (number):");
        if (choice < 1 || choice > list.size()) {
            System.out.println("Invalid option.");
            return false;
        }

        final CustomerCollaborator selected = list.get(choice - 1);

        // Step 3: show info and ask for confirmation
        System.out.println("\nCollaborator to disable:");
        System.out.printf("  Name    : %s%n", selected.systemUser().name());
        System.out.printf("  Email   : %s%n", selected.email());
        System.out.printf("  Position: %s%n", selected.position());
        System.out.printf("  Customer: %s%n", selected.customerReference());

        final String confirm = Console.readNonEmptyLine(
                "\nAre you sure you want to disable this collaborator? (yes/no):",
                "Please enter yes or no.");

        if (!confirm.equalsIgnoreCase("yes")) {
            System.out.println("Operation cancelled.");
            return false;
        }

        // Step 4: disable
        try {
            theController.disableCollaborator(selected.identity().toString());
            System.out.println("\nCollaborator disabled successfully.");
        } catch (final IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Disable Customer Collaborator";
    }
}