package aisafe.report.application;

import aisafe.flightplan.domain.FlightPlanRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for US112 - GenerateMonthlyReportController.
 *
 * <p>Uses a granting / denying AuthorizationService stub (extending the
 * framework's AuthorizationService, mirroring RemovePilotControllerTest) and a
 * stub MonthlyReportService, so the controller is tested without real
 * persistence or AuthzRegistry. Follows the AAA convention.
 */
class GenerateMonthlyReportControllerTest {

    // ── Authorization stubs (extend, like the project's tests) ──────────────

    static class GrantingAuthorizationServiceStub extends AuthorizationService {
        @Override public void ensureAuthenticatedUserHasAnyOf(final Role... roles) { /* granted */ }
    }

    static class DenyingAuthorizationServiceStub extends AuthorizationService {
        @Override public void ensureAuthenticatedUserHasAnyOf(final Role... roles) {
            throw new IllegalStateException("Not authorized");
        }
    }

    // ── Service stub (no persistence, no file I/O) ──────────────────────────

    static class MonthlyReportServiceStub extends MonthlyReportService {
        private boolean called = false;

        MonthlyReportServiceStub() {
            super((FlightPlanRepository) null);
        }

        @Override
        public MonthlyReportDTO generateMonthlyReport(final int year, final int month) {
            called = true;
            return new MonthlyReportDTO(year, month, 0,
                    Map.of(), Map.of(), "rendered", "/tmp/report.txt");
        }

        boolean wasCalled() {
            return called;
        }
    }

    // ── Tests ───────────────────────────────────────────────────────────────

    @Test
    void ensureAuthorizedUserCanGenerateReport() {
        final MonthlyReportServiceStub serviceStub = new MonthlyReportServiceStub();
        final GenerateMonthlyReportController controller =
                new GenerateMonthlyReportController(
                        new GrantingAuthorizationServiceStub(), serviceStub);

        final MonthlyReportDTO dto = controller.generateMonthlyReport(2026, 6);

        assertEquals(2026, dto.year);
        assertEquals(6, dto.month);
    }

    @Test
    void ensureServiceIsDelegatedTo() {
        final MonthlyReportServiceStub serviceStub = new MonthlyReportServiceStub();
        final GenerateMonthlyReportController controller =
                new GenerateMonthlyReportController(
                        new GrantingAuthorizationServiceStub(), serviceStub);

        controller.generateMonthlyReport(2026, 6);

        assertTrue(serviceStub.wasCalled());
    }

    @Test
    void ensureUnauthorizedUserIsBlocked() {
        final MonthlyReportServiceStub serviceStub = new MonthlyReportServiceStub();
        final GenerateMonthlyReportController controller =
                new GenerateMonthlyReportController(
                        new DenyingAuthorizationServiceStub(), serviceStub);

        assertThrows(IllegalStateException.class,
                () -> controller.generateMonthlyReport(2026, 6));
    }
}
