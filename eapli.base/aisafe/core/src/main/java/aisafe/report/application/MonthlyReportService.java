package aisafe.report.application;

import aisafe.flightplan.domain.FlightPlan;
import aisafe.flightplan.domain.FlightPlanRepository;
import eapli.framework.validations.Preconditions;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Application service for US112 - Monthly Report Generation.
 *
 * <p>Collects the flight plans created in a given month (filtering in memory,
 * since the repository has no date-range query), computes statistics, renders
 * a branded report via {@link MonthlyFlightPlanReport} and saves it to a file.
 *
 * <p>Modeled on {@code SimulationService} (US111): gathers data, builds a DTO,
 * writes a report file and uses its own {@link ReportException}.
 *
 * <p>The statistics logic is split into small package-private methods
 * ({@link #filterByMonth}, {@link #countByStatus}, {@link #countByRoute}) so it
 * can be unit-tested independently of file I/O and persistence.
 */
public class MonthlyReportService {

    private static final String REPORTS_DIR = "reports";

    private final FlightPlanRepository flightPlanRepository;

    public MonthlyReportService(final FlightPlanRepository flightPlanRepository) {
        this.flightPlanRepository = flightPlanRepository;
    }

    /**
     * Generates the monthly flight plan statistics report for the given month.
     *
     * @param year  full year (e.g. 2026)
     * @param month month number 1-12
     */
    public MonthlyReportDTO generateMonthlyReport(final int year, final int month) {
        Preconditions.ensure(month >= 1 && month <= 12,
                "Month must be between 1 and 12.");
        Preconditions.ensure(year >= 2000 && year <= 9999,
                "Year must be a valid 4-digit year.");

        final List<FlightPlan> plansOfMonth =
                filterByMonth(flightPlanRepository.findAll(), year, month);

        final int total = plansOfMonth.size();
        final Map<String, Integer> countByStatus = countByStatus(plansOfMonth);
        final Map<String, Integer> countByRoute  = countByRoute(plansOfMonth);

        final MonthlyFlightPlanReport report = new MonthlyFlightPlanReport(
                year, month, total, countByStatus, countByRoute);
        final String rendered = report.render();

        final String filePath = saveReport(year, month, rendered);

        return new MonthlyReportDTO(
                year, month, total, countByStatus, countByRoute, rendered, filePath);
    }

    // ── Statistics logic (package-private, unit-testable) ───────────────────

    /** Keeps only the flight plans whose creation date falls in the given month. */
    List<FlightPlan> filterByMonth(final Iterable<FlightPlan> plans,
                                   final int year, final int month) {
        final List<FlightPlan> result = new ArrayList<>();
        for (final FlightPlan plan : plans) {
            final LocalDateTime createdAt = plan.createdAt();
            if (createdAt == null) {
                continue;
            }
            if (createdAt.getYear() == year && createdAt.getMonthValue() == month) {
                result.add(plan);
            }
        }
        return result;
    }

    /** Counts the given flight plans grouped by status name. */
    Map<String, Integer> countByStatus(final List<FlightPlan> plans) {
        final Map<String, Integer> counts = new LinkedHashMap<>();
        for (final FlightPlan plan : plans) {
            counts.merge(plan.status().name(), 1, Integer::sum);
        }
        return counts;
    }

    /** Counts the given flight plans grouped by route code. */
    Map<String, Integer> countByRoute(final List<FlightPlan> plans) {
        final Map<String, Integer> counts = new TreeMap<>();
        for (final FlightPlan plan : plans) {
            counts.merge(plan.routeCode().toString(), 1, Integer::sum);
        }
        return counts;
    }

    // ── File output ─────────────────────────────────────────────────────────

    private String saveReport(final int year, final int month, final String content) {
        final File dir = new File(REPORTS_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new ReportException("Could not create reports directory: " + dir.getPath());
        }

        final String fileName = String.format("monthly_report_%d_%02d.txt", year, month);
        final File outFile = new File(dir, fileName);

        try (final FileWriter writer = new FileWriter(outFile)) {
            writer.write(content);
        } catch (final IOException e) {
            throw new ReportException("Failed to save report: " + e.getMessage());
        }

        return outFile.getAbsolutePath();
    }
}