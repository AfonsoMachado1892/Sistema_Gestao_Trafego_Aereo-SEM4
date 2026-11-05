package aisafe.persistence.impl.jpa;

import aisafe.aircontrolarea.domain.AirControlArea;
import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.aircontrolarea.domain.AirControlAreaName;
import aisafe.aircontrolarea.domain.AirControlAreaRepository;
import aisafe.sharedkernel.Coordinates;
import jakarta.persistence.TypedQuery;

import java.util.Map;
import java.util.Optional;

public class JpaAirControlAreaRepository
        extends AiSafeJpaRepositoryBase<AirControlArea, AirControlAreaCode, AirControlAreaCode>
        implements AirControlAreaRepository {

    public JpaAirControlAreaRepository(final String persistenceUnitName, final Map extendedProperties) {
        super(persistenceUnitName, extendedProperties, "code");
    }

    @Override
    public Optional<AirControlArea> findByCode(final AirControlAreaCode code) {
        final TypedQuery<AirControlArea> query = entityManager().createQuery(
                "SELECT a FROM AirControlArea a WHERE a.code = :code",
                AirControlArea.class);
        query.setParameter("code", code);
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Optional<AirControlArea> findByName(final AirControlAreaName name) {
        final TypedQuery<AirControlArea> query = entityManager().createQuery(
                "SELECT a FROM AirControlArea a WHERE LOWER(a.name.name) = LOWER(:name)",
                AirControlArea.class);
        query.setParameter("name", name.name());
        return query.getResultList().stream().findFirst();
    }

    @Override
    public Iterable<AirControlArea> findAreasContaining(final Coordinates coordinates) {
        final TypedQuery<AirControlArea> query = entityManager().createQuery(
                "SELECT a FROM AirControlArea a " +
                        "WHERE a.geographicBoundary.southwestCorner.latitude  <= :lat " +
                        "AND   a.geographicBoundary.northeastCorner.latitude  >= :lat " +
                        "AND   a.geographicBoundary.southwestCorner.longitude <= :lon " +
                        "AND   a.geographicBoundary.northeastCorner.longitude >= :lon",
                AirControlArea.class);
        query.setParameter("lat", coordinates.latitude());
        query.setParameter("lon", coordinates.longitude());
        return query.getResultList();
    }
}