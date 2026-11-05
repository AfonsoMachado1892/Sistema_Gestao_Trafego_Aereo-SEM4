package aisafe.report.application;

import aisafe.flightplan.domain.FlightPlanRepository;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

/**
 * Controller for US112 – Monthly Report Generation.
 *
 * <p>Mirrors GenerateSimulationReportController (US111) and the pilot
 * controllers (US076/US077): repositories are injected via the constructor;
 * the UI (composition root, in the app module) resolves them through
 * PersistenceContext. Authorization restricted to Flight Control Operator / Admin.
 */
@UseCaseController
public class GenerateMonthlyReportController {

    private final AuthorizationService authz;
    private final MonthlyReportService monthlyReportService;

    public GenerateMonthlyReportController(final FlightPlanRepository flightPlanRepository) {
        this(AuthzRegistry.authorizationService(),
                new MonthlyReportService(flightPlanRepository));
    }

    GenerateMonthlyReportController(final AuthorizationService authz,
                                    final MonthlyReportService monthlyReportService) {
        this.authz = authz;
        this.monthlyReportService = monthlyReportService;
    }

    public MonthlyReportDTO generateMonthlyReport(final int year, final int month) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.FLIGHT_CONTROL_OPERATOR,
                AiSafeRoles.ADMIN);
        return monthlyReportService.generateMonthlyReport(year, month);
    }
}