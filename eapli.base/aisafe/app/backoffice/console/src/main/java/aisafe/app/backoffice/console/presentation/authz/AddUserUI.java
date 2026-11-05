package aisafe.app.backoffice.console.presentation.authz;

import aisafe.persistence.PersistenceContext;
import aisafe.usermanagement.application.AddUserController;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.domain.repositories.ConcurrencyException;
import eapli.framework.domain.repositories.IntegrityViolationException;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@SuppressWarnings("squid:S106")
public class AddUserUI extends AbstractUI {

    private static final String CANCEL = "0";

    private final AddUserController controller =
            new AddUserController(PersistenceContext.repositories().aiSafeUsers());

    @Override
    protected boolean doShow() {
        showIntro();

        final String username = readUsername();

        if (username == null) {
            return false;
        }

        final String password = readPassword();

        if (password == null) {
            return false;
        }

        final String firstName = readName("First name");

        if (firstName == null) {
            return false;
        }

        final String lastName = readName("Last name");

        if (lastName == null) {
            return false;
        }

        final String email = readEmail();

        if (email == null) {
            return false;
        }

        final Integer countryCode = readCountryCode();

        if (countryCode == null) {
            return false;
        }

        final String phoneNumber = readPhoneNumber();

        if (phoneNumber == null) {
            return false;
        }

        final Set<Role> roles = readRoles();

        if (roles == null) {
            return false;
        }

        showSummary(
                username,
                firstName,
                lastName,
                email,
                countryCode,
                phoneNumber,
                roles
        );

        if (!confirm("Confirm backoffice user registration? (y/n):")) {
            System.out.println();
            System.out.println("Backoffice user registration cancelled.");
            return false;
        }

        try {
            final SystemUser result = controller.addUser(
                    username,
                    password,
                    firstName,
                    lastName,
                    email,
                    countryCode,
                    phoneNumber,
                    roles
            );

            showSuccess(result, roles);

        } catch (final IntegrityViolationException | ConcurrencyException e) {
            System.out.println();
            System.out.println("Could not register backoffice user.");
            System.out.println("Reason: username or e-mail is already in use.");

        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println();
            System.out.println("Could not register backoffice user.");

            if (e.getMessage() == null || e.getMessage().isBlank()) {
                System.out.println("Reason: invalid user data rejected by the authentication service.");
                System.out.println("Check username, password, e-mail and phone number formats.");
            } else {
                System.out.println("Reason: " + e.getMessage());
            }

        } catch (final Exception e) {
            System.out.println();
            System.out.println("Could not register backoffice user.");

            if (e.getMessage() == null || e.getMessage().isBlank()) {
                System.out.println("Reason: unexpected error while registering the user.");
            } else {
                System.out.println("Reason: " + e.getMessage());
            }
        }

        return false;
    }

    private void showIntro() {
        System.out.println();
        System.out.println("Register Backoffice User");
        System.out.println("========================");
        System.out.println("This operation registers a generic backoffice/system user.");
        System.out.println();
        System.out.println("Pilots and Air Transport Company Collaborators must be registered");
        System.out.println("through their own specific use cases.");
        System.out.println();
        System.out.println("You can type 0 at any input step to cancel.");
        System.out.println();
    }

    private String readUsername() {
        System.out.println("Username rules");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Use 3 to 30 characters.");
        System.out.println(" Allowed: letters, numbers, '.', '_' and '-'.");
        System.out.println(" Do not use spaces or accents.");
        System.out.println(" Example: joseCardoso");
        System.out.println("────────────────────────────────────────");

        while (true) {
            final String input = Console.readLine("Username (0 to cancel):").trim();

            if (input.equals(CANCEL)) {
                cancel();
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Username cannot be empty.");
                continue;
            }

            if (!input.matches("[A-Za-z0-9._-]{3,30}")) {
                System.out.println("  Invalid username.");
                System.out.println("  Use 3 to 30 characters: letters, numbers, '.', '_' or '-'.");
                System.out.println("  Do not use accents or spaces. Example: joseCardoso");
                continue;
            }

            return input;
        }
    }

