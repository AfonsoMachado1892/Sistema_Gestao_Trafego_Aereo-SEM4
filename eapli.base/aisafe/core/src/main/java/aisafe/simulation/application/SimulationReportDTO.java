package aisafe.simulation.application;

import java.util.List;

/**
 * DTO carrying the simulation report data read from the C-generated report file.
 */
public class SimulationReportDTO {

    public final int totalFlights;
    public final int totalSteps;
    public final int totalViolations;
    public final String environment;
    public final String globalResult;
    public final List<String> flightStatuses;
    public final List<String> violationEvents;
    public final String reportFilePath;

    public SimulationReportDTO(final int totalFlights,
                                final int totalSteps,
                                final int totalViolations,
                                final String environment,
                                final String globalResult,
                                final List<String> flightStatuses,
                                final List<String> violationEvents,
                                final String reportFilePath) {
        this.totalFlights    = totalFlights;
        this.totalSteps      = totalSteps;
        this.totalViolations = totalViolations;
        this.environment     = environment;
        this.globalResult    = globalResult;
        this.flightStatuses  = flightStatuses;
        this.violationEvents = violationEvents;
        this.reportFilePath  = reportFilePath;
    }

    @Override
    public String toString() {
        return "SimulationReport{flights=" + totalFlights
                + ", violations=" + totalViolations
                + ", result=" + globalResult + "}";
    }
}
