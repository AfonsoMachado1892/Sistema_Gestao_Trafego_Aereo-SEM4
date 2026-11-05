package aisafe.app.backoffice.console.presentation.aircraft;

import aisafe.aircraft.application.AddAircraftController;
import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.aircraft.domain.CabinConfiguration;
import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.persistence.PersistenceContext;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.List;
import java.util.Locale;

@SuppressWarnings("squid:S106")
public class AddAircraftUI extends AbstractUI {

    private static final String CANCEL = "0";
    private static final int PAGE_SIZE = 8;
    private static final String SEPARATOR =
            "────────────────────────────────────────────────────────────────────────────────────────────────────";

    private final AddAircraftController controller =
            new AddAircraftController(
                    PersistenceContext.repositories().aircrafts(),
                    PersistenceContext.repositories().aircraftModels(),
                    PersistenceContext.repositories().airTransportCompanies(),
                    PersistenceContext.repositories().customerCollaborators()
            );

    @Override
    protected boolean doShow() {
        try {
            showIntro();

            final AircraftModel selectedModel = selectAircraftModel();

            if (selectedModel == null) {
                return false;
            }

            showSelectedModel(selectedModel);

            final String registration = readUniqueRegistration();

            if (registration == null) {
                return false;
            }

            final String countryIsoCode = readIsoCode();

            if (countryIsoCode == null) {
                return false;
            }

            final String countryName = readRequiredText("Country name");

            if (countryName == null) {
                return false;
            }

            final Integer numCrewElements = readPositiveInt("Number of crew elements");

            if (numCrewElements == null) {
                return false;
            }

            final CabinConfigurationInput cabinInput = readCabinConfiguration(selectedModel);

            if (cabinInput == null) {
                return false;
            }

            final CabinConfiguration cabinConfiguration = CabinConfiguration.of(
                    cabinInput.economySeats,
                    cabinInput.businessSeats,
                    cabinInput.firstSeats
            );

            final Country country = Country.of(IsoCode.of(countryIsoCode), countryName);

            showSummary(
                    selectedModel,
                    registration,
                    country,
                    numCrewElements,
                    cabinInput
            );

            if (!confirm("Confirm aircraft registration? (y/n):")) {
                System.out.println();
                System.out.println("Aircraft registration cancelled.");
                return false;
            }

            controller.addAircraft(
                    registration,
                    selectedModel,
                    cabinConfiguration,
                    country,
                    numCrewElements
            );

            showSuccess(
                    selectedModel,
                    registration,
                    country,
                    numCrewElements,
                    cabinInput
            );

        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println();
            System.out.println("Could not add aircraft to fleet.");
            System.out.println("Reason: " + e.getMessage());
        }

        return false;
    }

    private void showIntro() {
        System.out.println();
        System.out.println("Add Aircraft to Fleet");
        System.out.println("=====================");
        System.out.println("This operation registers a new aircraft in your company fleet.");
        System.out.println("The aircraft will be created with ACTIVE status.");
        System.out.println();
        System.out.println("You can type 0 at any input step to cancel.");
        System.out.println();
    }

    private AircraftModel selectAircraftModel() {
        final List<AircraftModel> models = controller.listAircraftModels();

        if (models.isEmpty()) {
            System.out.println();
            System.out.println("No aircraft models registered.");
            System.out.println("Please register aircraft models before adding aircraft to the fleet.");
            return null;
        }

        int page = 0;
        final int totalPages = (int) Math.ceil((double) models.size() / PAGE_SIZE);

        while (true) {
            printAircraftModelPage(models, page, totalPages);

            final String input = Console.readLine("Option:").trim().toLowerCase(Locale.ROOT);

            if (input.equals(CANCEL)) {
                cancel();
                return null;
            }

            if (input.equals("n") && page < totalPages - 1) {
                page++;
                continue;
            }

            if (input.equals("p") && page > 0) {
                page--;
                continue;
            }

            if (input.isBlank()) {
                System.out.println("  Please insert an option.");
                continue;
            }

            try {
                final int option = Integer.parseInt(input);
                final int index = option - 1;

                if (index >= 0 && index < models.size()) {
                    return models.get(index);
                }

                System.out.printf("  Invalid option. Choose a number between 1 and %d.%n",
                        models.size());

            } catch (final NumberFormatException e) {
                System.out.println("  Invalid option. Please insert a valid number, n, p or 0.");
            }
        }
    }

