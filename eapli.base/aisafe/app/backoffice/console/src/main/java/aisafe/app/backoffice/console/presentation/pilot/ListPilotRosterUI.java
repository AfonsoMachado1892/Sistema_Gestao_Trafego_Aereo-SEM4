package aisafe.app.backoffice.console.presentation.pilot;

import aisafe.persistence.PersistenceContext;
import aisafe.pilot.application.ListPilotRosterController;
import aisafe.pilot.application.PilotDTO;
import eapli.framework.presentation.console.AbstractUI;

import java.util.List;

/**
 * Console UI for US076 — List Pilot Roster.
 */
@SuppressWarnings("squid:S106")
public class ListPilotRosterUI extends AbstractUI {

    private static final String LINE =
            "+--------------------------------+------------------------+--------+----------+";
    private static final String HEADER =
            "| %-30s | %-22s | %-6s | %-8s |%n";
    private static final String ROW =
            "| %-30s | %-22s | %-6s | %8d |%n";

    private final ListPilotRosterController controller = new ListPilotRosterController(
            PersistenceContext.repositories().pilots(),
            PersistenceContext.repositories().aircraftModels(),
            PersistenceContext.repositories().customerCollaborators()
    );

    @Override
    protected boolean doShow() {
        final List<PilotDTO> pilots = controller.listPilotRoster();

        if (pilots.isEmpty()) {
            System.out.println("\n  No pilots found for your company.\n");
            return false;
        }

        System.out.printf("%n  Pilot Roster (%d pilot(s))%n%n", pilots.size());
        System.out.println(LINE);
        System.out.printf(HEADER, "Name", "Pilot ID", "Status", "Certs");
        System.out.println(LINE);
        for (final PilotDTO pilot : pilots) {
            System.out.printf(ROW,
                    truncate(pilot.name, 30),
                    truncate(pilot.pilotID, 22),
                    pilot.status,
                    pilot.certificationCount);
        }
        System.out.println(LINE);
        System.out.println();

        return false;
    }

    private static String truncate(final String value, final int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max - 1) + "…";
    }

    @Override
    public String headline() {
        return "List Pilot Roster";
    }
}