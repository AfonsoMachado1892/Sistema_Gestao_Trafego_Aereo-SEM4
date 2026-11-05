package aisafe.aircraft.application;

import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.aircraft.domain.AircraftRepository;
import aisafe.aircraft.domain.CabinConfiguration;
import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.aircraftmodel.domain.AircraftModelRepository;
import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.airtransportcompany.domain.AirTransportCompanyRepository;
import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.sharedkernel.Country;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.util.ArrayList;
import java.util.List;

@UseCaseController
public class AddAircraftController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    private final AircraftService aircraftService;
    private final AircraftModelRepository aircraftModelRepository;
    private final AirTransportCompanyRepository airTransportCompanyRepository;
    private final CustomerCollaboratorRepository customerCollaboratorRepository;
    private final AircraftRepository aircraftRepository;


    public AddAircraftController(
            final AircraftRepository aircraftRepository,
            final AircraftModelRepository aircraftModelRepository,
            final AirTransportCompanyRepository airTransportCompanyRepository,
            final CustomerCollaboratorRepository customerCollaboratorRepository
    ) {
        this.aircraftService = new AircraftService(aircraftRepository);
        this.aircraftModelRepository = aircraftModelRepository;
        this.airTransportCompanyRepository = airTransportCompanyRepository;
        this.customerCollaboratorRepository = customerCollaboratorRepository;
        this.aircraftRepository = aircraftRepository;
    }

    public List<AircraftModel> listAircraftModels() {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,AiSafeRoles.ADMIN);
        final List<AircraftModel> models = new ArrayList<>();
        aircraftModelRepository.findAll().forEach(models::add);
        return models;
    }

    public Aircraft addAircraft(
            final String registration,
            final AircraftModel aircraftModel,
            final CabinConfiguration cabinConfiguration,
            final Country country,
            final int numCrewElements
    ) {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,AiSafeRoles.ADMIN);

        final SystemUser systemUser = authz.loggedinUserWithPermissions(
                        AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,AiSafeRoles.ADMIN)
                .orElseThrow(() -> new IllegalStateException("No authenticated user found."));

        final CustomerCollaborator collaborator = customerCollaboratorRepository
                .findBySystemUser(systemUser)
                .orElseThrow(() -> new IllegalStateException("No collaborator found for this user."));

        final AirTransportCompany airTransportCompany = airTransportCompanyRepository
                .findByIataCode(AirTransportCompanyCode.of(collaborator.customerCode()))
                .orElseThrow(() -> new IllegalStateException("Air transport company not found."));

        return aircraftService.addAircraft(
                registration,
                aircraftModel,
                airTransportCompany,
                cabinConfiguration,
                country,
                numCrewElements
        );
    }

    public boolean registrationExists(final String registration) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,
                AiSafeRoles.ADMIN);

        return aircraftRepository
                .findByRegistration(AircraftRegistration.of(registration))
                .isPresent();
    }
}