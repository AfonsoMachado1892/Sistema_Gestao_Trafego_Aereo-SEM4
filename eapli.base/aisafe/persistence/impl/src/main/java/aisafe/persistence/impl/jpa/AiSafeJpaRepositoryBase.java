package aisafe.persistence.impl.jpa;

import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.Map;

class AiSafeJpaRepositoryBase<T, K, I> extends JpaAutoTxRepository<T, K, I> {

    AiSafeJpaRepositoryBase(final String persistenceUnitName,
                            final Map extendedProperties,
                            final String identityFieldName) {
        super(persistenceUnitName, extendedProperties, identityFieldName);
    }
}