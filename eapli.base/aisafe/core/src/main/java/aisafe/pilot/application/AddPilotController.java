package aisafe.pilot.application;

import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.aircraftmodel.domain.AircraftModelRepository;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.pilot.domain.*;
import aisafe.usermanagement.domain.AiSafeRoles;
import aisafe.usermanagement.domain.AiSafeUserFactory;
import aisafe.usermanagement.domain.AiSafeUserRepository;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserManagementService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.time.util.CurrentTimeCalendars;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controller for US075 – Add a Pilot.
 */
@UseCaseController
public class AddPilotController {

    private final AuthorizationService authz;
    private final UserManagementService userSvc;
    private final PilotService pilotService;
    private final CustomerCollaboratorRepository customerCollaboratorRepository;
    private final AiSafeUserRepository aiSafeUserRepository;

    public AddPilotController(final PilotRepository pilotRepository,
                              final AircraftModelRepository aircraftModelRepository,
                              final CustomerCollaboratorRepository customerCollaboratorRepository,
                              final AiSafeUserRepository aiSafeUserRepository) {
        this(AuthzRegistry.authorizationService(),
                AuthzRegistry.userService(),
                pilotRepository,
                aircraftModelRepository,
                customerCollaboratorRepository,
                aiSafeUserRepository);
    }

    /** Package-private constructor for tests. */
    AddPilotController(final AuthorizationService authz,
                       final UserManagementService userSvc,
                       final PilotRepository pilotRepository,
                       final AircraftModelRepository aircraftModelRepository,
                       final CustomerCollaboratorRepository customerCollaboratorRepository,
                       final AiSafeUserRepository aiSafeUserRepository) {
        this.authz = authz;
        this.userSvc = userSvc;
        this.pilotService = new PilotService(pilotRepository, aircraftModelRepository);
        this.customerCollaboratorRepository = customerCollaboratorRepository;
        this.aiSafeUserRepository = aiSafeUserRepository;
    }

    /**
     * Returns all aircraft models as DTOs for the UI selection list.
     */
    public Iterable<AircraftModelDTO> listAircraftModels() {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,
                AiSafeRoles.ADMIN);
        return pilotService.listAircraftModels();
    }

    /**
     * Returns true if the authenticated user is an admin.
     */
    public boolean isAdmin() {
        final SystemUser user = authz.loggedinUserWithPermissions(
                        AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,
                        AiSafeRoles.ADMIN)
                .orElseThrow(() -> new IllegalStateException("No authenticated user found."));
        return user.hasAny(AiSafeRoles.ADMIN);
    }

    /**
     * Returns the IATA code of the company associated with the authenticated collaborator.
     */
    public String currentCompanyIataCode() {
        final SystemUser systemUser = authz.loggedinUserWithPermissions(
                        AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,
                        AiSafeRoles.ADMIN)
                .orElseThrow(() -> new IllegalStateException("No authenticated user found."));

        final CustomerCollaborator collaborator = customerCollaboratorRepository
                .findBySystemUser(systemUser)
                .orElseThrow(() -> new IllegalStateException(
                        "No collaborator found for the authenticated user."));

        return collaborator.customerCode();
    }

    /**
     * Registers a new pilot.
     *
     * <p>The UI passes only primitive data. This controller builds the domain objects.
     *
     * @param modelIDs    list of aircraft model IDs
     * @param hoursList   list of flight hours (parallel to modelIDs)
     * @param issuedDates list of issued dates (parallel to modelIDs)
     * @param validDates  list of valid until dates (parallel to modelIDs)
     */
    public PilotDTO addPilot(final String username,
                             final String password,
                             final String firstName,
                             final String lastName,
                             final String email,
                             final int countryCode,
                             final String phoneNumber,
                             final String companyIataCode,
                             final List<String> modelIDs,
                             final List<Integer> hoursList,
                             final List<LocalDate> issuedDates,
                             final List<LocalDate> validDates) {

        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,
                AiSafeRoles.ADMIN);

        final Set<Role> roles = new HashSet<>();
        roles.add(AiSafeRoles.PILOT);

        final SystemUser systemUser = userSvc.registerNewUser(
                username, password, firstName, lastName, email,
                roles, CurrentTimeCalendars.now());

        aiSafeUserRepository.save(
                new AiSafeUserFactory()
                        .withSystemUser(systemUser)
                        .withPhoneNumber(countryCode, phoneNumber)
                        .build()
        );

        final Set<Certification> certifications = buildCertifications(
                modelIDs, hoursList, issuedDates, validDates);

        final Pilot pilot = pilotService.addPilot(
                systemUser,
                AirTransportCompanyCode.of(companyIataCode),
                certifications);

        return toDTO(pilot);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Set<Certification> buildCertifications(final List<String> modelIDs,
                                                   final List<Integer> hoursList,
                                                   final List<LocalDate> issuedDates,
                                                   final List<LocalDate> validDates) {
        final Set<Certification> certifications = new HashSet<>();
        for (int i = 0; i < modelIDs.size(); i++) {
            final AircraftModel model = pilotService.findAircraftModel(modelIDs.get(i));
            certifications.add(Certification.of(
                    model,
                    FlightHours.of(hoursList.get(i)),
                    issuedDates.get(i),
                    validDates.get(i)));
        }
        return certifications;
    }

    private PilotDTO toDTO(final Pilot pilot) {
        return new PilotDTO(
                pilot.pilotID().toString(),
                pilot.systemUser().name().toString(),
                pilot.currentCompanyIataCode().toString(),
                pilot.status().name(),
                pilot.certifications().size()
        );
    }
}