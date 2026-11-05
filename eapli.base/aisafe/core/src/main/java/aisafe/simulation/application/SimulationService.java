package aisafe.simulation.application;

import eapli.framework.validations.Preconditions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Application service for US111 – Generate a Simulation Report.
 *
 * <p>Supports both Linux (direct execution) and Windows (via WSL).
 * The OS is detected automatically at runtime.
 * The C project is compiled automatically if the executable does not exist.
 */
public class SimulationService {

    private static final String SCOMP_DIR       = "scomp/sprint3";
    private static final String TESTS_DIR       = SCOMP_DIR + "/tests";
    private static final String REPORT_FILENAME = SCOMP_DIR + "/simulation_report.txt";
    private static final boolean IS_WINDOWS     =
            System.getProperty("os.name").toLowerCase().contains("win");

    public List<String> listFlightPlanFiles() {
        final File dir = new File(TESTS_DIR);
        final List<String> files = new ArrayList<>();

        if (!dir.exists() || !dir.isDirectory()) {
            return files;
        }

        final File[] jsonFiles = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (jsonFiles != null) {
            for (final File f : jsonFiles) {
                files.add(f.getName());
            }
            files.sort(String::compareTo);
        }

        return files;
    }

    /**
     * Runs the C simulation and returns the report as a DTO.
     * Compiles the C project automatically if the executable does not exist.
     */
    public SimulationReportDTO runSimulation(final String flightPlanFilename,
                                             final String weatherFilename) {
        Preconditions.nonEmpty(flightPlanFilename, "Flight plan filename must not be empty.");

        final File sccompDir = new File(SCOMP_DIR);
        final File flightPlanFile = new File(TESTS_DIR + "/" + flightPlanFilename);

        if (!flightPlanFile.exists()) {
            throw new SimulationException(
                    "Flight plan file not found: " + flightPlanFile.getPath());
        }

        // Compile automatically if executable does not exist
        ensureExecutableExists(sccompDir);

        final List<String> command = buildCommand(
                sccompDir, flightPlanFilename, weatherFilename);

        try {
            final ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            pb.inheritIO();

            if (!IS_WINDOWS) {
                pb.directory(sccompDir);
            }

            final Process process = pb.start();
            final int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new SimulationException("Simulation exited with code " + exitCode);
            }

        } catch (final IOException e) {
            throw new SimulationException("Failed to start simulation: " + e.getMessage());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SimulationException("Simulation was interrupted.");
        }

