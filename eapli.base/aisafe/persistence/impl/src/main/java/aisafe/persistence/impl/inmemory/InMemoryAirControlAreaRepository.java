package aisafe.persistence.impl.inmemory;

import aisafe.aircontrolarea.domain.AirControlArea;
import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.aircontrolarea.domain.AirControlAreaName;
import aisafe.aircontrolarea.domain.AirControlAreaRepository;
import aisafe.sharedkernel.Coordinates;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;

public class InMemoryAirControlAreaRepository
        extends InMemoryDomainRepository<AirControlArea, AirControlAreaCode>
        implements AirControlAreaRepository {

    @Override
    public Optional<AirControlArea> findByCode(final AirControlAreaCode code) {
        return matchOne(area -> area.code().equals(code));
    }

    @Override
    public Optional<AirControlArea> findByName(final AirControlAreaName name) {
        return matchOne(area -> area.name().equals(name));
    }
    @Override
    public Iterable<AirControlArea> findAreasContaining(final Coordinates coordinates) {
        return match(area -> area.contains(coordinates));
    }

}