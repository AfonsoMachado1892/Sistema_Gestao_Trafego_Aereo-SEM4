package aisafe.persistence.impl.jpa;

import aisafe.enginemodel.domain.EngineModel;
import aisafe.enginemodel.domain.EngineModelID;
import aisafe.enginemodel.domain.EngineModelRepository;
import aisafe.maker.domain.MakerID;
import jakarta.persistence.TypedQuery;

import java.util.Map;
import java.util.Optional;

public class JpaEngineModelRepository
        extends AiSafeJpaRepositoryBase<EngineModel, EngineModelID, EngineModelID>
        implements EngineModelRepository {

    public JpaEngineModelRepository(final String persistenceUnitName, final Map extendedProperties) {
        super(persistenceUnitName, extendedProperties, "engineModelID");
    }

    @Override
    public Optional<EngineModel> findByNameAndMaker(final String name, final MakerID makerID) {
        final TypedQuery<EngineModel> query = entityManager().createQuery(
                "SELECT e FROM EngineModel e WHERE e.name = :name AND e.maker.makerID = :makerID",
                EngineModel.class);
        query.setParameter("name", name);
        query.setParameter("makerID", makerID);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Optional<EngineModel> findByEngineModelID(final EngineModelID engineModelID) {
        final var query = entityManager().createQuery(
                "SELECT e FROM EngineModel e WHERE e.engineModelID = :engineModelID",
                EngineModel.class
        );
        query.setParameter("engineModelID", engineModelID);
        return query.getResultList().stream().findFirst();
    }
}