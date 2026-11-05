package aisafe.aircraftmodel.domain;

import aisafe.maker.domain.MakerID;
import eapli.framework.infrastructure.repositories.Repository;

import java.util.Optional;

/**
 * Repository interface for the {@link AircraftModel} aggregate root.
 *
 * <p>Provides access to persisted {@link AircraftModel} instances.
 * Implementations may use JPA or in-memory storage depending
 * on the active {@code RepositoryFactory} configuration.</p>
 *
 * @see AircraftModel
 * @see AircraftModelID
 */
public interface AircraftModelRepository extends Repository<AircraftModel, AircraftModelID> {

    /**
     * Returns the aircraft model with the given model ID and maker, if it exists.
     *
     * <p>Used to enforce the uniqueness invariant: the combination of
     * model ID and maker must be unique across all aircraft models.</p>
     *
     * @param modelID the model ID to search for; must not be {@code null}
     * @param makerID the maker identifier; must not be {@code null}
     * @return an {@link Optional} containing the aircraft model, or empty if not found
     */
    Optional<AircraftModel> findByModelIDAndMaker(AircraftModelID modelID, MakerID makerID);

    /**
     * Returns the aircraft model with the given ID using a plain JPQL query (no lock mode).
     *
     * <p>Used instead of {@code findById} which uses {@code LockModeType.OPTIMISTIC_FORCE_INCREMENT}
     * and requires an active transaction — incompatible with the eapli auto-tx pattern.</p>
     */
    Optional<AircraftModel> findByModelID(AircraftModelID modelID);
}