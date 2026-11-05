package aisafe.app.backoffice.console.presentation.flightplan;

import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public class PilotMenuUI extends AbstractUI {

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
        final Menu menu = new Menu("Pilot Menu >");

        menu.addItem(MenuItem.of(1, "Create Flight Plan", () -> {
            new CreateFlightPlanUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(2, "Import Flight Plan from File", () -> {
            new ImportFlightPlanFromFileUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(3, "List My Flight Plans", () -> {
            new ListCreatedFlightPlansUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(4, "Insert Weather Data in Flight Plan", () -> {
            new InsertWeatherDataInFlightUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(5, "Test / Validate Flight Plan", () -> {
            new TestFlightPlanUI().show();
            return true;
        }));



        menu.addItem(MenuItem.of(0, "Return", () -> false));

        return menu;
    }

    @Override
    public String headline() {
        return "Pilot Menu";
    }
}