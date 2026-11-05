package aisafe.enginemodel.domain;

import aisafe.maker.domain.MakerID;
import eapli.framework.infrastructure.repositories.Repository;

import java.util.Optional;

/**
 * Repository interface for the {@link EngineModel} aggregate root.
 *
 * <p>Provides access to persisted {@link EngineModel} instances.
 * Implementations may use JPA or in-memory storage depending
 * on the active {@code RepositoryFactory} configuration.</p>
 *
 * @see EngineModel
 * @see EngineModelID
 */
public interface EngineModelRepository extends Repository<EngineModel, EngineModelID> {

    /**
     * Returns the engine model with the given name and maker, if it exists.
     *
     * <p>Used to enforce the uniqueness invariant: the combination of
     * name and maker must be unique across all engine models.</p>
     *
     * @param name    the name to search for; must not be {@code null} or empty
     * @param makerID the maker identifier; must not be {@code null}
     * @return an {@link Optional} containing the engine model, or empty if not found
     */
    Optional<EngineModel> findByNameAndMaker(String name, MakerID makerID);

    Optional<EngineModel> findByEngineModelID(EngineModelID engineModelID);
}