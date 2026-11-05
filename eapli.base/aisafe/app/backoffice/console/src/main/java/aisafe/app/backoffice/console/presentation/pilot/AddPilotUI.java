package aisafe.app.backoffice.console.presentation.pilot;

import aisafe.pilot.application.AddPilotController;
import aisafe.pilot.application.AircraftModelDTO;
import aisafe.pilot.application.PilotDTO;
import aisafe.persistence.PersistenceContext;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("squid:S106")
public class AddPilotUI extends AbstractUI {

    private static final String CANCEL = "0";

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AddPilotController controller = new AddPilotController(
            PersistenceContext.repositories().pilots(),
            PersistenceContext.repositories().aircraftModels(),
            PersistenceContext.repositories().customerCollaborators(),
            PersistenceContext.repositories().aiSafeUsers()
    );

    @Override
    protected boolean doShow() {
        try {
            showIntro();

            final String companyIata = readCompanyIata();

            if (companyIata == null) {
                return false;
            }

            final String username = readRequiredText("Username");

            if (username == null) {
                return false;
            }

            final String password = readRequiredText("Password");

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

            final Integer countryCode = readPhoneCountryCode();

            if (countryCode == null) {
                return false;
            }

            final String phoneNumber = readPhoneNumber();

            if (phoneNumber == null) {
                return false;
            }

            final List<AircraftModelDTO> models = new ArrayList<>();
            controller.listAircraftModels().forEach(models::add);

            if (models.isEmpty()) {
                System.out.println();
                System.out.println("No aircraft models registered.");
                System.out.println("Please register aircraft models before adding pilots.");
                return false;
            }

            final CertificationInput certifications = readCertifications(models);

            if (certifications == null) {
                return false;
            }

            showSummary(
                    companyIata,
                    username,
                    firstName,
                    lastName,
                    email,
                    countryCode,
                    phoneNumber,
                    certifications
            );

            if (!confirm("Confirm pilot registration? (y/n):")) {
                System.out.println();
                System.out.println("Pilot registration cancelled.");
                return false;
            }

            final PilotDTO result = controller.addPilot(
                    username,
                    password,
                    firstName,
                    lastName,
                    email,
                    countryCode,
                    phoneNumber,
                    companyIata,
                    certifications.modelIDs,
                    certifications.hoursList,
                    certifications.issuedDates,
                    certifications.validDates
            );

            showSuccess(result);

        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println();
            System.out.println("Could not add pilot.");
            System.out.println("Reason: " + e.getMessage());

        } catch (final Exception e) {
            System.out.println();
            System.out.println("Could not add pilot.");
            System.out.println("Unexpected error: " + e.getClass().getName() + " - " + e.getMessage());
        }

        return false;
    }

    private void showIntro() {
        System.out.println();
        System.out.println("Add Pilot");
        System.out.println("=========");
        System.out.println("This operation registers a new pilot for an air transport company.");
        System.out.println("At least one aircraft model certification is required.");
        System.out.println();
        System.out.println("You can type 0 at any input step to cancel.");
        System.out.println();
    }

    private String readCompanyIata() {
        if (!controller.isAdmin()) {
            final String companyIata = controller.currentCompanyIataCode();
            System.out.println("Adding pilot to company: " + companyIata);
            System.out.println();
            return companyIata;
        }

        while (true) {
            final String input = Console.readLine("Company IATA code, e.g. TP (0 to cancel):")
                    .trim()
                    .toUpperCase(Locale.ROOT);

            if (input.equals(CANCEL)) {
                cancel();
                return null;
            }

            if (input.matches("[A-Z0-9]{2,3}")) {
                return input;
            }

            System.out.println("  Invalid company IATA code. Example: TP");
        }
    }

    private String readRequiredText(final String label) {
        while (true) {
            final String input = Console.readLine(label + " (0 to cancel):").trim();

            if (input.equals(CANCEL)) {
                cancel();
                return null;
            }

            if (!input.isBlank()) {
                return input;
            }

            System.out.println("  " + label + " cannot be empty.");
        }
    }

    private String readName(final String label) {
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
                System.out.println("  Invalid name. Use letters, spaces, apostrophes or hyphens.");
                continue;
            }

