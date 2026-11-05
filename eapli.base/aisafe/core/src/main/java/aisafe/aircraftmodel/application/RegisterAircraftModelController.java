package aisafe.aircraftmodel.application;

import aisafe.aircraftmodel.domain.*;
import aisafe.enginemodel.domain.EngineModel;
import aisafe.enginemodel.domain.EngineModelID;
import aisafe.enginemodel.domain.EngineModelRepository;
import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerID;
import aisafe.maker.domain.MakerRepository;
import aisafe.maker.domain.MakerType;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.Set;
import java.util.stream.Collectors;

@UseCaseController
public class RegisterAircraftModelController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final AircraftModelService aircraftModelService;
    private final MakerRepository makerRepository;
    private final EngineModelRepository engineModelRepository;

    public RegisterAircraftModelController(final MakerRepository makerRepository,
                                           final EngineModelRepository engineModelRepository,
                                           final AircraftModelRepository aircraftModelRepository) {
        this.makerRepository = makerRepository;
        this.engineModelRepository = engineModelRepository;
        this.aircraftModelService = new AircraftModelService(aircraftModelRepository);
    }

    /**
     * Returns all makers to be presented to the user.
     */
    public Iterable<Maker> getMakers() {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.BACKOFFICE_OPERATOR, AiSafeRoles.ADMIN);
        return makerRepository.findByType(MakerType.AIRCRAFT);
    }

    /**
     * Returns all engine models to be presented to the user.
     */
    public Iterable<EngineModel> getEngineModels() {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.BACKOFFICE_OPERATOR, AiSafeRoles.ADMIN);
        return engineModelRepository.findAll();
    }

    /**
     * Registers a new aircraft model in the system.
     */
    public AircraftModel registerAircraftModel(final String modelID,
                                               final String makerID,
                                               final String type,
                                               final Set<String> engineModelIDs,
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
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.BACKOFFICE_OPERATOR, AiSafeRoles.ADMIN);

        // fetch Maker object
        final Maker maker = makerRepository.findByMakerID(MakerID.of(makerID))
                .orElseThrow(() -> new IllegalArgumentException("Maker not found: " + makerID));

        // fetch EngineModel objects
        final Set<EngineModel> engineModels = engineModelIDs.stream()
                .map(id -> engineModelRepository.findByEngineModelID(EngineModelID.of(id))
                        .orElseThrow(() -> new IllegalArgumentException("Engine model not found: " + id)))
                .collect(Collectors.toSet());

        return aircraftModelService.registerAircraftModel(
                AircraftModelID.of(modelID),
                maker,
                AircraftType.valueOf(type),
                engineModels,
                emptyWeight, mtow, mzfw, maxFuelCapacity,
                serviceCeiling, cruiseSpeed, wingArea,
                dragCoefficient, liftCoefficient,
                maximumRange, maxPassengerCapacity, engineNumber);
    }
}