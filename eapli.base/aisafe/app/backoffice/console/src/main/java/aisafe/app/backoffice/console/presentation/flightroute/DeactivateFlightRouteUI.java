package aisafe.app.backoffice.console.presentation.flightroute;

import aisafe.flightroute.application.DeactivateFlightRouteController;
import aisafe.flightroute.application.FlightRouteDTO;
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
public class DeactivateFlightRouteUI extends AbstractUI {

    private static final String CANCEL = "0";
    private static final int PAGE_SIZE = 8;

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final DeactivateFlightRouteController controller =
            new DeactivateFlightRouteController(
                    PersistenceContext.repositories().flightRoutes(),
                    PersistenceContext.repositories().airports(),
                    PersistenceContext.repositories().flightPlans(),
                    PersistenceContext.repositories().customerCollaborators()
            );

    @Override
    protected boolean doShow() {
        showIntro();

        while (true) {
            try {
                final List<FlightRouteDTO> routes = new ArrayList<>();
                controller.listActiveRoutes().forEach(routes::add);

                if (routes.isEmpty()) {
                    System.out.println();
                    System.out.println("No active flight routes found for your company.");
                    return false;
                }

                final FlightRouteDTO selectedRoute = chooseRoute(routes);

                if (selectedRoute == null) {
                    return false;
                }

                final LocalDate deactivationDate = readDeactivationDate();

                if (deactivationDate == null) {
                    return false;
                }

                showSummary(selectedRoute, deactivationDate);

                if (!confirmDeactivation()) {
                    System.out.println();
                    System.out.println("Flight route deactivation cancelled.");
                    return false;
                }

                final FlightRouteDTO result =
                        controller.deactivateRoute(selectedRoute.routeCode, deactivationDate);

                showSuccess(result, deactivationDate);
                return false;

            } catch (final IllegalArgumentException | IllegalStateException e) {
                System.out.println();
                System.out.println("Could not deactivate flight route.");
                System.out.println("Reason: " + e.getMessage());

                if (!retryAfterError()) {
                    return false;
                }
            }
        }
    }

    private void showIntro() {
        System.out.println();
        System.out.println("Deactivate Flight Route");
        System.out.println("=======================");
        System.out.println("This operation schedules the deactivation of an active flight route.");
        System.out.println("The route will remain usable until the selected deactivation date.");
        System.out.println();
        System.out.println("You can type 0 at any input step to cancel.");
        System.out.println();
    }

    private FlightRouteDTO chooseRoute(final List<FlightRouteDTO> routes) {
        int page = 0;
        final int totalPages = (int) Math.ceil((double) routes.size() / PAGE_SIZE);

        while (true) {
            printRouteCatalogPage(routes, page, totalPages);

            final String input = Console.readLine("Option:").trim().toLowerCase(Locale.ROOT);

            if (input.equals(CANCEL)) {
                System.out.println();
                System.out.println("Flight route deactivation cancelled.");
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

                if (index >= 0 && index < routes.size()) {
                    final FlightRouteDTO selected = routes.get(index);
                    showSelectedRoute(selected);
                    return selected;
                }

                System.out.printf("  Invalid option. Choose a number between 1 and %d.%n",
                        routes.size());

            } catch (final NumberFormatException e) {
                System.out.println("  Invalid option. Please insert a valid number, n, p or 0.");
            }
        }
    }

    private void printRouteCatalogPage(final List<FlightRouteDTO> routes,
                                       final int page,
                                       final int totalPages) {
        final int from = page * PAGE_SIZE;
        final int to = Math.min(from + PAGE_SIZE, routes.size());

        System.out.printf("%nActive Flight Routes — Page %d of %d%n",
                page + 1,
                totalPages);
        System.out.println("────────────────────────────────────────────────────────────────────────────");
        System.out.printf(" %-3s %-8s %-7s %-7s %-9s %s%n",
                "#", "Route", "From", "To", "Type", "Schedule");
        System.out.println("────────────────────────────────────────────────────────────────────────────");

        for (int i = from; i < to; i++) {
            final FlightRouteDTO route = routes.get(i);

            System.out.printf(" %-3d %-8s %-7s %-7s %-9s %s%n",
                    i + 1,
                    route.routeCode,
                    route.departureAirportIata,
                    route.arrivalAirportIata,
                    route.flightType,
                    compactScheduleOneLine(route.schedule));
        }

        System.out.println("────────────────────────────────────────────────────────────────────────────");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  number - Select route to deactivate");

        if (page > 0) {
            System.out.println("  p      - Previous page");
        }

        if (page < totalPages - 1) {
            System.out.println("  n      - Next page");
        }

        System.out.println("  0      - Cancel");
        System.out.println();
    }

