package aisafe.costumerCollaborator.service;

import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.aircontrolarea.domain.AirControlAreaRepository;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.airtransportcompany.domain.AirTransportCompanyRepository;
import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.domain.CustomerCollaboratorFactory;
import aisafe.costumerCollaborator.domain.CustomerReference;
import aisafe.costumerCollaborator.domain.CustomerType;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.usermanagement.domain.legacyFiles.Email;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.validations.Preconditions;

import java.util.ArrayList;

public class CustomerCollaboratorService {

    private final CustomerCollaboratorRepository customerCollaboratorRepository;
    private final AirTransportCompanyRepository airTransportCompanyRepository;
    private final AirControlAreaRepository airControlAreaRepository;

    public CustomerCollaboratorService(
            final CustomerCollaboratorRepository customerCollaboratorRepository,
            final AirTransportCompanyRepository airTransportCompanyRepository,
            final AirControlAreaRepository airControlAreaRepository
    ) {
        Preconditions.noneNull(customerCollaboratorRepository, "Customer collaborator repository must be provided.");
        Preconditions.noneNull(airTransportCompanyRepository, "Air transport company repository must be provided.");
        Preconditions.noneNull(airControlAreaRepository, "Air control area repository must be provided.");

        this.customerCollaboratorRepository = customerCollaboratorRepository;
        this.airTransportCompanyRepository = airTransportCompanyRepository;
        this.airControlAreaRepository = airControlAreaRepository;
    }

    public CustomerCollaborator addCustomerCollaborator(
            final SystemUser systemUser,
            final CustomerType customerType,
            final String customerCode,
            final Email email,
            final String position
    ) {

        ensureCustomerExists(customerType, customerCode);
        ensureDistinctSystemUser(systemUser.username());
        ensureEmailNotUsed(email);

        final CustomerCollaborator customerCollaborator = new CustomerCollaboratorFactory()
                .withCustomerCode(customerCode)
                .withCustomerType(customerType)
                .withEmail(email)
                .withSystemUser(systemUser)
                .withPosition(position)
                .build();

        return customerCollaboratorRepository.save(customerCollaborator);
    }

    private void ensureCustomerExists(final CustomerType customerType, final String customerCode) {
        if (customerType == CustomerType.AIR_CONTROL_AREA) {
            final AirControlAreaCode code = AirControlAreaCode.of(customerCode);

            if (airControlAreaRepository.findByCode(code).isEmpty()) {
                throw new IllegalArgumentException("Air control area does not exist.");
            }

            return;
        }

        if (customerType == CustomerType.AIR_TRANSPORT_COMPANY) {
            final AirTransportCompanyCode code = AirTransportCompanyCode.of(customerCode);

            if (airTransportCompanyRepository.findByIataCode(code).isEmpty()) {
                throw new IllegalArgumentException("Air transport company does not exist.");
            }

            return;
        }

        throw new IllegalArgumentException("Unsupported customer type.");
    }

    public Iterable<CustomerCollaborator> listActiveCustomerCollaborators(
            final CustomerType customerType,
            final String customerCode
    ) {
        Preconditions.noneNull(customerType, "Customer type must be provided.");
        Preconditions.nonEmpty(customerCode, "Customer code must be provided.");

        ensureCustomerExists(customerType, customerCode);

        final CustomerReference customerReference = CustomerReference.of(customerType, customerCode);
        final ArrayList<CustomerCollaborator> activeCollaborators = new ArrayList<>();

        for (final CustomerCollaborator collaborator :
                customerCollaboratorRepository.findByCustomerReference(customerReference)) {

            if (collaborator.isActive()) {
                activeCollaborators.add(collaborator);
            }
        }

        return activeCollaborators;
    }

    private void ensureDistinctSystemUser(final Username username) {
        if (customerCollaboratorRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException(
                    "This system user is already associated with a customer collaborator."
            );
        }
    }

    private void ensureEmailNotUsed(final Email email) {
        if (customerCollaboratorRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException(
                    "This email is already associated with a customer collaborator."
            );
        }
    }
}