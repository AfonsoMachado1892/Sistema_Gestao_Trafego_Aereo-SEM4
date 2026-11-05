package dsl;

import dsl.parser.FlightPlanImportService;

import java.io.IOException;
import java.nio.file.Path;

public class FlightPlanDslApp {

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.out.println("Usage: java FlightPlanDslApp <flight-plan-file>");
            return;
        }

        FlightPlanImportService service = new FlightPlanImportService();
        FlightPlanImportService.ImportResult result = service.importFromFile(Path.of(args[0]));

        if (!result.isSuccess()) {
            System.out.println("Flight plan is invalid.");
            result.errors().forEach(System.out::println);
            return;
        }

        System.out.println("Flight plan is valid.");
        System.out.println("Flight ID: " + result.flightPlan().flightId());
        System.out.println("Flight type: " + result.flightPlan().flightType());
        System.out.println("Route: "
                + result.flightPlan().route().originAirportCode()
                + " -> "
                + result.flightPlan().route().destinationAirportCode());
        System.out.println("Aircraft: " + result.flightPlan().aircraftRegistration());
        System.out.println("Pilot: " + result.flightPlan().pilotId());
        System.out.println("Flights found: " + result.flightCount());
        System.out.println("Legs found: " + result.legCount());
        System.out.println("Segments found: " + result.segmentCount());
    }
}