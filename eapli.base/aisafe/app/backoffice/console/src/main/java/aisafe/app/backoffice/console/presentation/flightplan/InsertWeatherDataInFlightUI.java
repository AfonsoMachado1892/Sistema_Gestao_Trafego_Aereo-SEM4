package aisafe.app.backoffice.console.presentation.flightplan;

import aisafe.flightplan.application.InsertWeatherDataInFlightController;
import aisafe.flightplan.domain.FlightPlan;
import aisafe.flightplan.domain.FlightPlanStatus;
import aisafe.persistence.PersistenceContext;
import aisafe.pilot.domain.Pilot;
import aisafe.weather.domain.WeatherData;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@SuppressWarnings("squid:S106")
public class InsertWeatherDataInFlightUI extends AbstractUI {

    private static final int PAGE_SIZE = 8;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final InsertWeatherDataInFlightController controller =
            new InsertWeatherDataInFlightController(
                    PersistenceContext.repositories().flightPlans(),
                    PersistenceContext.repositories().weatherData(),
                    PersistenceContext.repositories().pilots(),
                    PersistenceContext.repositories().flightRoutes(),
                    PersistenceContext.repositories().airports()
            );

    @Override
    protected boolean doShow() {
        showIntro();

        final Pilot pilot;

        try {
            pilot = controller.getAuthenticatedPilot();
        } catch (final IllegalStateException e) {
            System.out.println();
            System.out.println("Could not identify authenticated pilot.");
            System.out.println("Reason: " + e.getMessage());
            return false;
        }

        final FlightPlan selectedFlightPlan = chooseEligibleFlightPlan(pilot);

        if (selectedFlightPlan == null) {
            return false;
        }

        final WeatherData currentWeatherData = currentWeatherDataFor(selectedFlightPlan);

        final InsertWeatherDataInFlightController.WeatherSearchSuggestion suggestion;

        try {
            suggestion = controller.suggestWeatherSearchFor(selectedFlightPlan);
        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println();
            System.out.println("Could not determine suggested weather search.");
            System.out.println("Reason: " + e.getMessage());
            System.out.println();
            System.out.println("This operation cannot continue because the system could not infer");
            System.out.println("the destination air control area for this flight plan.");
            return false;
        }

        showSelectedFlightPlan(selectedFlightPlan, currentWeatherData, suggestion);

        if (currentWeatherData != null) {
            if (!confirm("Replace current weather data? (y/n):")) {
                System.out.println();
                System.out.println("Current weather data was kept unchanged.");
                return false;
            }
        }

        final WeatherData selectedWeatherData = chooseWeatherDataFor(
                selectedFlightPlan,
                currentWeatherData,
                suggestion
        );

        if (selectedWeatherData == null) {
            return false;
        }

        showConfirmationSummary(
                selectedFlightPlan,
                currentWeatherData,
                selectedWeatherData
        );

        if (!confirm("Confirm weather data association? (y/n):")) {
            System.out.println();
            System.out.println("Operation cancelled.");
            return false;
        }

        try {
            final boolean previousTestWillBeVoided = willVoidPreviousTest(selectedFlightPlan);

            controller.insertWeatherData(
                    selectedFlightPlan.identity(),
                    selectedWeatherData.identity()
            );

            showSuccess(selectedFlightPlan, selectedWeatherData, previousTestWillBeVoided);
            return false;

        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println();
            System.out.println("Could not insert weather data.");
            System.out.println("Reason: " + e.getMessage());
            return false;
        }
    }

    private void showIntro() {
        System.out.println();
        System.out.println("Assign or Replace Weather Data");
        System.out.println("==============================");
        System.out.println("This operation associates existing weather data with one of your flight plans.");
        System.out.println("Weather data is used when testing/validating the flight plan.");
        System.out.println();
        System.out.println("The system will automatically search weather data for the destination");
        System.out.println("air control area and the flight departure date.");
        System.out.println();
        System.out.println("If the selected flight plan is TESTED or VALIDATED, the previous test result");
        System.out.println("will be voided and the flight plan will return to DRAFT.");
        System.out.println();
    }

