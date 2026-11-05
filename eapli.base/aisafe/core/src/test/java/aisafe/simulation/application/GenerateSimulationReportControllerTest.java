package aisafe.simulation.application;

import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GenerateSimulationReportController} (US111).
 * Uses the AAA convention (Arrange, Act, Assert).
 */
class GenerateSimulationReportControllerTest {

    // ── Stubs ────────────────────────────────────────────────────────────────

    static class AuthorizationServiceStub extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) {
            // no-op — allows all roles in tests
        }
    }

    static class AuthorizationServiceRejectStub extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) {
            throw new IllegalStateException("Not authorized.");
        }
    }

    static class SimulationServiceStub extends SimulationService {
        boolean runSimulationCalled = false;
        boolean listFilesCalled     = false;

        @Override
        public List<String> listFlightPlanFiles() {
            listFilesCalled = true;
            return List.of("flights_single.json", "flights_no_violations.json");
        }

        @Override
        public SimulationReportDTO runSimulation(final String flightPlanFilename,
                                                 final String weatherFilename) {
            runSimulationCalled = true;
            return new SimulationReportDTO(
                    2, 13, 0, "DISABLED", "PASSED",
                    List.of("Flight ID 1001 : COMPLETED", "Flight ID 1002 : COMPLETED"),
                    List.of(),
                    "/some/path/simulation_report.txt"
            );
        }
    }

    // ── Setup ────────────────────────────────────────────────────────────────

    private SimulationServiceStub simulationServiceStub;
    private GenerateSimulationReportController controller;
    private GenerateSimulationReportController unauthorizedController;

    @BeforeEach
    void setUp() {
        simulationServiceStub = new SimulationServiceStub();
        controller = new GenerateSimulationReportController(
                new AuthorizationServiceStub(),
                simulationServiceStub
        );
        unauthorizedController = new GenerateSimulationReportController(
                new AuthorizationServiceRejectStub(),
                simulationServiceStub
        );
    }

    // ── listFlightPlanFiles ───────────────────────────────────────────────────

    @Test
    void ensureListFlightPlanFilesReturnsAvailableFiles() {
        // Act
        final List<String> files = controller.listFlightPlanFiles();

        // Assert
        assertNotNull(files);
        assertFalse(files.isEmpty());
        assertTrue(files.contains("flights_single.json"));
    }

    @Test
    void ensureListFlightPlanFilesDelegatesToService() {
        // Act
        controller.listFlightPlanFiles();

        // Assert
        assertTrue(simulationServiceStub.listFilesCalled);
    }

    @Test
    void ensureListFlightPlanFilesRequiresAuthorization() {
        assertThrows(Exception.class,
                () -> unauthorizedController.listFlightPlanFiles());
    }

    // ── runSimulation ─────────────────────────────────────────────────────────

    @Test
    void ensureRunSimulationReturnsReport() {
        // Act
        final SimulationReportDTO report = controller.runSimulation(
                "flights_single.json", null);

        // Assert
        assertNotNull(report);
        assertEquals(2, report.totalFlights);
        assertEquals("PASSED", report.globalResult);
    }

    @Test
    void ensureRunSimulationDelegatesToService() {
        // Act
        controller.runSimulation("flights_single.json", null);

        // Assert
        assertTrue(simulationServiceStub.runSimulationCalled);
    }

    @Test
    void ensureRunSimulationRequiresAuthorization() {
        assertThrows(Exception.class,
                () -> unauthorizedController.runSimulation(
                        "flights_single.json", null));
    }

    @Test
    void ensureRunSimulationWithWeatherDelegatesToService() {
        // Act
        controller.runSimulation("flights_single.json", "weather_aca121_realistic.csv");

        // Assert
        assertTrue(simulationServiceStub.runSimulationCalled);
    }

    @Test
    void ensureReportContainsFlightStatuses() {
        // Act
        final SimulationReportDTO report = controller.runSimulation(
                "flights_single.json", null);

        // Assert
        assertFalse(report.flightStatuses.isEmpty());
        assertTrue(report.flightStatuses.stream()
                .anyMatch(s -> s.contains("COMPLETED")));
    }

    @Test
    void ensureReportContainsReportFilePath() {
        // Act
        final SimulationReportDTO report = controller.runSimulation(
                "flights_single.json", null);

        // Assert
        assertNotNull(report.reportFilePath);
        assertFalse(report.reportFilePath.isBlank());
    }
}