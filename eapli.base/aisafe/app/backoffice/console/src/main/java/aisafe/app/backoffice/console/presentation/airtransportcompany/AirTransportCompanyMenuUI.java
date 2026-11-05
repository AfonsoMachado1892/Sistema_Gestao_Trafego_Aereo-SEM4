package aisafe.app.backoffice.console.presentation.airtransportcompany;

import aisafe.app.backoffice.console.presentation.aircraft.AddAircraftUI;
import aisafe.app.backoffice.console.presentation.aircraft.DecommissionAircraftUI;
import aisafe.app.backoffice.console.presentation.aircraft.ListCompanyFleetUI;
import aisafe.app.backoffice.console.presentation.flightroute.CreateFlightRouteUI;
import aisafe.app.backoffice.console.presentation.flightroute.DeactivateFlightRouteUI;
import aisafe.app.backoffice.console.presentation.flightroute.ListFlightRoutesUI;
import aisafe.app.backoffice.console.presentation.pilot.AddPilotUI;
import aisafe.app.backoffice.console.presentation.pilot.ListPilotRosterUI;
import aisafe.app.backoffice.console.presentation.pilot.RemovePilotUI;
import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public class AirTransportCompanyMenuUI extends AbstractUI {

    @Override
    protected boolean doShow() {
        final Menu menu = buildMenu();
        final var renderer = new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
        boolean keepShowing;
        do {
            keepShowing = renderer.render();
        } while (keepShowing);
        return false;
    }

    private Menu buildMenu() {
        final Menu menu = new Menu("Air Transport Company Menu >");

        // ── Fleet ─────────────────────────────────────────────────────────────
        menu.addItem(MenuItem.of(1, "Add Aircraft to Fleet", () -> {
            new AddAircraftUI().show();
            return true;
        }));
        menu.addItem(MenuItem.of(2, "Decommission Aircraft", () -> {
            new DecommissionAircraftUI().show();
            return true;
        }));
        menu.addItem(MenuItem.of(3, "List Company Fleet", () -> {
            new ListCompanyFleetUI().show();
            return true;
        }));

        // ── Routes ────────────────────────────────────────────────────────────
        menu.addItem(MenuItem.of(4, "Create Flight Route", () -> {
            new CreateFlightRouteUI().show();
            return true;
        }));
        menu.addItem(MenuItem.of(5, "List Flight Routes", () -> {
            new ListFlightRoutesUI().show();
            return true;
        }));
        menu.addItem(MenuItem.of(6, "Deactivate Flight Route", () -> {
            new DeactivateFlightRouteUI().show();
            return true;
        }));

        // ── Pilots ────────────────────────────────────────────────────────────
        menu.addItem(MenuItem.of(7, "Add Pilot", () -> {
            new AddPilotUI().show();
            return true;
        }));
        menu.addItem(MenuItem.of(8, "List Pilot Roster", () -> {
            new ListPilotRosterUI().show();
            return true;
        }));
        menu.addItem(MenuItem.of(9, "Remove Pilot", () -> {
            new RemovePilotUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(0, "Return", () -> false));
        return menu;
    }

    @Override
    public String headline() {
        return "Air Transport Company Menu";
    }
}
