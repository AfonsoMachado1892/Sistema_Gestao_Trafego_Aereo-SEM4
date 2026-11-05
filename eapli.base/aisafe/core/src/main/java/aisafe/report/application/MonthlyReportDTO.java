package aisafe.report.application;

import java.util.Map;

public class MonthlyReportDTO {

    public final int year;
    public final int month;
    public final int totalFlightPlans;
    public final Map<String, Integer> countByStatus;
    public final Map<String, Integer> countByRoute;
    public final String renderedReport;
    public final String reportFilePath;

    public MonthlyReportDTO(final int year,
                            final int month,
                            final int totalFlightPlans,
                            final Map<String, Integer> countByStatus,
                            final Map<String, Integer> countByRoute,
                            final String renderedReport,
                            final String reportFilePath) {
        this.year             = year;
        this.month            = month;
        this.totalFlightPlans = totalFlightPlans;
        this.countByStatus    = countByStatus;
        this.countByRoute     = countByRoute;
        this.renderedReport   = renderedReport;
        this.reportFilePath   = reportFilePath;
    }

    @Override
    public String toString() {
        return "MonthlyReport{" + year + "-" + String.format("%02d", month)
                + ", totalFlightPlans=" + totalFlightPlans + "}";
    }
}