package aisafe.costumerCollaborator.service;

import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.domain.CustomerCollaboratorID;
import aisafe.costumerCollaborator.domain.CustomerReference;
import aisafe.usermanagement.domain.legacyFiles.Email;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.repositories.Repository;

import java.util.Optional;

public interface CustomerCollaboratorRepository
        extends Repository<CustomerCollaborator, CustomerCollaboratorID> {

    Optional<CustomerCollaborator> findByUsername(Username username);

    Optional<CustomerCollaborator> findByEmail(Email email);

    Iterable<CustomerCollaborator> findByCustomerReference(CustomerReference customerReference);

    Optional<CustomerCollaborator> findBySystemUser(SystemUser systemUser);

    Optional<CustomerCollaborator> findById(CustomerCollaboratorID id);

    Iterable<CustomerCollaborator> findAllActive();
}
