package aisafe.persistence.impl.jpa;

import aisafe.usermanagement.domain.AiSafeUser;
import aisafe.usermanagement.domain.AiSafeUserRepository;
import eapli.framework.infrastructure.authz.domain.model.Username;
import jakarta.persistence.TypedQuery;

import java.util.Map;
import java.util.Optional;

public class JpaAiSafeUserRepository
        extends AiSafeJpaRepositoryBase<AiSafeUser, Username, Username>
        implements AiSafeUserRepository {

    public JpaAiSafeUserRepository(final String persistenceUnitName, final Map extendedProperties) {
        super(persistenceUnitName, extendedProperties, "username");
    }

    @Override
    public Optional<AiSafeUser> findByUsername(final Username username) {
        final TypedQuery<AiSafeUser> query = entityManager().createQuery(
                "SELECT u FROM AiSafeUser u WHERE u.username = :username",
                AiSafeUser.class);
        query.setParameter("username", username);
        return query.getResultList().stream().findFirst();
    }
}