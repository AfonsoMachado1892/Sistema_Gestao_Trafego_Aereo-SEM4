package aisafe.infrastructure.bootstrappers;

import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.aircraftmodel.domain.AircraftModelID;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.pilot.application.PilotService;
import aisafe.pilot.domain.Certification;
import aisafe.pilot.domain.FlightHours;
import aisafe.pilot.domain.PilotRepository;
import aisafe.persistence.PersistenceContext;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.actions.Action;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class PilotBootstrapper extends AbstractUserBootstrapper implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(PilotBootstrapper.class);

    private final PilotRepository pilotRepository =
            PersistenceContext.repositories().pilots();

    private final PilotService pilotService = new PilotService(
            pilotRepository,
            PersistenceContext.repositories().aircraftModels()
    );

    @Override
    public boolean execute() {
        final AircraftModel aircraftModel = PersistenceContext.repositories()
                .aircraftModels()
                .findByModelID(AircraftModelID.of("777-200ER"))
                .orElse(null);

        if (aircraftModel == null) {
            LOGGER.warn("PilotBootstrapper: AircraftModel 777-200ER not found, skipping.");
            return true;
        }

        register(
                "pilot1", "Password1", "António", "Silva", "antonio.silva@tap.pt",
                351, "912111222",
                "TP",
                Set.of(Certification.of(
                        aircraftModel,
                        FlightHours.of(1200),
                        LocalDate.of(2023, 1, 1),
                        LocalDate.of(2028, 1, 1)
                ))
        );

        return true;
    }

    private void register(final String username,
                          final String password,
                          final String firstName,
                          final String lastName,
                          final String email,
                          final int countryCode,
                          final String phoneNumber,
                          final String companyIata,
                          final Set<Certification> certifications) {
        try {
            final Set<Role> roles = new HashSet<>();
            roles.add(AiSafeRoles.PILOT);

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

            if (pilotRepository.findBySystemUser(systemUser).isPresent()) {
                LOGGER.debug("Pilot already exists, skipping: {}", username);
                return;
            }

            pilotService.addPilot(
                    systemUser,
                    AirTransportCompanyCode.of(companyIata),
                    certifications
            );

            LOGGER.debug("»»» Pilot registered: {}", username);

        } catch (final Exception e) {
            LOGGER.warn("Pilot skipped [{}]: {}", username, e.getMessage());
        }
    }
}