    private FlightPlan chooseEligibleFlightPlan(final Pilot pilot) {
        final List<FlightPlan> eligibleFlightPlans = new ArrayList<>();

        controller.getMyFlightPlans(pilot.identity()).forEach(fp -> {
            if (isEligibleForWeatherInsertion(fp)) {
                eligibleFlightPlans.add(fp);
            }
        });

        if (eligibleFlightPlans.isEmpty()) {
            System.out.println();
            System.out.println("No eligible flight plans found.");
            System.out.println("Weather data can only be assigned to DRAFT, TESTED or VALIDATED flight plans.");
            return null;
        }

        int page = 0;
        final int totalPages = (int) Math.ceil((double) eligibleFlightPlans.size() / PAGE_SIZE);

        while (true) {
            printFlightPlanPage(eligibleFlightPlans, page, totalPages);

            System.out.println();
            System.out.println("Options:");
            System.out.println("  number - Select flight plan");
            if (page > 0) {
                System.out.println("  p      - Previous page");
            }
            if (page < totalPages - 1) {
                System.out.println("  n      - Next page");
            }
            System.out.println("  0      - Cancel");

            final String input = Console.readLine("Option:").trim().toLowerCase(Locale.ROOT);

            if (input.equals("0")) {
                System.out.println("Operation cancelled.");
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

                if (index >= 0 && index < eligibleFlightPlans.size()) {
                    return eligibleFlightPlans.get(index);
                }

                System.out.printf("  Invalid option. Choose a number between 1 and %d.%n",
                        eligibleFlightPlans.size());

            } catch (final NumberFormatException e) {
                System.out.println("  Invalid option.");
            }
        }
    }

    private boolean isEligibleForWeatherInsertion(final FlightPlan flightPlan) {
        return flightPlan.status() == FlightPlanStatus.DRAFT
                || flightPlan.status() == FlightPlanStatus.TESTED
                || flightPlan.status() == FlightPlanStatus.VALIDATED;
    }

    private void printFlightPlanPage(final List<FlightPlan> flightPlans,
                                     final int page,
                                     final int totalPages) {
        final int from = page * PAGE_SIZE;
        final int to = Math.min(from + PAGE_SIZE, flightPlans.size());

        System.out.printf("%nEligible Flight Plans — Page %d of %d%n", page + 1, totalPages);
        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────────────────");
        System.out.printf(" %-3s %-11s %-8s %-9s %-10s %-16s %-16s %s%n",
                "#", "Plan ID", "Route", "Aircraft", "Status", "Departure", "Arrival", "Weather");
        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────────────────");

        for (int i = from; i < to; i++) {
            final FlightPlan fp = flightPlans.get(i);

            System.out.printf(" %-3d %-11s %-8s %-9s %-10s %-16s %-16s %s%n",
                    i + 1,
                    shortId(fp.identity().toString()),
                    fp.routeCode(),
                    fp.aircraftRegistration().code(),
                    fp.status(),
                    formatDateTime(fp.plannedDepartureDateTime()),
                    formatDateTime(fp.plannedArrivalDateTime()),
                    fp.weatherDataID() == null ? "none" : "assigned");
        }

        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────────────────");
    }

    private WeatherData currentWeatherDataFor(final FlightPlan flightPlan) {
        if (flightPlan.weatherDataID() == null) {
            return null;
        }

        final Optional<WeatherData> weatherData =
                controller.getWeatherDataById(flightPlan.weatherDataID());

        return weatherData.orElse(null);
    }

