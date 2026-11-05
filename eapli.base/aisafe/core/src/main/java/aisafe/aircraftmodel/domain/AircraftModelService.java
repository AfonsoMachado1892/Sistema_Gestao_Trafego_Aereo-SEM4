package aisafe.aircraftmodel.domain;

import aisafe.enginemodel.domain.EngineModel;
import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerType;
import eapli.framework.domain.services.DomainService;
import eapli.framework.validations.Preconditions;

import java.util.Set;

@DomainService
public class AircraftModelService {

    private final AircraftModelRepository aircraftModelRepository;

    public AircraftModelService(final AircraftModelRepository aircraftModelRepository) {
        this.aircraftModelRepository = aircraftModelRepository;
    }

    /**
     * Creates and saves a new aircraft model after validating all invariants.
     *
     * @throws IllegalArgumentException if the maker is not of type AIRCRAFT (enforced by AircraftModel constructor)
     * @throws IllegalStateException    if an aircraft model with the same ID and maker already exists
     */
    public AircraftModel registerAircraftModel(final AircraftModelID modelID,
                                               final Maker maker,
                                               final AircraftType type,
                                               final Set<EngineModel> engineModels,
                                               final double emptyWeight,
                                               final double mtow,
                                               final double mzfw,
                                               final double maxFuelCapacity,
                                               final double serviceCeiling,
                                               final double cruiseSpeed,
                                               final double wingArea,
                                               final double dragCoefficient,
                                               final double liftCoefficient,
                                               final double maximumRange,
                                               final int maxPassengerCapacity,
                                               final int engineNumber) {
        Preconditions.nonEmpty(engineModels,
                "At least one certified engine model must be provided");

        validateMakerIsAircraft(maker);

        // Validate uniqueness of modelID + maker combination
        aircraftModelRepository.findByModelIDAndMaker(modelID, maker.identity())
                .ifPresent(a -> {
                    throw new IllegalStateException(
                            "Aircraft model already exists for this maker: " + modelID);
                });

        // Maker type validation is enforced by the AircraftModel constructor
        final var aircraftModel = AircraftModel.of(
                modelID, maker, type, engineModels,
                emptyWeight, mtow, mzfw, maxFuelCapacity,
                serviceCeiling, cruiseSpeed, wingArea,
                dragCoefficient, liftCoefficient,
                maximumRange, maxPassengerCapacity, engineNumber);

        return aircraftModelRepository.save(aircraftModel);
    }

    private void validateMakerIsAircraft(final Maker maker) {
        Preconditions.ensure(maker.hasType(MakerType.AIRCRAFT),
                "Maker must be of type AIRCRAFT to register an aircraft model");
    }

    /**
     * Adds a certified engine model to an existing aircraft model.
     *
     * <p>Uses {@code ofIdentity} instead of {@code findById} to avoid
     * {@code LockModeType.OPTIMISTIC_FORCE_INCREMENT} which requires an active
     * transaction. {@code ofIdentity} uses a plain JPQL query with no lock mode.</p>
     *
     * @throws IllegalArgumentException if the aircraft model does not exist
     * @throws IllegalArgumentException if the engine model is already certified
     * @throws IllegalArgumentException if the engine type is incompatible
     */
    public AircraftModel addEngineModel(final AircraftModelID aircraftModelID,
                                        final EngineModel engineModel) {
        final AircraftModel aircraftModel = aircraftModelRepository
                .findByModelID(aircraftModelID)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aircraft model not found: " + aircraftModelID));

        aircraftModel.addCertifiedEngine(engineModel);

        return aircraftModelRepository.save(aircraftModel);
    }

    /**
     * Removes a certified engine model from an existing aircraft model.
     *
     * @throws IllegalArgumentException if the aircraft model does not exist
     * @throws IllegalArgumentException if the engine model is not certified
     * @throws IllegalArgumentException if it is the last certified engine model
     */
    public AircraftModel removeEngineModel(final AircraftModelID aircraftModelID,
                                           final EngineModel engineModel) {
        final AircraftModel aircraftModel = aircraftModelRepository
                .findByModelID(aircraftModelID)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aircraft model not found: " + aircraftModelID));

        aircraftModel.removeCertifiedEngine(engineModel);

        return aircraftModelRepository.save(aircraftModel);
    }
}