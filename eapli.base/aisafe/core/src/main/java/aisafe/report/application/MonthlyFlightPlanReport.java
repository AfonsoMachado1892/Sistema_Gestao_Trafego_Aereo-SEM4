package aisafe.report.application;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

public class MonthlyFlightPlanReport extends Report {

    private final int year;
    private final int month;
    private final int totalFlightPlans;
    private final Map<String, Integer> countByStatus;
    private final Map<String, Integer> countByRoute;

    public MonthlyFlightPlanReport(final int year,
                                   final int month,
                                   final int totalFlightPlans,
                                   final Map<String, Integer> countByStatus,
                                   final Map<String, Integer> countByRoute) {
        this.year             = year;
        this.month            = month;
        this.totalFlightPlans = totalFlightPlans;
        this.countByStatus    = countByStatus;
        this.countByRoute     = countByRoute;
    }

    @Override
    protected String reportTitle() {
        return "Monthly Flight Plan Statistics";
    }

    @Override
    protected String buildBody() {
        final String monthName =
                Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        final StringBuilder sb = new StringBuilder();
        sb.append("Reference period : ").append(monthName).append(" ").append(year).append("\n");
        sb.append("Total flight plans created: ").append(totalFlightPlans).append("\n");

        sb.append("\n--- Flight Plans by Status ---\n");
        if (countByStatus.isEmpty()) {
            sb.append("  (no data)\n");
        } else {
            countByStatus.forEach((status, count) ->
                    sb.append(String.format("  %-12s : %d%n", status, count)));
        }

        sb.append("\n--- Flight Plans by Route ---\n");
        if (countByRoute.isEmpty()) {
            sb.append("  (no data)\n");
        } else {
            countByRoute.forEach((route, count) ->
                    sb.append(String.format("  %-12s : %d%n", route, count)));
        }

        return sb.toString();
    }
}