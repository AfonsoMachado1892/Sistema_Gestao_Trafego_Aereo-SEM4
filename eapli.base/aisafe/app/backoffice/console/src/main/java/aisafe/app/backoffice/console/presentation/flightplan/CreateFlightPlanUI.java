package aisafe.app.backoffice.console.presentation.flightplan;

import aisafe.aircraft.domain.Aircraft;
import aisafe.flightplan.application.FlightPlanDTO;
import aisafe.flightplan.domain.FuelUnit;
import aisafe.flightplan.application.CreateFlightPlanController;
import aisafe.flightroute.application.FlightRouteDTO;
import aisafe.persistence.PersistenceContext;
import eapli.framework.infrastructure.authz.application.exceptions.UnauthorizedException;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("squid:S106")
public class CreateFlightPlanUI extends AbstractUI {

    private static final String CANCEL = "0";

    private static final int BOX_WIDTH = 78;
    private static final int BOX_CONTENT_WIDTH = BOX_WIDTH - 4;

    private static final long WARNING_MAX_DURATION_HOURS = 24;
    private static final double WARNING_HIGH_FUEL_AMOUNT = 100_000.0;

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, yyyy-MM-dd HH:mm", Locale.ENGLISH);

    private final CreateFlightPlanController controller =
            new CreateFlightPlanController(
                    PersistenceContext.repositories().flightPlans(),
                    PersistenceContext.repositories().flightRoutes(),
                    PersistenceContext.repositories().aircrafts(),
                    PersistenceContext.repositories().pilots()
            );

    @Override
    protected boolean doShow() {
        showIntro();

        try {
            final DraftInput input = readInitialDraftInput();

            if (input == null) {
                return false;
            }

            while (true) {
                final ValidationPreview preview = buildValidationPreview(input);

                showSummary(input, preview);

                if (preview.isReady()) {
                    if (!confirmCreation()) {
                        System.out.println();
                        System.out.println("Flight plan creation cancelled.");
                        return false;
                    }

                    createFlightPlan(input);
                    return false;
                }

                final EditAction action = readCorrectionAction(preview);

                switch (action) {
                    case CHANGE_ROUTE:
                        final FlightRouteDTO newRoute = chooseRoute();

                        if (newRoute == null) {
                            return false;
                        }

                        input.selectedRoute = newRoute;
                        break;

                    case CHANGE_AIRCRAFT:
                        final String newAircraft = chooseAircraft();

                        if (newAircraft == null) {
                            return false;
                        }

                        input.aircraftRegistration = newAircraft;
                        break;

                    case CHANGE_DEPARTURE:
                        final LocalDateTime newDeparture = readDateTime("Planned departure");

                        if (newDeparture == null) {
                            return false;
                        }

                        input.plannedDeparture = newDeparture;

                        if (!input.plannedArrival.isAfter(input.plannedDeparture)) {
                            System.out.println();
                            System.out.println("The current planned arrival is no longer after the new departure.");
                            System.out.println("Please insert the planned arrival again.");

                            final LocalDateTime newArrival = readArrivalAfter(input.plannedDeparture);

                            if (newArrival == null) {
                                return false;
                            }

                            input.plannedArrival = newArrival;
                        }
                        break;

                    case USE_SUGGESTED_DEPARTURE:
                        if (preview.suggestedDeparture == null) {
                            System.out.println();
                            System.out.println("No suggested departure is currently available.");
                            break;
                        }

                        input.plannedDeparture = preview.suggestedDeparture;

                        System.out.println();
                        System.out.println("Suggested departure applied:");
                        System.out.println("  " + formatDateTimeWithWeekday(input.plannedDeparture));

                        if (!input.plannedArrival.isAfter(input.plannedDeparture)) {
                            System.out.println();
                            System.out.println("The current planned arrival is no longer after the new departure.");
                            System.out.println("Please insert the planned arrival again.");

                            final LocalDateTime newArrival = readArrivalAfter(input.plannedDeparture);

                            if (newArrival == null) {
                                return false;
                            }

                            input.plannedArrival = newArrival;
                        }
                        break;

                    case CHANGE_ARRIVAL:
                        final LocalDateTime changedArrival = readArrivalAfter(input.plannedDeparture);

                        if (changedArrival == null) {
                            return false;
                        }

                        input.plannedArrival = changedArrival;
                        break;

                    case CHANGE_FUEL:
                        final double changedFuelAmount = readFuelAmount();

                        if (changedFuelAmount < 0) {
                            return false;
                        }

                        final FuelUnit changedFuelUnit = readFuelUnit();

                        if (changedFuelUnit == null) {
                            return false;
                        }

                        input.fuelAmount = changedFuelAmount;
                        input.fuelUnit = changedFuelUnit;
                        break;

                    case CONTINUE_ANYWAY:
                        if (preview.hasBlockingIssues()) {
                            System.out.println();
                            System.out.println("This flight plan has blocking issues and cannot be created yet.");
                            break;
                        }

                        if (!confirmCreation()) {
                            System.out.println();
                            System.out.println("Flight plan creation cancelled.");
                            return false;
                        }

                        createFlightPlan(input);
                        return false;

                    case CANCEL:
                        System.out.println();
                        System.out.println("Flight plan creation cancelled.");
                        return false;

                    default:
                        throw new IllegalStateException("Unexpected action: " + action);
                }
            }

        } catch (final UnauthorizedException e) {
            System.out.println();
            System.out.println("You are not authorized to create flight plans.");
            System.out.println("Please login with a Pilot account.");
            return false;

        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println();
            System.out.println("Could not create flight plan.");
            System.out.println("Reason: " + e.getMessage());
            return false;
        }
    }

