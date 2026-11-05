package aisafe.persistence.impl.jpa;

import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerID;
import aisafe.maker.domain.MakerRepository;
import aisafe.maker.domain.MakerType;
import jakarta.persistence.TypedQuery;

import java.util.Map;
import java.util.Optional;

public class JpaMakerRepository
        extends AiSafeJpaRepositoryBase<Maker, MakerID, MakerID>
        implements MakerRepository {

    public JpaMakerRepository(final String persistenceUnitName, final Map extendedProperties) {
        super(persistenceUnitName, extendedProperties, "makerID");
    }

    @Override
    public Iterable<Maker> findByType(final MakerType type) {
        final TypedQuery<Maker> query = entityManager().createQuery(
                "SELECT m FROM Maker m JOIN m.makerType t WHERE t = :type",
                Maker.class);
        query.setParameter("type", type);
        return query.getResultList();
    }

    @Override
    public Optional<Maker> findByName(final String name) {
        final TypedQuery<Maker> query = entityManager().createQuery(
                "SELECT m FROM Maker m WHERE LOWER(m.name) = LOWER(:name)",
                Maker.class);
        query.setParameter("name", name);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Optional<Maker> findByMakerID(final MakerID makerID) {
        final var query = entityManager().createQuery(
                "SELECT m FROM Maker m WHERE m.makerID = :makerID",
                Maker.class
        );
        query.setParameter("makerID", makerID);
        return query.getResultList().stream().findFirst();
    }
}