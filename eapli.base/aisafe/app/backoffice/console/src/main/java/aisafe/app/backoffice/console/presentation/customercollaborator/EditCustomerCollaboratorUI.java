package aisafe.app.backoffice.console.presentation.customercollaborator;

import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.service.EditCustomerCollaboratorController;
import aisafe.persistence.PersistenceContext;
import eapli.framework.presentation.console.AbstractUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EditCustomerCollaboratorUI extends AbstractUI {

    private final EditCustomerCollaboratorController controller =
            new EditCustomerCollaboratorController(
                    PersistenceContext.repositories().customerCollaborators(),
                    PersistenceContext.repositories().aiSafeUsers()
            );

    private final Scanner scanner = new Scanner(System.in);

    @Override
    protected boolean doShow() {

        // Step 1: list active collaborators to pick from
        final List<CustomerCollaborator> list = new ArrayList<>();
        controller.activeCollaborators().forEach(list::add);

        if (list.isEmpty()) {
            System.out.println("No active collaborators found.");
            return false;
        }

        System.out.println("\nSelect a collaborator to edit:");
        for (int i = 0; i < list.size(); i++) {
            final CustomerCollaborator c = list.get(i);
            System.out.printf("  %d. %s | %s | %s%n",
                    i + 1,
                    c.systemUser().name(),
                    c.email(),
                    c.position());
        }

        System.out.print("Option: ");
        final int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (final NumberFormatException e) {
            System.out.println("Invalid option.");
            return false;
        }

        if (choice < 1 || choice > list.size()) {
            System.out.println("Invalid option.");
            return false;
        }

        final CustomerCollaborator selected = list.get(choice - 1);
        final String id = selected.identity().toString();

        // Step 2: show current info
        System.out.println("\nCurrent info:");
        System.out.printf("  Name    : %s%n", selected.systemUser().name());
        System.out.printf("  Email   : %s%n", selected.email());
        System.out.printf("  Position: %s%n", selected.position());

        // Step 3: collect new values (blank = keep current)
        System.out.print("\nNew email (leave blank to keep current): ");
        final String inputEmail = scanner.nextLine().trim();
        final String newEmail = inputEmail.isEmpty()
                ? selected.email().toString()
                : inputEmail;

        System.out.println("New phone (leave blank to keep current)");
        System.out.print("Insert a country code: ");
        final int countryCode = scanner.nextInt();
        scanner.nextLine();
        final String inputPhone = scanner.nextLine().trim();


        try {
            if (inputPhone.isEmpty()) {
                selected.changeEmail(
                        aisafe.usermanagement.domain.legacyFiles.Email.of(newEmail));
                System.out.println("\nCollaborator updated successfully.");
            } else {
                controller.editCollaborator(id, newEmail, inputPhone, countryCode);
                System.out.println("\nCollaborator updated successfully.");
            }
        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println("Update failed: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Edit Customer Collaborator";
    }
}