    private DraftInput readInitialDraftInput() {
        final FlightRouteDTO selectedRoute = chooseRoute();

        if (selectedRoute == null) {
            return null;
        }

        final String aircraftRegistration = chooseAircraft();

        if (aircraftRegistration == null) {
            return null;
        }

        final LocalDateTime plannedDeparture = readDateTime("Planned departure");

        if (plannedDeparture == null) {
            return null;
        }

        final LocalDateTime plannedArrival = readArrivalAfter(plannedDeparture);

        if (plannedArrival == null) {
            return null;
        }

        final double fuelAmount = readFuelAmount();

        if (fuelAmount < 0) {
            return null;
        }

        final FuelUnit fuelUnit = readFuelUnit();

        if (fuelUnit == null) {
            return null;
        }

        final DraftInput input = new DraftInput();
        input.selectedRoute = selectedRoute;
        input.aircraftRegistration = aircraftRegistration;
        input.plannedDeparture = plannedDeparture;
        input.plannedArrival = plannedArrival;
        input.fuelAmount = fuelAmount;
        input.fuelUnit = fuelUnit;

        return input;
    }

    private void createFlightPlan(final DraftInput input) {
        final String description = buildAutomaticDescription(input);

        final FlightPlanDTO flightPlan = controller.createFlightPlan(
                input.selectedRoute.routeCode,
                input.aircraftRegistration,
                input.plannedDeparture,
                input.plannedArrival,
                input.fuelAmount,
                input.fuelUnit,
                description
        );

        showSuccess(flightPlan);
    }

    private void showIntro() {
        System.out.println();
        System.out.println("Create Flight Plan");
        System.out.println("==================");
        System.out.println("This operation creates a new flight plan in DRAFT status.");
        System.out.println("DSL validation is not performed in this step.");
        System.out.println();
        System.out.println("You can type 0 at any input step to cancel.");
        System.out.println();
    }

