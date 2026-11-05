package aisafe.aircraftmodel.application;

import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.aircraftmodel.domain.AircraftModelID;
import aisafe.aircraftmodel.domain.AircraftModelRepository;
import aisafe.enginemodel.domain.EngineModel;
import aisafe.enginemodel.domain.EngineModelID;
import aisafe.enginemodel.domain.EngineModelRepository;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class RemoveEngineModelFromAircraftModelController {

    private final AuthorizationService authz;
    private final AircraftModelRepository aircraftModelRepository;
    private final EngineModelRepository engineModelRepository;

    /** Production constructor — uses the global registry. */
    public RemoveEngineModelFromAircraftModelController(
            final AircraftModelRepository aircraftModelRepository,
            final EngineModelRepository engineModelRepository) {
        this(AuthzRegistry.authorizationService(), aircraftModelRepository, engineModelRepository);
    }

    /** Package-private constructor for tests — allows injecting stubs. */
    RemoveEngineModelFromAircraftModelController(
            final AuthorizationService authz,
            final AircraftModelRepository aircraftModelRepository,
            final EngineModelRepository engineModelRepository) {
        this.authz = authz;
        this.aircraftModelRepository = aircraftModelRepository;
        this.engineModelRepository = engineModelRepository;
    }

    public Iterable<AircraftModel> getAircraftModels() {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);
        return aircraftModelRepository.findAll();
    }

    public Iterable<EngineModel> getEngineModels() {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);
        return engineModelRepository.findAll();
    }

    public AircraftModel removeEngineModel(final String aircraftModelID,
                                           final String engineModelID) {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);

        final AircraftModel aircraftModel = aircraftModelRepository
                .findById(AircraftModelID.of(aircraftModelID))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aircraft model not found: " + aircraftModelID));

        final EngineModel engineModel = engineModelRepository
                .findByEngineModelID(EngineModelID.of(engineModelID))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Engine model not found: " + engineModelID));

        aircraftModel.removeCertifiedEngine(engineModel);
        return aircraftModelRepository.save(aircraftModel);
    }
}