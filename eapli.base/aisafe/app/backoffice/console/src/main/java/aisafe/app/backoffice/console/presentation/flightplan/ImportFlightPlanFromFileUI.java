package aisafe.app.backoffice.console.presentation.flightplan;

import aisafe.flightplan.application.CreateFlightPlanFromFileController;
import aisafe.flightplan.application.FlightPlanDTO;
import aisafe.flightplan.domain.CreateFlightPlanFromFileService;
import aisafe.persistence.PersistenceContext;
import eapli.framework.infrastructure.authz.application.exceptions.UnauthorizedException;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ImportFlightPlanFromFileUI extends AbstractUI {

    private final CreateFlightPlanFromFileController controller =
            new CreateFlightPlanFromFileController(
                    PersistenceContext.repositories().flightPlans(),
                    PersistenceContext.repositories().flightRoutes(),
                    PersistenceContext.repositories().aircrafts(),
                    PersistenceContext.repositories().airports(),
                    PersistenceContext.repositories().weatherData(),
                    PersistenceContext.repositories().pilots()
            );

    @Override
    protected boolean doShow() {
        try {
            final String filePath = chooseFlightPlanFile();

            if (filePath == null) {
                return false;
            }

            final CreateFlightPlanFromFileService.ImportFlightPlanResult result =
                    controller.createFromFile(filePath);

            if (!result.isSuccess()) {
                showErrors(result);
                return false;
            }

            showCreatedFlightPlan(result.flightPlan());
            showImportedSegments(result.segments());

            System.out.println();

            System.out.println("DSL Validation");
            System.out.println("-------------------------");
            System.out.println("Lexical Analysis: PASSED");
            System.out.println("Syntactic Analysis: PASSED");
            System.out.println("Semantic Analysis: PASSED");

            System.out.println();
            System.out.println("Flight plan imported and created successfully.");
            System.out.println("The flight plan is now available in the system in DRAFT status.");

        } catch (final UnauthorizedException e) {
            System.out.println();
            System.out.println("You are not authorized to import flight plans.");
            System.out.println("Please login with a Pilot account.");

        } catch (final IOException e) {
            System.out.println();
            System.out.println("Could not read file.");
            System.out.println("Reason: " + e.getMessage());

        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println();
            System.out.println("Flight plan import failed.");
            System.out.println("Reason: " + e.getMessage());
        }

        return false;
    }

    private String chooseFlightPlanFile() throws IOException {
        final List<Path> files = findExampleFlightFiles();

        if (files.isEmpty()) {
            System.out.println("No .flight files were found in: "
                    + examplesDirectory().toAbsolutePath());

            final String path = Console.readLine("Insert flight plan file path (.flight) or 0 to return: ")
                    .trim();

            if (path.equals("0")) {
                return null;
            }

            return path;
        }

        System.out.println();
        System.out.println("Available example flight plan files:");
        System.out.println();

        for (int i = 0; i < files.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, files.get(i).getFileName());
        }

        System.out.println("C. Insert custom path");
        System.out.println("0. Return");
        System.out.println();

        final int option = Console.readInteger("Please choose an option: ");

        if (option == 0) {
            return null;
        }

        if (option == 9) {
            final String path = Console.readLine("Insert flight plan file path (.flight): ")
                    .trim();

            if (path.equals("0")) {
                return null;
            }

            return path;
        }

        if (option < 1 || option > files.size()) {
            System.out.println("Invalid option.");
            return null;
        }

        return files.get(option - 1).toString();
    }

    private List<Path> findExampleFlightFiles() throws IOException {
        final Path examplesDirectory = examplesDirectory();

        if (!Files.exists(examplesDirectory) || !Files.isDirectory(examplesDirectory)) {
            return List.of();
        }

        try (var stream = Files.list(examplesDirectory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".flight"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList();
        }
    }

    private Path examplesDirectory() {
        final Path fromRepositoryRoot = Path.of("lprog", "example");

        if (Files.isDirectory(fromRepositoryRoot)) {
            return fromRepositoryRoot;
        }

        final Path fromAisafeRoot = Path.of("..", "..", "lprog", "example");

        if (Files.isDirectory(fromAisafeRoot)) {
            return fromAisafeRoot;
        }

        return fromRepositoryRoot;
    }

    private void showErrors(final CreateFlightPlanFromFileService.ImportFlightPlanResult result) {
        System.out.println();
        System.out.println("Flight plan import failed.");
        System.out.println("Errors:");

        for (final String error : result.errors()) {
            System.out.println(" - " + error);
        }
    }

    private void showCreatedFlightPlan(final FlightPlanDTO flightPlan) {
        System.out.println();
        System.out.println("Imported Flight Plan Created");
        System.out.println("----------------------------");
        System.out.println("Flight Plan ID : " + flightPlan.flightPlanID());
        System.out.println("Route          : " + flightPlan.routeCode());
        System.out.println("Aircraft       : " + flightPlan.aircraftRegistration());
        System.out.println("Pilot          : " + controller.pilotDisplayNameFor(flightPlan));
        System.out.println("Departure      : " + flightPlan.plannedDepartureDateTime());
        System.out.println("Arrival        : " + flightPlan.plannedArrivalDateTime());
        System.out.println("Fuel           : " + flightPlan.fuelAmount() + " " + flightPlan.fuelUnit());
        System.out.println("Status         : " + flightPlan.status());

        if (flightPlan.weatherDataID() != null) {
            System.out.println("Weather Data   : automatically generated from DSL segments");
        }
    }

    private void showImportedSegments(
            final List<CreateFlightPlanFromFileService.ImportedSegmentView> segments) {

        System.out.println();
        System.out.println("Imported Flight Segments");
        System.out.println("------------------------");

        if (segments == null || segments.isEmpty()) {
            System.out.println("No segments were found in the imported flight plan.");
            return;
        }

        System.out.printf("  %-4s %-23s %-23s %-15s %-15s %-20s %-12s%n",
                "#",
                "From",
                "To",
                "Altitude",
                "Width",
                "Wind",
                "DSL Pos");

        System.out.println("  " + "-".repeat(120));

        for (int i = 0; i < segments.size(); i++) {
            final CreateFlightPlanFromFileService.ImportedSegmentView segment = segments.get(i);

            final String from = String.format(Locale.US, "(%.3f, %.3f)",
                    segment.startLatitude(),
                    segment.startLongitude());

            final String to = String.format(Locale.US, "(%.3f, %.3f)",
                    segment.endLatitude(),
                    segment.endLongitude());

            final String altitude = String.format(Locale.US, "%.0f m",
                    segment.altitudeMeters());

            final String width = String.format(Locale.US, "%.0f m",
                    segment.widthMeters());

            final String wind = String.format(Locale.US, "%.0f deg %.0f m/s",
                    segment.windDirectionDegrees(),
                    segment.windSpeedMetersPerSecond());

            final String dslPosition = String.format("%d:%d",
                    segment.line(),
                    segment.column());

            System.out.printf("  %-4d %-23s %-23s %-15s %-15s %-20s %-12s%n",
                    i + 1,
                    from,
                    to,
                    altitude,
                    width,
                    wind,
                    dslPosition);
        }
    }

    @Override
    public String headline() {
        return "Import Flight Plan From File";
    }
}