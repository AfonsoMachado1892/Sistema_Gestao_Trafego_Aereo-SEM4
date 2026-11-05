package aisafe.aircontrolarea.domain;

import aisafe.sharedkernel.Coordinates;
import eapli.framework.infrastructure.repositories.Repository;

import java.util.Optional;

/**
 * Repository interface for the {@link AirControlArea} aggregate root.
 *
 * <p>Provides access to persisted {@link AirControlArea} instances.
 * Implementations may use JPA or in-memory storage depending
 * on the active {@code RepositoryFactory} configuration.</p>
 *
 * @see AirControlArea
 * @see AirControlAreaCode
 */
public interface AirControlAreaRepository extends Repository<AirControlArea, AirControlAreaCode> {

    /**
     * Returns the air control area with the given name, if it exists.
     *
     * <p>The comparison is case-insensitive.</p>
     *
     * @param name the name to search for; must not be {@code null} or empty
     * @return an {@link Optional} containing the air control area, or empty if not found
     */
    Optional<AirControlArea> findByName(AirControlAreaName name);

    Optional<AirControlArea> findByCode(AirControlAreaCode code);

    Iterable<AirControlArea> findAreasContaining(Coordinates coordinates);
}