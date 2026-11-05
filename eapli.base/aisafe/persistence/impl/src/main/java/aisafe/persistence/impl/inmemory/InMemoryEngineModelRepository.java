package aisafe.persistence.impl.inmemory;

import aisafe.enginemodel.domain.EngineModel;
import aisafe.enginemodel.domain.EngineModelID;
import aisafe.enginemodel.domain.EngineModelRepository;
import aisafe.maker.domain.MakerID;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryEngineModelRepository
        extends InMemoryDomainRepository<EngineModel, EngineModelID>
        implements EngineModelRepository {

    @Override
    public Optional<EngineModel> findByNameAndMaker(final String name, final MakerID makerID) {
        return matchOne(e -> e.name().equalsIgnoreCase(name)
                && e.maker().identity().equals(makerID));
    }

    @Override
    public Optional<EngineModel> findByEngineModelID(final EngineModelID engineModelID) {
        return matchOne(e -> e.identity().equals(engineModelID));
    }
}