package aisafe.costumerCollaborator.domain;

import aisafe.usermanagement.domain.legacyFiles.Email;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "CUSTOMER_COLLABORATOR")
public class CustomerCollaborator implements Serializable, AggregateRoot<CustomerCollaboratorID> {

    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @EmbeddedId
    private CustomerCollaboratorID customerCollaboratorID;

    @Enumerated(EnumType.STRING)
    private CustomerStatus status;

    @Embedded
    private CustomerReference customerReference;

    @Embedded
    private Email email;

    @OneToOne
    private SystemUser systemUser;

    private String position;



    protected CustomerCollaborator() {
        // for ORM
    }

    public CustomerCollaborator(
            final CustomerCollaboratorID customerCollaboratorID,
            final String customerCode,
            final CustomerType customerType,
            final Email email,
            final SystemUser systemUser,
            final String position
    ) {
        Preconditions.noneNull(customerCollaboratorID, "Insert a valid customer collaborator ID.");
        Preconditions.nonEmpty(customerCode, "Insert a valid customer code.");
        Preconditions.noneNull(customerType, "Insert a valid customer type.");
        Preconditions.noneNull(email, "Insert a valid email.");
        Preconditions.noneNull(systemUser, "Insert a valid System User.");
        Preconditions.nonEmpty(position, "Insert a valid position.");

        this.customerCollaboratorID = customerCollaboratorID;
        this.customerReference = CustomerReference.of(customerType, customerCode);
        this.email = email;
        this.systemUser = systemUser;
        this.position = position.trim();
        this.status = CustomerStatus.ACTIVE;
    }

    public static CustomerCollaborator of(final String customerCode, final CustomerType customerType, final Email email, final SystemUser systemUser, final String position
    ) {
        return new CustomerCollaborator(
                CustomerCollaboratorID.newID(),
                customerCode,
                customerType,
                email,
                systemUser,
                position
        );
    }

    public CustomerCollaboratorID customerCollaboratorID() {
        return customerCollaboratorID;
    }

    public CustomerReference customerReference() {
        return customerReference;
    }

    public CustomerType customerType() {
        return customerReference.customerType();
    }

    public String customerCode() {
        return customerReference.customerCode();
    }

    public Email email() {
        return email;
    }

    public SystemUser systemUser() {
        return systemUser;
    }

    public String position() {
        return position;
    }

    public CustomerStatus status() {
        return status;
    }

    public boolean isActive() {
        return CustomerStatus.ACTIVE.equals(status);
    }

    public void disable() {
        this.status = CustomerStatus.DISABLED;
    }

    public void changeEmail(final Email email) {
        Preconditions.noneNull(email, "Insert a valid email.");
        this.email = email;
    }

    @Override
    public CustomerCollaboratorID identity() {
        return customerCollaboratorID;
    }

    @Override
    public boolean sameAs(final Object other) {
        if (!(other instanceof CustomerCollaborator)) {
            return false;
        }

        final CustomerCollaborator that = (CustomerCollaborator) other;

        if (this == that) {
            return true;
        }

        return this.customerCollaboratorID.equals(that.customerCollaboratorID)
                && this.customerReference.equals(that.customerReference)
                && this.email.equals(that.email)
                && this.systemUser.equals(that.systemUser)
                && this.position.equals(that.position)
                && this.status.equals(that.status);
    }

    @Override
    public boolean equals(final Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public int hashCode() {
        return DomainEntities.hashCode(this);
    }

    @Override
    public String toString() {
        return String.format(
                "%s - %s - %s - %s - %s",
                customerCollaboratorID,
                systemUser,
                customerReference,
                position,
                status
        );
    }
}
