package aisafe.costumerCollaborator.domain;

import aisafe.usermanagement.domain.legacyFiles.Email;
import eapli.framework.domain.model.DomainFactory;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.validations.Preconditions;

public class CustomerCollaboratorFactory implements DomainFactory<CustomerCollaborator> {

    private String customerCode;
    private CustomerType customerType;
    private Email email;
    private SystemUser systemUser;
    private String position;

    public CustomerCollaboratorFactory withCustomerCode(final String customerCode) {
        this.customerCode = customerCode;
        return this;
    }

    public CustomerCollaboratorFactory withCustomerType(final CustomerType customerType) {
        this.customerType = customerType;
        return this;
    }

    public CustomerCollaboratorFactory withEmail(final Email email) {
        this.email = email;
        return this;
    }

    public CustomerCollaboratorFactory withSystemUser(final SystemUser systemUser) {
        this.systemUser = systemUser;
        return this;
    }

    public CustomerCollaboratorFactory withPosition(final String position) {
        this.position = position;
        return this;
    }

    @Override
    public CustomerCollaborator build() {

        return  CustomerCollaborator.of(
                customerCode,
                customerType,
                email,
                systemUser,
                position
        );
    }
}