package aisafe.persistence.impl.jpa;

import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.aircraftmodel.domain.AircraftModelID;
import aisafe.aircraftmodel.domain.AircraftModelRepository;
import aisafe.maker.domain.MakerID;
import jakarta.persistence.TypedQuery;

import java.util.Map;
import java.util.Optional;

public class JpaAircraftModelRepository
        extends AiSafeJpaRepositoryBase<AircraftModel, AircraftModelID, AircraftModelID>
        implements AircraftModelRepository {

    public JpaAircraftModelRepository(final String persistenceUnitName, final Map extendedProperties) {
        super(persistenceUnitName, extendedProperties, "modelID");
    }

    @Override
    public Optional<AircraftModel> findByModelIDAndMaker(final AircraftModelID modelID, final MakerID makerID) {
        final TypedQuery<AircraftModel> query = entityManager().createQuery(
                "SELECT m FROM AircraftModel m WHERE m.modelID = :modelID AND m.maker.makerID = :makerID",
                AircraftModel.class);
        query.setParameter("modelID", modelID);
        query.setParameter("makerID", makerID);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Optional<AircraftModel> findByModelID(final AircraftModelID modelID) {
        final TypedQuery<AircraftModel> query = entityManager().createQuery(
                "SELECT m FROM AircraftModel m WHERE m.modelID = :modelID",
                AircraftModel.class);
        query.setParameter("modelID", modelID);
        return query.getResultList().stream().findFirst();
    }
}