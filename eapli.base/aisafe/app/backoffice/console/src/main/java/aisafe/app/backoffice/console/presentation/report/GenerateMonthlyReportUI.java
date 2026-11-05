package aisafe.app.backoffice.console.presentation.report;

import aisafe.persistence.PersistenceContext;
import aisafe.report.application.GenerateMonthlyReportController;
import aisafe.report.application.MonthlyReportDTO;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.time.LocalDate;

@SuppressWarnings("squid:S106")
public class GenerateMonthlyReportUI extends AbstractUI {

    private final GenerateMonthlyReportController controller =
            new GenerateMonthlyReportController(
                    PersistenceContext.repositories().flightPlans());

    @Override
    protected boolean doShow() {
        try {
            final int currentYear = LocalDate.now().getYear();

            final int year = Console.readInteger(
                    "\nYear (e.g. " + currentYear + ", 0 to cancel):");
            if (year == 0) {
                System.out.println("Operation cancelled.");
                return false;
            }

            final int month = Console.readInteger("Month (1-12, 0 to cancel):");
            if (month == 0) {
                System.out.println("Operation cancelled.");
                return false;
            }
            if (month < 1 || month > 12) {
                System.out.println("Invalid month. Must be between 1 and 12.");
                return false;
            }

            System.out.println("\n── Configuration ────────────────────────────────");
            System.out.printf("  Report : Monthly Flight Plan Statistics%n");
            System.out.printf("  Period : %02d/%d%n", month, year);
            System.out.println("─────────────────────────────────────────────────");

            final String confirm = Console.readLine("\nGenerate report? (y/n):");
            if (!confirm.equalsIgnoreCase("y")) {
                System.out.println("Operation cancelled.");
                return false;
            }

            final MonthlyReportDTO report =
                    controller.generateMonthlyReport(year, month);

            System.out.println();
            System.out.println(report.renderedReport);
            System.out.printf("Report saved to: %s%n", report.reportFilePath);

        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (final Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Generate Monthly Report";
    }
}