package aisafe.app.backoffice.console.presentation.airport;

import aisafe.airport.application.RegisterAirportController;
import aisafe.airport.domain.Airport;
import aisafe.persistence.PersistenceContext;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("squid:S106")
public class ListAirportsUI extends AbstractUI {

    private static final int PAGE_SIZE = 10;

    private final RegisterAirportController controller =
            new RegisterAirportController(
                    PersistenceContext.repositories().airports(),
                    PersistenceContext.repositories().airControlAreas()
            );

    @Override
    protected boolean doShow() {
        final List<Airport> airports = new ArrayList<>();
        controller.listAirports().forEach(airports::add);

        if (airports.isEmpty()) {
            System.out.println("\nNo airports registered.");
            return false;
        }

        System.out.printf("%nTotal: %d airport(s)%n", airports.size());

        int page = 0;
        final int totalPages = (int) Math.ceil((double) airports.size() / PAGE_SIZE);

        while (true) {
            final int from = page * PAGE_SIZE;
            final int to = Math.min(from + PAGE_SIZE, airports.size());

            System.out.printf("%nPage %d of %d%n", page + 1, totalPages);
            System.out.printf("  %-6s %-6s %-35s %-25s %-25s %-12s %s%n",
                    "IATA", "ICAO", "Name", "Locality", "Coordinates", "Elevation", "ACA");
            System.out.println("  " + "-".repeat(130));

            for (int i = from; i < to; i++) {
                final Airport airport = airports.get(i);

                System.out.printf("  %-6s %-6s %-35s %-25s %-25s %-12s %s%n",
                        airport.iataCode(),
                        airport.icaoCode(),
                        truncate(airport.name().toString(), 35),
                        truncate(airport.locality().toString(), 25),
                        truncate(airport.coordinates().toString(), 25),
                        airport.elevation(),
                        airport.airControlArea()
                );
            }

            if (totalPages == 1) {
                break;
            }

            System.out.println();
            if (page > 0) {
                System.out.println("  p. Previous page");
            }
            if (page < totalPages - 1) {
                System.out.println("  n. Next page");
            }
            System.out.println("  q. Quit");

            final String input = Console.readLine("Option:").trim().toLowerCase();

            if (input.equals("q")) {
                break;
            }

            if (input.equals("n") && page < totalPages - 1) {
                page++;
            } else if (input.equals("p") && page > 0) {
                page--;
            } else {
                System.out.println("  Invalid option.");
            }
        }

        return false;
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

    @Override
    public String headline() {
        return "List Airports";
    }
}