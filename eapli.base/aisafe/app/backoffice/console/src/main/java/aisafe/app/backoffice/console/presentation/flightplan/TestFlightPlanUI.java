package aisafe.app.backoffice.console.presentation.flightplan;

import aisafe.flightplan.application.FlightPlanDTO;
import aisafe.flightplan.application.FlightPlanTestBoard;
import aisafe.flightplan.application.TestFlightPlanController;
import aisafe.persistence.PersistenceContext;
import eapli.framework.infrastructure.authz.application.exceptions.UnauthorizedException;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("squid:S106")
public class TestFlightPlanUI extends AbstractUI {

    // ── ANSI colours ─────────────────────────────────────────────────────────
    private static final String RESET   = "\u001B[0m";
    private static final String BOLD    = "\u001B[1m";
    private static final String GREEN   = "\u001B[32m";
    private static final String RED     = "\u001B[31m";
    private static final String YELLOW  = "\u001B[33m";
    private static final String CYAN    = "\u001B[36m";
    private static final String BLUE    = "\u001B[34m";
    private static final String WHITE   = "\u001B[37m";
    private static final String BG_GREEN = "\u001B[42m";
    private static final String BG_RED   = "\u001B[41m";

    private static final String CANCEL = "0";
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final int PROGRESS_BAR_WIDTH = 40;

    private final TestFlightPlanController controller =
            new TestFlightPlanController(
                    PersistenceContext.repositories().flightPlans(),
                    PersistenceContext.repositories().flightRoutes(),
                    PersistenceContext.repositories().aircrafts(),
                    PersistenceContext.repositories().airports(),
                    PersistenceContext.repositories().weatherData(),
                    PersistenceContext.repositories().pilots()
            );

    @Override
    protected boolean doShow() {
        showHeader();

        try {
            final FlightPlanDTO selectedFlightPlan = chooseDraftFlightPlan();

            if (selectedFlightPlan == null) {
                return false;
            }

            showSelectedFlightPlan(selectedFlightPlan);

            if (!confirmTest()) {
                System.out.println();
                System.out.println(YELLOW + "  Flight plan test/validation cancelled." + RESET);
                return false;
            }

            // Run the test first so progress can show live metrics
            final FlightPlanTestBoard board =
                    controller.testAndValidate(selectedFlightPlan.flightPlanID());

            // Make board available to animatePlaneBar for turbulence + preview
            this.progressBoard = board;

            showProgress();
            showFinalReport(board, selectedFlightPlan);

        } catch (final UnauthorizedException e) {
            System.out.println();
            System.out.println(RED + "  You are not authorized to test/validate flight plans." + RESET);
            System.out.println(RED + "  Please login with a Pilot account." + RESET);

        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println();
            System.out.println(RED + "  Could not test/validate flight plan." + RESET);
            System.out.println(RED + "  Reason: " + e.getMessage() + RESET);
        }

        return false;
    }

