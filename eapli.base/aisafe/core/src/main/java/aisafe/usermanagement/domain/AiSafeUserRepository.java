package aisafe.usermanagement.domain;

import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.repositories.Repository;

import java.util.Optional;

public interface AiSafeUserRepository extends Repository<AiSafeUser, Username> {

    Optional<AiSafeUser> findByUsername(Username username);
}