    private void printAircraftModelPage(final List<AircraftModel> models,
                                        final int page,
                                        final int totalPages) {
        final int from = page * PAGE_SIZE;
        final int to = Math.min(from + PAGE_SIZE, models.size());

        System.out.printf("%nAvailable Aircraft Models — Page %d of %d%n",
                page + 1,
                totalPages);
        System.out.println(SEPARATOR);
        System.out.printf(" %-3s %-16s %-16s %-12s %-12s %-14s %s%n",
                "#", "Model", "Maker", "Type", "Capacity", "Max Fuel", "Range");
        System.out.println(SEPARATOR);

        for (int i = from; i < to; i++) {
            final AircraftModel model = models.get(i);

            System.out.printf(" %-3d %-16s %-16s %-12s %-12s %-14s %s%n",
                    i + 1,
                    model.identity(),
                    model.maker().name(),
                    model.type(),
                    model.maxPassengerCapacity() + " pax",
                    formatKg(model.maxFuelCapacity()),
                    formatKm(model.maximumRange()));
        }

        System.out.println(SEPARATOR);
        System.out.println();
        System.out.println("Options:");
        System.out.println("  number - Select aircraft model");

        if (page > 0) {
            System.out.println("  p      - Previous page");
        }

        if (page < totalPages - 1) {
            System.out.println("  n      - Next page");
        }

        System.out.println("  0      - Cancel");
        System.out.println();
    }

    private void showSelectedModel(final AircraftModel selectedModel) {
        System.out.println();
        System.out.println("Selected Aircraft Model");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Model        : " + selectedModel.identity());
        System.out.println(" Maker        : " + selectedModel.maker().name());
        System.out.println(" Type         : " + selectedModel.type());
        System.out.println(" Capacity     : " + selectedModel.maxPassengerCapacity() + " pax");
        System.out.println(" Max fuel     : " + formatKg(selectedModel.maxFuelCapacity()));
        System.out.println(" Range        : " + formatKm(selectedModel.maximumRange()));
        System.out.println(" Engines      : " + selectedModel.engineNumber());
        System.out.println(" Cruise speed : " + String.format(Locale.US, "%.0f m/s", selectedModel.cruiseSpeed()));
        System.out.println(" Ceiling      : " + String.format(Locale.US, "%.0f m", selectedModel.serviceCeiling()));
        System.out.println("────────────────────────────────────────");
        System.out.println();
    }

    private String readUniqueRegistration() {
        while (true) {
            final String input = Console.readLine("Aircraft registration, e.g. CS-TAD (0 to cancel):")
                    .trim()
                    .toUpperCase(Locale.ROOT);

            if (input.equals(CANCEL)) {
                cancel();
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Registration cannot be empty.");
                continue;
            }

            if (!input.matches("[A-Z0-9-]{2,10}")) {
                System.out.println("  Invalid registration format. Example: CS-TAD");
                continue;
            }

            if (controller.registrationExists(input)) {
                System.out.printf("  Registration '%s' already exists. Please choose another.%n",
                        input);
                continue;
            }

            return input;
        }
    }

    private String readIsoCode() {
        while (true) {
            final String input = Console.readLine("Country ISO code, e.g. PT (0 to cancel):")
                    .trim()
                    .toUpperCase(Locale.ROOT);

            if (input.equals(CANCEL)) {
                cancel();
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Country ISO code cannot be empty.");
                continue;
            }

            if (input.matches("^[A-Z]{2}$")) {
                return input;
            }

            System.out.println("  Invalid ISO code. Use exactly 2 letters. Example: PT");
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

    private Integer readPositiveInt(final String prompt) {
        while (true) {
            final String input = Console.readLine(prompt + " (0 to cancel):").trim();

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
                System.out.println("  Please insert a valid number.");
            }
        }
    }

    private Integer readNonNegativeInt(final String prompt) {
        while (true) {
            final String input = Console.readLine(prompt + " (0 to cancel):").trim();

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

                if (value >= 0) {
                    return value;
                }

                System.out.println("  Value must be zero or greater.");

            } catch (final NumberFormatException e) {
                System.out.println("  Please insert a valid number.");
            }
        }
    }