    private FlightRouteDTO chooseRoute() {
        final List<FlightRouteDTO> routes = new ArrayList<>();

        try {
            controller.listAvailableRoutes().forEach(routes::add);
        } catch (final UnauthorizedException e) {
            throw e;
        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println("Could not load available routes.");
            System.out.println("Reason: " + e.getMessage());
            return null;
        }

        if (routes.isEmpty()) {
            System.out.println("No active routes are available for your company.");
            return null;
        }

        System.out.println();
        System.out.println("Available Flight Routes");
        System.out.println("────────────────────────────────────────────────────────────────────────────");
        System.out.printf(" %-3s %-8s %-7s %-7s %-9s %s%n",
                "#", "Route", "From", "To", "Type", "Schedule");
        System.out.println("────────────────────────────────────────────────────────────────────────────");

        for (int i = 0; i < routes.size(); i++) {
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
        System.out.println("  0. Cancel");
        System.out.println();

        while (true) {
            final String input = Console.readLine("Choose route:").trim();

            if (input.equals(CANCEL)) {
                System.out.println("Flight plan creation cancelled.");
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Please insert an option.");
                continue;
            }

            try {
                final int option = Integer.parseInt(input);

                if (option >= 1 && option <= routes.size()) {
                    final FlightRouteDTO selected = routes.get(option - 1);
                    showSelectedRoute(selected);
                    return selected;
                }

                System.out.printf("  Invalid option. Choose a number between 1 and %d.%n",
                        routes.size());

            } catch (final NumberFormatException e) {
                System.out.println("  Invalid option. Please insert a valid number.");
            }
        }
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

    private String chooseAircraft() {
        final List<Aircraft> aircrafts = new ArrayList<>();

        try {
            controller.listAvailableAircrafts().forEach(aircrafts::add);
        } catch (final UnauthorizedException e) {
            throw e;
        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println("Could not load available aircrafts.");
            System.out.println("Reason: " + e.getMessage());
            return null;
        }

        if (aircrafts.isEmpty()) {
            System.out.println("No operational aircrafts are available for your company.");
            return null;
        }

        System.out.println();
        System.out.println("Available Aircraft");
        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────");
        System.out.printf(" %-3s %-15s %-22s %-10s %-10s %-14s %s%n",
                "#", "Registration", "Model", "Type", "Capacity", "Max Fuel", "Range");
        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────");

        for (int i = 0; i < aircrafts.size(); i++) {
            final Aircraft aircraft = aircrafts.get(i);

            System.out.printf(" %-3d %-15s %-22s %-10s %-10s %-14s %s%n",
                    i + 1,
                    aircraft.aircraftCode().code(),
                    shortText(aircraft.aircraftModel().identity().toString(), 22),
                    aircraft.aircraftModel().type(),
                    aircraft.aircraftModel().maxPassengerCapacity() + " pax",
                    formatKg(aircraft.aircraftModel().maxFuelCapacity()),
                    formatKm(aircraft.aircraftModel().maximumRange()));
        }

        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────");
        System.out.println();
        System.out.println("  0. Cancel");
        System.out.println();

        while (true) {
            final String input = Console.readLine("Choose aircraft:").trim();

            if (input.equals(CANCEL)) {
                System.out.println("Flight plan creation cancelled.");
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Please insert an option.");
                continue;
            }

            try {
                final int option = Integer.parseInt(input);

                if (option >= 1 && option <= aircrafts.size()) {
                    final Aircraft selected = aircrafts.get(option - 1);
                    showSelectedAircraft(selected);
                    return selected.aircraftCode().code();
                }

                System.out.printf("  Invalid option. Choose a number between 1 and %d.%n",
                        aircrafts.size());

            } catch (final NumberFormatException e) {
                System.out.println("  Invalid option. Please insert a valid number.");
            }
        }
    }

    private void showSelectedAircraft(final Aircraft selected) {
        System.out.println();
        System.out.println("Selected Aircraft");
        System.out.println("────────────────────────────────────────");
        System.out.printf(" %-12s : %s%n", "Registration", selected.aircraftCode().code());
        System.out.printf(" %-12s : %s%n", "Model", selected.aircraftModel().identity());
        System.out.printf(" %-12s : %s%n", "Type", selected.aircraftModel().type());
        System.out.printf(" %-12s : %s%n", "Capacity", selected.aircraftModel().maxPassengerCapacity() + " pax");
        System.out.printf(" %-12s : %s%n", "Max fuel", formatKg(selected.aircraftModel().maxFuelCapacity()));
        System.out.printf(" %-12s : %s%n", "Range", formatKm(selected.aircraftModel().maximumRange()));
        System.out.printf(" %-12s : %s%n", "Crew", selected.numCrewElements());
        System.out.printf(" %-12s : %s%n", "Country", selected.country());
        System.out.printf(" %-12s : %s%n", "Status", selected.status());
        System.out.println("────────────────────────────────────────");
        System.out.println();
    }

    private LocalDateTime readDateTime(final String label) {
        System.out.println();
        System.out.println(label);
        System.out.println("-".repeat(label.length()));

        final LocalDate date = readDate(label + " date");

        if (date == null) {
            return null;
        }

        final LocalTime time = readTime(label + " time");

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
                System.out.println("Flight plan creation cancelled.");
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Date cannot be empty.");
                continue;
            }

            try {
                return LocalDate.parse(input);
            } catch (final DateTimeParseException e) {
                System.out.println("  Invalid date format. Example: 2026-06-15");
            }
        }
    }

