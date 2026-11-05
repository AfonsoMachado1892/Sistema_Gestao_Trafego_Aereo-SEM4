package aisafe.report.application;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Abstract base for all AISafe reports (US112 – foundational).
 * Applies the Template Method pattern so every report type shares the same
 * branding and structure (header + body + footer); each concrete report
 * defines only its own title and body.
 */
public abstract class Report {

    private static final String BRAND = "AISafe";
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    protected abstract String reportTitle();

    protected abstract String buildBody();

    public final String render() {
        final StringBuilder sb = new StringBuilder();
        sb.append(buildHeader());
        sb.append(buildBody());
        sb.append(buildFooter());
        return sb.toString();
    }

    private String buildHeader() {
        final StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append("  ").append(BRAND).append(" - ").append(reportTitle()).append("\n");
        sb.append("  Generated at: ")
                .append(LocalDateTime.now().format(TIMESTAMP)).append("\n");
        sb.append("==================================================\n\n");
        return sb.toString();
    }

    private String buildFooter() {
        final StringBuilder sb = new StringBuilder();
        sb.append("\n==================================================\n");
        sb.append("  End of report - (C) ").append(BRAND).append("\n");
        sb.append("==================================================\n");
        return sb.toString();
    }
}