    private void showSelectedRoute(final FlightRouteDTO selected) {
        System.out.println();
        System.out.println("Selected Flight Route");
        System.out.println("────────────────────────────────────────");
        System.out.printf(" %-10s : %s%n", "Route", selected.routeCode);
        System.out.printf(" %-10s : %s -> %s%n",
                "Path",
                selected.departureAirportIata,
                selected.arrivalAirportIata);
        System.out.printf(" %-10s : %s%n", "Type", selected.flightType);
        System.out.printf(" %-10s : %s%n", "Schedule", compactScheduleOneLine(selected.schedule));
        System.out.println("────────────────────────────────────────");
        System.out.println();
    }

    private LocalDate readDeactivationDate() {
        while (true) {
            final String input = Console.readLine("Deactivation date (yyyy-MM-dd) (0 to cancel):")
                    .trim();

            if (input.equals(CANCEL)) {
                System.out.println();
                System.out.println("Flight route deactivation cancelled.");
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Date cannot be empty.");
                continue;
            }

            try {
                final LocalDate date = LocalDate.parse(input, DATE_FMT);

                if (date.isBefore(LocalDate.now())) {
                    System.out.println("  Deactivation date cannot be in the past.");
                    continue;
                }

                return date;

            } catch (final DateTimeParseException e) {
                System.out.println("  Invalid date format. Example: 2030-05-05");
            }
        }
    }

    private void showSummary(final FlightRouteDTO route,
                             final LocalDate deactivationDate) {
        System.out.println();
        System.out.println("Deactivation Summary");
        System.out.println("────────────────────────────────────────");
        System.out.printf(" %-18s : %s%n", "Route", route.routeCode);
        System.out.printf(" %-18s : %s -> %s%n",
                "Path",
                route.departureAirportIata,
                route.arrivalAirportIata);
        System.out.printf(" %-18s : %s%n", "Type", route.flightType);
        System.out.printf(" %-18s : %s%n", "Current schedule", compactScheduleOneLine(route.schedule));
        System.out.printf(" %-18s : %s%n", "Deactivation date", deactivationDate.format(DATE_FMT));
        System.out.println("────────────────────────────────────────");
        System.out.println();

        System.out.println("Impact");
        System.out.println("────────────────────────────────────────");
        System.out.println(" This route will remain active until the deactivation date.");
        System.out.println(" New flight plans should not be created for this route after that date.");
        System.out.println(" Existing future flight plans may prevent this deactivation.");
        System.out.println("────────────────────────────────────────");
        System.out.println();
    }

    private boolean confirmDeactivation() {
        while (true) {
            final String confirm = Console.readLine("Confirm route deactivation? (y/n):")
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

    private boolean retryAfterError() {
        while (true) {
            final String input = Console.readLine("\nTry again? (y/n):")
                    .trim()
                    .toLowerCase(Locale.ROOT);

            if (input.equals("y")) {
                System.out.println();
                return true;
            }

            if (input.equals("n")) {
                return false;
            }

            System.out.println("  Invalid option. Please answer 'y' or 'n'.");
        }
    }

    private void showSuccess(final FlightRouteDTO result,
                             final LocalDate deactivationDate) {
        System.out.println();
        System.out.println("Flight route deactivation scheduled successfully.");
        System.out.println("────────────────────────────────────────");
        System.out.printf(" %-18s : %s%n", "Route", result.routeCode);
        System.out.printf(" %-18s : %s -> %s%n",
                "Path",
                result.departureAirportIata,
                result.arrivalAirportIata);
        System.out.printf(" %-18s : %s%n", "Type", result.flightType);
        System.out.printf(" %-18s : %s%n", "Deactivation date", deactivationDate.format(DATE_FMT));
        System.out.println("────────────────────────────────────────");
        System.out.println();
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

    private String compactScheduleOneLine(final String schedule) {
        final List<String> parts = new ArrayList<>();

        for (final String line : formatSchedule(schedule)) {
            final String[] split = line.split("\\s+at\\s+", 2);

            if (split.length == 2) {
                parts.add(shortWeekday(split[0]) + " " + split[1]);
            } else {
                parts.add(line);
            }
        }

        if (parts.isEmpty()) {
            return "No schedule information";
        }

        return String.join(" | ", parts);
    }

    private String shortWeekday(final String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        final String normalized = value.trim().toLowerCase(Locale.ROOT);

        switch (normalized) {
            case "monday":
                return "Mon";
            case "tuesday":
                return "Tue";
            case "wednesday":
                return "Wed";
            case "thursday":
                return "Thu";
            case "friday":
                return "Fri";
            case "saturday":
                return "Sat";
            case "sunday":
                return "Sun";
            default:
                return capitalize(value);
        }
    }

    private String capitalize(final String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        final String lower = value.trim().toLowerCase(Locale.ROOT);

        return lower.substring(0, 1).toUpperCase(Locale.ROOT) + lower.substring(1);
    }

    @Override
    public String headline() {
        return "Deactivate Flight Route";
    }
}