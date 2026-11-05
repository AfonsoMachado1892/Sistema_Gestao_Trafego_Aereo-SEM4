package aisafe.app.common.console.presentation.authz;

import eapli.framework.actions.Actions;
import eapli.framework.actions.menu.Menu;
import eapli.framework.actions.menu.MenuItem;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.Role;

public class MyUserMenu extends Menu {

    private static final String MENU_TITLE = "My account >";

    private static final int EXIT_OPTION = 0;

    private static final int CHANGE_PASSWORD_OPTION = 1;
    private static final int CHECK_OPTION = 2;

    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    public MyUserMenu() {
        super(MENU_TITLE);
        buildMyUserMenu(null);
    }

    public MyUserMenu(final Role onlyWithThis) {
        super(MENU_TITLE);
        buildMyUserMenu(onlyWithThis);
    }

    private void buildMyUserMenu(final Role onlyWithThis) {
        if (authz.hasSession()) {
            addItem(MenuItem.of(CHANGE_PASSWORD_OPTION, "Change password", new ChangePasswordUI()::show));
            addItem(MenuItem.of(CHECK_OPTION, "Check personal information",
                    new CheckPersonalInformationUI()::show));
        }

        addItem(MenuItem.of(EXIT_OPTION, "Return", Actions.SUCCESS));
    }
}