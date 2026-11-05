package aisafe.persistence.impl.inmemory;


import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.domain.CustomerCollaboratorID;
import aisafe.costumerCollaborator.domain.CustomerReference;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.usermanagement.domain.legacyFiles.Email;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryCustomerCollaboratorRepository
        extends InMemoryDomainRepository<CustomerCollaborator, CustomerCollaboratorID>
        implements CustomerCollaboratorRepository {

    @Override
    public Optional<CustomerCollaborator> findByUsername(final Username username) {
        return matchOne(collaborator -> collaborator.systemUser().username().equals(username));
    }

    @Override
    public Optional<CustomerCollaborator> findByEmail(final Email email) {
        return matchOne(collaborator -> collaborator.email().equals(email));
    }

    @Override
    public Iterable<CustomerCollaborator> findByCustomerReference(final CustomerReference customerReference) {
        return match(collaborator -> collaborator.customerReference().equals(customerReference));
    }

    @Override
    public Optional<CustomerCollaborator> findBySystemUser(final SystemUser systemUser){

        return matchOne(collaborator -> collaborator.systemUser().equals(systemUser));
    }
    @Override
    public Optional<CustomerCollaborator> findById(final CustomerCollaboratorID id) {
        return matchOne(collaborator -> collaborator.identity().equals(id));
    }

    @Override
    public Iterable<CustomerCollaborator> findAllActive() {
        return match(CustomerCollaborator::isActive);
    }
}