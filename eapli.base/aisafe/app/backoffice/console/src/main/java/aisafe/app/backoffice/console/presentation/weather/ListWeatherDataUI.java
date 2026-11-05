package aisafe.app.backoffice.console.presentation.weather;

import aisafe.persistence.PersistenceContext;
import aisafe.weather.application.ListWeatherDataController;
import aisafe.weather.domain.WeatherData;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("squid:S106")
public class ListWeatherDataUI extends AbstractUI {

    private static final String RETURN = "0";
    private static final int PAGE_SIZE = 8;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String SEPARATOR =
            "────────────────────────────────────────────────────────────────────────────────────────────────────";

    private final ListWeatherDataController controller = new ListWeatherDataController(
            AuthzRegistry.authorizationService(),
            PersistenceContext.repositories().weatherData()
    );

    @Override
    protected boolean doShow() {
        showIntro();

        final List<WeatherData> weatherData = chooseWeatherDataScope();

        if (weatherData == null) {
            return false;
        }

        if (weatherData.isEmpty()) {
            System.out.println();
            System.out.println("No weather data found.");
            return false;
        }

        browseWeatherData(weatherData);

        return false;
    }

    private void showIntro() {
        System.out.println();
        System.out.println("Consult Weather Data");
        System.out.println("====================");
        System.out.println("This operation lists weather data registered in the system.");
        System.out.println("You can browse all records or filter by air control area and date.");
        System.out.println();
        System.out.println("You can type 0 at any input step to return.");
        System.out.println();
    }

    private List<WeatherData> chooseWeatherDataScope() {
        while (true) {
            System.out.println("Weather Data Search");
            System.out.println("────────────────────────────────────────");
            System.out.println("  1. List all weather data");
            System.out.println("  2. Filter by air control area and date");
            System.out.println("  0. Return");
            System.out.println();

            final String option = Console.readLine("Choose option:").trim();

            switch (option) {
                case "1":
                    return toList(controller.allWeatherData());

                case "2":
                    return filterByAreaAndDate();

                case "0":
                    return null;

                default:
                    System.out.println("  Invalid option.");
                    System.out.println();
            }
        }
    }

    private List<WeatherData> filterByAreaAndDate() {
        final String areaCode = readAreaCode();

        if (areaCode == null) {
            return null;
        }

        final LocalDate day = readDate();

        if (day == null) {
            return null;
        }

        return toList(controller.filteredWeatherData(areaCode, day));
    }

    private void browseWeatherData(final List<WeatherData> list) {
        int page = 0;
        final int totalPages = (int) Math.ceil((double) list.size() / PAGE_SIZE);

        while (true) {
            printWeatherDataPage(list, page, totalPages);

            final String input = Console.readLine("Option:").trim().toLowerCase(Locale.ROOT);

            if (input.equals(RETURN)) {
                return;
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

                if (index >= 0 && index < list.size()) {
                    showWeatherDataDetails(list.get(index), option);
                    Console.readLine("Press ENTER to continue...");
                    continue;
                }

                System.out.printf("  Invalid option. Choose a number between 1 and %d.%n",
                        list.size());

            } catch (final NumberFormatException e) {
                System.out.println("  Invalid option. Please insert a valid number, n, p or 0.");
            }
        }
    }

    private void printWeatherDataPage(final List<WeatherData> list,
                                      final int page,
                                      final int totalPages) {
        final int from = page * PAGE_SIZE;
        final int to = Math.min(from + PAGE_SIZE, list.size());

        System.out.printf("%nWeather Data — Page %d of %d%n", page + 1, totalPages);
        System.out.println("Total records: " + list.size());
        System.out.println(SEPARATOR);
        System.out.printf(" %-3s %-10s %-7s %-10s %-13s %-13s %-12s %s%n",
                "#", "Weather ID", "Area", "Date", "Time", "Altitude", "Wind", "Direction");
        System.out.println(SEPARATOR);

        for (int i = from; i < to; i++) {
            final WeatherData wd = list.get(i);

            System.out.printf(" %-3d %-10s %-7s %-10s %-13s %-13s %-12s %s%n",
                    i + 1,
                    shortId(wd.identity().toString()),
                    wd.airControlAreaCode(),
                    wd.day(),
                    wd.startTime() + "-" + wd.endTime(),
                    wd.minAltitude() + "-" + wd.maxAltitude() + " ft",
                    formatKnots(wd.windSpeed()),
                    formatDegrees(wd.windDirection()));
        }

        System.out.println(SEPARATOR);
        System.out.println();
        System.out.println("Options:");
        System.out.println("  number - View weather data details");

        if (page > 0) {
            System.out.println("  p      - Previous page");
        }

        if (page < totalPages - 1) {
            System.out.println("  n      - Next page");
        }

        System.out.println("  0      - Return");
        System.out.println();
    }

    private void showWeatherDataDetails(final WeatherData wd,
                                        final int number) {
        System.out.println();
        System.out.println("Weather Data Details");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Number      : " + number);
        System.out.println(" Weather ID  : " + wd.identity());
        System.out.println(" Area code   : " + wd.airControlAreaCode());
        System.out.println(" Date        : " + wd.day());
        System.out.println(" Time window : " + wd.startTime() + " - " + wd.endTime());
        System.out.println(" Altitude    : " + wd.minAltitude() + " - " + wd.maxAltitude() + " ft");
        System.out.println(" Wind        : " + formatKnots(wd.windSpeed())
                + " at " + formatDegrees(wd.windDirection()));
        System.out.println();
        System.out.println("Bounding Box");
        System.out.println("────────────────────────────────────────");
        System.out.printf(Locale.US, " Start       : %.4f, %.4f%n",
                wd.boundingBox().start().latitude(),
                wd.boundingBox().start().longitude());
        System.out.printf(Locale.US, " End         : %.4f, %.4f%n",
                wd.boundingBox().end().latitude(),
                wd.boundingBox().end().longitude());
        System.out.println("────────────────────────────────────────");
        System.out.println();
    }

    private String readAreaCode() {
        while (true) {
            final String input = Console.readLine("Air control area code, e.g. LPPC (0 to return):")
                    .trim()
                    .toUpperCase(Locale.ROOT);

            if (input.equals(RETURN)) {
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Air control area code cannot be empty.");
                continue;
            }

            if (!input.matches("[A-Z0-9_-]+")) {
                System.out.println("  Invalid area code. Use letters, numbers, '-' or '_'.");
                continue;
            }

            return input;
        }
    }

    private LocalDate readDate() {
        while (true) {
            final String input = Console.readLine("Date (yyyy-MM-dd) (0 to return):").trim();

            if (input.equals(RETURN)) {
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

    private List<WeatherData> toList(final Iterable<WeatherData> iterable) {
        final List<WeatherData> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }

    private String shortId(final String id) {
        if (id == null || id.length() <= 8) {
            return id;
        }

        return id.substring(0, 8);
    }

    private String formatKnots(final double value) {
        return String.format(Locale.US, "%.2f kts", value);
    }

    private String formatDegrees(final double value) {
        return String.format(Locale.US, "%.2f°", value);
    }

    @Override
    public String headline() {
        return "Consult Weather Data";
    }
}