    @Override
    public String headline() {
        return "Test Flight Plan";
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private void showHeader() {
        System.out.println();
        System.out.println(CYAN + "                                         |"        + RESET);
        System.out.println(CYAN + "                                         |"        + RESET);
        System.out.println(CYAN + "                                         |"        + RESET);
        System.out.println(CYAN + "                                       .-'-."      + RESET);
        System.out.println(CYAN + "                                      ' ___ '"     + RESET);
        System.out.println(CYAN + "                            ---------'  .-.  '---------" + RESET);
        System.out.println(CYAN + "             ________________________'  '-'  '________________________" + RESET);
        System.out.println(CYAN + "             ''''''-|---|--/    \\==][^',_m_,'^][==/    \\--|---|-''''" + RESET);
        System.out.println(CYAN + "                           \\    /  ||/   H   \\||  \\    /" + RESET);
        System.out.println(CYAN + "                            '--'   OO   O|O   OO   '--'" + RESET);
        System.out.println();
        System.out.println(BOLD + CYAN + "  ╔══════════════════════════════════════════════════════╗" + RESET);
        System.out.println(BOLD + CYAN + "  ║              ✈  AISAFE TEST CENTER  ✈               ║" + RESET);
        System.out.println(BOLD + CYAN + "  ║       Flight Plan Validation and Test Simulation     ║" + RESET);
        System.out.println(BOLD + CYAN + "  ╚══════════════════════════════════════════════════════╝" + RESET);
        System.out.println();
        System.out.println(WHITE + "  This operation validates and tests one of your DRAFT flight plans." + RESET);
        System.out.println(WHITE + "  Only validated flight plans can be used operationally." + RESET);
        System.out.println();
    }

    // ── Choose flight plan ────────────────────────────────────────────────────

    private FlightPlanDTO chooseDraftFlightPlan() {
        final List<FlightPlanDTO> draftPlans = new ArrayList<>();

        try {
            controller.listDraftFlightPlans().forEach(draftPlans::add);
        } catch (final UnauthorizedException e) {
            throw e;
        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println(RED + "  Could not load draft flight plans." + RESET);
            System.out.println(RED + "  Reason: " + e.getMessage() + RESET);
            return null;
        }

        if (draftPlans.isEmpty()) {
            System.out.println(YELLOW + "  No DRAFT flight plans are available for testing." + RESET);
            return null;
        }

        System.out.println(BOLD + "  Draft Flight Plans" + RESET);
        System.out.println(CYAN + "  ╔════╦════════════╦══════════╦═══════════════╦══════════════════╦══════════════════╗" + RESET);
        System.out.println(CYAN + "  ║  # ║  Plan ID   ║  Route   ║   Aircraft    ║    Departure     ║     Arrival      ║" + RESET);
        System.out.println(CYAN + "  ╠════╬════════════╬══════════╬═══════════════╬══════════════════╬══════════════════╣" + RESET);

        for (int i = 0; i < draftPlans.size(); i++) {
            final FlightPlanDTO fp = draftPlans.get(i);
            System.out.printf(CYAN + "  ║" + RESET + " %-2d " + CYAN + "║" + RESET
                            + " %-10s " + CYAN + "║" + RESET
                            + " %-8s " + CYAN + "║" + RESET
                            + " %-13s " + CYAN + "║" + RESET
                            + " %-15s " + CYAN + "║" + RESET
                            + " %-15s " + CYAN + "║" + RESET + "%n",
                    i + 1,
                    shortId(fp.flightPlanID()),
                    fp.routeCode(),
                    fp.aircraftRegistration(),
                    formatDateTime(fp.plannedDepartureDateTime()),
                    formatDateTime(fp.plannedArrivalDateTime()));
        }

        System.out.println(CYAN + "  ╚════╩════════════╩══════════╩═══════════════╩══════════════════╩══════════════════╝" + RESET);
        System.out.println();
        System.out.println("  " + YELLOW + "0" + RESET + ". Cancel");
        System.out.println();

        while (true) {
            final String input = Console.readLine("  Choose flight plan:").trim();

            if (input.equals(CANCEL)) {
                System.out.println(YELLOW + "  Flight plan test/validation cancelled." + RESET);
                return null;
            }

            if (input.isBlank()) {
                System.out.println(RED + "  Please insert an option." + RESET);
                continue;
            }

            try {
                final int option = Integer.parseInt(input);

                if (option >= 1 && option <= draftPlans.size()) {
                    return draftPlans.get(option - 1);
                }

                System.out.printf(RED + "  Invalid option. Choose a number between 1 and %d.%n" + RESET,
                        draftPlans.size());

            } catch (final NumberFormatException e) {
                System.out.println(RED + "  Invalid option. Please insert a valid number." + RESET);
            }
        }
    }

    // ── Selected flight plan ──────────────────────────────────────────────────

    private void showSelectedFlightPlan(final FlightPlanDTO flightPlan) {
        System.out.println();
        System.out.println(BOLD + "  Selected Flight Plan" + RESET);
        System.out.println(CYAN + "  ╔════════════════════════════════════════════════════════════╗" + RESET);
        System.out.printf( CYAN + "  ║" + RESET + "  Flight Plan ID  : %-40s" + CYAN + "║%n" + RESET, shortId(flightPlan.flightPlanID()));
        System.out.printf( CYAN + "  ║" + RESET + "  Route           : %-40s" + CYAN + "║%n" + RESET, flightPlan.routeCode());
        System.out.printf( CYAN + "  ║" + RESET + "  Aircraft        : %-40s" + CYAN + "║%n" + RESET, flightPlan.aircraftRegistration());
        System.out.printf( CYAN + "  ║" + RESET + "  Pilot           : %-40s" + CYAN + "║%n" + RESET, controller.pilotDisplayNameFor(flightPlan));
        System.out.printf( CYAN + "  ║" + RESET + "  Departure       : %-40s" + CYAN + "║%n" + RESET, formatDateTime(flightPlan.plannedDepartureDateTime()));
        System.out.printf( CYAN + "  ║" + RESET + "  Arrival         : %-40s" + CYAN + "║%n" + RESET, formatDateTime(flightPlan.plannedArrivalDateTime()));
        System.out.printf( CYAN + "  ║" + RESET + "  Fuel            : %-40s" + CYAN + "║%n" + RESET, flightPlan.fuelAmount() + " " + flightPlan.fuelUnit());
        System.out.printf( CYAN + "  ║" + RESET + "  Status          : %-49s" + CYAN + "║%n" + RESET, YELLOW + flightPlan.status() + RESET);
        System.out.println(CYAN + "  ╚════════════════════════════════════════════════════════════╝" + RESET);
        System.out.println();
    }

    // ── Confirm ───────────────────────────────────────────────────────────────

    private boolean confirmTest() {
        while (true) {
            final String confirm = Console.readLine(
                            "  " + BOLD + "Confirm test/validation?" + RESET + " (y/n): ")
                    .trim()
                    .toLowerCase();

            if (confirm.equals("y")) return true;
            if (confirm.equals("n")) return false;

            System.out.println(RED + "  Invalid option. Please answer 'y' or 'n'." + RESET);
        }
    }

    // ── Progress ──────────────────────────────────────────────────────────────

    private static final String[] STEPS = {
            "Checking flight plan ownership and status",
            "Loading route, aircraft, pilot and airports",
            "Validating domain rules",
            "Building simulated flight plan",
            "Executing flight plan test",
            "Storing result and updating status"
    };

    // Index of the "Executing flight plan test" step
    private static final int TEST_STEP_INDEX = 4;

    // Stored after the controller call to show in progress preview
    private FlightPlanTestBoard progressBoard = null;

    private void showProgress() {
        System.out.println();
        System.out.println(BOLD + "  Initiating flight plan validation sequence..." + RESET);
        System.out.println();

        for (int i = 0; i < STEPS.length; i++) {
            animatePlaneBar(i, STEPS[i]);
        }

        System.out.println();
        System.out.println(GREEN + BOLD + "  All systems go. Computing final result..." + RESET);
        System.out.println();

        sleep(400);
    }

    private void animatePlaneBar(final int stepIndex, final String label) {
        final int frames = 20;
        final int barWidth = PROGRESS_BAR_WIDTH;

        // Weather turbulence only on the test execution step
        final boolean isTestStep = stepIndex == TEST_STEP_INDEX;

        for (int frame = 0; frame <= frames; frame++) {
            final int filled = (frame * barWidth) / frames;
            final int planePos = Math.max(0, filled - 1);

            final StringBuilder bar = new StringBuilder();

            // On the test step with weather, add turbulence colour shifts
            final String trackColour = isTestStep && progressBoard != null
                    && progressBoard.weatherUsed() ? YELLOW : GREEN;

            bar.append(trackColour);

            for (int j = 0; j < barWidth; j++) {
                if (j < planePos) {
                    // Turbulence: random ~ on some positions when weather active
                    if (isTestStep && progressBoard != null
                            && progressBoard.weatherUsed()
                            && j % 5 == 3) {
                        bar.append(RESET).append(YELLOW).append("~").append(RESET).append(trackColour);
                    } else {
                        bar.append("═");
                    }
                } else if (j == planePos) {
                    bar.append(RESET).append(CYAN).append("✈").append(RESET).append(trackColour);
                } else {
                    bar.append("·");
                }
            }

            bar.append(RESET);

            final int percent = (frame * 100) / frames;

            // Weather warning suffix on the test step
            final String suffix = isTestStep && progressBoard != null
                    && progressBoard.weatherUsed() && frame > 5 && frame < 15
                    ? YELLOW + " ⛈ Headwind detected! Adjusting..." + RESET
                    : "";

            System.out.printf("\r  [%s] %3d%%  %s%-43s%s%s",
                    bar, percent, WHITE, label, RESET, suffix);

            sleep(38);
        }

        System.out.printf("\r  [%s] %3d%%  %s%-43s%s%n",
                GREEN + "═".repeat(PROGRESS_BAR_WIDTH - 1) + "✔" + RESET,
                100,
                GREEN, label, RESET);

        // Feature 3 — after the test step, show quick preview
        if (isTestStep && progressBoard != null && progressBoard.simulationExecuted()) {
            sleep(150);
            showProgressPreview();
        }

        sleep(80);
    }

    private void showProgressPreview() {
        final double loaded   = progressBoard.loadedFuelKg();
        final double required = progressBoard.estimatedFuelRequiredKg();
        final double pct      = loaded > 0 ? (required / loaded) * 100.0 : 100.0;
        final double distance = progressBoard.totalDistanceKm();

        System.out.println();
        System.out.printf("      %sDistance%s  : %.0f km%n", CYAN, RESET, distance);
        System.out.printf("      %sFuel used%s  : %s  %.0f%%%n",
                CYAN, RESET, fuelBar(pct), pct);
        System.out.println();
    }

    // ── Final report ──────────────────────────────────────────────────────────

    private void showFinalReport(final FlightPlanTestBoard board, final FlightPlanDTO flightPlan) {
        System.out.println();
        System.out.println(BOLD + "  Flight Plan Validation Report" + RESET);
        System.out.println(CYAN + "  ╔════════════════════════════════════════════════════════════════════════╗" + RESET);
        printReportLine("Flight Plan ID",  shortId(board.flightPlanID()));
        printReportLine("Route",           board.routeCode());
        printReportLine("Aircraft",        board.aircraftRegistration());
        printReportLine("Pilot",           controller.pilotDisplayNameFor(flightPlan));
        printReportLine("Tested at",       formatDateTime(board.testedAt()));
        printReportLine("Result",          board.passed()
                ? GREEN + BOLD + "PASSED ✔" + RESET
                : RED   + BOLD + "FAILED ✘" + RESET);
        printReportLine("Final status",    board.passed()
                ? GREEN + board.finalStatus() + RESET
                : RED   + board.finalStatus() + RESET);
        System.out.println(CYAN + "  ╚════════════════════════════════════════════════════════════════════════╝" + RESET);

        System.out.println();
        System.out.println(BOLD + "  Simulation Metrics" + RESET);
        System.out.println(CYAN + "  ╔════════════════════════════════════════════════════════════════════════╗" + RESET);

        if (!board.simulationExecuted()) {
            System.out.println(CYAN + "  ║" + RESET + "  " + YELLOW
                    + "Simulation not executed — validation failed early." + RESET);
            System.out.println(CYAN + "  ║" + RESET);
        } else {
            final double loaded   = board.loadedFuelKg();
            final double required = board.estimatedFuelRequiredKg();
            final double remaining = board.estimatedFuelRemainingKg();
            final double pct = loaded > 0 ? (required / loaded) * 100.0 : 100.0;

            printReportLine("Total distance",          String.format("%.2f km", board.totalDistanceKm()));
            printReportLine("Loaded fuel",             String.format("%.2f kg", loaded));
            printReportLine("Estimated fuel required", String.format("%.2f kg", required));

            final String remainingColour = remaining >= 0 ? GREEN : RED;
            printReportLine("Estimated fuel remaining",
                    remainingColour + String.format("%.2f kg", remaining) + RESET);

            // Visual fuel bar
            final String bar = fuelBar(pct);
            System.out.printf(CYAN + "  ║" + RESET + "  %-26s: %s %s%.0f%%%s%n",
                    "Fuel usage",
                    bar,
                    pct > 100 ? RED : pct > 85 ? YELLOW : GREEN,
                    pct,
                    RESET);
        }

        System.out.println(CYAN + "  ╚════════════════════════════════════════════════════════════════════════╝" + RESET);

        System.out.println();
        System.out.println(BOLD + "  Weather Data" + RESET);
        System.out.println(CYAN + "  ╔════════════════════════════════════════════════════════════════════════╗" + RESET);

        if (board.weatherUsed()) {
            printReportLine("Used",           GREEN + "Yes ☁" + RESET);
            printReportLine("Wind speed",     String.format("%.2f kts", board.weatherWindSpeed()));
            printReportLine("Wind direction", String.format("%.2f°",    board.weatherWindDirection()));
            printReportLine("Fuel factor",    String.format("%.2f",     board.weatherFuelFactor()));
        } else {
            printReportLine("Used", YELLOW + "No" + RESET);
            System.out.println(CYAN + "  ║" + RESET + "  " + WHITE
                    + "Standard no-weather assumptions were used." + RESET);
        }

        System.out.println(CYAN + "  ╚════════════════════════════════════════════════════════════════════════╝" + RESET);

        System.out.println();
        System.out.println(BOLD + "  Validation Messages" + RESET);
        System.out.println(CYAN + "  ╔════════════════════════════════════════════════════════════════════════╗" + RESET);

        if (board.messages().isEmpty()) {
            System.out.println(CYAN + "  ║" + RESET + "  " + WHITE + "No validation messages." + RESET);
        } else {
            for (final String message : board.messages()) {
                final String colour = message.toLowerCase().contains("passed")
                        || message.toLowerCase().contains("passed all") ? GREEN : WHITE;
                System.out.printf(CYAN + "  ║" + RESET + "  %s- %s%s%n", colour, message, RESET);
            }
        }

        System.out.println(CYAN + "  ╚════════════════════════════════════════════════════════════════════════╝" + RESET);

        System.out.println();

        if (board.passed()) {
            showResultBadge(true);
        } else {
            showResultBadge(false);
        }
    }

    // ── Result badge ──────────────────────────────────────────────────────────

    private void showResultBadge(final boolean passed) {
        if (passed) {
            System.out.println(GREEN + "⠀⠀⠀⣖⠲⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⠉⡇⠀⠀⠀⠀⠀⠀⠀" + RESET);
            System.out.println(GREEN + "⠀⠀⠀⠸⡆⠹⡀⣠⢤⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡏⠀⡧⢤⡄⠀⠀⠀⠀⠀" + RESET);
            System.out.println(GREEN + "⠀⠀⠀⠀⡧⢄⣹⣅⣜⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⠁⠀⢹⠚⠃⠀⠀⠀⠀⠀" + RESET);
            System.out.println(GREEN + "⠀⣀⠴⢒⣉⡹⣶⣤⣀⡉⠉⠒⠒⠒⠤⠤⣀⣀⣀⠇⠀⠀⢸⠠⣄⠀⠀⠀⠀⠀" + RESET);
            System.out.println(GREEN + "⠀⠈⠉⠁⠀⠀⠀⠉⠒⠯⣟⣲⠦⣤⣀⡀⠀⠀⠈⠉⠉⠉⠛⠒⠻⢥⣀⠀⠀⠀" + RESET);
            System.out.println(GREEN + "⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠙⣲⡬⠭⠿⢷⣦⣤⢄⣀⠀⠀⠚⠛⠛⠓⢦⡀" + RESET);
            System.out.println(GREEN + "⠀⠀⠀⠀⠀⠀⠀⢀⣀⠤⠴⠚⠉⠁⠀⠀⠀⠀⣀⣉⡽⣕⣯⡉⠉⠉⠑⢒⣒⡾" + RESET);
            System.out.println(GREEN + "⠀⠀⣀⡠⠴⠒⠉⠉⠀⢀⣀⣀⠤⡤⢶⣶⣋⠉⠉⠀⠀⠀⠈⠉⠉⠉⠉⠉⠁⠀" + RESET);
            System.out.println(GREEN + "⣖⣉⣁⣠⠤⠶⡶⡶⢍⡉⠀⠀⠀⠙⠒⠯⠜⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀" + RESET);
            System.out.println(GREEN + "⠀⠁⠀⠀⠀⠀⠑⢦⣯⠇" + RESET);

            System.out.println();
            System.out.println(BG_GREEN + BOLD + "                                                              " + RESET);
            System.out.println(BG_GREEN + BOLD + "      ✈   FLIGHT PLAN VALIDATED — CLEARED FOR TAKEOFF   ✈    " + RESET);
            System.out.println(BG_GREEN + BOLD + "                                                              " + RESET);
            System.out.println();
            System.out.println(GREEN + BOLD + "  ✔  Flight plan successfully validated." + RESET);
            System.out.println(GREEN + "     The flight plan is now ready for operational use." + RESET);
        } else {
            System.out.println(RED + "  __|__"                                             + RESET);
            System.out.println(RED + "  \\___/"                                            + RESET);
            System.out.println(RED + "   | |"                                              + RESET);
            System.out.println(RED + "   | |"                                              + RESET);
            System.out.println(RED + "  _|_|______________"                                + RESET);
            System.out.println(RED + "           /|\\"                                      + RESET);
            System.out.println(RED + "         */ | \\*"                                    + RESET);
            System.out.println(RED + "         / -+- \\"                                   + RESET);
            System.out.println(RED + "     ---o--(_)--o---"                                 + RESET);
            System.out.println(RED + "       /  0 \" 0  \\"                               + RESET);
            System.out.println(RED + "     */     |     \\*"                               + RESET);
            System.out.println(RED + "     /      |      \\"                              + RESET);
            System.out.println(RED + "   */       |       \\*"                             + RESET);
            System.out.println();
            System.out.println(BG_RED + BOLD + "                                                              " + RESET);
            System.out.println(BG_RED + BOLD + "      ✘   FLIGHT PLAN REJECTED — NOT CLEARED FOR TAKEOFF      " + RESET);
            System.out.println(BG_RED + BOLD + "                                                              " + RESET);
            System.out.println();
            System.out.println(RED + BOLD + "  ✘  Flight plan rejected." + RESET);
            System.out.println(RED + "     Please review the report and create a new draft." + RESET);
        }

        System.out.println();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void printReportLine(final String label, final String value) {
        System.out.printf(CYAN + "  ║" + RESET + "  %-26s: %s%n", label, value);
    }

    /**
     * Builds a 20-char visual fuel bar.
     * Green  ≤ 85%  safe
     * Yellow  > 85% tight
     * Red     > 100% over capacity / insufficient
     */
    private String fuelBar(final double pct) {
        final int barWidth = 20;
        final int filled   = (int) Math.min(Math.round((pct / 100.0) * barWidth), barWidth);
        final String colour = pct > 100 ? RED : pct > 85 ? YELLOW : GREEN;

        return colour
                + "█".repeat(filled)
                + RESET
                + WHITE
                + "░".repeat(Math.max(0, barWidth - filled))
                + RESET;
    }

    private String formatDateTime(final LocalDateTime dateTime) {
        if (dateTime == null) return "-";
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    private String shortId(final Object id) {
        if (id == null) return "-";
        final String value = id.toString();
        return value.length() <= 8 ? value : value.substring(0, 8);
    }

    private void sleep(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}