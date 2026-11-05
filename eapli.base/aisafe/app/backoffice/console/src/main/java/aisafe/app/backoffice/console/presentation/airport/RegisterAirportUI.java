package aisafe.app.backoffice.console.presentation.airport;

import aisafe.aircontrolarea.domain.AirControlArea;
import aisafe.airport.application.RegisterAirportController;
import aisafe.persistence.PersistenceContext;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("squid:S106")
public class RegisterAirportUI extends AbstractUI {

    private static final String CANCEL = "0";
    private static final double CANCEL_DOUBLE = Double.MIN_VALUE;

    private final RegisterAirportController controller =
            new RegisterAirportController(
                    PersistenceContext.repositories().airports(),
                    PersistenceContext.repositories().airControlAreas()
            );

    @Override
    protected boolean doShow() {
        while (true) {
            try {
                final String iataCode = readUniqueIata();
                if (iataCode == null) return false;

                final String icaoCode = readUniqueIcao();
                if (icaoCode == null) return false;

                final String name = readOrCancel("Airport name", "Name cannot be empty");
                if (name == null) return false;

                final String cityName = readOrCancel("City/Town", "City cannot be empty");
                if (cityName == null) return false;

                final String countryIsoCode = readCountryIsoCode();
                if (countryIsoCode == null) return false;

                final String countryName = readOrCancel("Country name", "Country name cannot be empty");
                if (countryName == null) return false;

                final double latitude = readLatitude();
                if (latitude == CANCEL_DOUBLE) return false;

                final double longitude = readLongitude();
                if (longitude == CANCEL_DOUBLE) return false;

                final double elevation = readDoubleOrCancel("Elevation in meters");
                if (elevation == CANCEL_DOUBLE) return false;

                final String airControlAreaCode = selectAirControlArea(latitude, longitude);
                if (airControlAreaCode == null) return false;

                showSummary(
                        iataCode,
                        icaoCode,
                        name,
                        cityName,
                        countryIsoCode,
                        countryName,
                        latitude,
                        longitude,
                        elevation,
                        airControlAreaCode
                );

                if (!confirmRegistration()) {
                    System.out.println("Registration cancelled.");
                    return false;
                }

                try {
                    controller.registerAirport(
                            iataCode,
                            icaoCode,
                            name,
                            cityName,
                            countryIsoCode,
                            countryName,
                            latitude,
                            longitude,
                            elevation,
                            airControlAreaCode
                    );

                    System.out.println("\nAirport registered successfully.");
                    return false;

                } catch (final IllegalArgumentException | IllegalStateException e) {
                    System.out.println();
                    System.out.println("Could not register airport.");
                    System.out.println("Reason: " + e.getMessage());

                    if (!retryAfterError()) {
                        return false;
                    }
                }

            } catch (final IllegalArgumentException | IllegalStateException e) {
                System.out.println();
                System.out.println("Invalid input.");
                System.out.println("Reason: " + e.getMessage());

                if (!retryAfterError()) {
                    return false;
                }
            }
        }
    }

    private String readUniqueIata() {
        while (true) {
            final String input = Console.readLine("Airport IATA code (0 to cancel):")
                    .trim()
                    .toUpperCase();

            if (input.equals(CANCEL)) {
                System.out.println("Registration cancelled.");
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  IATA code cannot be empty.");
                continue;
            }

            if (!input.matches("^[A-Z]{3}$")) {
                System.out.println("  IATA code must have exactly 3 letters, e.g. LIS.");
                continue;
            }

            if (controller.iataCodeExists(input)) {
                System.out.printf(
                        "  IATA code '%s' already exists. Please choose another.%n",
                        input);
                continue;
            }

            return input;
        }
    }

    private String readUniqueIcao() {
        while (true) {
            final String input = Console.readLine("Airport ICAO code (0 to cancel):")
                    .trim()
                    .toUpperCase();

            if (input.equals(CANCEL)) {
                System.out.println("Registration cancelled.");
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  ICAO code cannot be empty.");
                continue;
            }

            if (!input.matches("^[A-Z]{4}$")) {
                System.out.println("  ICAO code must have exactly 4 letters, e.g. LPPT.");
                continue;
            }

            if (controller.icaoCodeExists(input)) {
                System.out.printf(
                        "  ICAO code '%s' already exists. Please choose another.%n",
                        input);
                continue;
            }

            return input;
        }
    }

    private String readOrCancel(final String prompt, final String errorMessage) {
        while (true) {
            final String value = Console.readLine(prompt + " (0 to cancel):").trim();

            if (value.equals(CANCEL)) {
                System.out.println("Registration cancelled.");
                return null;
            }

            if (value.isBlank()) {
                System.out.println("  " + errorMessage + ".");
                continue;
            }

            return value;
        }
    }

