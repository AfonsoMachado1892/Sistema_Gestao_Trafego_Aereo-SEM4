package aisafe.app.backoffice.console.presentation.aircraft;

import aisafe.aircraft.application.DecommissionAircraftController;
import aisafe.aircraft.domain.Aircraft;
import aisafe.persistence.PersistenceContext;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("squid:S106")
public class DecommissionAircraftUI extends AbstractUI {

    private static final int PAGE_SIZE = 8;
    private static final String CANCEL = "0";
    private static final String SEPARATOR =
            "────────────────────────────────────────────────────────────────────────────────────────────────────";

    private final DecommissionAircraftController controller = new DecommissionAircraftController(
            PersistenceContext.repositories().aircrafts(),
            PersistenceContext.repositories().airTransportCompanies(),
            PersistenceContext.repositories().customerCollaborators(),
            PersistenceContext.repositories().flights()
    );

    @Override
    protected boolean doShow() {
        showIntro();

        final List<Aircraft> activeFleet = loadActiveFleet();

        if (activeFleet.isEmpty()) {
            System.out.println();
            System.out.println("No active aircraft available for decommission.");
            return false;
        }

        final Aircraft selected = chooseAircraft(activeFleet);

        if (selected == null) {
            return false;
        }

        showSummary(selected);

        if (!confirmDecommission()) {
            System.out.println();
            System.out.println("Aircraft decommission cancelled.");
            return false;
        }

        try {
            final Aircraft result = controller.decommissionAircraft(selected.aircraftCode().code());

            showSuccess(result);
            return false;

        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println();
            System.out.println("Could not decommission aircraft.");
            System.out.println("Reason: " + e.getMessage());
            return false;
        }
    }

    private void showIntro() {
        System.out.println();
        System.out.println("Decommission Aircraft");
        System.out.println("=====================");
        System.out.println("This operation decommissions an active aircraft from your company fleet.");
        System.out.println("Only ACTIVE aircraft are listed as available for decommission.");
        System.out.println();
        System.out.println("You can type 0 to cancel.");
        System.out.println();
    }

    private List<Aircraft> loadActiveFleet() {
        final List<Aircraft> activeFleet = new ArrayList<>();

        controller.getCompanyAircraft().forEach(aircraft -> {
            if (isActive(aircraft)) {
                activeFleet.add(aircraft);
            }
        });

        return activeFleet;
    }

    private boolean isActive(final Aircraft aircraft) {
        return aircraft.status() != null
                && "ACTIVE".equalsIgnoreCase(aircraft.status().toString());
    }

    private Aircraft chooseAircraft(final List<Aircraft> activeFleet) {
        int page = 0;
        final int totalPages = (int) Math.ceil((double) activeFleet.size() / PAGE_SIZE);

        while (true) {
            printAircraftPage(activeFleet, page, totalPages);

            final String input = Console.readLine("Option:").trim().toLowerCase(Locale.ROOT);

            if (input.equals(CANCEL)) {
                System.out.println();
                System.out.println("Aircraft decommission cancelled.");
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

                if (index >= 0 && index < activeFleet.size()) {
                    final Aircraft selected = activeFleet.get(index);
                    showSelectedAircraft(selected, option);
                    return selected;
                }

                System.out.printf("  Invalid option. Choose a number between 1 and %d.%n",
                        activeFleet.size());

            } catch (final NumberFormatException e) {
                System.out.println("  Invalid option. Please insert a valid number, n, p or 0.");
            }
        }
    }

