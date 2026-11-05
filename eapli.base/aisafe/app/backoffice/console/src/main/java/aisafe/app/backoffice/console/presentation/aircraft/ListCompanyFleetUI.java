package aisafe.app.backoffice.console.presentation.aircraft;

import aisafe.aircraft.application.ListCompanyFleetController;
import aisafe.aircraft.domain.Aircraft;
import aisafe.persistence.PersistenceContext;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.List;
import java.util.Locale;

@SuppressWarnings("squid:S106")
public class ListCompanyFleetUI extends AbstractUI {

    private static final int PAGE_SIZE = 8;
    private static final String RETURN = "0";
    private static final String SEPARATOR =
            "────────────────────────────────────────────────────────────────────────────────────────────────────";

    private final ListCompanyFleetController controller = new ListCompanyFleetController(
            PersistenceContext.repositories().aircrafts(),
            PersistenceContext.repositories().airTransportCompanies(),
            PersistenceContext.repositories().customerCollaborators()
    );

    @Override
    protected boolean doShow() {
        final List<Aircraft> fleet = controller.listCompanyFleet();

        if (fleet.isEmpty()) {
            System.out.println();
            System.out.println("No aircraft found in the company fleet.");
            return false;
        }

        int page = 0;
        final int totalPages = (int) Math.ceil((double) fleet.size() / PAGE_SIZE);

        while (true) {
            printFleetPage(fleet, page, totalPages);

            final String input = Console.readLine("Option:").trim().toLowerCase(Locale.ROOT);

            if (input.equals(RETURN)) {
                return false;
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

                if (index >= 0 && index < fleet.size()) {
                    showAircraftDetails(fleet.get(index), option);
                    Console.readLine("Press ENTER to continue...");
                    continue;
                }

                System.out.printf("  Invalid option. Choose a number between 1 and %d.%n",
                        fleet.size());

            } catch (final NumberFormatException e) {
                System.out.println("  Invalid option. Please insert a valid number, n, p or 0.");
            }
        }
    }

    private void printFleetPage(final List<Aircraft> fleet,
                                final int page,
                                final int totalPages) {
        final int from = page * PAGE_SIZE;
        final int to = Math.min(from + PAGE_SIZE, fleet.size());

        System.out.println();
        System.out.printf("Company Fleet — Page %d of %d%n", page + 1, totalPages);
        System.out.println(SEPARATOR);
        System.out.printf(" %-3s %-14s %-16s %-10s %-16s %-6s %-12s %s%n",
                "#", "Registration", "Model", "Company", "Status", "Crew", "Capacity", "Country");
        System.out.println(SEPARATOR);

        for (int i = from; i < to; i++) {
            final Aircraft aircraft = fleet.get(i);

            System.out.printf(" %-3d %-14s %-16s %-10s %-16s %-6d %-12s %s%n",
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
        System.out.println("  number - View aircraft details");

        if (page > 0) {
            System.out.println("  p      - Previous page");
        }

        if (page < totalPages - 1) {
            System.out.println("  n      - Next page");
        }

        System.out.println("  0      - Return");
        System.out.println();
    }

    private void showAircraftDetails(final Aircraft aircraft,
                                     final int number) {
        System.out.println();
        System.out.println("Aircraft Details");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Number       : " + number);
        System.out.println(" Registration : " + aircraft.aircraftCode().code());
        System.out.println(" Model        : " + aircraft.aircraftModel().identity());
        System.out.println(" Type         : " + aircraft.aircraftModel().type());
        System.out.println(" Company      : " + aircraft.airCompany().identity());
        System.out.println(" Country      : " + aircraft.country());
        System.out.println(" Status       : " + aircraft.status());
        System.out.println(" Crew members : " + aircraft.numCrewElements());
        System.out.println(" Capacity     : " + aircraft.aircraftModel().maxPassengerCapacity() + " pax");
        System.out.println(" Max fuel     : " + formatKg(aircraft.aircraftModel().maxFuelCapacity()));
        System.out.println(" Range        : " + formatKm(aircraft.aircraftModel().maximumRange()));

        if (aircraft.cabinConfiguration() != null) {
            System.out.println(" Cabin config : " + aircraft.cabinConfiguration());
        }

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
        return "List Company Fleet";
    }
}