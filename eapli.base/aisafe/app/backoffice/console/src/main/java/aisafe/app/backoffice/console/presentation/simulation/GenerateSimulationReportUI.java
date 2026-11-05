package aisafe.app.backoffice.console.presentation.simulation;

import aisafe.simulation.application.GenerateSimulationReportController;
import aisafe.simulation.application.SimulationReportDTO;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.List;

@SuppressWarnings("squid:S106")
public class GenerateSimulationReportUI extends AbstractUI {

    private final GenerateSimulationReportController controller =
            new GenerateSimulationReportController();

    private static final List<String> WEATHER_FILES = List.of(
            "weather_aca121_realistic.csv",
            "weather_aca121_medium.csv",
            "weather_aca121_high.csv",
            "weather_aca121_intense.csv"
    );

    @Override
    protected boolean doShow() {
        try {
            // ── 1. List available flight plans ────────────────────────────────
            final List<String> flightPlans = controller.listFlightPlanFiles();

            if (flightPlans.isEmpty()) {
                System.out.println("No flight plan files found in scomp/sprint3/tests/.");
                return false;
            }

            System.out.println("\nAvailable flight plans:");
            for (int i = 0; i < flightPlans.size(); i++) {
                System.out.printf("  %d - %s%n", i + 1, flightPlans.get(i));
            }

            final int planChoice = Console.readInteger("\nSelect flight plan:");
            if (planChoice < 1 || planChoice > flightPlans.size()) {
                System.out.println("Invalid option.");
                return false;
            }

            final String selectedPlan = flightPlans.get(planChoice - 1);

            // ── 2. Weather (optional) ─────────────────────────────────────────
            System.out.println("\nWeather configuration:");
            System.out.println("  0 - No weather");
            for (int i = 0; i < WEATHER_FILES.size(); i++) {
                System.out.printf("  %d - %s%n", i + 1, WEATHER_FILES.get(i));
            }
            final int weatherChoice = Console.readInteger("Select weather (0 = none):");
            final String weatherFile;
            if (weatherChoice >= 1 && weatherChoice <= WEATHER_FILES.size()) {
                weatherFile = WEATHER_FILES.get(weatherChoice - 1);
            } else {
                weatherFile = null;
            }

            // ── 3. Summary and confirmation ───────────────────────────────────
            System.out.println("\n── Configuration ────────────────────────────────");
            System.out.printf("  Flight plan : %s%n", selectedPlan);
            System.out.printf("  Weather     : %s%n",
                    weatherFile != null ? weatherFile : "disabled");
            System.out.println("─────────────────────────────────────────────────");

            final String confirm = Console.readLine("\nStart simulation? (y/n):");
            if (!confirm.equalsIgnoreCase("y")) {
                System.out.println("Simulation cancelled.");
                return false;
            }

            // ── 4. Run simulation ─────────────────────────────────────────────
            System.out.println("\nRunning simulation, please wait...");
            System.out.println("========================================");

            final SimulationReportDTO report = controller.runSimulation(
                    selectedPlan, weatherFile);

            // ── 5. Display report summary ─────────────────────────────────────
            System.out.println("\n========================================");
            System.out.println("       SIMULATION REPORT SUMMARY        ");
            System.out.println("========================================");
            System.out.printf("  Total flights    : %d%n", report.totalFlights);
            System.out.printf("  Total steps      : %d%n", report.totalSteps);
            System.out.printf("  Total violations : %d%n", report.totalViolations);
            System.out.printf("  Environment      : %s%n", report.environment);
            System.out.printf("  Global result    : %s%n", report.globalResult);

            System.out.println("\n--- Flight Execution Status ---");
            if (report.flightStatuses.isEmpty()) {
                System.out.println("  No flight status data available.");
            } else {
                report.flightStatuses.forEach(s -> System.out.println("  " + s));
            }

            if (report.totalViolations > 0) {
                System.out.println("\n--- Safety Violations ---");
                if (report.violationEvents.isEmpty()) {
                    System.out.println("  Details not available.");
                } else {
                    for (final String violation : report.violationEvents) {
                        for (final String detail : violation.split("\n")) {
                            System.out.println("  " + detail);
                        }
                        System.out.println();
                    }
                }
            }

            System.out.println("========================================");
            System.out.printf("Full report saved to: %s%n", report.reportFilePath);
            System.out.println("========================================");

        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (final Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Generate Simulation Report";
    }
}