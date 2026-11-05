package aisafe.enginemodel.application;

import aisafe.enginemodel.domain.*;
import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerID;
import aisafe.maker.domain.MakerRepository;
import aisafe.maker.domain.MakerType;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class RegisterEngineModelController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final EngineModelService engineModelService;
    private final MakerRepository makerRepository;

    public RegisterEngineModelController(final MakerRepository makerRepository,
                                         final EngineModelRepository engineModelRepository) {
        this.makerRepository = makerRepository;
        this.engineModelService = new EngineModelService(engineModelRepository);
    }

    /**
     * Returns all makers of type ENGINE to be presented to the user.
     */
    public Iterable<Maker> getEngineMakers() {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);
        return makerRepository.findByType(MakerType.ENGINE);
    }

    /**
     * Registers a new engine model in the system.
     */
    public EngineModel registerEngineModel(final String engineModelID,
                                           final String name,
                                           final String type,
                                           final double power,
                                           final String fuel,
                                           final double tsfc,
                                           final String makerID) {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);

        // fetch Maker object — controller is responsible for this
        final Maker maker = makerRepository.findByMakerID(MakerID.of(makerID))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Maker not found: " + makerID));

        return engineModelService.registerEngineModel(
                EngineModelID.of(engineModelID),
                name,
                EngineType.valueOf(type),
                power,
                FuelType.valueOf(fuel),
                tsfc,
                maker
        );
    }
}