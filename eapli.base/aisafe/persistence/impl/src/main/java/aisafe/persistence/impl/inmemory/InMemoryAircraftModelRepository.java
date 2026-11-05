package aisafe.persistence.impl.inmemory;

import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.aircraftmodel.domain.AircraftModelID;
import aisafe.aircraftmodel.domain.AircraftModelRepository;
import aisafe.maker.domain.MakerID;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryAircraftModelRepository
        extends InMemoryDomainRepository<AircraftModel, AircraftModelID>
        implements AircraftModelRepository {

    @Override
    public Optional<AircraftModel> findByModelIDAndMaker(final AircraftModelID modelID,
                                                         final MakerID makerID) {
        return matchOne(a -> a.identity().equals(modelID)
                && a.maker().identity().equals(makerID));
    }

    @Override
    public Optional<AircraftModel> findByModelID(final AircraftModelID modelID) {
        return matchOne(a -> a.identity().equals(modelID));
    }
}