        return readReport(REPORT_FILENAME);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Compiles the C project using make if the executable does not exist.
     */
    private void ensureExecutableExists(final File sccompDir) {
        final File executable = new File(SCOMP_DIR + "/simulation");

        if (!IS_WINDOWS && executable.exists()) {
            return; // already compiled on Linux
        }

        System.out.println("Compiling simulation...");

        try {
            final List<String> makeCommand = new ArrayList<>();

            if (IS_WINDOWS) {
                makeCommand.add("wsl");
                makeCommand.add("--cd");
                makeCommand.add(toWslPath(sccompDir.getAbsolutePath()));
                makeCommand.add("make");
            } else {
                makeCommand.add("make");
            }

            final ProcessBuilder pb = new ProcessBuilder(makeCommand);
            pb.redirectErrorStream(true);
            pb.inheritIO();

            if (!IS_WINDOWS) {
                pb.directory(sccompDir);
            }

            final Process process = pb.start();
            final int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new SimulationException(
                        "Failed to compile simulation (make exited with code " + exitCode + ").");
            }

            System.out.println("Compilation successful.");

        } catch (final IOException e) {
            throw new SimulationException("Failed to run make: " + e.getMessage());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SimulationException("Compilation was interrupted.");
        }
    }

    private List<String> buildCommand(final File sccompDir,
                                      final String flightPlanFilename,
                                      final String weatherFilename) {
        final List<String> command = new ArrayList<>();

        if (IS_WINDOWS) {
            command.add("wsl");
            command.add("--cd");
            command.add(toWslPath(sccompDir.getAbsolutePath()));
            command.add("./simulation");
        } else {
            command.add("./simulation");
        }

        command.add("tests/" + flightPlanFilename);

        if (weatherFilename != null && !weatherFilename.isBlank()) {
            command.add("--weather");
            command.add("data/" + weatherFilename);
        }

        return command;
    }

    /**
     * Converts a Windows absolute path to a WSL path.
     */
    private String toWslPath(final String windowsPath) {
        String path = windowsPath.replace("\\", "/");
        if (path.length() >= 2 && path.charAt(1) == ':') {
            final char driveLetter = Character.toLowerCase(path.charAt(0));
            path = "/mnt/" + driveLetter + path.substring(2);
        }
        return path;
    }

    private SimulationReportDTO readReport(final String reportPath) {
        final File reportFile = new File(reportPath);
        if (!reportFile.exists()) {
            throw new SimulationException(
                    "Report file not found after simulation: " + reportPath);
        }

        try (final BufferedReader reader = new BufferedReader(new FileReader(reportFile))) {
            int    totalFlights    = 0;
            int    totalSteps      = 0;
            int    totalViolations = 0;
            String environment     = "DISABLED";
            String globalResult    = "UNKNOWN";
            final List<String> flightStatuses  = new ArrayList<>();
            final List<String> violationEvents = new ArrayList<>();

            String line;
            String section = "";
            StringBuilder currentViolation = null;

            while ((line = reader.readLine()) != null) {
                final String trimmed = line.trim();

                if (trimmed.startsWith("Total flights")) {
                    totalFlights = parseIntAfterColon(trimmed);
                } else if (trimmed.startsWith("Total steps")) {
                    totalSteps = parseIntAfterColon(trimmed);
                } else if (trimmed.startsWith("Total violations")) {
                    totalViolations = parseIntAfterColon(trimmed);
                } else if (trimmed.startsWith("Environment")) {
                    environment = parseStringAfterColon(trimmed);
                } else if (trimmed.startsWith("Global result")) {
                    globalResult = parseStringAfterColon(trimmed);
                } else if (trimmed.equals("Flight Execution Status")) {
                    section = "flights";
                } else if (trimmed.equals("Safety Violation Events")) {
                    section = "violations";
                } else if (section.equals("flights") && trimmed.startsWith("Flight ID")) {
                    flightStatuses.add(trimmed);
                } else if (section.equals("violations")) {
                    if (trimmed.startsWith("Violation") && trimmed.endsWith(":")) {
                        if (currentViolation != null) {
                            violationEvents.add(currentViolation.toString().trim());
                        }
                        currentViolation = new StringBuilder(trimmed).append("\n");
                    } else if (currentViolation != null && !trimmed.isEmpty()
                            && !trimmed.startsWith("===")
                            && !trimmed.startsWith("End of report")) {
                        if (trimmed.startsWith("...")) {
                            violationEvents.add(currentViolation.toString().trim());
                            currentViolation = null;
                            violationEvents.add(trimmed);
                        } else {
                            currentViolation.append("  ").append(trimmed).append("\n");
                        }
                    }
                }
            }

            if (currentViolation != null) {
                violationEvents.add(currentViolation.toString().trim());
            }

            return new SimulationReportDTO(
                    totalFlights, totalSteps, totalViolations,
                    environment, globalResult,
                    flightStatuses, violationEvents,
                    reportFile.getAbsolutePath());

        } catch (final IOException e) {
            throw new SimulationException("Failed to read report: " + e.getMessage());
        }
    }

    private int parseIntAfterColon(final String line) {
        try {
            return Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
        } catch (final NumberFormatException e) {
            return 0;
        }
    }

    private String parseStringAfterColon(final String line) {
        return line.substring(line.indexOf(':') + 1).trim();
    }
}