    private void printAircraftPage(final List<Aircraft> activeFleet,
                                   final int page,
                                   final int totalPages) {
        final int from = page * PAGE_SIZE;
        final int to = Math.min(from + PAGE_SIZE, activeFleet.size());

        System.out.printf("%nActive Aircraft Available for Decommission — Page %d of %d%n",
                page + 1,
                totalPages);
        System.out.println(SEPARATOR);
        System.out.printf(" %-3s %-14s %-16s %-10s %-8s %-6s %-12s %s%n",
                "#", "Registration", "Model", "Company", "Status", "Crew", "Capacity", "Country");
        System.out.println(SEPARATOR);

        for (int i = from; i < to; i++) {
            final Aircraft aircraft = activeFleet.get(i);

            System.out.printf(" %-3d %-14s %-16s %-10s %-8s %-6d %-12s %s%n",
                    i + 1,
                    aircraft.aircraftCode().code(),
                    aircraft.aircraftModel().identity(),
                    aircraft.airCompany().identity(),
                    aircraft.status(),
                    aircraft.numCrewElements(),
                    cabinCapacity(aircraft),
                    aircraft.country());
        }

        System.out.println(SEPARATOR);
        System.out.println();
        System.out.println("Options:");
        System.out.println("  number - Select aircraft to decommission");

        if (page > 0) {
            System.out.println("  p      - Previous page");
        }

        if (page < totalPages - 1) {
            System.out.println("  n      - Next page");
        }

        System.out.println("  0      - Cancel");
        System.out.println();
    }

    private void showSelectedAircraft(final Aircraft aircraft,
                                      final int number) {
        System.out.println();
        System.out.println("Selected Aircraft");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Number       : " + number);
        System.out.println(" Registration : " + aircraft.aircraftCode().code());
        System.out.println(" Model        : " + aircraft.aircraftModel().identity());
        System.out.println(" Type         : " + aircraft.aircraftModel().type());
        System.out.println(" Company      : " + aircraft.airCompany().identity());
        System.out.println(" Country      : " + aircraft.country());
        System.out.println(" Status       : " + aircraft.status());
        System.out.println(" Crew members : " + aircraft.numCrewElements());
        System.out.println(" Capacity     : " + cabinCapacity(aircraft));
        System.out.println(" Max fuel     : " + formatKg(aircraft.aircraftModel().maxFuelCapacity()));
        System.out.println(" Range        : " + formatKm(aircraft.aircraftModel().maximumRange()));

        if (aircraft.cabinConfiguration() != null) {
            System.out.println(" Cabin config : " + aircraft.cabinConfiguration());
        }

        System.out.println("────────────────────────────────────────");
        System.out.println();
    }

    private void showSummary(final Aircraft selected) {
        System.out.println();
        System.out.println("Decommission Summary");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Registration : " + selected.aircraftCode().code());
        System.out.println(" Model        : " + selected.aircraftModel().identity());
        System.out.println(" Type         : " + selected.aircraftModel().type());
        System.out.println(" Company      : " + selected.airCompany().identity());
        System.out.println(" Country      : " + selected.country());
        System.out.println(" Current status : " + selected.status());
        System.out.println(" New status     : DECOMMISSIONED");
        System.out.println(" Crew members   : " + selected.numCrewElements());
        System.out.println(" Capacity       : " + cabinCapacity(selected));
        System.out.println("────────────────────────────────────────");
        System.out.println();

        System.out.println("Impact");
        System.out.println("────────────────────────────────────────");
        System.out.println(" The aircraft will no longer be available for new flight operations.");
        System.out.println(" Existing business rules may prevent decommission if active flights depend on it.");
        System.out.println("────────────────────────────────────────");
        System.out.println();
    }

    private boolean confirmDecommission() {
        while (true) {
            final String confirm = Console.readLine("Confirm aircraft decommission? (y/n):")
                    .trim()
                    .toLowerCase(Locale.ROOT);

            if (confirm.equals("y")) {
                return true;
            }

            if (confirm.equals("n")) {
                return false;
            }

            System.out.println("  Invalid option. Please answer 'y' or 'n'.");
        }
    }

    private void showSuccess(final Aircraft result) {
        System.out.println();
        System.out.println("Aircraft successfully decommissioned.");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Registration : " + result.aircraftCode().code());
        System.out.println(" Model        : " + result.aircraftModel().identity());
        System.out.println(" Status       : " + result.status());
        System.out.println("────────────────────────────────────────");
        System.out.println();
    }

    private String cabinCapacity(final Aircraft aircraft) {
        if (aircraft.cabinConfiguration() == null) {
            return "-";
        }

        return aircraft.cabinConfiguration().totalSeats() + " pax";
    }

    private String formatKg(final double value) {
        return String.format(Locale.US, "%,.2f kg", value);
    }

    private String formatKm(final double value) {
        return String.format(Locale.US, "%,.0f km", value);
    }

    @Override
    public String headline() {
        return "Decommission Aircraft";
    }
}