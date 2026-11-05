package aisafe.app.bootstrap;

import aisafe.app.common.console.BaseApp;
import aisafe.infrastructure.bootstrappers.AirControlAreaBootstrapper;
import aisafe.infrastructure.bootstrappers.AirportBootstrapper;
import aisafe.infrastructure.bootstrappers.MasterUsersBootstrapper;
import aisafe.usermanagement.domain.AiSafePasswordPolicy;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.PlainTextEncoder;
import eapli.framework.infrastructure.pubsub.EventDispatcher;
import aisafe.persistence.PersistenceContext;

@SuppressWarnings("squid:S106")
public final class AiSafeBootstrap extends BaseApp {

	private AiSafeBootstrap() {
	}

	public static void main(final String[] args) {
		AuthzRegistry.configure(PersistenceContext.repositories().users(),
				new AiSafePasswordPolicy(), new PlainTextEncoder());
		new AiSafeBootstrap().run(args);
	}



	protected void doMain(final String[] args) {
		System.out.println("\n\n------- MASTER DATA -------");
		new MasterUsersBootstrapper().execute();

		System.out.println("\n\n------- AIR CONTROL AREAS -------");
		new AirControlAreaBootstrapper().execute();

		System.out.println("\n\n------- AIRPORTS -------");
		new AirportBootstrapper().execute();
	}

	@Override
	protected String appTitle() {
		return "AISafe Bootstrap";
	}

	@Override
	protected String appGoodbye() {
		return "Bootstrap data done.";
	}

	@Override
	protected void doSetupEventHandlers(final EventDispatcher dispatcher) {
		// no events in AISafe bootstrap
	}


}