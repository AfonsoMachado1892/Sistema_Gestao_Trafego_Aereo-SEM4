package aisafe.aircraft.application;

import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRepository;
import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.airtransportcompany.domain.AirTransportCompanyRepository;
import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.util.ArrayList;
import java.util.List;

@UseCaseController
public class ListCompanyFleetController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    private final AircraftService aircraftService;
    private final AircraftRepository aircraftRepository;
    private final AirTransportCompanyRepository airTransportCompanyRepository;
    private final CustomerCollaboratorRepository customerCollaboratorRepository;

    public ListCompanyFleetController(
            final AircraftRepository aircraftRepository,
            final AirTransportCompanyRepository airTransportCompanyRepository,
            final CustomerCollaboratorRepository customerCollaboratorRepository
    ) {
        this.aircraftService = new AircraftService(aircraftRepository);
        this.aircraftRepository = aircraftRepository;
        this.airTransportCompanyRepository = airTransportCompanyRepository;
        this.customerCollaboratorRepository = customerCollaboratorRepository;
    }

    public List<Aircraft> listCompanyFleet() {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,
                AiSafeRoles.ADMIN
        );

        final SystemUser systemUser = authz.loggedinUserWithPermissions(
                        AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,
                        AiSafeRoles.ADMIN
                )
                .orElseThrow(() -> new IllegalStateException("No authenticated user found."));

        final List<Aircraft> fleet = new ArrayList<>();

        if (systemUser.hasAny(AiSafeRoles.ADMIN)) {
            aircraftRepository.findAll().forEach(fleet::add);
            return fleet;
        }

        final AirTransportCompany company = resolveCompany(systemUser);
        aircraftService.listAircraftByCompany(company).forEach(fleet::add);

        return fleet;
    }

    private AirTransportCompany resolveCompany(final SystemUser systemUser) {
        final CustomerCollaborator collaborator = customerCollaboratorRepository
                .findBySystemUser(systemUser)
                .orElseThrow(() -> new IllegalStateException("No collaborator found for this user."));

        return airTransportCompanyRepository
                .findByIataCode(AirTransportCompanyCode.of(collaborator.customerCode()))
                .orElseThrow(() -> new IllegalStateException("Air transport company not found."));
    }
}