package aisafe.app.backoffice.console.presentation.weather;

import aisafe.aircontrolarea.domain.AirControlArea;
import aisafe.persistence.PersistenceContext;
import aisafe.weather.application.RegisterWeatherDataController;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("squid:S106")
public class RegisterWeatherDataUI extends AbstractUI {

    private static final String CANCEL = "0";
    private static final int PAGE_SIZE = 8;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final String SEPARATOR =
            "────────────────────────────────────────────────────────────────────────────────────────────";

    private final RegisterWeatherDataController controller = new RegisterWeatherDataController(
            PersistenceContext.repositories().airControlAreas(),
            PersistenceContext.repositories().weatherData()
    );

    @Override
    protected boolean doShow() {
        try {
            showIntro();

            final AirControlArea selectedArea = chooseAirControlArea();

            if (selectedArea == null) {
                return false;
            }

            showSelectedArea(selectedArea);

            final LocalDate day = readDate("Date");

            if (day == null) {
                return false;
            }

            final LocalTime startTime = readTime("Start time");

            if (startTime == null) {
                return false;
            }

            final LocalTime endTime = readEndTime(startTime);

            if (endTime == null) {
                return false;
            }

            final Double latStart = readDoubleInRange(
                    "Bounding box start latitude",
                    -90.0,
                    90.0
            );

            if (latStart == null) {
                return false;
            }

            final Double lonStart = readDoubleInRange(
                    "Bounding box start longitude",
                    -180.0,
                    180.0
            );

            if (lonStart == null) {
                return false;
            }

            final Double latEnd = readDoubleInRange(
                    "Bounding box end latitude",
                    -90.0,
                    90.0
            );

            if (latEnd == null) {
                return false;
            }

            final Double lonEnd = readDoubleInRange(
                    "Bounding box end longitude",
                    -180.0,
                    180.0
            );

            if (lonEnd == null) {
                return false;
            }

            final Integer minAltitude = readIntegerAtLeast(
                    "Minimum altitude in feet",
                    0
            );

            if (minAltitude == null) {
                return false;
            }

            final Integer maxAltitude = readMaximumAltitude(minAltitude);

            if (maxAltitude == null) {
                return false;
            }

            final Double windSpeed = readDoubleAtLeast(
                    "Wind speed in knots",
                    0.0
            );

            if (windSpeed == null) {
                return false;
            }

            final Double windDirection = readDoubleInRange(
                    "Wind direction in degrees",
                    0.0,
                    360.0
            );

            if (windDirection == null) {
                return false;
            }

            final String areaCode = selectedArea.identity().toString();

            showSummary(
                    areaCode,
                    day,
                    startTime,
                    endTime,
                    latStart,
                    lonStart,
                    latEnd,
                    lonEnd,
                    minAltitude,
                    maxAltitude,
                    windSpeed,
                    windDirection
            );

            if (!confirm("Confirm weather data registration? (y/n):")) {
                System.out.println();
                System.out.println("Weather data registration cancelled.");
                return false;
            }

            controller.registerWeatherData(
                    areaCode,
                    day,
                    startTime,
                    endTime,
                    latStart,
                    lonStart,
                    latEnd,
                    lonEnd,
                    minAltitude,
                    maxAltitude,
                    windSpeed,
                    windDirection
            );

            showSuccess(
                    areaCode,
                    day,
                    startTime,
                    endTime,
                    minAltitude,
                    maxAltitude,
                    windSpeed,
                    windDirection
            );

            return false;

        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println();
            System.out.println("Could not register weather data.");
            System.out.println("Reason: " + e.getMessage());
            return false;
        }
    }

    private void showIntro() {
        System.out.println();
        System.out.println("Register Weather Data");
        System.out.println("=====================");
        System.out.println("This operation registers weather data for an air control area.");
        System.out.println("Weather data can later be associated with flight plans and used in validation.");
        System.out.println();
        System.out.println("You can type 0 at any input step to cancel.");
        System.out.println();
    }

