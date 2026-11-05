package aisafe.aircraftmodel.application;

import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.aircraftmodel.domain.AircraftModelID;
import aisafe.aircraftmodel.domain.AircraftModelRepository;
import aisafe.aircraftmodel.domain.AircraftModelService;
import aisafe.enginemodel.domain.EngineModel;
import aisafe.enginemodel.domain.EngineModelID;
import aisafe.enginemodel.domain.EngineModelRepository;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class AddEngineModelToAircraftModelController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final AircraftModelService aircraftModelService;
    private final AircraftModelRepository aircraftModelRepository;
    private final EngineModelRepository engineModelRepository;

    public AddEngineModelToAircraftModelController(
            final AircraftModelRepository aircraftModelRepository,
            final EngineModelRepository engineModelRepository) {
        this.aircraftModelRepository = aircraftModelRepository;
        this.engineModelRepository = engineModelRepository;
        this.aircraftModelService = new AircraftModelService(aircraftModelRepository);
    }

    /**
     * Returns all aircraft models to be presented to the user.
     */
    public Iterable<AircraftModel> getAircraftModels() {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);
        return aircraftModelRepository.findAll();
    }

    /**
     * Returns all engine models to be presented to the user.
     */
    public Iterable<EngineModel> getEngineModels() {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);
        return engineModelRepository.findAll();
    }

    /**
     * Adds a certified engine model to an aircraft model.
     */
    public AircraftModel addEngineModel(final String aircraftModelID,
                                        final String engineModelID) {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);

        // fetch EngineModel object — controller is responsible for this
        final EngineModel engineModel = engineModelRepository
                .findByEngineModelID(EngineModelID.of(engineModelID))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Engine model not found: " + engineModelID));

        return aircraftModelService.addEngineModel(
                AircraftModelID.of(aircraftModelID),
                engineModel);
    }
}