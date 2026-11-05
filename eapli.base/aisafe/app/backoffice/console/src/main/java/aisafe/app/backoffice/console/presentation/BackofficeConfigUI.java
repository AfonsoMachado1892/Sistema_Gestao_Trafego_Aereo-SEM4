package aisafe.app.backoffice.console.presentation;

import aisafe.app.backoffice.console.presentation.aircraftmodel.AddEngineModelToAircraftModelUI;
import aisafe.app.backoffice.console.presentation.aircraftmodel.RegisterAircraftModelUI;
import aisafe.app.backoffice.console.presentation.aircraftmodel.RemoveEngineModelFromAircraftModelUI;
import aisafe.app.backoffice.console.presentation.aircontrolarea.ListAirControlAreasUI;
import aisafe.app.backoffice.console.presentation.aircontrolarea.RegisterAirControlAreaUI;
import aisafe.app.backoffice.console.presentation.airport.ListAirportsUI;
import aisafe.app.backoffice.console.presentation.airport.RegisterAirportUI;
import aisafe.app.backoffice.console.presentation.airtransportcompany.RegisterAirTransportCompanyUI;
import aisafe.app.backoffice.console.presentation.authz.AddUserAction;
import aisafe.app.backoffice.console.presentation.authz.DisableEnableUserUI;
import aisafe.app.backoffice.console.presentation.customercollaborator.AddCustomerCollaboratorUI;
import aisafe.app.backoffice.console.presentation.customercollaborator.ListCustomerCollaboratorsUI;
import aisafe.app.backoffice.console.presentation.enginemodel.RegisterEngineModelUI;
import aisafe.app.backoffice.console.presentation.weather.ImportBulkWeatherDataUI;
import aisafe.app.backoffice.console.presentation.weather.ListWeatherDataUI;
import aisafe.app.backoffice.console.presentation.weather.RegisterWeatherDataUI;
import aisafe.app.backoffice.console.presentation.simulation.GenerateSimulationReportUI;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.exceptions.UnauthorizedException;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.presentation.console.AbstractUI;
import eapli.framework.presentation.console.menu.MenuItemRenderer;
import eapli.framework.presentation.console.menu.VerticalMenuRenderer;

public class BackofficeConfigUI extends AbstractUI {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    private int optionNumber;

    @Override
    protected boolean doShow() {
        try {
            final Menu menu = buildMenu();
            final var renderer = new VerticalMenuRenderer(menu, MenuItemRenderer.DEFAULT);

            boolean keepShowing;
            do {
                keepShowing = renderer.render();
            } while (keepShowing);

        } catch (final UnauthorizedException e) {
            System.out.println();
            System.out.println("You are not authorized to access Backoffice Config.");
            return false;

        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println();
            System.out.println("Could not open Backoffice Config.");
            System.out.println("Reason: " + e.getMessage());
            return false;
        }

        return false;
    }

    private Menu buildMenu() {
        final Menu menu = new Menu("Backoffice Config >");
        optionNumber = 1;

        final SystemUser user = authz.loggedinUserWithPermissions(
                        AiSafeRoles.ADMIN,
                        AiSafeRoles.BACKOFFICE_OPERATOR,
                        AiSafeRoles.WEATHER_PERSON
                )
                .orElseThrow(() -> new IllegalStateException("No authenticated user found."));

        if (user.hasAny(AiSafeRoles.ADMIN)) {
            addAdminOptions(menu);
            addBackofficeOperatorOptions(menu);
            addWeatherPersonOptions(menu);
            addGenerateSimulationReportOption(menu);
        } else {
            if (user.hasAny(AiSafeRoles.BACKOFFICE_OPERATOR)) {
                addBackofficeOperatorOptions(menu);
            }

            if (user.hasAny(AiSafeRoles.WEATHER_PERSON)) {
                addWeatherPersonOptions(menu);
            }
        }

        menu.addItem(MenuItem.of(0, "Return", () -> false));

        return menu;
    }

    private int nextOption() {
        return optionNumber++;
    }

    private void addAdminOptions(final Menu menu) {
        menu.addItem(MenuItem.of(nextOption(), "Register Backoffice User", () -> {
            new AddUserAction().execute();
            return true;
        }));

        menu.addItem(MenuItem.of(nextOption(), "Disable / Enable Backoffice User", () -> {
            new DisableEnableUserUI().show();
            return true;
        }));
    }

    private void addBackofficeOperatorOptions(final Menu menu) {
        menu.addItem(MenuItem.of(nextOption(), "Register Engine Model", () -> {
            new RegisterEngineModelUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(nextOption(), "Register Aircraft Model", () -> {
            new RegisterAircraftModelUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(nextOption(), "Add Engine Model to Aircraft Model", () -> {
            new AddEngineModelToAircraftModelUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(nextOption(), "Remove Engine Model from Aircraft Model", () -> {
            new RemoveEngineModelFromAircraftModelUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(nextOption(), "Register Air Control Area", () -> {
            new RegisterAirControlAreaUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(nextOption(), "List Air Control Areas", () -> {
            new ListAirControlAreasUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(nextOption(), "Register Airport", () -> {
            new RegisterAirportUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(nextOption(), "List Airports", () -> {
            new ListAirportsUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(nextOption(), "Register Air Transport Company", () -> {
            new RegisterAirTransportCompanyUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(nextOption(), "Add Customer Collaborator", () -> {
            new AddCustomerCollaboratorUI().show();
            return true;
        }));

        menu.addItem(MenuItem.of(nextOption(), "List Active Customer Collaborators", () -> {
            new ListCustomerCollaboratorsUI().show();
            return true;
        }));
    }

    private void addWeatherPersonOptions(final Menu menu) {
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
    }

    private void addGenerateSimulationReportOption(final Menu menu) {
        menu.addItem(MenuItem.of(nextOption(), "Generate Simulation Report", () -> {
            new GenerateSimulationReportUI().show();
            return true;
        }));
    }

    @Override
    public String headline() {
        return "Backoffice Config";
    }
}