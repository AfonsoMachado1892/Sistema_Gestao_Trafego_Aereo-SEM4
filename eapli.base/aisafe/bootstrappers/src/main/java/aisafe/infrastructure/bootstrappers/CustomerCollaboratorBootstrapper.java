package aisafe.infrastructure.bootstrappers;

import aisafe.costumerCollaborator.domain.CustomerType;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.costumerCollaborator.service.CustomerCollaboratorService;
import aisafe.persistence.PersistenceContext;
import aisafe.usermanagement.domain.legacyFiles.Email;
import eapli.framework.actions.Action;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("squid:S106")
public class CustomerCollaboratorBootstrapper extends AbstractUserBootstrapper implements Action {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AirControlAreaBootstrapper.class);

    private final CustomerCollaboratorRepository customerCollaboratorRepository =
            PersistenceContext.repositories().customerCollaborators();

    private final CustomerCollaboratorService service =
            new CustomerCollaboratorService(
                    customerCollaboratorRepository,
                    PersistenceContext.repositories().airTransportCompanies(),
                    PersistenceContext.repositories().airControlAreas()
            );

    @Override
    public boolean execute() {
        register("atccollab1", "Password1", "João", "Silva", "joao.silva@tap.pt",
                351, "912345678", CustomerType.AIR_TRANSPORT_COMPANY, "TP", "Operations Manager");

        register("fcouser1", "Password1", "Maria", "Santos", "maria.santos@lppc.pt",
                351, "923456789", CustomerType.AIR_CONTROL_AREA, "LPPC", "Flight Control Operator");

        return true;
    }

    private void register(
            final String username,
            final String password,
            final String firstName,
            final String lastName,
            final String email,
            final int countryCode,
            final String phoneNumber,
            final CustomerType customerType,
            final String customerCode,
            final String position
    ) {
        try {
            if (customerCollaboratorRepository.findByUsername(Username.valueOf(username)).isPresent()) {
                LOGGER.debug("Customer collaborator already exists: %s%n", username);
                return;
            }

            if (customerCollaboratorRepository.findByEmail(Email.of(email)).isPresent()) {
                LOGGER.debug("Customer collaborator email already exists: %s%n", email);
                return;
            }

            final Set<Role> roles = new HashSet<>();
            roles.add(customerType.role());

            final SystemUser systemUser = registerUser(
                    username,
                    password,
                    firstName,
                    lastName,
                    email,
                    countryCode,
                    phoneNumber,
                    roles
            );

            service.addCustomerCollaborator(
                    systemUser,
                    customerType,
                    customerCode,
                    Email.of(email),
                    position
            );

            LOGGER.debug("Customer collaborator registered: {}", username);
        } catch (final IllegalArgumentException | IllegalStateException e) {
            LOGGER.debug("Customer collaborator already exists: {}", username);
        }
    }
}