package aisafe.aircontrolarea.application;

import aisafe.aircontrolarea.domain.*;
import eapli.framework.validations.Preconditions;

public class AirControlAreaService {

    private final AirControlAreaRepository airControlAreaRepository;

    public AirControlAreaService(final AirControlAreaRepository airControlAreaRepository) {
        Preconditions.noneNull(airControlAreaRepository, "Air control area repository must be provided.");
        this.airControlAreaRepository = airControlAreaRepository;
    }

    public AirControlArea registerAirControlArea(
            final AirControlAreaCode code,
            final GeographicBoundary geographicBoundary,
            final AirControlAreaName name
    ) {

        final AirControlArea airControlArea = new AirControlAreaFactory()
                .withCode(code)
                .withGeographicBoundary(geographicBoundary)
                .withName(name)
                .build();

        if (airControlAreaRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("Air control area code already exists.");
        }

        if (airControlAreaRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Air control area name already exists.");
        }

        for (final AirControlArea existingArea : airControlAreaRepository.findAll()) {
            if (existingArea.overlapsWith(airControlArea)) {
                throw new IllegalArgumentException("Air control area overlaps an existing air control area.");
            }
        }

        return airControlAreaRepository.save(airControlArea);
    }

    public Iterable<AirControlArea> listAirControlAreas() {
        return airControlAreaRepository.findAll();
    }
}