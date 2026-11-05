package aisafe.aircraft.application;

import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRepository;
import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.airtransportcompany.domain.AirTransportCompanyRepository;
import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.flight.domain.FlightRepository;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;

import java.util.ArrayList;
import java.util.List;

@UseCaseController
public class DecommissionAircraftController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    private final AircraftService aircraftService;
    private final AircraftRepository aircraftRepository;
    private final AirTransportCompanyRepository airTransportCompanyRepository;
    private final CustomerCollaboratorRepository customerCollaboratorRepository;
    private final FlightRepository flightRepository;

    public DecommissionAircraftController(
            final AircraftRepository aircraftRepository,
            final AirTransportCompanyRepository airTransportCompanyRepository,
            final CustomerCollaboratorRepository customerCollaboratorRepository,
            final FlightRepository flightRepository
    ) {
        this.aircraftService = new AircraftService(aircraftRepository);
        this.aircraftRepository = aircraftRepository;
        this.airTransportCompanyRepository = airTransportCompanyRepository;
        this.customerCollaboratorRepository = customerCollaboratorRepository;
        this.flightRepository = flightRepository;
    }

    public List<Aircraft> getCompanyAircraft() {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,
                AiSafeRoles.ADMIN
        );

        final SystemUser systemUser = authenticatedUser();

        final List<Aircraft> fleet = new ArrayList<>();

        if (systemUser.hasAny(AiSafeRoles.ADMIN)) {
            aircraftRepository.findAll().forEach(fleet::add);
        } else {
            final AirTransportCompany company = resolveCompany(systemUser);
            aircraftService.listAircraftByCompany(company).forEach(fleet::add);
        }

        return fleet;
    }

    public Aircraft decommissionAircraft(final String registration) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,
                AiSafeRoles.ADMIN
        );

        final SystemUser systemUser = authenticatedUser();

        final AirTransportCompany company;

        if (systemUser.hasAny(AiSafeRoles.ADMIN)) {
            final Aircraft aircraft = aircraftRepository.findByRegistration(
                    aisafe.aircraft.domain.AircraftRegistration.of(registration)
            ).orElseThrow(() -> new IllegalArgumentException(
                    "Aircraft with registration " + registration + " not found."
            ));

            company = aircraft.airCompany();
        } else {
            company = resolveCompany(systemUser);
        }

        return aircraftService.decommissionAircraft(registration, company, flightRepository);
    }

    private SystemUser authenticatedUser() {
        return authz.loggedinUserWithPermissions(
                        AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,
                        AiSafeRoles.ADMIN
                )
                .orElseThrow(() -> new IllegalStateException("No authenticated user found."));
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