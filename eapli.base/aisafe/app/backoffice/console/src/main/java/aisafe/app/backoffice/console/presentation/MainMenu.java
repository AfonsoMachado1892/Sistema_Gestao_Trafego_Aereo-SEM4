package aisafe.app.backoffice.console.presentation;

import aisafe.Application;
import aisafe.app.backoffice.console.presentation.airtransportcompany.AirTransportCompanyMenuUI;
import aisafe.app.backoffice.console.presentation.flightplan.PilotMenuUI;
import aisafe.app.backoffice.console.presentation.weather.WeatherPersonMenuUI;
import aisafe.app.backoffice.console.presentation.simulation.FlightControlOperatorMenuUI;
import aisafe.app.common.console.presentation.authz.LogoutAndAskUI;
import aisafe.app.common.console.presentation.authz.MyUserMenu;
import aisafe.infrastructure.bootstrappers.WeatherPersonBootstrapper;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.menu.HorizontalMenuRenderer;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.MenuRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public class MainMenu extends AbstractUI {

    private static final String SEPARATOR_LABEL = "--------------";
    private static final int LOGOUT_OPTION = 0;

    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    private Menu menu;
    private MenuRenderer renderer;

    public MainMenu() {
        rebuildMenu();
    }

    private void rebuildMenu() {
        menu = buildMainMenu();
        renderer = getRenderer(menu);
    }

    private MenuRenderer getRenderer(final Menu menu) {
        if (Application.settings().isMenuLayoutHorizontal()) {
            return new HorizontalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
        }
        return new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);
    }

    @Override
    public boolean doShow() {
        rebuildMenu();
        return renderer.render();
    }

    @Override
    public boolean show() {
        drawFormTitle();
        return doShow();
    }

    @Override
    public String headline() {
        return authz.session()
                .map(s -> "Backoffice [ @" + s.authenticatedUser().identity() + " ]")
                .orElse("Backoffice [ ==Anonymous== ]");
    }

    private Menu buildMainMenu() {
        final Menu mainMenu = new Menu();

        int option = 1;

        final Menu myUserMenu = new MyUserMenu(AiSafeRoles.ADMIN);
        mainMenu.addSubMenu(option++, myUserMenu);

        if (canAccessBackofficeConfig()) {
            mainMenu.addItem(MenuItem.separator(SEPARATOR_LABEL));
            mainMenu.addItem(option++, "Backoffice Config >", new BackofficeConfigUI()::show);
        }

        if (hasRole(AiSafeRoles.PILOT)) {
            mainMenu.addItem(MenuItem.separator(SEPARATOR_LABEL));
            mainMenu.addItem(option++, "Pilot Menu >", new PilotMenuUI()::show);
        }

        if (hasRole(AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR)) {
            mainMenu.addItem(MenuItem.separator(SEPARATOR_LABEL));
            mainMenu.addItem(option++, "Air Transport Company Menu >",
                    new AirTransportCompanyMenuUI()::show);
        }

        if(hasRole(AiSafeRoles.WEATHER_PERSON)){
            mainMenu.addItem(MenuItem.separator(SEPARATOR_LABEL));
            mainMenu.addItem(option++,"Weather Person Menu >",
                    new WeatherPersonMenuUI()::show);
        }

        if (hasRole(AiSafeRoles.FLIGHT_CONTROL_OPERATOR)) {
            mainMenu.addItem(MenuItem.separator(SEPARATOR_LABEL));
            mainMenu.addItem(option++, "Flight Control Operator Menu >",
                    new FlightControlOperatorMenuUI()::show);
        }

        mainMenu.addItem(MenuItem.separator(SEPARATOR_LABEL));
        mainMenu.addItem(LOGOUT_OPTION, "Logout", new LogoutAndAskUI()::show);

        return mainMenu;
    }

    private boolean canAccessBackofficeConfig() {
        return hasAnyRole(
                AiSafeRoles.ADMIN,
                AiSafeRoles.BACKOFFICE_OPERATOR
        );
    }

    private boolean hasAnyRole(final Role... roles) {
        return authz.session()
                .map(session -> session.authenticatedUser().hasAny(roles))
                .orElse(false);
    }

    private boolean hasRole(final Role role) {
        return hasAnyRole(role);
    }
}