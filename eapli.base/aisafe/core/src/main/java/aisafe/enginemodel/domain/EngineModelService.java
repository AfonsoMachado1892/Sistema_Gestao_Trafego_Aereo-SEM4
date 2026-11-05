package aisafe.enginemodel.domain;

import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerType;
import eapli.framework.domain.services.DomainService;
import eapli.framework.validations.Invariants;

/**
 * Domain service responsible for coordinating the creation of an {@link EngineModel}.
 *
 * <p>Validates that the maker is of type ENGINE and that the name+maker
 * combination is unique. The maker object is provided by the controller.</p>
 */
@DomainService
public class EngineModelService {

    private final EngineModelRepository engineModelRepository;

    public EngineModelService(final EngineModelRepository engineModelRepository) {
        this.engineModelRepository = engineModelRepository;
    }

    /**
     * Creates and saves a new engine model after validating all invariants.
     *
     * @throws IllegalStateException if the maker is not of type ENGINE
     * @throws IllegalStateException if an engine model with the same name and maker already exists
     */
    public EngineModel registerEngineModel(final EngineModelID engineModelID,
                                           final String name,
                                           final EngineType type,
                                           final double power,
                                           final FuelType fuel,
                                           final double tsfc,
                                           final Maker maker) {
        // 1. Validate maker is of type ENGINE
        Invariants.ensure(maker.hasType(MakerType.ENGINE),
                "Maker must be of type ENGINE to register an engine model");

        // 2. Validate uniqueness of name + maker combination
        engineModelRepository.findByNameAndMaker(name, maker.identity())
                .ifPresent(e -> {
                    throw new IllegalStateException(
                            "Engine model already exists for this maker: " + name);
                });

        // 3. Create and save
        final var engineModel = EngineModel.of(
                engineModelID, name, type, power, fuel, tsfc, maker);

        return engineModelRepository.save(engineModel);
    }
}