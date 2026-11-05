package aisafe.persistence.impl.inmemory;

import aisafe.usermanagement.domain.AiSafeUser;
import aisafe.usermanagement.domain.AiSafeUserRepository;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryAiSafeUserRepository
        extends InMemoryDomainRepository<AiSafeUser, Username>
        implements AiSafeUserRepository {

    @Override
    public Optional<AiSafeUser> findByUsername(final Username username) {
        return ofIdentity(username);
    }
}