    private AirControlArea chooseAirControlArea() {
        final List<AirControlArea> areaList = new ArrayList<>();
        controller.getAirControlAreas().forEach(areaList::add);

        if (areaList.isEmpty()) {
            System.out.println();
            System.out.println("No air control areas registered in the system.");
            System.out.println("Please register air control areas before registering weather data.");
            return null;
        }

        int page = 0;
        final int totalPages = (int) Math.ceil((double) areaList.size() / PAGE_SIZE);

        while (true) {
            printAirControlAreaPage(areaList, page, totalPages);

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

                if (index >= 0 && index < areaList.size()) {
                    return areaList.get(index);
                }

                System.out.printf("  Invalid option. Choose a number between 1 and %d.%n",
                        areaList.size());

            } catch (final NumberFormatException e) {
                System.out.println("  Invalid option. Please insert a valid number, n, p or 0.");
            }
        }
    }

    private void printAirControlAreaPage(final List<AirControlArea> areaList,
                                         final int page,
                                         final int totalPages) {
        final int from = page * PAGE_SIZE;
        final int to = Math.min(from + PAGE_SIZE, areaList.size());

        System.out.printf("%nAvailable Air Control Areas — Page %d of %d%n",
                page + 1,
                totalPages);
        System.out.println(SEPARATOR);
        System.out.printf(" %-3s %-8s %-30s%n",
                "#", "Code", "Name");
        System.out.println(SEPARATOR);

        for (int i = from; i < to; i++) {
            final AirControlArea area = areaList.get(i);

            System.out.printf(" %-3d %-8s %-30s%n",
                    i + 1,
                    area.identity(),
                    area.name());
        }

        System.out.println(SEPARATOR);
        System.out.println();
        System.out.println("Options:");
        System.out.println("  number - Select air control area");

        if (page > 0) {
            System.out.println("  p      - Previous page");
        }

        if (page < totalPages - 1) {
            System.out.println("  n      - Next page");
        }

        System.out.println("  0      - Cancel");
        System.out.println();
    }

    private void showSelectedArea(final AirControlArea selectedArea) {
        System.out.println();
        System.out.println("Selected Air Control Area");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Code : " + selectedArea.identity());
        System.out.println(" Name : " + selectedArea.name());
        System.out.println("────────────────────────────────────────");
        System.out.println();
    }

    private LocalDate readDate(final String label) {
        while (true) {
            final String input = Console.readLine(label + " (yyyy-MM-dd) (0 to cancel):").trim();

            if (input.equals(CANCEL)) {
                cancel();
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

    private LocalTime readTime(final String label) {
        while (true) {
            final String input = Console.readLine(label + " (HH:mm) (0 to cancel):").trim();

            if (input.equals(CANCEL)) {
                cancel();
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Time cannot be empty.");
                continue;
            }

            try {
                return LocalTime.parse(input, TIME_FORMATTER);
            } catch (final DateTimeParseException e) {
                System.out.println("  Invalid time format. Example: 14:30");
            }
        }
    }

    private LocalTime readEndTime(final LocalTime startTime) {
        while (true) {
            final LocalTime endTime = readTime("End time");

            if (endTime == null) {
                return null;
            }

            if (endTime.isAfter(startTime)) {
                return endTime;
            }

            System.out.println("  End time must be after start time.");
        }
    }

    private Double readDoubleInRange(final String label,
                                     final double min,
                                     final double max) {
        while (true) {
            final String input = Console.readLine(
                    label + " (" + formatPlain(min) + " to " + formatPlain(max) + ") (0 to cancel):"
            ).trim();

            if (input.equals(CANCEL)) {
                cancel();
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Value cannot be empty.");
                continue;
            }

            try {
                final double value = Double.parseDouble(input.replace(",", "."));

                if (value >= min && value <= max) {
                    return value;
                }

                System.out.printf(Locale.US,
                        "  Value must be between %.2f and %.2f.%n",
                        min,
                        max);

            } catch (final NumberFormatException e) {
                System.out.println("  Please insert a valid number.");
            }
        }
    }

    private Double readDoubleAtLeast(final String label,
                                     final double min) {
        while (true) {
            final String input = Console.readLine(
                    label + " (>= " + formatPlain(min) + ") (0 to cancel):"
            ).trim();

            if (input.equals(CANCEL)) {
                cancel();
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Value cannot be empty.");
                continue;
            }

            try {
                final double value = Double.parseDouble(input.replace(",", "."));

                if (value >= min) {
                    return value;
                }

                System.out.printf(Locale.US,
                        "  Value must be greater than or equal to %.2f.%n",
                        min);

            } catch (final NumberFormatException e) {
                System.out.println("  Please insert a valid number.");
            }
        }
    }

    private Integer readIntegerAtLeast(final String label,
                                       final int min) {
        while (true) {
            final String input = Console.readLine(
                    label + " (>= " + min + ") (0 to cancel):"
            ).trim();

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

                if (value >= min) {
                    return value;
                }

                System.out.println("  Value must be greater than or equal to " + min + ".");

            } catch (final NumberFormatException e) {
                System.out.println("  Please insert a valid integer.");
            }
        }
    }

    private Integer readMaximumAltitude(final int minAltitude) {
        while (true) {
            final Integer maxAltitude = readIntegerAtLeast("Maximum altitude in feet", minAltitude);

            if (maxAltitude == null) {
                return null;
            }

            return maxAltitude;
        }
    }

    private void showSummary(final String areaCode,
                             final LocalDate day,
                             final LocalTime startTime,
                             final LocalTime endTime,
                             final double latStart,
                             final double lonStart,
                             final double latEnd,
                             final double lonEnd,
                             final int minAltitude,
                             final int maxAltitude,
                             final double windSpeed,
                             final double windDirection) {
        System.out.println();
        System.out.println("Weather Data Registration Summary");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Area code    : " + areaCode);
        System.out.println(" Date         : " + day.format(DATE_FORMATTER));
        System.out.println(" Time window  : " + startTime.format(TIME_FORMATTER)
                + " - " + endTime.format(TIME_FORMATTER));
        System.out.println(" Altitude     : " + minAltitude + " - " + maxAltitude + " ft");
        System.out.println(" Wind         : " + formatKnots(windSpeed)
                + " at " + formatDegrees(windDirection));
        System.out.println();
        System.out.println("Bounding Box");
        System.out.println("────────────────────────────────────────");
        System.out.printf(Locale.US, " Start        : %.4f, %.4f%n", latStart, lonStart);
        System.out.printf(Locale.US, " End          : %.4f, %.4f%n", latEnd, lonEnd);
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

    private void showSuccess(final String areaCode,
                             final LocalDate day,
                             final LocalTime startTime,
                             final LocalTime endTime,
                             final int minAltitude,
                             final int maxAltitude,
                             final double windSpeed,
                             final double windDirection) {
        System.out.println();
        System.out.println("Weather data registered successfully.");
        System.out.println("────────────────────────────────────────");
        System.out.println(" Area code   : " + areaCode);
        System.out.println(" Date        : " + day.format(DATE_FORMATTER));
        System.out.println(" Time window : " + startTime.format(TIME_FORMATTER)
                + " - " + endTime.format(TIME_FORMATTER));
        System.out.println(" Altitude    : " + minAltitude + " - " + maxAltitude + " ft");
        System.out.println(" Wind        : " + formatKnots(windSpeed)
                + " at " + formatDegrees(windDirection));
        System.out.println("────────────────────────────────────────");
        System.out.println();
    }

    private void cancel() {
        System.out.println();
        System.out.println("Weather data registration cancelled.");
    }

    private String formatKnots(final double value) {
        return String.format(Locale.US, "%.2f kts", value);
    }

    private String formatDegrees(final double value) {
        return String.format(Locale.US, "%.2f°", value);
    }

    private String formatPlain(final double value) {
        if (value == Math.rint(value)) {
            return String.format(Locale.US, "%.0f", value);
        }

        return String.format(Locale.US, "%.2f", value);
    }

    @Override
    public String headline() {
        return "Register Weather Data";
    }
}