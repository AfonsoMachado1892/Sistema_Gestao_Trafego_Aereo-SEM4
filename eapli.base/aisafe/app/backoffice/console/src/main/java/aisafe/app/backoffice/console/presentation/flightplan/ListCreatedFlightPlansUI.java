package aisafe.app.backoffice.console.presentation.flightplan;

import aisafe.flightplan.application.CreateFlightPlanController;
import aisafe.flightplan.application.FlightPlanDTO;
import aisafe.persistence.PersistenceContext;
import aisafe.weather.application.ListWeatherDataController;
import aisafe.weather.domain.WeatherDataID;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.exceptions.UnauthorizedException;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("squid:S106")
public class ListCreatedFlightPlansUI extends AbstractUI {

    private static final int PAGE_SIZE = 8;
    private static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CreateFlightPlanController controller =
            new CreateFlightPlanController(
                    PersistenceContext.repositories().flightPlans(),
                    PersistenceContext.repositories().flightRoutes(),
                    PersistenceContext.repositories().aircrafts(),
                    PersistenceContext.repositories().pilots()
            );

    @Override
    protected boolean doShow() {
        final List<FlightPlanDTO> flightPlans = new ArrayList<>();

        try {
            controller.listMyCreatedFlightPlans().forEach(flightPlans::add);
        } catch (final UnauthorizedException e) {
            System.out.println();
            System.out.println("You are not authorized to list created flight plans.");
            System.out.println("Please login with a Pilot account.");
            return false;
        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println();
            System.out.println("Could not load created flight plans.");
            System.out.println("Reason: " + e.getMessage());
            return false;
        }

        if (flightPlans.isEmpty()) {
            System.out.println();
            System.out.println("No created flight plans found.");
            return false;
        }

        System.out.printf("%nTotal: %d created flight plan(s)%n", flightPlans.size());

        int page = 0;
        final int totalPages = (int) Math.ceil((double) flightPlans.size() / PAGE_SIZE);

        while (true) {
            printPage(flightPlans, page, totalPages);

            System.out.println();
            System.out.println("Options:");
            System.out.println("  number - View flight plan details");
            if (page > 0) {
                System.out.println("  p      - Previous page");
            }
            if (page < totalPages - 1) {
                System.out.println("  n      - Next page");
            }
            System.out.println("  0      - Return");

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

                if (index >= 0 && index < flightPlans.size()) {
                    showDetails(flightPlans.get(index), selectedNumber);
                } else {
                    System.out.printf("  Invalid option. Choose a number between 1 and %d.%n",
                            flightPlans.size());
                }

            } catch (final NumberFormatException e) {
                System.out.println("  Invalid option.");
            }
        }
    }

    private void printPage(final List<FlightPlanDTO> flightPlans,
                           final int page,
                           final int totalPages) {
        final int from = page * PAGE_SIZE;
        final int to = Math.min(from + PAGE_SIZE, flightPlans.size());

        System.out.printf("%nPage %d of %d%n", page + 1, totalPages);
        System.out.println("Created Flight Plans");
        System.out.println("--------------------");

        System.out.printf("%-4s %-12s %-8s %-9s %-10s %-16s %-16s %-12s%n",
                "#", "Plan ID", "Route", "Aircraft", "Status",
                "Departure", "Arrival", "Fuel");

        System.out.println("-".repeat(105));

        for (int i = from; i < to; i++) {
            final FlightPlanDTO fp = flightPlans.get(i);

            System.out.printf("%-4d %-12s %-8s %-9s %-10s %-16s %-16s %-12s%n",
                    i + 1,
                    shortId(fp.flightPlanID()),
                    fp.routeCode(),
                    fp.aircraftRegistration(),
                    fp.status(),
                    formatDateTime(fp.plannedDepartureDateTime()),
                    formatDateTime(fp.plannedArrivalDateTime()),
                    fp.fuelAmount() + " " + fp.fuelUnit()
            );
        }
    }

    private void showDetails(final FlightPlanDTO flightPlan, final int selectedNumber) {
        System.out.println();
        System.out.println("Flight Plan Details");
        System.out.println("===================");
        System.out.println("  Number       : " + selectedNumber);
        System.out.println("  Full ID      : " + flightPlan.flightPlanID());
        System.out.println("  Route        : " + flightPlan.routeCode());
        System.out.println("  Aircraft     : " + flightPlan.aircraftRegistration());
        System.out.println("  Pilot        : " + controller.pilotDisplayNameFor(flightPlan));
        System.out.println("  Departure    : " + formatDateTime(flightPlan.plannedDepartureDateTime()));
        System.out.println("  Arrival      : " + formatDateTime(flightPlan.plannedArrivalDateTime()));
        System.out.println("  Fuel         : " + flightPlan.fuelAmount() + " " + flightPlan.fuelUnit());
        System.out.println("  Status       : " + flightPlan.status());
        System.out.println("  Created at   : " + formatDateTime(flightPlan.createdAt()));

        if (flightPlan.weatherDataID() != null) {
            System.out.println();
            System.out.println("  Weather Data ID : " + flightPlan.weatherDataID());

            final String answer = Console.readLine("View full weather data details? (y/n):");
            if (answer.equalsIgnoreCase("y")) {
                final ListWeatherDataController controller1 = new ListWeatherDataController(
                        AuthzRegistry.authorizationService(),
                        PersistenceContext.repositories().weatherData()
                );

                final WeatherDataID weatherDataID = WeatherDataID.of(flightPlan.weatherDataID());

                controller1.getWeatherDataByID(weatherDataID).ifPresentOrElse(
                        wd -> {
                            System.out.println();
                            System.out.println("  Weather Data Details");
                            System.out.println("  --------------------");
                            System.out.println("  Area Code    : " + wd.airControlAreaCode());
                            System.out.println("  Day          : " + wd.day());
                            System.out.println("  Start Time   : " + wd.startTime());
                            System.out.println("  End Time     : " + wd.endTime());
                            System.out.printf ("  BBox Start   : %.4f, %.4f%n",
                                    wd.boundingBox().start().latitude(),
                                    wd.boundingBox().start().longitude());
                            System.out.printf ("  BBox End     : %.4f, %.4f%n",
                                    wd.boundingBox().end().latitude(),
                                    wd.boundingBox().end().longitude());
                            System.out.println("  Alt Min (ft) : " + wd.minAltitude());
                            System.out.println("  Alt Max (ft) : " + wd.maxAltitude());
                            System.out.printf ("  Wind Speed   : %.2f knots%n", wd.windSpeed());
                            System.out.printf ("  Wind Dir     : %.2f °%n", wd.windDirection());
                        },
                        () -> System.out.println("  Weather data not found.")
                );
            }
        } else {
            System.out.println();
            System.out.println("  Weather Data : Not assigned");
        }

        System.out.println();
        Console.readLine("Press ENTER to continue...");
    }

    private String shortId(final String id) {
        if (id == null || id.length() <= 8) {
            return id;
        }

        return id.substring(0, 8) + "...";
    }

    private String formatDateTime(final LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FMT);
    }

    @Override
    public String headline() {
        return "List My Created Flight Plans";
    }
}