    private void showSelectedFlightPlan(
            final FlightPlan flightPlan,
            final WeatherData currentWeatherData,
            final InsertWeatherDataInFlightController.WeatherSearchSuggestion suggestion) {

        System.out.println();
        System.out.println("Selected Flight Plan");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Full ID     : " + flightPlan.identity());
        System.out.println(" Route       : " + flightPlan.routeCode());
        System.out.println(" Path        : " + suggestion.departureAirport() + " -> " + suggestion.arrivalAirport());
        System.out.println(" Aircraft    : " + flightPlan.aircraftRegistration().code());
        System.out.println(" Departure   : " + formatDateTime(flightPlan.plannedDepartureDateTime()));
        System.out.println(" Arrival     : " + formatDateTime(flightPlan.plannedArrivalDateTime()));
        System.out.println(" Fuel        : " + flightPlan.fuelQuantity());
        System.out.println(" Status      : " + flightPlan.status());
        System.out.println(" Weather     : " + (flightPlan.weatherDataID() == null
                ? "not assigned"
                : "assigned"));
        System.out.println("────────────────────────────────────────");

        System.out.println();
        System.out.println("Suggested Weather Search");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Arrival airport : " + suggestion.arrivalAirport()
                + " - " + suggestion.arrivalAirportName());
        System.out.println(" Air control area: " + suggestion.areaCode());
        System.out.println(" Flight date     : " + suggestion.flightDate());
        System.out.println("────────────────────────────────────────");

        if (currentWeatherData != null) {
            showCurrentWeatherData(currentWeatherData);
        } else {
            System.out.println();
            System.out.println("No weather data is currently assigned to this flight plan.");
        }

        if (willVoidPreviousTest(flightPlan)) {
            System.out.println();
            System.out.println("Warning");
            System.out.println("────────────────────────────────────────");
            System.out.println("This flight plan already has a validation/test result.");
            System.out.println("Changing weather data will void the previous result");
            System.out.println("and return the flight plan status to DRAFT.");
            System.out.println("────────────────────────────────────────");
        }