            return input;
        }
    }

    private String readEmail() {
        while (true) {
            final String input = Console.readLine("Email (0 to cancel):").trim();

            if (input.equals(CANCEL)) {
                cancel();
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Email cannot be empty.");
                continue;
            }

            if (!input.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                System.out.println("  Invalid email format. Example: pilot@example.com");
                continue;
            }

            return input;
        }
    }

    private Integer readPhoneCountryCode() {
        while (true) {
            final String input = Console.readLine("Phone country code, e.g. +351 (0 to cancel):")
                    .trim();

            if (input.equals(CANCEL)) {
                cancel();
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Phone country code cannot be empty.");
                continue;
            }

            final String normalized = input.startsWith("+")
                    ? input.substring(1)
                    : input;

            if (!normalized.matches("\\d{1,4}")) {
                System.out.println("  Invalid phone country code. Example: +351");
                continue;
            }

            return Integer.parseInt(normalized);
        }
    }

    private String readPhoneNumber() {
        while (true) {
            final String input = Console.readLine("Phone number (0 to cancel):")
                    .trim()
                    .replace(" ", "");

            if (input.equals(CANCEL)) {
                cancel();
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Phone number cannot be empty.");
                continue;
            }

            if (!input.matches("\\d{6,15}")) {
                System.out.println("  Invalid phone number. Use 6 to 15 digits.");
                continue;
            }

            return input;
        }
    }

    private CertificationInput readCertifications(final List<AircraftModelDTO> models) {
        final CertificationInput input = new CertificationInput();

        while (true) {
            showAircraftModels(models);
            showAddedCertifications(input);

            if (input.modelIDs.isEmpty()) {
                System.out.println("  0. Cancel pilot creation");
            } else {
                System.out.println("  0. Finish certifications");
            }

            System.out.println();

            final String raw = Console.readLine("Select aircraft model:").trim();

            if (raw.equals(CANCEL)) {
                if (input.modelIDs.isEmpty()) {
                    cancel();
                    return null;
                }

                return input;
            }

            final int option;

            try {
                option = Integer.parseInt(raw);
            } catch (final NumberFormatException e) {
                System.out.println("  Invalid option. Please insert a valid number.");
                continue;
            }

            if (option < 1 || option > models.size()) {
                System.out.printf("  Invalid option. Choose between 1 and %d.%n", models.size());
                continue;
            }

            final AircraftModelDTO selected = models.get(option - 1);

            if (input.modelIDs.contains(selected.modelID)) {
                System.out.println("  This aircraft model is already certified for this pilot.");
                continue;
            }

            final Integer hours = readPositiveInteger(
                    "Flight hours on " + selected.modelID + " (0 to cancel):"
            );

            if (hours == null) {
                return null;
            }

            final LocalDate issuedAt = readDate("Issued date (yyyy-MM-dd) (0 to cancel):");

            if (issuedAt == null) {
                return null;
            }

            final LocalDate validUntil = readValidUntilDate(issuedAt);

            if (validUntil == null) {
                return null;
            }

            input.modelIDs.add(selected.modelID);
            input.modelLabels.add(modelLabel(selected));
            input.hoursList.add(hours);
            input.issuedDates.add(issuedAt);
            input.validDates.add(validUntil);

            System.out.println();
            System.out.printf("  Certification added: %s | %dh | %s to %s%n",
                    selected.modelID,
                    hours,
                    issuedAt,
                    validUntil);
            System.out.println();
        }
    }

    private void showAircraftModels(final List<AircraftModelDTO> models) {
        System.out.println();
        System.out.println("Available Aircraft Models");
        System.out.println("────────────────────────────────────────────────────────────────────");
        System.out.printf(" %-3s %-16s %-16s %-12s%n",
                "#", "Model", "Maker", "Type");
        System.out.println("────────────────────────────────────────────────────────────────────");

        for (int i = 0; i < models.size(); i++) {
            final AircraftModelDTO model = models.get(i);

            System.out.printf(" %-3d %-16s %-16s %-12s%n",
                    i + 1,
                    model.modelID,
                    model.makerName,
                    model.type);
        }

        System.out.println("────────────────────────────────────────────────────────────────────");
    }

    private void showAddedCertifications(final CertificationInput input) {
        System.out.println();
        System.out.println("Pilot Certifications");
        System.out.println("────────────────────────────────────────────────────────────────────");

        if (input.modelIDs.isEmpty()) {
            System.out.println(" No certifications added yet.");
            System.out.println(" At least one certification is required.");
        } else {
            System.out.printf(" %-3s %-16s %-8s %-12s %-12s%n",
                    "#", "Model", "Hours", "Issued", "Valid until");
            System.out.println("────────────────────────────────────────────────────────────────────");

            for (int i = 0; i < input.modelIDs.size(); i++) {
                System.out.printf(" %-3d %-16s %-8d %-12s %-12s%n",
                        i + 1,
                        input.modelIDs.get(i),
                        input.hoursList.get(i),
                        input.issuedDates.get(i),
                        input.validDates.get(i));
            }
        }

        System.out.println("────────────────────────────────────────────────────────────────────");
    }

    private Integer readPositiveInteger(final String prompt) {
        while (true) {
            final String input = Console.readLine(prompt).trim();

            if (input.equals(CANCEL)) {
                cancel();
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Value cannot be empty.");
                continue;
            }

            try {
                final int value = Integer.parseInt(input);

                if (value > 0) {
                    return value;
                }

                System.out.println("  Value must be greater than zero.");

            } catch (final NumberFormatException e) {
                System.out.println("  Invalid number.");
            }
        }
    }

    private LocalDate readDate(final String prompt) {
        while (true) {
            final String input = Console.readLine(prompt).trim();

            if (input.equals(CANCEL)) {
                cancel();
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Date cannot be empty.");
                continue;
            }

            try {
                return LocalDate.parse(input, DATE_FMT);
            } catch (final DateTimeParseException e) {
                System.out.println("  Invalid date format. Use yyyy-MM-dd. Example: 2023-01-15");
            }
        }
    }

    private LocalDate readValidUntilDate(final LocalDate issuedAt) {
        while (true) {
            final LocalDate validUntil = readDate("Valid until (yyyy-MM-dd) (0 to cancel):");

            if (validUntil == null) {
                return null;
            }

            if (validUntil.isAfter(issuedAt)) {
                return validUntil;
            }

            System.out.println("  Valid until date must be after issued date.");
        }
    }

    private void showSummary(final String companyIata,
                             final String username,
                             final String firstName,
                             final String lastName,
                             final String email,
                             final int countryCode,
                             final String phoneNumber,
                             final CertificationInput certifications) {
        System.out.println();
        System.out.println("Pilot Registration Summary");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Company        : " + companyIata);
        System.out.println(" Username       : " + username);
        System.out.println(" Name           : " + firstName + " " + lastName);
        System.out.println(" Email          : " + email);
        System.out.println(" Phone          : +" + countryCode + " " + phoneNumber);
        System.out.println(" Certifications : " + certifications.modelIDs.size());
        System.out.println("────────────────────────────────────────");

        for (int i = 0; i < certifications.modelIDs.size(); i++) {
            System.out.printf("  %d. %s | %dh | %s to %s%n",
                    i + 1,
                    certifications.modelIDs.get(i),
                    certifications.hoursList.get(i),
                    certifications.issuedDates.get(i),
                    certifications.validDates.get(i));
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

    private void showSuccess(final PilotDTO result) {
        System.out.println();
        System.out.println("Pilot registered successfully.");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Pilot ID       : " + result.pilotID);
        System.out.println(" Name           : " + result.name);
        System.out.println(" Company        : " + result.companyIata);
        System.out.println(" Status         : " + result.status);
        System.out.println(" Certifications : " + result.certificationCount);
        System.out.println("────────────────────────────────────────");
    }

    private String modelLabel(final AircraftModelDTO model) {
        return model.modelID + " (" + model.makerName + ", " + model.type + ")";
    }

    private void cancel() {
        System.out.println();
        System.out.println("Pilot registration cancelled.");
    }

    @Override
    public String headline() {
        return "Add Pilot";
    }

    private static final class CertificationInput {
        private final List<String> modelIDs = new ArrayList<>();
        private final List<String> modelLabels = new ArrayList<>();
        private final List<Integer> hoursList = new ArrayList<>();
        private final List<LocalDate> issuedDates = new ArrayList<>();
        private final List<LocalDate> validDates = new ArrayList<>();
    }
}