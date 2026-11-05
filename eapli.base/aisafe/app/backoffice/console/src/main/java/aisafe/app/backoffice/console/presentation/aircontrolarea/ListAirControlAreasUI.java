package aisafe.app.backoffice.console.presentation.aircontrolarea;

import aisafe.aircontrolarea.application.RegisterAirControlAreaController;
import aisafe.aircontrolarea.domain.AirControlArea;
import aisafe.persistence.PersistenceContext;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("squid:S106")
public class ListAirControlAreasUI extends AbstractUI {

    private static final int PAGE_SIZE = 10;

    private final RegisterAirControlAreaController controller =
            new RegisterAirControlAreaController(
                    PersistenceContext.repositories().airControlAreas()
            );

    @Override
    protected boolean doShow() {
        final List<AirControlArea> areas = new ArrayList<>();
        controller.listAirControlAreas().forEach(areas::add);

        if (areas.isEmpty()) {
            System.out.println("\nNo air control areas registered.");
            return false;
        }

        System.out.printf("%nTotal: %d air control area(s)%n", areas.size());

        int page = 0;
        final int totalPages = (int) Math.ceil((double) areas.size() / PAGE_SIZE);

        while (true) {
            final int from = page * PAGE_SIZE;
            final int to = Math.min(from + PAGE_SIZE, areas.size());

            System.out.printf("%nPage %d of %d%n", page + 1, totalPages);
            System.out.printf("%-10s %-25s %s%n", "Code", "Name", "Boundary");
            System.out.println("-".repeat(80));

            for (int i = from; i < to; i++) {
                final AirControlArea area = areas.get(i);
                System.out.printf("%-10s %-25s %s%n",
                        area.code(),
                        area.name(),
                        area.boundaryDescription());
            }

            if (totalPages == 1) break;

            System.out.println();
            if (page > 0) System.out.println("  p. Previous page");
            if (page < totalPages - 1) System.out.println("  n. Next page");
            System.out.println("  q. Quit");

            final String input = Console.readLine("Option:").trim().toLowerCase();

            if (input.equals("q")) break;
            if (input.equals("n") && page < totalPages - 1) {
                page++;
            } else if (input.equals("p") && page > 0) {
                page--;
            } else {
                System.out.println("Invalid option.");
            }
        }

        return false;
    }

    @Override
    public String headline() {
        return "List Air Control Areas";
    }
}