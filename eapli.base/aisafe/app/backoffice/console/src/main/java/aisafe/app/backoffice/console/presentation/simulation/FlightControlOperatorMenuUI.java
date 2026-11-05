package aisafe.app.backoffice.console.presentation.simulation;

import aisafe.app.backoffice.console.presentation.report.GenerateMonthlyReportUI;
import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public class FlightControlOperatorMenuUI extends AbstractUI {

    private int option = 0;

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
        final Menu menu = new Menu("Flight Control Operator Menu >");

        menu.addItem(MenuItem.of(nextOption(), "Generate Simulation Report", () -> {
            new GenerateSimulationReportUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(nextOption(), "Generate Monthly Report", () -> {
            new GenerateMonthlyReportUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(0, "Return", () -> false));
        return menu;
    }

    private int nextOption() {
        return ++option;
    }

    @Override
    public String headline() {
        return "Flight Control Operator Menu";
    }
}