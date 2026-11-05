package aisafe.persistence.impl.inmemory;

import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerID;
import aisafe.maker.domain.MakerRepository;
import aisafe.maker.domain.MakerType;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryMakerRepository
        extends InMemoryDomainRepository<Maker, MakerID>
        implements MakerRepository {

    @Override
    public Iterable<Maker> findByType(final MakerType type) {
        return match(m -> m.hasType(type));
    }

    @Override
    public Optional<Maker> findByName(final String name) {
        return matchOne(m -> m.name().equalsIgnoreCase(name));
    }

    @Override
    public Optional<Maker> findByMakerID(final MakerID makerID) {
        return matchOne(m -> m.identity().equals(makerID));
    }
}