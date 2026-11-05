package aisafe.costumerCollaborator.service;

import aisafe.aircontrolarea.domain.AirControlArea;
import aisafe.aircontrolarea.domain.AirControlAreaRepository;
import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.airtransportcompany.domain.AirTransportCompanyRepository;
import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.costumerCollaborator.domain.CustomerType;
import aisafe.usermanagement.domain.*;
import aisafe.usermanagement.domain.legacyFiles.Email;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;
import eapli.framework.infrastructure.authz.application.UserManagementService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.time.util.CurrentTimeCalendars;
import eapli.framework.validations.Preconditions;

import java.util.HashSet;
import java.util.Set;

@UseCaseController
public class AddCustomerCollaboratorController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final UserManagementService userSvc = AuthzRegistry.userService();
    private final CustomerCollaboratorRepository customerCollaboratorRepository;
    private final CustomerCollaboratorService customerCollaboratorService;
    private final AirTransportCompanyRepository airTransportCompanyRepository;
    private final AirControlAreaRepository airControlAreaRepository;
    private final AiSafeUserRepository aiSafeUserRepository;

    public AddCustomerCollaboratorController(
            final CustomerCollaboratorRepository customerCollaboratorRepository,
            final AirTransportCompanyRepository airTransportCompanyRepository,
            final AirControlAreaRepository airControlAreaRepository,
            final AiSafeUserRepository aiSafeUserRepository
    ) {

        this.airTransportCompanyRepository = airTransportCompanyRepository;
        this.airControlAreaRepository = airControlAreaRepository;
        this.aiSafeUserRepository = aiSafeUserRepository;
        this.customerCollaboratorRepository = customerCollaboratorRepository;

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

    public boolean usernameAlreadyExists(final String username) {
        ensureBackofficeOperatorOrAdmin();
        return customerCollaboratorRepository
                .findByUsername(Username.valueOf(username))
                .isPresent();
    }

    public boolean emailAlreadyExists(final String email) {
        ensureBackofficeOperatorOrAdmin();
        return customerCollaboratorRepository
                .findByEmail(Email.of(email))
                .isPresent();
    }

    public Iterable<AirControlArea> listAirControlAreas() {
        ensureBackofficeOperatorOrAdmin();
        return airControlAreaRepository.findAll();
    }

    public CustomerCollaborator addCustomerCollaborator(
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
        ensureBackofficeOperatorOrAdmin();

        final Set<Role> roles = new HashSet<>();
        roles.add(roleForCustomerType(customerType));

        final SystemUser systemUser = userSvc.registerNewUser(
                username,
                password,
                firstName,
                lastName,
                email,
                roles,
                CurrentTimeCalendars.now()
        );

        final AiSafeUser aiSafeUser = new AiSafeUserFactory().withSystemUser(systemUser).withPhoneNumber(countryCode, phoneNumber).build();
        aiSafeUserRepository.save(aiSafeUser);

        return customerCollaboratorService.addCustomerCollaborator(
                systemUser,
                customerType,
                customerCode,
                Email.of(email),
                position
        );
    }

    private void ensureBackofficeOperatorOrAdmin() {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN
        );
    }

    private Role roleForCustomerType(final CustomerType customerType) {
        if (customerType == CustomerType.AIR_TRANSPORT_COMPANY) {
            return AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR;
        }

        if (customerType == CustomerType.AIR_CONTROL_AREA) {
            return AiSafeRoles.FLIGHT_CONTROL_OPERATOR;
        }

        throw new IllegalArgumentException("Unsupported customer type.");
    }
}