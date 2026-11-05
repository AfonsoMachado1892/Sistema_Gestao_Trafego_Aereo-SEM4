package aisafe.maker.domain;

import eapli.framework.infrastructure.repositories.Repository;

import java.util.Optional;

/**
 * Repository interface for the {@link Maker} aggregate root.
 *
 * <p>Provides access to persisted {@link Maker} instances.
 * Implementations may use JPA or in-memory storage depending
 * on the active {@code RepositoryFactory} configuration.</p>
 *
 * @see Maker
 * @see MakerID
 */
public interface MakerRepository extends Repository<Maker, MakerID> {

    /**
     * Returns all makers that have the specified {@link MakerType}.
     *
     * <p>A maker may have more than one type (e.g. both ENGINE and AIRCRAFT),
     * so a maker is included in the result if it has at least the given type.</p>
     *
     * @param type the type to filter by; must not be {@code null}
     * @return all makers of the given type; never {@code null}, may be empty
     */
    Iterable<Maker> findByType(MakerType type);

    /**
     * Returns the maker with the given name, if it exists.
     *
     * <p>The comparison is case-insensitive.</p>
     *
     * @param name the name to search for; must not be {@code null} or empty
     * @return an {@link Optional} containing the maker, or empty if not found
     */
    Optional<Maker> findByName(String name);

    Optional<Maker> findByMakerID(MakerID makerID);
}