    private CabinConfigurationInput readCabinConfiguration(final AircraftModel selectedModel) {
        final int maxCapacity = selectedModel.maxPassengerCapacity();

        System.out.println();
        System.out.println("Cabin Configuration");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Model capacity : " + maxCapacity + " pax");
        System.out.println(" At least one seat is required.");
        System.out.println(" Total seats cannot exceed the model capacity.");
        System.out.println("────────────────────────────────────────");

        while (true) {
            final Integer economySeats = readNonNegativeInt("Economy seats");

            if (economySeats == null) {
                return null;
            }

            final Integer businessSeats = readNonNegativeInt("Business seats");

            if (businessSeats == null) {
                return null;
            }

            final Integer firstSeats = readNonNegativeInt("First class seats");

            if (firstSeats == null) {
                return null;
            }

            final int totalSeats = economySeats + businessSeats + firstSeats;

            if (totalSeats <= 0) {
                System.out.println();
                System.out.println("  Cabin configuration must have at least one seat.");
                System.out.println("  Please insert the cabin configuration again.");
                continue;
            }

            if (totalSeats > maxCapacity) {
                System.out.println();
                System.out.printf("  Cabin configuration exceeds model capacity: %d/%d seats.%n",
                        totalSeats,
                        maxCapacity);
                System.out.println("  Please insert the cabin configuration again.");
                continue;
            }

            return new CabinConfigurationInput(
                    economySeats,
                    businessSeats,
                    firstSeats
            );
        }
    }

    private void showSummary(final AircraftModel selectedModel,
                             final String registration,
                             final Country country,
                             final int numCrewElements,
                             final CabinConfigurationInput cabinInput) {
        System.out.println();
        System.out.println("Aircraft Registration Summary");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Registration  : " + registration);
        System.out.println(" Model         : " + selectedModel.identity());
        System.out.println(" Maker         : " + selectedModel.maker().name());
        System.out.println(" Type          : " + selectedModel.type());
        System.out.println(" Company       : current company");
        System.out.println(" Country       : " + country);
        System.out.println(" Crew members  : " + numCrewElements);
        System.out.println(" Cabin config  : Economy " + cabinInput.economySeats
                + " | Business " + cabinInput.businessSeats
                + " | First " + cabinInput.firstSeats
                + " | Total " + cabinInput.totalSeats() + " / "
                + selectedModel.maxPassengerCapacity());
        System.out.println(" Max fuel      : " + formatKg(selectedModel.maxFuelCapacity()));
        System.out.println(" Range         : " + formatKm(selectedModel.maximumRange()));
        System.out.println(" Initial status: ACTIVE");
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

    private void showSuccess(final AircraftModel selectedModel,
                             final String registration,
                             final Country country,
                             final int numCrewElements,
                             final CabinConfigurationInput cabinInput) {
        System.out.println();
        System.out.println("Aircraft registered successfully.");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Registration : " + registration);
        System.out.println(" Model        : " + selectedModel.identity());
        System.out.println(" Type         : " + selectedModel.type());
        System.out.println(" Country      : " + country);
        System.out.println(" Crew members : " + numCrewElements);
        System.out.println(" Capacity     : " + cabinInput.totalSeats() + " pax");
        System.out.println(" Status       : ACTIVE");
        System.out.println("────────────────────────────────────────");
        System.out.println();
    }

    private void cancel() {
        System.out.println();
        System.out.println("Aircraft registration cancelled.");
    }

    private String formatKg(final double value) {
        return String.format(Locale.US, "%,.2f kg", value);
    }

    private String formatKm(final double value) {
        return String.format(Locale.US, "%,.0f km", value);
    }

    @Override
    public String headline() {
        return "Add Aircraft to Fleet";
    }

    private static final class CabinConfigurationInput {
        private final int economySeats;
        private final int businessSeats;
        private final int firstSeats;

        private CabinConfigurationInput(final int economySeats,
                                        final int businessSeats,
                                        final int firstSeats) {
            this.economySeats = economySeats;
            this.businessSeats = businessSeats;
            this.firstSeats = firstSeats;
        }

        private int totalSeats() {
            return economySeats + businessSeats + firstSeats;
        }
    }
}