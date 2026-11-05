package aisafe.app.backoffice.console.presentation.weather;

import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public class WeatherPersonMenuUI  extends AbstractUI {

    int option = 0;

    @Override
    protected boolean doShow() {
        final Menu menu = buildMenu();
        final var renderer = new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);

        boolean keepShowing;
        do {
            keepShowing = renderer.render();
        } while (keepShowing);

        return false;    }

    private Menu buildMenu(){

        final Menu menu = new Menu("Weather Person Menu >");
        menu.addItem(MenuItem.of(nextOption(), "Register Weather Data", () -> {
            new RegisterWeatherDataUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(nextOption(), "Consult Weather Data", () -> {
            new ListWeatherDataUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(nextOption(), "Import Weather Data from File", () -> {
            new ImportBulkWeatherDataUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(0, "Return", () -> false));

        return menu;

    }

    private int nextOption(){
        return ++option;
    }

    @Override
    public String headline() {
        return "Weather Person Menu";
    }
}