    private String readPassword() {
        System.out.println();
        System.out.println("Password rules");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Must have at least 6 characters.");
        System.out.println(" Must contain at least one uppercase letter.");
        System.out.println(" Must contain at least one lowercase letter.");
        System.out.println(" Must contain at least one digit.");
        System.out.println(" Example: Password1");
        System.out.println("────────────────────────────────────────");

        while (true) {
            final String input = Console.readLine("Password (0 to cancel):").trim();

            if (input.equals(CANCEL)) {
                cancel();
                return null;
            }

            if (input.length() < 6) {
                System.out.println("  Password must have at least 6 characters.");
                continue;
            }

            if (!input.matches(".*[A-Z].*")) {
                System.out.println("  Password must contain at least one uppercase letter.");
                continue;
            }

            if (!input.matches(".*[a-z].*")) {
                System.out.println("  Password must contain at least one lowercase letter.");
                continue;
            }

            if (!input.matches(".*\\d.*")) {
                System.out.println("  Password must contain at least one digit.");
                continue;
            }

            return input;
        }
    }

    private String readName(final String label) {
        System.out.println();
        System.out.println(label + " rules");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Use 2 to 60 characters.");
        System.out.println(" Allowed: letters, spaces, apostrophes and hyphens.");
        System.out.println(" Accents are allowed here.");
        System.out.println(" Example: José");
        System.out.println("────────────────────────────────────────");

        while (true) {
            final String input = Console.readLine(label + " (0 to cancel):").trim();

            if (input.equals(CANCEL)) {
                cancel();
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  " + label + " cannot be empty.");
                continue;
            }

            if (!input.matches("[\\p{L} .'-]{2,60}")) {
                System.out.println("  Invalid " + label.toLowerCase(Locale.ROOT) + ".");
                System.out.println("  Use letters, spaces, apostrophes or hyphens.");
                continue;
            }

            return input;
        }
    }

    private String readEmail() {
        System.out.println();
        System.out.println("E-mail rules");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Must follow a standard e-mail format.");
        System.out.println(" Example: user@example.com");
        System.out.println("────────────────────────────────────────");

        while (true) {
            final String input = Console.readLine("E-mail (0 to cancel):").trim();

            if (input.equals(CANCEL)) {
                cancel();
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  E-mail cannot be empty.");
                continue;
            }

            if (!input.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                System.out.println("  Invalid e-mail format. Example: user@example.com");
                continue;
            }

            return input;
        }
    }

    private Integer readCountryCode() {
        System.out.println();
        System.out.println("Phone country code rules");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Use 1 to 3 digits.");
        System.out.println(" You may include '+' or omit it.");
        System.out.println(" Examples: +351, 351, 44, 1");
        System.out.println("────────────────────────────────────────");

        while (true) {
            final String input = Console.readLine("Phone country code (0 to cancel):")
                    .trim();

            if (input.equals(CANCEL)) {
                cancel();
                return null;
            }

            final String normalized = input.startsWith("+")
                    ? input.substring(1)
                    : input;

            if (!normalized.matches("\\d{1,3}")) {
                System.out.println("  Invalid country code. Use 1 to 3 digits. Example: +351");
                continue;
            }

            final int value = Integer.parseInt(normalized);

            if (value < 1 || value > 999) {
                System.out.println("  Country code must be between 1 and 999.");
                continue;
            }

            return value;
        }
    }

    private String readPhoneNumber() {
        System.out.println();
        System.out.println("Phone number rules");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Use 6 to 15 digits.");
        System.out.println(" Do not use spaces, '+' or letters.");
        System.out.println(" Example: 910291392");
        System.out.println("────────────────────────────────────────");

        while (true) {
            final String input = Console.readLine("Phone number (0 to cancel):")
                    .trim()
                    .replace(" ", "");

            if (input.equals(CANCEL)) {
                cancel();
                return null;
            }

            if (!input.matches("\\d{6,15}")) {
                System.out.println("  Invalid phone number. Use 6 to 15 digits.");
                continue;
            }

            return input;
        }
    }

