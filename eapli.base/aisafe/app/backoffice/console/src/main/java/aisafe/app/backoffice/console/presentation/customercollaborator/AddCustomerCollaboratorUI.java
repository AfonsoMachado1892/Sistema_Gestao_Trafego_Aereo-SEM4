package aisafe.app.backoffice.console.presentation.customercollaborator;

import aisafe.aircontrolarea.domain.AirControlArea;
import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.domain.CustomerType;
import aisafe.costumerCollaborator.service.AddCustomerCollaboratorController;
import aisafe.persistence.PersistenceContext;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("squid:S106")
public class AddCustomerCollaboratorUI extends AbstractUI {

    private final AddCustomerCollaboratorController controller =
            new AddCustomerCollaboratorController(
                    PersistenceContext.repositories().customerCollaborators(),
                    PersistenceContext.repositories().airTransportCompanies(),
                    PersistenceContext.repositories().airControlAreas(),
                    PersistenceContext.repositories().aiSafeUsers()
            );

    @Override
    protected boolean doShow() {
        try {
            // ── 1. Tipo de cliente ────────────────────────────────────────────
            final CustomerType customerType = selectCustomerType();
            if (customerType == null) return false;

            // ── 2. Código do cliente ──────────────────────────────────────────
            final String customerCode = selectCustomerCode(customerType);
            if (customerCode == null) return false;

            // ── 3. Username — verificação imediata ────────────────────────────
            final String username = readUniqueUsername();
            if (username == null) return false;

            // ── 4. Password ───────────────────────────────────────────────────
            final String password = readValidPassword();
            if (password == null) return false;

            // ── 5. Nome ───────────────────────────────────────────────────────
            final String firstName = readOrCancel(
                    "First name", "First name cannot be empty");
            if (firstName == null) return false;

            final String lastName = readOrCancel(
                    "Last name", "Last name cannot be empty");
            if (lastName == null) return false;

            // ── 6. Email — verificação imediata ───────────────────────────────
            final String email = readUniqueEmail();
            if (email == null) return false;

            // ── 7. Telefone ───────────────────────────────────────────────────
            final int countryCode = readCountryCode();
            if (countryCode < 0) return false;

            final String phoneNumber = readPhoneNumber();
            if (phoneNumber == null) return false;

            // ── 8. Posição ────────────────────────────────────────────────────
            final String position = selectPosition(customerType);
            if (position == null) return false;

            // ── 9. Resumo e confirmação ───────────────────────────────────────
            System.out.println("\n── Summary ──────────────────────────────────────");
            System.out.printf("  Customer type : %s%n", customerType);
            System.out.printf("  Customer code : %s%n", customerCode);
            System.out.printf("  Username      : %s%n", username);
            System.out.printf("  Name          : %s %s%n", firstName, lastName);
            System.out.printf("  Email         : %s%n", email);
            System.out.printf("  Phone         : +%d %s%n", countryCode, phoneNumber);
            System.out.printf("  Position      : %s%n", position);
            System.out.println("─────────────────────────────────────────────────");

            final String confirm = Console.readLine("\nConfirm registration? (y/n):");
            if (!confirm.equalsIgnoreCase("y")) {
                System.out.println("Registration cancelled.");
                return false;
            }

            // ── 10. Registar ──────────────────────────────────────────────────
            final CustomerCollaborator collaborator = controller.addCustomerCollaborator(
                    username, password, firstName, lastName, email,
                    countryCode, phoneNumber, customerType, customerCode, position
            );

            System.out.println("\nCustomer collaborator registered successfully.");
            System.out.println(collaborator);

        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (final Exception e) {
            System.out.println("Error: Operation could not be completed. Please check the inserted data.");
        }

        return false;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private CustomerType selectCustomerType() {
        System.out.println("\nCustomer type:");
        System.out.println("  1. Air Transport Company");
        System.out.println("  2. Air Control Area");
        System.out.println("  0. Cancel");

        while (true) {
            final int option = Console.readInteger("Select:");
            switch (option) {
                case 0:
                    System.out.println("Registration cancelled.");
                    return null;
                case 1:
                    return CustomerType.AIR_TRANSPORT_COMPANY;
                case 2:
                    return CustomerType.AIR_CONTROL_AREA;
                default:
                    System.out.println("  Please choose 1, 2 or 0.");
            }
        }
    }

    private String selectCustomerCode(final CustomerType customerType) {
        if (customerType == CustomerType.AIR_TRANSPORT_COMPANY) {
            return selectAirTransportCompany();
        }
        return selectAirControlArea();
    }

    private String selectAirTransportCompany() {
        final List<AirTransportCompany> companies = new ArrayList<>();
        controller.listAirTransportCompanies().forEach(companies::add);

        if (companies.isEmpty()) {
            System.out.println("No air transport companies registered.");
            return null;
        }

        System.out.println("\nAvailable Air Transport Companies:");
        System.out.printf("  %-4s %-10s %s%n", "#", "IATA", "Name");
        System.out.println("  " + "-".repeat(40));
        for (int i = 0; i < companies.size(); i++) {
            System.out.printf("  %-4d %-10s %s%n",
                    i + 1,
                    companies.get(i).identity(),
                    companies.get(i).name());
        }
        System.out.println("  0. Cancel");

        while (true) {
            final int option = Console.readInteger("\nSelect company:");
            if (option == 0) {
                System.out.println("Registration cancelled.");
                return null;
            }
            if (option >= 1 && option <= companies.size()) {
                return companies.get(option - 1).identity().toString();
            }
            System.out.printf("  Please choose between 1 and %d.%n", companies.size());
        }
    }

    private String selectAirControlArea() {
        final List<AirControlArea> areas = new ArrayList<>();
        controller.listAirControlAreas().forEach(areas::add);

        if (areas.isEmpty()) {
            System.out.println("No air control areas registered.");
            return null;
        }

        System.out.println("\nAvailable Air Control Areas:");
        System.out.printf("  %-4s %-10s %s%n", "#", "Code", "Name");
        System.out.println("  " + "-".repeat(40));
        for (int i = 0; i < areas.size(); i++) {
            System.out.printf("  %-4d %-10s %s%n",
                    i + 1,
                    areas.get(i).code(),
                    areas.get(i).name());
        }
        System.out.println("  0. Cancel");

        while (true) {
            final int option = Console.readInteger("\nSelect area:");
            if (option == 0) {
                System.out.println("Registration cancelled.");
                return null;
            }
            if (option >= 1 && option <= areas.size()) {
                return areas.get(option - 1).code().toString();
            }
            System.out.printf("  Please choose between 1 and %d.%n", areas.size());
        }
    }

    private String readUniqueUsername() {
        while (true) {
            final String input = Console.readNonEmptyLine(
                    "Username (0 to cancel):",
                    "Username cannot be empty"
            ).trim();

            if (input.equals("0")) {
                System.out.println("Registration cancelled.");
                return null;
            }

            if (controller.usernameAlreadyExists(input)) {
                System.out.printf(
                        "  Username '%s' already exists. Please choose another.%n", input);
            } else {
                return input;
            }
        }
    }

    private String readUniqueEmail() {
        while (true) {
            final String input = Console.readNonEmptyLine(
                    "Email (0 to cancel):",
                    "Email cannot be empty"
            ).trim();

            if (input.equals("0")) {
                System.out.println("Registration cancelled.");
                return null;
            }

            if (!input.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
                System.out.println("  Invalid email format.");
                continue;
            }

            if (controller.emailAlreadyExists(input)) {
                System.out.printf(
                        "  Email '%s' already exists. Please choose another.%n", input);
            } else {
                return input;
            }
        }
    }

    private String readValidPassword() {
        while (true) {
            final String input = Console.readNonEmptyLine(
                    "Password (0 to cancel):",
                    "Password cannot be empty"
            ).trim();

            if (input.equals("0")) {
                System.out.println("Registration cancelled.");
                return null;
            }

            if (input.length() < 6) {
                System.out.println("  Password must have at least 6 characters.");
                continue;
            }
            if (!input.matches(".*\\d.*")) {
                System.out.println("  Password must contain at least one digit.");
                continue;
            }
            if (!input.matches(".*[A-Z].*")) {
                System.out.println("  Password must contain at least one uppercase letter.");
                continue;
            }

            return input;
        }
    }

    private int readCountryCode() {
        while (true) {
            final String input = Console.readLine(
                    "Phone country code (0 to cancel):").trim();

            if (input.equals("0")) {
                System.out.println("Registration cancelled.");
                return -1;
            }

            try {
                final int code = Integer.parseInt(input);
                if (code >= 1 && code <= 999) return code;
                System.out.println("  Country code must be between 1 and 999.");
            } catch (final NumberFormatException e) {
                System.out.println("  Please insert a valid number.");
            }
        }
    }

    private String readPhoneNumber() {
        while (true) {
            final String input = Console.readNonEmptyLine(
                    "Phone number (0 to cancel):",
                    "Phone number cannot be empty"
            ).trim();

            if (input.equals("0")) {
                System.out.println("Registration cancelled.");
                return null;
            }

            if (input.matches("\\d{1,15}")) return input;
            System.out.println(
                    "  Phone number must contain only digits and have at most 15 digits.");
        }
    }

    private String selectPosition(final CustomerType customerType) {
        final String[] options = customerType == CustomerType.AIR_TRANSPORT_COMPANY
                ? new String[]{
                "Operations Manager",
                "Fleet Manager",
                "Route Manager",
                "Pilot Roster Manager",
                "Air Transport Company Collaborator"}
                : new String[]{
                "Flight Control Operator",
                "Flight Plan Validation Operator",
                "Air Traffic Simulation Operator",
                "Area Control Supervisor"};

        System.out.println("\nPosition:");
        for (int i = 0; i < options.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, options[i]);
        }
        System.out.println("  0. Cancel");

        while (true) {
            final int option = Console.readInteger("Select position:");
            if (option == 0) {
                System.out.println("Registration cancelled.");
                return null;
            }
            if (option >= 1 && option <= options.length) {
                return options[option - 1];
            }
            System.out.printf("  Please choose between 1 and %d.%n", options.length);
        }
    }

    private String readOrCancel(final String prompt, final String errorMessage) {
        final String value = Console.readNonEmptyLine(
                prompt + " (0 to cancel):", errorMessage).trim();

        if ("0".equals(value)) {
            System.out.println("Registration cancelled.");
            return null;
        }

        return value;
    }

    @Override
    public String headline() {
        return "Add Customer Collaborator";
    }
}