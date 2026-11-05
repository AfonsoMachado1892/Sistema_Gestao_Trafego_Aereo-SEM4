package aisafe.usermanagement.domain;

import eapli.framework.domain.model.DomainFactory;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.validations.Preconditions;

public class AiSafeUserFactory implements DomainFactory<AiSafeUser> {

    private SystemUser systemUser;
    private PhoneNumber phoneNumber;

    public AiSafeUserFactory withSystemUser(final SystemUser systemUser){

        this.systemUser = systemUser;
        return this;

    }

    public AiSafeUserFactory withPhoneNumber(final int countryCode, final String phoneNumber){

        this.phoneNumber = PhoneNumber.of(countryCode, phoneNumber);
        return this;

    }

    @Override
    public AiSafeUser build(){


        return AiSafeUser.of(systemUser, phoneNumber);
    }


}