    private LocalTime readTime(final String label) {
        while (true) {
            final String input = Console.readLine(label + " (HH:mm) (0 to cancel):")
                    .trim();

            if (input.equals(CANCEL)) {
                System.out.println("Flight plan creation cancelled.");
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

    private LocalDateTime readArrivalAfter(final LocalDateTime plannedDeparture) {
        while (true) {
            final LocalDateTime plannedArrival = readDateTime("Planned arrival");

            if (plannedArrival == null) {
                return null;
            }

            if (plannedArrival.isAfter(plannedDeparture)) {
                return plannedArrival;
            }

            System.out.println();
            System.out.println("  Planned arrival must be after planned departure.");
            System.out.println("  Planned departure: " + formatDateTime(plannedDeparture));
            System.out.println("  Please insert the planned arrival again.");
        }
    }

    private double readFuelAmount() {
        while (true) {
            final String input = Console.readLine("Fuel quantity (0 to cancel):").trim();

            if (input.equals(CANCEL)) {
                System.out.println("Flight plan creation cancelled.");
                return -1;
            }

            if (input.isBlank()) {
                System.out.println("  Fuel quantity cannot be empty.");
                continue;
            }

            try {
                final double amount = Double.parseDouble(input.replace(",", "."));

                if (amount > 0) {
                    return amount;
                }

                System.out.println("  Fuel quantity must be greater than zero.");

            } catch (final NumberFormatException e) {
                System.out.println("  Invalid number. Example: 12000");
            }
        }
    }

    private FuelUnit readFuelUnit() {
        while (true) {
            System.out.println();
            System.out.println("Fuel unit");
            System.out.println("---------");
            System.out.println("  1. KG");
            System.out.println("  2. L");
            System.out.println("  0. Cancel");

            final String input = Console.readLine("Choose fuel unit:").trim();

            if (input.equals(CANCEL)) {
                System.out.println("Flight plan creation cancelled.");
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Please insert an option.");
                continue;
            }

            try {
                final int option = Integer.parseInt(input);

                switch (option) {
                    case 1:
                        return FuelUnit.KG;
                    case 2:
                        return FuelUnit.L;
                    default:
                        System.out.println("  Invalid option. Choose 1, 2 or 0.");
                }

            } catch (final NumberFormatException e) {
                System.out.println("  Invalid option. Please insert a valid number.");
            }
        }
    }

    private ValidationPreview buildValidationPreview(final DraftInput input) {
        final ValidationPreview preview = new ValidationPreview();

        final boolean allowedDeparture = controller.routeAllowsDepartureAt(
                input.selectedRoute.routeCode,
                input.plannedDeparture
        );

        if (!allowedDeparture) {
            final LocalDateTime suggestedDeparture = suggestedDepartureForSameDay(
                    input.selectedRoute.schedule,
                    input.plannedDeparture
            );

            preview.suggestedDeparture = suggestedDeparture;

            if (suggestedDeparture != null) {
                preview.addBlockingIssue(
                        IssueCode.ROUTE_SCHEDULE_MISMATCH,
                        "Departure time does not match route schedule.",
                        List.of(
                                "Route " + input.selectedRoute.routeCode + " operates on "
                                        + capitalize(input.plannedDeparture.getDayOfWeek().toString())
                                        + " at " + suggestedDeparture.toLocalTime() + ".",
                                "Entered: " + formatDateTimeWithWeekday(input.plannedDeparture),
                                "Suggested: " + formatDateTimeWithWeekday(suggestedDeparture)
                        )
                );
            } else {
                preview.addBlockingIssue(
                        IssueCode.ROUTE_SCHEDULE_MISMATCH,
                        "Departure is not allowed by route schedule.",
                        List.of(
                                "Route " + input.selectedRoute.routeCode + " operates on:",
                                formatScheduleInline(input.selectedRoute.schedule),
                                "Entered: " + formatDateTimeWithWeekday(input.plannedDeparture)
                        )
                );
            }
        }

        final Duration duration = Duration.between(input.plannedDeparture, input.plannedArrival);

        if (duration.toHours() > WARNING_MAX_DURATION_HOURS) {
            preview.addWarning(
                    IssueCode.UNREALISTIC_DURATION,
                    "Arrival time seems unrealistic.",
                    List.of("Duration: " + formatDuration(input.plannedDeparture, input.plannedArrival))
            );
        }

        if (input.fuelAmount > WARNING_HIGH_FUEL_AMOUNT) {
            preview.addWarning(
                    IssueCode.HIGH_FUEL,
                    "Fuel quantity is unusually high.",
                    List.of(
                            "Entered: " + formatFuel(input.fuelAmount, input.fuelUnit),
                            "This may cause the flight plan to be rejected during validation."
                    )
            );
        }

        return preview;
    }

    private LocalDateTime suggestedDepartureForSameDay(final String schedule,
                                                       final LocalDateTime enteredDeparture) {
        final DayOfWeek enteredDay = enteredDeparture.getDayOfWeek();

        for (final String line : formatSchedule(schedule)) {
            final String[] parts = line.split("\\s+at\\s+", 2);

            if (parts.length != 2) {
                continue;
            }

            final DayOfWeek scheduleDay = parseDayOfWeek(parts[0]);
            final LocalTime scheduleTime = parseScheduleTime(parts[1]);

            if (scheduleDay == enteredDay && scheduleTime != null) {
                return LocalDateTime.of(enteredDeparture.toLocalDate(), scheduleTime);
            }
        }

        return null;
    }

    private DayOfWeek parseDayOfWeek(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        final String normalized = value.trim().toUpperCase(Locale.ROOT);

        try {
            return DayOfWeek.valueOf(normalized);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

    private LocalTime parseScheduleTime(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalTime.parse(value.trim());
        } catch (final DateTimeParseException e) {
            return null;
        }
    }

    private void showSummary(final DraftInput input,
                             final ValidationPreview preview) {
        System.out.println();

        printBoxHeader("Flight Plan Summary");
        printBoxRow("Route", input.selectedRoute.routeCode + "    "
                + input.selectedRoute.departureAirportIata + " -> "
                + input.selectedRoute.arrivalAirportIata + "    "
                + input.selectedRoute.flightType);
        printBoxRow("Aircraft", input.aircraftRegistration);
        printBoxRow("Departure", formatDateTimeWithWeekday(input.plannedDeparture));
        printBoxRow("Arrival", formatDateTimeWithWeekday(input.plannedArrival));
        printBoxRow("Duration", formatDuration(input.plannedDeparture, input.plannedArrival));
        printBoxRow("Fuel", formatFuel(input.fuelAmount, input.fuelUnit));
        printBoxRow("Description", "automatically generated");
        printBoxRow("Status", "DRAFT");
        printBoxFooter();

        System.out.println();

        printBoxHeader("Validation Preview");

        if (preview.hasBlockingIssues()) {
            printBoxLine("Status: BLOCKED");
        } else if (preview.hasWarnings()) {
            printBoxLine("Status: READY WITH WARNINGS");
        } else {
            printBoxLine("Status: READY");
        }

        printBoxBlank();

        if (preview.isReady()) {
            printBoxLine("[OK] Departure matches route schedule.");
            printBoxLine("[OK] Arrival is after departure.");
            printBoxLine("[OK] Fuel quantity is positive.");
            printBoxLine("[OK] Aircraft was selected.");
        } else {
            for (final PreviewIssue issue : preview.blockingIssues) {
                printIssue("[X]", issue);
            }

            for (final PreviewIssue warning : preview.warnings) {
                printIssue("[!]", warning);
            }
        }

        printBoxFooter();

        System.out.println();

        if (preview.hasBlockingIssues()) {
            System.out.println("Result: flight plan cannot be created yet.");
        } else if (preview.hasWarnings()) {
            System.out.println("Result: flight plan can be created, but warnings should be reviewed.");
        } else {
            System.out.println("Result: flight plan can be created in DRAFT status.");
        }

        System.out.println();
    }

    private void printIssue(final String marker,
                            final PreviewIssue issue) {
        printBoxLine(marker + " " + issue.title);

        for (final String detail : issue.details) {
            if (detail.contains(System.lineSeparator())) {
                for (final String line : detail.split(System.lineSeparator())) {
                    printBoxLine("    " + line);
                }
            } else {
                printBoxLine("    " + detail);
            }
        }

        printBoxBlank();
    }

    private EditAction readCorrectionAction(final ValidationPreview preview) {
        while (true) {
            printCorrectionMenu(preview);

            final String input = Console.readLine("Choose option:").trim();

            if (input.isBlank()) {
                System.out.println("  Please insert an option.");
                continue;
            }

            try {
                final int option = Integer.parseInt(input);

                switch (option) {
                    case 0:
                        return EditAction.CANCEL;
                    case 1:
                        return EditAction.CHANGE_ROUTE;
                    case 2:
                        return EditAction.CHANGE_AIRCRAFT;
                    case 3:
                        return EditAction.CHANGE_DEPARTURE;
                    case 4:
                        return EditAction.CHANGE_ARRIVAL;
                    case 5:
                        return EditAction.CHANGE_FUEL;
                    case 6:
                        if (preview.suggestedDeparture != null && preview.hasBlockingIssues()) {
                            return EditAction.USE_SUGGESTED_DEPARTURE;
                        }

                        if (!preview.hasBlockingIssues()) {
                            return EditAction.CONTINUE_ANYWAY;
                        }

                        System.out.println("  Cannot continue while blocking issues exist.");
                        break;
                    default:
                        System.out.println("  Invalid option. Choose one of the available options.");
                }

            } catch (final NumberFormatException e) {
                System.out.println("  Invalid option. Please insert a valid number.");
            }
        }
    }

    private void printCorrectionMenu(final ValidationPreview preview) {
        final Map<Integer, String> recommended = recommendedFixes(preview);
        final Map<Integer, String> allOptions = allEditOptions(preview);

        System.out.println("Recommended fixes");
        System.out.println("-----------------");

        if (recommended.isEmpty()) {
            System.out.println("  No specific fixes recommended.");
        } else {
            for (final Map.Entry<Integer, String> entry : recommended.entrySet()) {
                System.out.println("  " + entry.getKey() + ". " + entry.getValue());
            }
        }

        System.out.println();
        System.out.println("Other options");
        System.out.println("-------------");

        for (final Map.Entry<Integer, String> entry : allOptions.entrySet()) {
            if (!recommended.containsKey(entry.getKey())) {
                System.out.println("  " + entry.getKey() + ". " + entry.getValue());
            }
        }

        System.out.println("  0. Cancel");
        System.out.println();
    }

    private Map<Integer, String> recommendedFixes(final ValidationPreview preview) {
        final Map<Integer, String> recommended = new LinkedHashMap<>();
        final Set<IssueCode> issueCodes = preview.issueCodes();

        if (preview.suggestedDeparture != null && issueCodes.contains(IssueCode.ROUTE_SCHEDULE_MISMATCH)) {
            recommended.put(6, "Use suggested departure time   sets: "
                    + formatDateTimeWithWeekday(preview.suggestedDeparture));
        }

        if (issueCodes.contains(IssueCode.ROUTE_SCHEDULE_MISMATCH)) {
            recommended.put(3, "Change departure date/time     fixes: route schedule mismatch");
            recommended.put(1, "Change route                   fixes: route schedule mismatch");
        }

        if (issueCodes.contains(IssueCode.UNREALISTIC_DURATION)) {
            recommended.put(4, "Change arrival date/time       fixes: unrealistic duration");
        }

        if (issueCodes.contains(IssueCode.HIGH_FUEL)) {
            recommended.put(5, "Change fuel quantity/unit      fixes: unusually high fuel");
        }

        return recommended;
    }

    private Map<Integer, String> allEditOptions(final ValidationPreview preview) {
        final Map<Integer, String> options = new LinkedHashMap<>();

        options.put(1, "Change route");
        options.put(2, "Change aircraft");
        options.put(3, "Change departure date/time");
        options.put(4, "Change arrival date/time");
        options.put(5, "Change fuel quantity/unit");

        if (!preview.hasBlockingIssues()) {
            options.put(6, "Continue and create flight plan");
        }

        return options;
    }

    private boolean confirmCreation() {
        while (true) {
            final String confirm = Console.readLine("Confirm flight plan creation? (y/n):")
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

    private void showSuccess(final FlightPlanDTO flightPlan) {
        System.out.println();
        printBoxHeader("Flight Plan Created");
        printBoxRow("Flight Plan ID", flightPlan.flightPlanID());
        printBoxRow("Route", flightPlan.routeCode());
        printBoxRow("Aircraft", flightPlan.aircraftRegistration());
        printBoxRow("Departure", formatDateTimeWithWeekday(flightPlan.plannedDepartureDateTime()));
        printBoxRow("Arrival", formatDateTimeWithWeekday(flightPlan.plannedArrivalDateTime()));
        printBoxRow("Fuel", flightPlan.fuelAmount() + " " + flightPlan.fuelUnit());
        printBoxRow("Status", flightPlan.status());
        printBoxFooter();
        System.out.println();
    }

    private String buildAutomaticDescription(final DraftInput input) {
        return String.format(Locale.US, """
                Draft flight plan created manually in US080.
                Route: %s
                Aircraft: %s
                Planned departure: %s
                Planned arrival: %s
                Fuel: %s
                DSL validation: not performed
                """,
                input.selectedRoute.routeCode,
                input.aircraftRegistration,
                formatDateTime(input.plannedDeparture),
                formatDateTime(input.plannedArrival),
                formatFuel(input.fuelAmount, input.fuelUnit)
        ).trim();
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

    private String formatScheduleInline(final String schedule) {
        final StringBuilder builder = new StringBuilder();

        for (final String line : formatSchedule(schedule)) {
            final String[] parts = line.split("\\s+at\\s+", 2);

            builder.append("  - ");

            if (parts.length == 2) {
                builder.append(String.format("%-10s %s", capitalize(parts[0]), parts[1]));
            } else {
                builder.append(line);
            }

            builder.append(System.lineSeparator());
        }

        return builder.toString().stripTrailing();
    }

    private String formatDateTime(final LocalDateTime dateTime) {
        return dateTime.toLocalDate() + " at " + dateTime.toLocalTime();
    }

    private String formatDateTimeWithWeekday(final LocalDateTime dateTime) {
        return DATE_TIME_FORMAT.format(dateTime);
    }

    private String formatDuration(final LocalDateTime departure,
                                  final LocalDateTime arrival) {
        final Duration duration = Duration.between(departure, arrival);

        final long days = duration.toDays();
        final long hoursPart = duration.toHoursPart();
        final long minutesPart = duration.toMinutesPart();

        if (days > 0) {
            return String.format(Locale.US, "%d days, %dh %02dm",
                    days,
                    hoursPart,
                    minutesPart);
        }

        return String.format(Locale.US, "%dh %02dm",
                duration.toHours(),
                minutesPart);
    }

    private String formatFuel(final double amount,
                              final FuelUnit unit) {
        return String.format(Locale.US, "%,.2f %s",
                amount,
                unit.name().toLowerCase(Locale.ROOT));
    }

    private String formatKg(final double value) {
        return String.format(Locale.US, "%,.2f kg", value);
    }

    private String formatKm(final double value) {
        return String.format(Locale.US, "%,.0f km", value);
    }

    private String shortText(final String value, final int maxLength) {
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

        final String lower = value.trim().toLowerCase(Locale.ROOT);

        return lower.substring(0, 1).toUpperCase(Locale.ROOT) + lower.substring(1);
    }

    private void printBoxHeader(final String title) {
        final String normalizedTitle = " " + title + " ";
        final int left = Math.max(0, (BOX_WIDTH - 2 - normalizedTitle.length()) / 2);
        final int right = BOX_WIDTH - 2 - normalizedTitle.length() - left;

        System.out.println("+" + "-".repeat(left) + normalizedTitle + "-".repeat(right) + "+");
    }

    private void printBoxFooter() {
        System.out.println("+" + "-".repeat(BOX_WIDTH - 2) + "+");
    }

    private void printBoxBlank() {
        printBoxLine("");
    }

    private void printBoxRow(final String label,
                             final String value) {
        final String content = String.format(Locale.US, "%-12s | %s", label, value);
        printBoxLine(content);
    }

    private void printBoxLine(final String text) {
        final List<String> wrapped = wrap(text, BOX_CONTENT_WIDTH);

        for (final String line : wrapped) {
            System.out.printf("| %-" + BOX_CONTENT_WIDTH + "s |%n", line);
        }
    }

    private List<String> wrap(final String text,
                              final int maxWidth) {
        final List<String> lines = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            lines.add("");
            return lines;
        }

        String remaining = text;

        while (remaining.length() > maxWidth) {
            int split = remaining.lastIndexOf(' ', maxWidth);

            if (split <= 0) {
                split = maxWidth;
            }

            lines.add(remaining.substring(0, split).stripTrailing());
            remaining = remaining.substring(split).stripLeading();
        }

        lines.add(remaining);

        return lines;
    }

    @Override
    public String headline() {
        return "Create Flight Plan";
    }

    private static final class DraftInput {
        private FlightRouteDTO selectedRoute;
        private String aircraftRegistration;
        private LocalDateTime plannedDeparture;
        private LocalDateTime plannedArrival;
        private double fuelAmount;
        private FuelUnit fuelUnit;
    }

    private enum EditAction {
        CHANGE_ROUTE,
        CHANGE_AIRCRAFT,
        CHANGE_DEPARTURE,
        USE_SUGGESTED_DEPARTURE,
        CHANGE_ARRIVAL,
        CHANGE_FUEL,
        CONTINUE_ANYWAY,
        CANCEL
    }

    private enum IssueCode {
        ROUTE_SCHEDULE_MISMATCH,
        UNREALISTIC_DURATION,
        HIGH_FUEL
    }

    private static final class PreviewIssue {
        private final IssueCode code;
        private final String title;
        private final List<String> details;

        private PreviewIssue(final IssueCode code,
                             final String title,
                             final List<String> details) {
            this.code = code;
            this.title = title;
            this.details = details;
        }
    }

    private static final class ValidationPreview {
        private final List<PreviewIssue> blockingIssues = new ArrayList<>();
        private final List<PreviewIssue> warnings = new ArrayList<>();
        private LocalDateTime suggestedDeparture;

        private void addBlockingIssue(final IssueCode code,
                                      final String title,
                                      final List<String> details) {
            blockingIssues.add(new PreviewIssue(code, title, details));
        }

        private void addWarning(final IssueCode code,
                                final String title,
                                final List<String> details) {
            warnings.add(new PreviewIssue(code, title, details));
        }

        private boolean hasBlockingIssues() {
            return !blockingIssues.isEmpty();
        }

        private boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        private boolean isReady() {
            return blockingIssues.isEmpty() && warnings.isEmpty();
        }

        private Set<IssueCode> issueCodes() {
            final Set<IssueCode> codes = new LinkedHashSet<>();

            for (final PreviewIssue issue : blockingIssues) {
                codes.add(issue.code);
            }

            for (final PreviewIssue warning : warnings) {
                codes.add(warning.code);
            }

            return codes;
        }
    }
}