        System.out.println();
    }

    private void showCurrentWeatherData(final WeatherData weatherData) {
        System.out.println();
        System.out.println("Current Weather Data");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Weather ID : " + shortId(weatherData.identity().toString()));
        System.out.println(" Area       : " + weatherData.airControlAreaCode());
        System.out.println(" Date       : " + weatherData.day());
        System.out.println(" Time       : " + weatherData.startTime() + " - " + weatherData.endTime());
        System.out.println(" Altitude   : " + weatherData.minAltitude() + " - "
                + weatherData.maxAltitude() + " ft");
        System.out.println(" Wind       : " + formatKnots(weatherData.windSpeed())
                + " at " + formatDegrees(weatherData.windDirection()));
        System.out.println("────────────────────────────────────────");
    }

    private WeatherData chooseWeatherDataFor(
            final FlightPlan flightPlan,
            final WeatherData currentWeatherData,
            final InsertWeatherDataInFlightController.WeatherSearchSuggestion suggestion) {

        String searchArea = suggestion.areaCode();
        LocalDate searchDate = suggestion.flightDate();

        while (true) {
            System.out.println();
            System.out.println("Searching weather data...");
            System.out.println("────────────────────────────────────────");
            System.out.println(" Search area : " + searchArea);
            System.out.println(" Search date : " + searchDate.format(DATE_FORMATTER));
            System.out.println(" Flight date : " + flightPlan.plannedDepartureDateTime().toLocalDate());
            System.out.println("────────────────────────────────────────");

            final WeatherData selected = searchAndChooseWeatherData(
                    searchArea,
                    searchDate,
                    currentWeatherData
            );

            if (selected != null) {
                return selected;
            }

            final SearchAction action = readWeatherSearchAction(searchArea, searchDate);

            switch (action) {
                case SEARCH_AGAIN:
                    break;

                case CHANGE_AREA:
                    final String newArea = readAreaCode("Air control area code, e.g. ACA");

                    if (newArea == null) {
                        return null;
                    }

                    searchArea = newArea;
                    break;

                case CHANGE_DATE:
                    final LocalDate newDate = readDate("Weather date");

                    if (newDate == null) {
                        return null;
                    }

                    searchDate = newDate;
                    break;

                case CANCEL:
                    System.out.println("Operation cancelled.");
                    return null;

                default:
                    throw new IllegalStateException("Unexpected action: " + action);
            }
        }
    }

    private WeatherData searchAndChooseWeatherData(final String areaCode,
                                                   final LocalDate date,
                                                   final WeatherData currentWeatherData) {
        final List<WeatherData> weatherDataList = new ArrayList<>();

        controller.getWeatherDataByAreaAndDate(areaCode, date).forEach(weatherDataList::add);

        if (weatherDataList.isEmpty()) {
            System.out.println();
            System.out.println("No weather data found.");
            System.out.println("────────────────────────────────────────");
            System.out.println(" Search area : " + areaCode);
            System.out.println(" Search date : " + date.format(DATE_FORMATTER));
            System.out.println("────────────────────────────────────────");
            System.out.println("You can change the area or date and search again.");
            return null;
        }

        int page = 0;
        final int totalPages = (int) Math.ceil((double) weatherDataList.size() / PAGE_SIZE);

        while (true) {
            printWeatherDataPage(weatherDataList, page, totalPages, currentWeatherData);

            System.out.println();
            System.out.println("Options:");
            System.out.println("  number - Select weather data");
            if (page > 0) {
                System.out.println("  p      - Previous page");
            }
            if (page < totalPages - 1) {
                System.out.println("  n      - Next page");
            }
            System.out.println("  0      - Back to search options");

            final String input = Console.readLine("Option:").trim().toLowerCase(Locale.ROOT);

            if (input.equals("0")) {
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

                if (index >= 0 && index < weatherDataList.size()) {
                    return weatherDataList.get(index);
                }

                System.out.printf("  Invalid option. Choose a number between 1 and %d.%n",
                        weatherDataList.size());

            } catch (final NumberFormatException e) {
                System.out.println("  Invalid option.");
            }
        }
    }

    private void printWeatherDataPage(final List<WeatherData> weatherDataList,
                                      final int page,
                                      final int totalPages,
                                      final WeatherData currentWeatherData) {
        final int from = page * PAGE_SIZE;
        final int to = Math.min(from + PAGE_SIZE, weatherDataList.size());

        System.out.printf("%nAvailable Weather Data — Page %d of %d%n", page + 1, totalPages);
        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────");
        System.out.printf(" %-3s %-11s %-8s %-10s %-13s %-13s %-12s %s%n",
                "#", "Weather ID", "Area", "Date", "Time", "Altitude", "Wind", "Direction");
        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────");

        for (int i = from; i < to; i++) {
            final WeatherData wd = weatherDataList.get(i);
            final String marker = isCurrentWeather(wd, currentWeatherData) ? "*" : "";

            System.out.printf(" %-3s %-11s %-8s %-10s %-13s %-13s %-12s %s%n",
                    (i + 1) + marker,
                    shortId(wd.identity().toString()),
                    wd.airControlAreaCode(),
                    wd.day(),
                    wd.startTime() + "-" + wd.endTime(),
                    wd.minAltitude() + "-" + wd.maxAltitude() + " ft",
                    formatKnots(wd.windSpeed()),
                    formatDegrees(wd.windDirection()));
        }

        System.out.println("────────────────────────────────────────────────────────────────────────────────────────────");

        if (currentWeatherData != null) {
            System.out.println("* currently assigned weather data");
        }
    }

    private boolean isCurrentWeather(final WeatherData candidate,
                                     final WeatherData currentWeatherData) {
        return currentWeatherData != null
                && candidate.identity().equals(currentWeatherData.identity());
    }

    private SearchAction readWeatherSearchAction(final String areaCode,
                                                 final LocalDate searchDate) {
        while (true) {
            System.out.println();
            System.out.println("Weather Search Options");
            System.out.println("────────────────────────────────────────");
            System.out.println(" Search area : " + areaCode);
            System.out.println(" Search date : " + searchDate.format(DATE_FORMATTER));
            System.out.println("────────────────────────────────────────");
            System.out.println("  1. Search again");
            System.out.println("  2. Change search area manually");
            System.out.println("  3. Change search date");
            System.out.println("  0. Cancel");
            System.out.println();

            final String option = Console.readLine("Choose option:").trim();

            switch (option) {
                case "1":
                    return SearchAction.SEARCH_AGAIN;
                case "2":
                    return SearchAction.CHANGE_AREA;
                case "3":
                    return SearchAction.CHANGE_DATE;
                case "0":
                    return SearchAction.CANCEL;
                default:
                    System.out.println("  Invalid option.");
            }
        }
    }

    private void showConfirmationSummary(final FlightPlan flightPlan,
                                         final WeatherData previousWeatherData,
                                         final WeatherData newWeatherData) {
        System.out.println();
        System.out.println("Confirm Weather Association");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Flight plan      : " + shortId(flightPlan.identity().toString())
                + " (" + flightPlan.routeCode() + ")");
        System.out.println(" Current status   : " + flightPlan.status());

        if (previousWeatherData == null) {
            System.out.println(" Previous weather : none");
        } else {
            System.out.println(" Previous weather : " + shortId(previousWeatherData.identity().toString())
                    + " | " + previousWeatherData.airControlAreaCode()
                    + " | " + previousWeatherData.day());
        }

        System.out.println(" New weather      : " + shortId(newWeatherData.identity().toString())
                + " | " + newWeatherData.airControlAreaCode()
                + " | " + newWeatherData.day());
        System.out.println(" New wind         : " + formatKnots(newWeatherData.windSpeed())
                + " at " + formatDegrees(newWeatherData.windDirection()));

        if (willVoidPreviousTest(flightPlan)) {
            System.out.println(" Previous test    : will be voided");
            System.out.println(" New status       : DRAFT");
        } else {
            System.out.println(" New status       : " + flightPlan.status());
        }

        System.out.println("────────────────────────────────────────");
        System.out.println();
    }

    private void showSuccess(final FlightPlan flightPlan,
                             final WeatherData weatherData,
                             final boolean previousTestVoided) {
        System.out.println();
        System.out.println("Weather data associated successfully.");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Flight plan  : " + shortId(flightPlan.identity().toString()));
        System.out.println(" Weather data : " + shortId(weatherData.identity().toString()));
        System.out.println(" Area         : " + weatherData.airControlAreaCode());
        System.out.println(" Date         : " + weatherData.day());
        System.out.println(" Wind         : " + formatKnots(weatherData.windSpeed())
                + " at " + formatDegrees(weatherData.windDirection()));

        if (previousTestVoided) {
            System.out.println(" Test result  : voided");
            System.out.println(" Status       : DRAFT");
            System.out.println();
            System.out.println("Please test/validate this flight plan again before operational use.");
        } else {
            System.out.println(" Status       : " + flightPlan.status());
        }

        System.out.println("────────────────────────────────────────");
    }

    private boolean willVoidPreviousTest(final FlightPlan flightPlan) {
        return flightPlan.status() == FlightPlanStatus.TESTED
                || flightPlan.status() == FlightPlanStatus.VALIDATED;
    }

    private String readAreaCode(final String label) {
        while (true) {
            final String input = Console.readLine(label + " (0 to cancel):")
                    .trim()
                    .toUpperCase(Locale.ROOT);

            if (input.equals("0")) {
                System.out.println("Operation cancelled.");
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Air control area code cannot be empty.");
                continue;
            }

            if (!input.matches("[A-Z0-9_-]+")) {
                System.out.println("  Invalid area code. Use only letters, numbers, '-' or '_'.");
                System.out.println("  Example: ACA");
                continue;
            }

            return input;
        }
    }

    private LocalDate readDate(final String label) {
        while (true) {
            final String input = Console.readLine(label + " (yyyy-MM-dd) (0 to cancel):").trim();

            if (input.equals("0")) {
                System.out.println("Operation cancelled.");
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Date cannot be empty.");
                continue;
            }

            try {
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (final DateTimeParseException e) {
                System.out.println("  Invalid date format. Example: 2026-06-15");
            }
        }
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

    private String shortId(final String id) {
        if (id == null || id.length() <= 8) {
            return id;
        }

        return id.substring(0, 8);
    }

    private String formatDateTime(final LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    private String formatKnots(final double value) {
        return String.format(Locale.US, "%.2f kts", value);
    }

    private String formatDegrees(final double value) {
        return String.format(Locale.US, "%.2f°", value);
    }

    @Override
    public String headline() {
        return "Insert Weather Data in Flight Plan";
    }

    private enum SearchAction {
        SEARCH_AGAIN,
        CHANGE_AREA,
        CHANGE_DATE,
        CANCEL
    }
}