    private String readCountryIsoCode() {
        while (true) {
            final String input = Console.readLine("Country ISO code (0 to cancel):")
                    .trim()
                    .toUpperCase();

            if (input.equals(CANCEL)) {
                System.out.println("Registration cancelled.");
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  ISO code cannot be empty.");
                continue;
            }

            if (!input.matches("^[A-Z]{2}$")) {
                System.out.println("  Country ISO code must have exactly 2 letters, e.g. PT.");
                continue;
            }

            return input;
        }
    }

    private double readLatitude() {
        while (true) {
            final double value = readDoubleOrCancel("Latitude");
            if (value == CANCEL_DOUBLE) {
                return CANCEL_DOUBLE;
            }

            if (value < -90 || value > 90) {
                System.out.println("  Latitude must be between -90 and 90.");
                continue;
            }

            return value;
        }
    }

    private double readLongitude() {
        while (true) {
            final double value = readDoubleOrCancel("Longitude");
            if (value == CANCEL_DOUBLE) {
                return CANCEL_DOUBLE;
            }

            if (value < -180 || value > 180) {
                System.out.println("  Longitude must be between -180 and 180.");
                continue;
            }

            return value;
        }
    }

    private double readDoubleOrCancel(final String prompt) {
        while (true) {
            final String value = Console.readLine(prompt + " (0 to cancel):").trim();

            if (value.equals(CANCEL)) {
                System.out.println("Registration cancelled.");
                return CANCEL_DOUBLE;
            }

            if (value.isBlank()) {
                System.out.println("  Please insert a value.");
                continue;
            }

            try {
                return Double.parseDouble(value.replace(",", "."));
            } catch (final NumberFormatException e) {
                System.out.println("  Please insert a valid numeric value.");
            }
        }
    }

    private String selectAirControlArea(final double latitude, final double longitude) {
        final List<AirControlArea> areas = new ArrayList<>();
        controller.listAreasContaining(latitude, longitude).forEach(areas::add);

        if (areas.isEmpty()) {
            System.out.println();
            System.out.println("No air control area covers these coordinates:");
            System.out.printf("  Latitude : %.6f%n", latitude);
            System.out.printf("  Longitude: %.6f%n", longitude);
            System.out.println("Please insert coordinates inside an existing air control area.");

            return null;
        }

        System.out.println("\nAir control areas covering these coordinates:");
        System.out.printf("  %-4s %-10s %s%n", "#", "Code", "Name");
        System.out.println("  " + "-".repeat(50));

        for (int i = 0; i < areas.size(); i++) {
            System.out.printf("  %-4d %-10s %s%n",
                    i + 1,
                    areas.get(i).code(),
                    areas.get(i).name());
        }

        while (true) {
            final String input = Console.readLine("\nSelect air control area (0 to cancel):")
                    .trim();

            if (input.equals(CANCEL)) {
                System.out.println("Registration cancelled.");
                return null;
            }

            if (input.isBlank()) {
                System.out.println("  Please insert an option.");
                continue;
            }

            try {
                final int option = Integer.parseInt(input);

                if (option >= 1 && option <= areas.size()) {
                    return areas.get(option - 1).code().toString();
                }

                System.out.printf("  Invalid option. Choose a number between 1 and %d.%n",
                        areas.size());

            } catch (final NumberFormatException e) {
                System.out.println("  Invalid option. Please insert a valid number.");
            }
        }
    }

    private void showSummary(final String iataCode,
                             final String icaoCode,
                             final String name,
                             final String cityName,
                             final String countryIsoCode,
                             final String countryName,
                             final double latitude,
                             final double longitude,
                             final double elevation,
                             final String airControlAreaCode) {
        System.out.println();
        System.out.println("Airport Registration Summary");
        System.out.println("============================");
        System.out.printf("  IATA        : %s%n", iataCode);
        System.out.printf("  ICAO        : %s%n", icaoCode);
        System.out.printf("  Name        : %s%n", name);
        System.out.printf("  City        : %s%n", cityName);
        System.out.printf("  Country     : %s (%s)%n", countryName, countryIsoCode);
        System.out.printf("  Coordinates : %.6f, %.6f%n", latitude, longitude);
        System.out.printf("  Elevation   : %.2f m%n", elevation);
        System.out.printf("  ACA         : %s%n", airControlAreaCode);
    }

    private boolean confirmRegistration() {
        while (true) {
            final String confirm = Console.readLine("\nConfirm registration? (y/n):")
                    .trim()
                    .toLowerCase();

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
                    .toLowerCase();

            if (input.equals("y")) {
                return true;
            }

            if (input.equals("n")) {
                return false;
            }

            System.out.println("  Invalid option. Please answer 'y' or 'n'.");
        }
    }

    @Override
    public String headline() {
        return "Register Airport";
    }
}