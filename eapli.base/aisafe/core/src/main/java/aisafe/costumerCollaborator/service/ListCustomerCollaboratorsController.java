package aisafe.costumerCollaborator.service;

import aisafe.aircontrolarea.domain.AirControlArea;
import aisafe.aircontrolarea.domain.AirControlAreaRepository;
import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.airtransportcompany.domain.AirTransportCompanyRepository;
import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.domain.CustomerType;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.validations.Preconditions;

@UseCaseController
public class ListCustomerCollaboratorsController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();

    private final CustomerCollaboratorService customerCollaboratorService;
    private final AirTransportCompanyRepository airTransportCompanyRepository;
    private final AirControlAreaRepository airControlAreaRepository;

    public ListCustomerCollaboratorsController(
            final CustomerCollaboratorRepository customerCollaboratorRepository,
            final AirTransportCompanyRepository airTransportCompanyRepository,
            final AirControlAreaRepository airControlAreaRepository
    ) {
        Preconditions.noneNull(customerCollaboratorRepository, "Customer collaborator repository must be provided.");
        Preconditions.noneNull(airTransportCompanyRepository, "Air transport company repository must be provided.");
        Preconditions.noneNull(airControlAreaRepository, "Air control area repository must be provided.");

        this.airTransportCompanyRepository = airTransportCompanyRepository;
        this.airControlAreaRepository = airControlAreaRepository;

        this.customerCollaboratorService = new CustomerCollaboratorService(
                customerCollaboratorRepository,
                airTransportCompanyRepository,
                airControlAreaRepository
        );
    }

    public CustomerType[] customerTypes() {
        ensureBackofficeOperatorOrAdmin();
        return CustomerType.values();
    }

    public Iterable<AirTransportCompany> listAirTransportCompanies() {
        ensureBackofficeOperatorOrAdmin();
        return airTransportCompanyRepository.findAll();
    }

    public Iterable<AirControlArea> listAirControlAreas() {
        ensureBackofficeOperatorOrAdmin();
        return airControlAreaRepository.findAll();
    }

    public Iterable<CustomerCollaborator> listCustomerCollaborators(
            final CustomerType customerType,
            final String customerCode
    ) {
        ensureBackofficeOperatorOrAdmin();

        return customerCollaboratorService.listActiveCustomerCollaborators(
                customerType,
                customerCode
        );
    }

    private void ensureBackofficeOperatorOrAdmin() {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN
        );
    }
}