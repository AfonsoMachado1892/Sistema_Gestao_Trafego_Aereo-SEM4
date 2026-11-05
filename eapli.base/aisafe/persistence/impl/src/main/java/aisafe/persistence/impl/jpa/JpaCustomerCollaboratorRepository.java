package aisafe.persistence.impl.jpa;

import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.domain.CustomerCollaboratorID;
import aisafe.costumerCollaborator.domain.CustomerReference;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.usermanagement.domain.legacyFiles.Email;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import jakarta.persistence.TypedQuery;

import java.util.Map;
import java.util.Optional;

public class JpaCustomerCollaboratorRepository
        extends AiSafeJpaRepositoryBase<CustomerCollaborator, CustomerCollaboratorID, CustomerCollaboratorID>
        implements CustomerCollaboratorRepository {

    public JpaCustomerCollaboratorRepository(final String persistenceUnitName, final Map extendedProperties) {
        super(persistenceUnitName, extendedProperties, "customerCollaboratorID");
    }

    @Override
    public Optional<CustomerCollaborator> findByUsername(final Username username) {
        final TypedQuery<CustomerCollaborator> query = entityManager().createQuery(
                "SELECT c FROM CustomerCollaborator c WHERE c.systemUser.username = :username",
                CustomerCollaborator.class);
        query.setParameter("username", username);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Optional<CustomerCollaborator> findByEmail(final Email email) {
        final TypedQuery<CustomerCollaborator> query = entityManager().createQuery(
                "SELECT c FROM CustomerCollaborator c WHERE c.email = :email",
                CustomerCollaborator.class);
        query.setParameter("email", email);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Iterable<CustomerCollaborator> findByCustomerReference(final CustomerReference customerReference) {
        final TypedQuery<CustomerCollaborator> query = entityManager().createQuery(
                "SELECT c FROM CustomerCollaborator c WHERE c.customerReference = :ref",
                CustomerCollaborator.class);
        query.setParameter("ref", customerReference);
        return query.getResultList();
    }

    @Override
    public Optional<CustomerCollaborator> findBySystemUser(final SystemUser systemUser) {
        final TypedQuery<CustomerCollaborator> query = entityManager().createQuery(
                "SELECT c FROM CustomerCollaborator c WHERE c.systemUser = :user",
                CustomerCollaborator.class);
        query.setParameter("user", systemUser);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Optional<CustomerCollaborator> findById(final CustomerCollaboratorID id) {
        final TypedQuery<CustomerCollaborator> query = entityManager().createQuery(
                "SELECT c FROM CustomerCollaborator c WHERE c.customerCollaboratorID = :id",
                CustomerCollaborator.class);
        query.setParameter("id", id);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Iterable<CustomerCollaborator> findAllActive() {
        final TypedQuery<CustomerCollaborator> query = entityManager().createQuery(
                "SELECT c FROM CustomerCollaborator c WHERE c.status = 'ACTIVE'",
                CustomerCollaborator.class);
        return query.getResultList();
    }
}