    private Set<Role> readRoles() {
        final Set<Role> selectedRoles = new HashSet<>();
        final Role[] availableRoles = filteredRoles();

        System.out.println();
        System.out.println("Role selection rules");
        System.out.println("────────────────────────────────────────");
        System.out.println(" At least one role is required.");
        System.out.println(" This use case only registers generic backoffice/system users.");
        System.out.println(" PILOT and AIR_TRANSPORT_COMPANY_COLLABORATOR are not available here.");
        System.out.println(" Use their own registration use cases instead.");
        System.out.println("────────────────────────────────────────");

        while (true) {
            showRoleSelection(selectedRoles, availableRoles);

            final String input = Console.readLine("Choose option:").trim();

            if (input.equals(CANCEL)) {
                if (selectedRoles.isEmpty()) {
                    cancel();
                    return null;
                }

                return selectedRoles;
            }

            if (input.isBlank()) {
                System.out.println("  Please insert an option.");
                continue;
            }

            try {
                final int option = Integer.parseInt(input);

                if (option < 1 || option > availableRoles.length) {
                    System.out.printf("  Invalid option. Choose between 1 and %d.%n",
                            availableRoles.length);
                    continue;
                }

                final Role selected = availableRoles[option - 1];

                if (selectedRoles.contains(selected)) {
                    System.out.println("  Role already selected.");
                    continue;
                }

                selectedRoles.add(selected);
                System.out.println("  Role selected: " + selected);

            } catch (final NumberFormatException e) {
                System.out.println("  Invalid option. Please insert a valid number.");
            }
        }
    }

    private Role[] filteredRoles() {
        return Arrays.stream(controller.getRoleTypes())
                .filter(role -> !role.equals(AiSafeRoles.PILOT))
                .filter(role -> !role.equals(AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR))
                .toArray(Role[]::new);
    }

    private void showRoleSelection(final Set<Role> selectedRoles,
                                   final Role[] availableRoles) {
        System.out.println();
        System.out.println("Role Selection");
        System.out.println("────────────────────────────────────────");

        if (selectedRoles.isEmpty()) {
            System.out.println("Selected roles: none");
        } else {
            System.out.println("Selected roles:");
            for (final Role role : selectedRoles) {
                System.out.println("  - " + role);
            }
        }

        System.out.println("────────────────────────────────────────");

        for (int i = 0; i < availableRoles.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, availableRoles[i]);
        }

        if (selectedRoles.isEmpty()) {
            System.out.println("  0. Cancel registration");
        } else {
            System.out.println("  0. Finish role selection");
        }

        System.out.println();
    }

    private void showSummary(final String username,
                             final String firstName,
                             final String lastName,
                             final String email,
                             final int countryCode,
                             final String phoneNumber,
                             final Set<Role> roles) {
        System.out.println();
        System.out.println("Backoffice User Registration Summary");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Username : " + username);
        System.out.println(" Name     : " + firstName + " " + lastName);
        System.out.println(" E-mail   : " + email);
        System.out.println(" Phone    : +" + countryCode + " " + phoneNumber);
        System.out.println(" Roles    : " + roles.size());

        for (final Role role : roles) {
            System.out.println("          - " + role);
        }

        System.out.println("────────────────────────────────────────");
        System.out.println();
    }

    private boolean confirm(final String prompt) {
        while (true) {
            final String input = Console.readLine(prompt).trim().toLowerCase(Locale.ROOT);

            if (input.equals("y")) {
                return true;
            }

            if (input.equals("n")) {
                return false;
            }

            System.out.println("  Invalid option. Please answer 'y' or 'n'.");
        }
    }

    private void showSuccess(final SystemUser result,
                             final Set<Role> roles) {
        System.out.println();
        System.out.println("Backoffice user registered successfully.");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Username : " + result.username());
        System.out.println(" Name     : " + result.name());
        System.out.println(" E-mail   : " + result.email());
        System.out.println(" Roles    : " + roles.size());

        for (final Role role : roles) {
            System.out.println("          - " + role);
        }

        System.out.println("────────────────────────────────────────");
        System.out.println();
    }

    private void cancel() {
        System.out.println();
        System.out.println("Backoffice user registration cancelled.");
    }

    @Override
    public String headline() {
        return "Register Backoffice User";
    }
}