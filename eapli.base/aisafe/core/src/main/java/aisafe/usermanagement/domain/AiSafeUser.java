package aisafe.usermanagement.domain;

import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "AI_SAFE_USER")
public class AiSafeUser implements AggregateRoot<Username>, Serializable {

    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @EmbeddedId
    private Username username;

    @OneToOne
    private SystemUser systemUser;

    @Embedded
    private PhoneNumber phoneNumber;

    protected AiSafeUser(){}

    public AiSafeUser(final PhoneNumber phoneNumber, final SystemUser systemUser){

        Preconditions.noneNull(phoneNumber, systemUser);
        this.systemUser = systemUser;
        this.username = systemUser.username();
        this.phoneNumber = phoneNumber;

    }

    public static AiSafeUser of(final SystemUser systemUser, final PhoneNumber phoneNumber){

        return new AiSafeUser(phoneNumber, systemUser);

    }

    @Override
    public Username identity(){

        return username;

    }

    public PhoneNumber phoneNumber(){

        return phoneNumber;

    }

    public void changePhoneNumber(final PhoneNumber phoneNumber) {
        Preconditions.noneNull(phoneNumber, "Phone number cannot be null.");
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean sameAs(final Object other) {
        if (!(other instanceof AiSafeUser)) {
            return false;
        }

        final AiSafeUser that = (AiSafeUser) other;

        if (this == that) {
            return true;
        }

        return this.username.equals(that.username)
                && this.phoneNumber.equals(that.phoneNumber);
    }

    @Override
    public boolean equals(final Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public int hashCode() {
        return DomainEntities.hashCode(this);
    }
}
