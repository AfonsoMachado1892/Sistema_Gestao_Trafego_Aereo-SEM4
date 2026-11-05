package aisafe.app.backoffice.console.presentation.flightroute;

import aisafe.flightroute.application.DeactivateFlightRouteController;
import aisafe.flightroute.application.FlightRouteDTO;
import aisafe.persistence.PersistenceContext;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("squid:S106")
public class ListFlightRoutesUI extends AbstractUI {

    private static final int PAGE_SIZE = 8;

    private final DeactivateFlightRouteController controller =
            new DeactivateFlightRouteController(
                    PersistenceContext.repositories().flightRoutes(),
                    PersistenceContext.repositories().airports(),
                    PersistenceContext.repositories().flightPlans(),
                    PersistenceContext.repositories().customerCollaborators()
            );

    @Override
    protected boolean doShow() {
        final List<FlightRouteDTO> routes = new ArrayList<>();

        try {
            controller.listCompanyRoutes().forEach(routes::add);
        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println();
            System.out.println("Could not load flight routes.");
            System.out.println("Reason: " + e.getMessage());
            return false;
        }

        if (routes.isEmpty()) {
            System.out.println();
            System.out.println("No flight routes found for your company.");
            return false;
        }

        System.out.printf("%nTotal: %d flight route(s)%n", routes.size());

        int page = 0;
        final int totalPages = (int) Math.ceil((double) routes.size() / PAGE_SIZE);

        while (true) {
            printPage(routes, page, totalPages);

            System.out.println();
            System.out.println("Options:");
            System.out.println("number - View route details");
            if (page > 0) {
                System.out.println("p      - Previous page");
            }
            if (page < totalPages - 1) {
                System.out.println("n      - Next page");
            }
            System.out.println("0      - Return");

            final String input = Console.readLine("Option:").trim().toLowerCase();

            if (input.equals("0")) {
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

            try {
                final int selectedNumber = Integer.parseInt(input);
                final int index = selectedNumber - 1;

                if (index >= 0 && index < routes.size()) {
                    showDetails(routes.get(index), selectedNumber);
                } else {
                    System.out.printf("Invalid option. Choose a number between 1 and %d.%n",
                            routes.size());
                }

            } catch (final NumberFormatException e) {
                System.out.println("Invalid option. Please insert a valid number.");
            }
        }
    }

    private void printPage(final List<FlightRouteDTO> routes,
                           final int page,
                           final int totalPages) {
        final int from = page * PAGE_SIZE;
        final int to = Math.min(from + PAGE_SIZE, routes.size());

        System.out.printf("%nPage %d of %d%n", page + 1, totalPages);
        System.out.println("Flight Routes");
        System.out.println("-------------");

        System.out.printf("%-4s %-8s %-6s %-6s %-8s %-10s %-18s%n",
                "#", "Route", "From", "To", "Type", "Status", "Deactivation Date");
        System.out.println("-".repeat(75));

        for (int i = from; i < to; i++) {
            final FlightRouteDTO route = routes.get(i);

            System.out.printf("%-4d %-8s %-6s %-6s %-8s %-10s %-18s%n",
                    i + 1,
                    route.routeCode,
                    route.departureAirportIata,
                    route.arrivalAirportIata,
                    route.flightType,
                    route.routeStatus,
                    route.deactivationDate);
        }
    }

    private void showDetails(final FlightRouteDTO route, final int selectedNumber) {
        System.out.println();
        System.out.println("Flight Route Details");
        System.out.println("====================");
        System.out.println("Number            : " + selectedNumber);
        System.out.println("Code              : " + route.routeCode);
        System.out.printf("Route             : %s -> %s%n",
                route.departureAirportIata,
                route.arrivalAirportIata);
        System.out.println("Company           : " + route.companyIata);
        System.out.println("Type              : " + route.flightType);
        System.out.println("Status            : " + route.routeStatus);
        System.out.println("Deactivation date : " + route.deactivationDate);
        System.out.println("Schedule          :");
        printSchedule(route.schedule);
        System.out.println();

        Console.readLine("Press ENTER to continue...");
    }

    private void printSchedule(final String schedule) {
        for (final String line : formatSchedule(schedule)) {
            final String[] parts = line.split("\\s+at\\s+", 2);

            if (parts.length == 2) {
                System.out.printf("- %-10s %s%n", capitalize(parts[0]), parts[1]);
            } else {
                System.out.println("- " + line);
            }
        }
    }

    private List<String> formatSchedule(final String schedule) {
        final List<String> lines = new ArrayList<>();

        if (schedule == null || schedule.isBlank()) {
            lines.add("No schedule information available");
            return lines;
        }

        String cleaned = schedule.trim();

        final int openBracket = cleaned.indexOf('[');
        final int closeBracket = cleaned.lastIndexOf(']');

        if (openBracket >= 0 && closeBracket > openBracket) {
            cleaned = cleaned.substring(openBracket + 1, closeBracket).trim();
        }

        if (cleaned.isBlank()) {
            lines.add("No schedule information available");
            return lines;
        }

        final String[] entries = cleaned.split(",");

        for (final String entry : entries) {
            final String formatted = entry.trim();

            if (!formatted.isBlank()) {
                lines.add(formatted);
            }
        }

        if (lines.isEmpty()) {
            lines.add(schedule);
        }

        return lines;
    }

    private String capitalize(final String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        final String lower = value.trim().toLowerCase();

        return lower.substring(0, 1).toUpperCase() + lower.substring(1);
    }

    @Override
    public String headline() {
        return "List Flight Routes";
    }
}