package aisafe.app.backoffice.console.presentation.authz;

import eapli.framework.actions.Action;

public class AddUserAction implements Action {

    @Override
    public boolean execute() {
        return new AddUserUI().show();
    }
}