package aisafe.app.common.console.presentation.authz;

import aisafe.authz.AuthenticationCredentialHandler;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

@SuppressWarnings("squid:S106")
public class LogoutAndAskUI extends AbstractUI {

    @Override
    protected boolean doShow() {
        new LogoutUI().show();

        System.out.println();
        System.out.println("What do you want to do?");
        System.out.println("1. Login again");
        System.out.println("0. Exit program");

        final int option = Console.readInteger("Option:");

        if (option == 1) {
            new LoginUI(new AuthenticationCredentialHandler()).show();
            return false;
        }

        System.out.println("Bye, Bye");
        System.exit(0);
        return true;
    }

    @Override
    public String headline() {
        return "Logout";
    }
}