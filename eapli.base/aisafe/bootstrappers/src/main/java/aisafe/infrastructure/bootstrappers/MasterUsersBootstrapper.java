package aisafe.infrastructure.bootstrappers;

import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.actions.Action;
import eapli.framework.infrastructure.authz.domain.model.Role;

import java.util.HashSet;
import java.util.Set;

public class MasterUsersBootstrapper extends AbstractUserBootstrapper implements Action {

    @Override
    public boolean execute() {
        registerAdmin("admin", TestDataConstants.PASSWORD1, "Jane", "Doe Admin",
                "jane.doe@email.local");

        new AirTransportCompanyBootstrapper().execute();
        new MakerBootstrapper().execute();
        new EngineModelBootstrapper().execute();
        new AircraftModelBootstrapper().execute();
        new AirControlAreaBootstrapper().execute();
        new AirportBootstrapper().execute();
        new CustomerCollaboratorBootstrapper().execute();
        new PilotBootstrapper().execute();
        new AircraftBootstrapper().execute();
        new FlightRouteBootstrapper().execute();
        new FlightPlanBootstrapper().execute();
        new WeatherPersonBootstrapper().execute();
        new WeatherDataBootstrapper().execute();


        return true;
    }

    private void registerAdmin(final String username, final String password,
                               final String firstName, final String lastName,
                               final String email) {
        final Set<Role> roles = new HashSet<>();
        roles.add(AiSafeRoles.ADMIN);

        registerUser(username, password, firstName, lastName, email, 351, "912345678", roles);
    }
}