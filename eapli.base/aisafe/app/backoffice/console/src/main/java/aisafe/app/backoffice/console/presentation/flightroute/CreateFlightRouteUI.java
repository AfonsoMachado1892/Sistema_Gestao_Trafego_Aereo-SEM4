package aisafe.app.backoffice.console.presentation.flightroute;

import aisafe.flightroute.application.AirportDTO;
import aisafe.flightroute.application.CreateFlightRouteController;
import aisafe.flightroute.application.FlightRouteDTO;
import aisafe.flightroute.domain.CharterSchedule;
import aisafe.flightroute.domain.RegularSchedule;
import aisafe.flightroute.domain.ScheduleEntry;
import aisafe.persistence.PersistenceContext;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings("squid:S106")
public class CreateFlightRouteUI extends AbstractUI {

    private static final String CANCEL = "0";
    private static final int PAGE_SIZE = 10;

    private final CreateFlightRouteController controller =
            new CreateFlightRouteController(
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
                final List<AirportDTO> airports = new ArrayList<>();
                controller.listAirports().forEach(airports::add);

                if (airports.isEmpty()) {
                    System.out.println("No airports registered. Please register airports first.");
                    return false;
                }

                final String companyIata = currentCompanyIataCode();
                if (companyIata == null) {
                    return false;
                }

                final String routeCode = readRouteCode(companyIata);
                if (routeCode == null) {
                    return false;
                }

                final AirportDTO departureAirport = chooseAirport(
                        airports,
                        "Choose departure airport",
                        null
                );
                if (departureAirport == null) {
                    return false;
                }

                final AirportDTO arrivalAirport = chooseAirport(
                        airports,
                        "Choose arrival airport",
                        departureAirport.iataCode
                );
                if (arrivalAirport == null) {
                    return false;
                }

                final int routeType = readRouteType();
                if (routeType == 0) {
                    return false;
                }

                final FlightRouteDTO result;

                if (routeType == 1) {
                    final RegularSchedule schedule = readRegularSchedule();
                    if (schedule == null) {
                        return false;
                    }

                    showSummary(
                            routeCode,
                            departureAirport,
                            arrivalAirport,
                            "REGULAR",
                            schedule.toString()
                    );

                    if (!confirmCreation()) {
                        System.out.println("Creation cancelled.");
                        return false;
                    }

                    result = controller.createRegularFlightRoute(
                            routeCode,
                            departureAirport.iataCode,
                            arrivalAirport.iataCode,
                            schedule
                    );

                } else {
                    final CharterSchedule schedule = readCharterSchedule();
                    if (schedule == null) {
                        return false;
                    }

                    showSummary(
                            routeCode,
                            departureAirport,
                            arrivalAirport,
                            "CHARTER",
                            schedule.toString()
                    );

                    if (!confirmCreation()) {
                        System.out.println("Creation cancelled.");
                        return false;
                    }

                    result = controller.createCharterFlightRoute(
                            routeCode,
                            departureAirport.iataCode,
                            arrivalAirport.iataCode,
                            schedule
                    );
                }

                showSuccess(result);
                return false;

            } catch (final IllegalArgumentException | IllegalStateException e) {
                System.out.println();
                System.out.println("Could not create flight route.");
                System.out.println("Reason: " + e.getMessage());

                if (!retryAfterError()) {
                    return false;
                }
            }
        }
    }

    private void showIntro() {
        System.out.println();
        System.out.println("Create Flight Route");
        System.out.println("===================");
        System.out.println("This operation creates a new flight route for your air transport company.");
        System.out.println("You can type 0 at any input step to cancel.");
        System.out.println();
    }

    private String currentCompanyIataCode() {
        try {
            final String companyIata = controller.currentCompanyIataCode();

            System.out.printf("Creating route for company: %s%n", companyIata);
            System.out.printf("Route code must start with '%s' followed by 1 to 4 digits.%n",
                    companyIata);
            System.out.printf("Example: %s101%n", companyIata);
            System.out.println();

            return companyIata;

        } catch (final IllegalStateException e) {
            System.out.println();
            System.out.println("Could not identify your company.");
            System.out.println("Reason: " + e.getMessage());
            return null;
        }
    }

    private String readRouteCode(final String companyIata) {
        final String normalizedCompany = companyIata.trim().toUpperCase();
        final String regex = "^" + Pattern.quote(normalizedCompany) + "\\d{1,4}$";

        while (true) {
            final String input = Console.readLine("Route code (0 to cancel):")
                    .trim()
                    .toUpperCase();

            if (input.equals(CANCEL)) {
                System.out.println("Flight route creation cancelled.");
                return null;
            }

            if (input.isBlank()) {
                System.out.println("Route code cannot be empty.");
                continue;
            }

            if (!input.matches(regex)) {
                System.out.printf("Invalid route code. It must be %s followed by 1 to 4 digits.%n",
                        normalizedCompany);
                System.out.printf("  Example: %s101%n", normalizedCompany);
                continue;
            }

            return input;
        }
    }

    private AirportDTO chooseAirport(final List<AirportDTO> airports,
                                     final String title,
                                     final String excludedIata) {
        int page = 0;
        final int totalPages = (int) Math.ceil((double) airports.size() / PAGE_SIZE);

        while (true) {
            printAirportPage(airports, page, totalPages, title, excludedIata);

            System.out.println();
            System.out.println("Options:");
            System.out.println("number - Select airport");
            if (page > 0) {
                System.out.println("p      - Previous page");
            }
            if (page < totalPages - 1) {
                System.out.println("n      - Next page");
            }
            System.out.println("0      - Cancel");

            final String input = Console.readLine("Option:").trim().toLowerCase();

            if (input.equals(CANCEL)) {
                System.out.println("Flight route creation cancelled.");
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

            try {
                final int selectedNumber = Integer.parseInt(input);
                final int index = selectedNumber - 1;

                if (index < 0 || index >= airports.size()) {
                    System.out.printf("Invalid option. Choose a number between 1 and %d.%n",
                            airports.size());
                    continue;
                }

                final AirportDTO selected = airports.get(index);

                if (excludedIata != null
                        && selected.iataCode.equalsIgnoreCase(excludedIata)) {
                    System.out.println("Arrival airport cannot be the same as departure airport.");
                    continue;
                }

                showSelectedAirport(selected);
                return selected;

            } catch (final NumberFormatException e) {
                System.out.println("Invalid option. Please insert a valid number.");
            }
        }
    }

    private void printAirportPage(final List<AirportDTO> airports,
                                  final int page,
                                  final int totalPages,
                                  final String title,
                                  final String excludedIata) {
        final int from = page * PAGE_SIZE;
        final int to = Math.min(from + PAGE_SIZE, airports.size());

        System.out.println();
        System.out.println(title);
        System.out.println("-".repeat(title.length()));
        System.out.printf("Page %d of %d%n", page + 1, totalPages);
        System.out.println();

        System.out.printf("%-4s %-6s %-6s %-35s %-20s%n",
                "#", "IATA", "ICAO", "Name", "City");
        System.out.println("  " + "-".repeat(85));

        for (int i = from; i < to; i++) {
            final AirportDTO airport = airports.get(i);

            final String marker =
                    excludedIata != null && airport.iataCode.equalsIgnoreCase(excludedIata)
                            ? " (selected as departure)"
                            : "";

            System.out.printf("  %-4d %-6s %-6s %-35s %-20s%s%n",
                    i + 1,
                    airport.iataCode,
                    airport.icaoCode,
                    truncate(airport.name, 35),
                    truncate(airport.city, 20),
                    marker);
        }
    }

    private void showSelectedAirport(final AirportDTO airport) {
        System.out.println();
        System.out.println("Selected airport");
        System.out.println("----------------");
        System.out.println("  IATA : " + airport.iataCode);
        System.out.println("  ICAO : " + airport.icaoCode);
        System.out.println("  Name : " + airport.name);
        System.out.println("  City : " + airport.city);
        System.out.println();
    }

    private int readRouteType() {
        while (true) {
            System.out.println();
            System.out.println("Route type");
            System.out.println("----------");
            System.out.println("  1. Regular");
            System.out.println("  2. Charter");
            System.out.println("  0. Cancel");

            final String input = Console.readLine("Choose route type:").trim();

            if (input.equals(CANCEL)) {
                System.out.println("Flight route creation cancelled.");
                return 0;
            }

            if (input.isBlank()) {
                System.out.println("Please insert an option.");
                continue;
            }

            try {
                final int option = Integer.parseInt(input);

                if (option == 1 || option == 2) {
                    return option;
                }

                System.out.println("Invalid option. Choose 1 for Regular, 2 for Charter, or 0 to cancel.");

            } catch (final NumberFormatException e) {
                System.out.println("Invalid option. Please insert a valid number.");
            }
        }
    }

    private RegularSchedule readRegularSchedule() {
        final List<ScheduleEntry> entries = new ArrayList<>();

        System.out.println();
        System.out.println("Regular schedule");
        System.out.println("----------------");
        System.out.println("Insert at least one weekly departure entry.");

        while (true) {
            final DayOfWeek dayOfWeek = readDayOfWeek();
            if (dayOfWeek == null) {
                return null;
            }

            final LocalTime departureTime = readLocalTime("Departure time");
            if (departureTime == null) {
                return null;
            }

            entries.add(ScheduleEntry.of(dayOfWeek, departureTime));
            System.out.printf("  Added: %s at %s%n",
                    capitalize(dayOfWeek.name()),
                    departureTime);

            if (!askAddAnotherEntry()) {
                break;
            }
        }

        return RegularSchedule.of(entries);
    }

    private DayOfWeek readDayOfWeek() {
        while (true) {
            System.out.println();
            System.out.println("Day of week");
            System.out.println("-----------");
            System.out.println("  1. Monday");
            System.out.println("  2. Tuesday");
            System.out.println("  3. Wednesday");
            System.out.println("  4. Thursday");
            System.out.println("  5. Friday");
            System.out.println("  6. Saturday");
            System.out.println("  7. Sunday");
            System.out.println("  0. Cancel");

            final String input = Console.readLine("Choose day:").trim();

            if (input.equals(CANCEL)) {
                System.out.println("Flight route creation cancelled.");
                return null;
            }

            try {
                final int option = Integer.parseInt(input);

                if (option >= 1 && option <= 7) {
                    return DayOfWeek.of(option);
                }

                System.out.println("  Invalid option. Choose a number between 1 and 7, or 0 to cancel.");

            } catch (final NumberFormatException e) {
                System.out.println("  Invalid option. Please insert a valid number.");
            }
        }
    }

    private boolean askAddAnotherEntry() {
        while (true) {
            final String answer = Console.readLine("Add another schedule entry? (y/n):")
                    .trim()
                    .toLowerCase();

            if (answer.equals("y")) {
                return true;
            }

            if (answer.equals("n")) {
                return false;
            }

            System.out.println("  Invalid option. Please answer 'y' or 'n'.");
        }
    }

    private CharterSchedule readCharterSchedule() {
        System.out.println();
        System.out.println("Charter schedule");
        System.out.println("----------------");

        final LocalDateTime departure = readDateTime("Charter departure");
        if (departure == null) {
            return null;
        }

        final LocalDateTime arrival = readArrivalAfter(departure);
        if (arrival == null) {
            return null;
        }

        return CharterSchedule.of(departure, arrival);
    }

    private LocalDateTime readArrivalAfter(final LocalDateTime departure) {
        while (true) {
            final LocalDateTime arrival = readDateTime("Charter arrival");

            if (arrival == null) {
                return null;
            }

            if (arrival.isAfter(departure)) {
                return arrival;
            }

            System.out.println();
            System.out.println("Arrival must be after departure.");
            System.out.println("Departure: " + formatDateTime(departure));
            System.out.println("Please insert the arrival again.");
        }
    }

    private LocalDateTime readDateTime(final String label) {
        System.out.println();
        System.out.println(label);
        System.out.println("-".repeat(label.length()));

        final LocalDate date = readDate(label + " date");
        if (date == null) {
            return null;
        }

        final LocalTime time = readLocalTime(label + " time");
        if (time == null) {
            return null;
        }

        return LocalDateTime.of(date, time);
    }

    private LocalDate readDate(final String label) {
        while (true) {
            final String input = Console.readLine(label + " (yyyy-MM-dd) (0 to cancel):")
                    .trim();

            if (input.equals(CANCEL)) {
                System.out.println("Flight route creation cancelled.");
                return null;
            }

            if (input.isBlank()) {
                System.out.println("Date cannot be empty.");
                continue;
            }

            try {
                return LocalDate.parse(input);
            } catch (final DateTimeParseException e) {
                System.out.println("Invalid date format. Example: 2026-06-15");
            }
        }
    }

    private LocalTime readLocalTime(final String label) {
        while (true) {
            final String input = Console.readLine(label + " (HH:mm) (0 to cancel):")
                    .trim();

            if (input.equals(CANCEL)) {
                System.out.println("Flight route creation cancelled.");
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Time cannot be empty.");
                continue;
            }

            try {
                return LocalTime.parse(input);
            } catch (final DateTimeParseException e) {
                System.out.println("  Invalid time format. Example: 09:30");
            }
        }
    }

    private void showSummary(final String routeCode,
                             final AirportDTO departureAirport,
                             final AirportDTO arrivalAirport,
                             final String flightType,
                             final String schedule) {
        System.out.println();
        System.out.println("Flight Route Summary");
        System.out.println("====================");
        System.out.println("  Route code : " + routeCode.toUpperCase());
        System.out.printf("  Departure  : %s - %s%n",
                departureAirport.iataCode,
                departureAirport.city);
        System.out.printf("  Arrival    : %s - %s%n",
                arrivalAirport.iataCode,
                arrivalAirport.city);
        System.out.println("  Type       : " + flightType);
        System.out.println("  Schedule   :");
        printSchedule(schedule);
        System.out.println();
    }

    private boolean confirmCreation() {
        while (true) {
            final String confirm = Console.readLine("Confirm creation? (y/n):")
                    .trim()
                    .toLowerCase();

            if (confirm.equals("y")) {
                return true;
            }

            if (confirm.equals("n")) {
                return false;
            }

            System.out.println("Invalid option. Please answer 'y' or 'n'.");
        }
    }

    private boolean retryAfterError() {
        while (true) {
            final String input = Console.readLine("\nTry again? (y/n):")
                    .trim()
                    .toLowerCase();

            if (input.equals("y")) {
                System.out.println();
                return true;
            }

            if (input.equals("n")) {
                return false;
            }

            System.out.println("Invalid option. Please answer 'y' or 'n'.");
        }
    }

    private void showSuccess(final FlightRouteDTO result) {
        System.out.println();
        System.out.println("Flight route created successfully.");
        System.out.println("----------------------------------");
        System.out.println("  Route code : " + result.routeCode);
        System.out.printf("  Route      : %s -> %s%n",
                result.departureAirportIata,
                result.arrivalAirportIata);
        System.out.println("  Type       : " + result.flightType);
        System.out.println();
    }

    private void printSchedule(final String schedule) {
        for (final String line : formatSchedule(schedule)) {
            final String[] parts = line.split("\\s+at\\s+", 2);

            if (parts.length == 2) {
                System.out.printf("       - %-10s %s%n", capitalize(parts[0]), parts[1]);
            } else {
                System.out.println("       - " + line);
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

    private String formatDateTime(final LocalDateTime dateTime) {
        return dateTime.toLocalDate() + " at " + dateTime.toLocalTime();
    }

    private String truncate(final String value, final int maxLength) {
        if (value == null) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        if (maxLength <= 3) {
            return value.substring(0, maxLength);
        }

        return value.substring(0, maxLength - 3) + "...";
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
        return "Create Flight Route";
    }
}