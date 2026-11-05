package aisafe.app.backoffice.console;

import aisafe.app.backoffice.console.presentation.MainMenu;
import aisafe.app.common.console.BaseApp;
import aisafe.app.common.console.presentation.authz.LoginUI;

import aisafe.authz.AuthenticationCredentialHandler;
import aisafe.infrastructure.bootstrappers.AirControlAreaBootstrapper;
import aisafe.infrastructure.bootstrappers.AirportBootstrapper;
import aisafe.infrastructure.bootstrappers.MasterUsersBootstrapper;
import aisafe.usermanagement.domain.AiSafePasswordPolicy;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.pubsub.EventDispatcher;
import aisafe.persistence.PersistenceContext;
import server.TcpServer;

@SuppressWarnings("squid:S106")
public final class BackofficeApp extends BaseApp {

    private BackofficeApp() {
    }

    public static void main(final String[] args) {
        AuthzRegistry.configure(PersistenceContext.repositories().users(),
                new AiSafePasswordPolicy(), new PlainTextEncoder());

        new MasterUsersBootstrapper().execute();

        Thread tcpServer = new Thread(new TcpServer());
        tcpServer.setDaemon(true);
        tcpServer.start();

        System.out.println();
        System.out.println();

        new BackofficeApp().run(args);
    }

    @Override
    protected void doMain(final String[] args) {
        if (new LoginUI(new AuthenticationCredentialHandler()).show()) {
            final var menu = new MainMenu();
            menu.mainLoop();
        }
    }

    @Override
    protected String appTitle() {
        return "AISafe Backoffice";
    }

    @Override
    protected String appGoodbye() {
        return "Backoffice closed.";
    }

    @Override
    protected void doSetupEventHandlers(final EventDispatcher dispatcher) {
        // no events in AISafe backoffice for now
    }
}