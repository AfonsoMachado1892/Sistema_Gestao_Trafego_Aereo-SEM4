package aisafe.app.backoffice.console.presentation.pilot;

import aisafe.persistence.PersistenceContext;
import aisafe.pilot.application.PilotDTO;
import aisafe.pilot.application.RemovePilotController;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.List;

/**
 * Console UI for US077 — Remove a Pilot.
 */
@SuppressWarnings("squid:S106")
public class RemovePilotUI extends AbstractUI {

    private static final String LINE =
            "+-----+--------------------------------+------------------------+----------+";
    private static final String HEADER =
            "| %-3s | %-30s | %-22s | %-8s |%n";
    private static final String ROW =
            "| %3d | %-30s | %-22s | %8d |%n";

    private final RemovePilotController controller = new RemovePilotController(
            PersistenceContext.repositories().pilots(),
            PersistenceContext.repositories().aircraftModels(),
            PersistenceContext.repositories().flightPlans(),
            PersistenceContext.repositories().customerCollaborators()
    );

    @Override
    protected boolean doShow() {
        final List<PilotDTO> pilots = controller.getActivePilots();

        if (pilots.isEmpty()) {
            System.out.println("\n  No active pilots found for your company.\n");
            return false;
        }

        printPilotTable(pilots);

        final int selection = readSelection(pilots.size());
        if (selection == 0) {
            System.out.println("\n  Operation cancelled. Returning to menu.\n");
            return false;
        }

        final PilotDTO selected = pilots.get(selection - 1);
        printSummary(selected);

        final String confirm = Console.readLine("\n  Confirm removal? (y/n): ");
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("\n  Operation cancelled.\n");
            return false;
        }

        try {
            final PilotDTO result = controller.removePilot(selected.pilotID);
            System.out.printf("%n  Pilot \"%s\" successfully removed (status: %s).%n%n",
                    result.name, result.status);
            return true;
        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println("\n  Error: " + e.getMessage() + "\n");
            return false;
        }
    }

    private void printPilotTable(final List<PilotDTO> pilots) {
        System.out.printf("%n  Active Pilots (%d)%n%n", pilots.size());
        System.out.println(LINE);
        System.out.printf(HEADER, "#", "Name", "Company / Pilot ID", "Certs");
        System.out.println(LINE);
        for (int i = 0; i < pilots.size(); i++) {
            final PilotDTO p = pilots.get(i);
            System.out.printf(ROW,
                    i + 1,
                    truncate(p.name, 30),
                    truncate(p.companyIata + " / " + p.pilotID, 22),
                    p.certificationCount);
        }
        System.out.println(LINE);
    }

    private void printSummary(final PilotDTO pilot) {
        System.out.println("\n  Selected pilot:");
        System.out.println("  ----------------------------------------");
        System.out.printf("   Name           : %s%n", pilot.name);
        System.out.printf("   Pilot ID       : %s%n", pilot.pilotID);
        System.out.printf("   Company        : %s%n", pilot.companyIata);
        System.out.printf("   Status         : %s%n", pilot.status);
        System.out.printf("   Certifications : %d%n", pilot.certificationCount);
        System.out.println("  ----------------------------------------");
    }

    private int readSelection(final int max) {
        int index;
        do {
            index = Console.readInteger("\n  Select pilot to remove (1-" + max + ", 0 to go back): ");
            if (index < 0 || index > max) {
                System.out.println("  Invalid selection. Choose between 1 and " + max
                        + ", or 0 to go back.");
            }
        } while (index < 0 || index > max);
        return index;
    }

    private static String truncate(final String value, final int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }

    @Override
    public String headline() {
        return "Remove Pilot";
    }
}