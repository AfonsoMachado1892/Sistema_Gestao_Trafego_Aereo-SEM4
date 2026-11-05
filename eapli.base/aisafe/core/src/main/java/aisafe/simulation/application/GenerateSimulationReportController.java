package aisafe.simulation.application;

import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.List;

/**
 * Controller for US111 – Generate a Simulation Report.
 */
@UseCaseController
public class GenerateSimulationReportController {

    private final AuthorizationService authz;
    private final SimulationService simulationService;

    public GenerateSimulationReportController() {
        this(AuthzRegistry.authorizationService(), new SimulationService());
    }

    GenerateSimulationReportController(final AuthorizationService authz,
                                       final SimulationService simulationService) {
        this.authz = authz;
        this.simulationService = simulationService;
    }

    public List<String> listFlightPlanFiles() {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.FLIGHT_CONTROL_OPERATOR,
                AiSafeRoles.ADMIN);
        return simulationService.listFlightPlanFiles();
    }

    public SimulationReportDTO runSimulation(final String flightPlanFilename,
                                             final String weatherFilename) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.FLIGHT_CONTROL_OPERATOR,
                AiSafeRoles.ADMIN);
        return simulationService.runSimulation(flightPlanFilename, weatherFilename);
    }
}