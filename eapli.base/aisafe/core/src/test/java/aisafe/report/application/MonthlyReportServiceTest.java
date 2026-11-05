package aisafe.report.application;

import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.flightplan.domain.*;
import aisafe.flightroute.domain.*;
import aisafe.pilot.domain.Pilot;
import aisafe.pilot.domain.PilotID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for US112 - MonthlyReportService.
 *
 * <p>Uses an inline stub of {@link FlightPlanRepository}.
 *
 * <p>NOTE: FlightPlan.createdAt() is always set to LocalDateTime.now() and
 * cannot be controlled, so date-based filtering is exercised against the
 * current month; the per-status / per-route counting is tested directly via the
 * package-private helpers, independently of the creation date.
 */
class MonthlyReportServiceTest {

    // ── Repository stub ─────────────────────────────────────────────────────

    static class FlightPlanRepositoryStub implements FlightPlanRepository {
        final List<FlightPlan> store = new ArrayList<>();

        void addPlan(final FlightPlan fp) {
            store.add(fp);
        }

        @Override
        public Iterable<FlightPlan> findByPilotID(final PilotID id) {
            return store.stream()
                    .filter(fp -> fp.pilotID().equals(id))
                    .toList();
        }

        @Override
        public Iterable<FlightPlan> findByRouteCode(final RouteCode rc) {
            return store.stream()
                    .filter(fp -> fp.routeCode().equals(rc))
                    .toList();
        }

        @Override
        public Iterable<FlightPlan> findByAircraftRegistration(final AircraftRegistration r) {
            return store.stream()
                    .filter(fp -> fp.aircraftRegistration().equals(r))
                    .toList();
        }

        @Override
        public Iterable<FlightPlan> findByStatus(final FlightPlanStatus s) {
            return store.stream()
                    .filter(fp -> fp.status() == s)
                    .toList();
        }

        @Override
        public boolean existsActiveByRouteCodeAfter(final RouteCode rc,
                                                    final LocalDate d) {
            return store.stream()
                    .anyMatch(fp -> fp.routeCode().equals(rc)
                            && fp.plannedDepartureDateTime().toLocalDate().isAfter(d)
                            && fp.status() != FlightPlanStatus.CANCELLED
                            && fp.status() != FlightPlanStatus.REJECTED);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S extends FlightPlan> S save(final S e) {
            store.add(e);
            return e;
        }

        @Override
        public Iterable<FlightPlan> findAll() {
            return Collections.unmodifiableList(store);
        }

        @Override
        public Optional<FlightPlan> findById(final FlightPlanID id) {
            return store.stream()
                    .filter(fp -> fp.identity().equals(id))
                    .findFirst();
        }

        @Override
        public void delete(final FlightPlan e) {
            store.remove(e);
        }

        @Override
        public void deleteById(final FlightPlanID id) {
            store.removeIf(fp -> fp.identity().equals(id));
        }

        @Override
        public long count() {
            return store.size();
        }
    }

    // ── Fake domain objects ─────────────────────────────────────────────────

    static class FakeAircraft extends Aircraft {

        private final AircraftRegistration registration;
        private final AirTransportCompanyCode companyCode;

        FakeAircraft(final AircraftRegistration registration,
                     final AirTransportCompanyCode companyCode) {
            this.registration = registration;
            this.companyCode = companyCode;
        }

        @Override
        public AircraftRegistration identity() {
            return registration;
        }

        @Override
        public AircraftRegistration aircraftCode() {
            return registration;
        }

        @Override
        public boolean isOperational() {
            return true;
        }

        @Override
        public boolean belongsTo(final AirTransportCompanyCode companyCode) {
            return this.companyCode.equals(companyCode);
        }
    }

    static class FakePilot extends Pilot {

        private final PilotID pilotID;
        private final AirTransportCompanyCode companyCode;

        FakePilot(final PilotID pilotID,
                  final AirTransportCompanyCode companyCode) {
            this.pilotID = pilotID;
            this.companyCode = companyCode;
        }

        @Override
        public PilotID identity() {
            return pilotID;
        }

        @Override
        public PilotID pilotID() {
            return pilotID;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public AirTransportCompanyCode currentCompanyIataCode() {
            return companyCode;
        }

        @Override
        public boolean belongsTo(final AirTransportCompanyCode companyCode) {
            return this.companyCode.equals(companyCode);
        }
    }

    // ── Fixtures ────────────────────────────────────────────────────────────

    private static final AirTransportCompanyCode TP =
            AirTransportCompanyCode.of("TP");

    private static final AircraftRegistration AIRCRAFT_REGISTRATION =
            AircraftRegistration.of("CS-TTP");

    private RegularSchedule regularSchedule() {
        return RegularSchedule.of(List.of(
                ScheduleEntry.of(DayOfWeek.MONDAY, LocalTime.of(9, 0))
        ));
    }

    private FlightRoute routeFor(final String routeCode) {
        return new FlightRouteFactory()
                .withRouteCode(RouteCode.of(routeCode))
                .withDepartureAirport(aisafe.airport.domain.AirportIataCode.of("LIS"))
                .withArrivalAirport(aisafe.airport.domain.AirportIataCode.of("OPO"))
                .withCompany(TP)
                .asRegular(regularSchedule())
                .build();
    }

    private Aircraft validAircraft() {
        return new FakeAircraft(AIRCRAFT_REGISTRATION, TP);
    }

    private Pilot validPilot() {
        return new FakePilot(PilotID.newID(), TP);
    }

    // ── Helper: build a flight plan in a given status ───────────────────────

    private FlightPlan planFor(final String routeCode,
                               final FlightPlanStatus targetStatus) {
        final FlightPlan plan = FlightPlan.draft(
                FlightPlanID.newID(),
                routeFor(routeCode),
                validAircraft(),
                validPilot(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                FuelQuantity.of(5000.0, FuelUnit.KG),
                FlightPlanDescription.of("Test flight plan")
        );

        switch (targetStatus) {
            case TESTED:
                plan.markTested();
                break;
            case VALIDATED:
                plan.markTested();
                plan.markValidated();
                break;
            case REJECTED:
                plan.reject();
                break;
            case CANCELLED:
                plan.cancel();
                break;
            case DRAFT:
            default:
                break;
        }

        return plan;
    }

    private FlightPlanRepositoryStub repo;
    private MonthlyReportService service;

    @BeforeEach
    void setUp() {
        repo = new FlightPlanRepositoryStub();
        service = new MonthlyReportService(repo);
    }

    // ── Validation ──────────────────────────────────────────────────────────

    @Test
    void ensureRejectsInvalidMonth() {
        assertThrows(IllegalArgumentException.class,
                () -> service.generateMonthlyReport(2026, 13));
    }

    @Test
    void ensureRejectsMonthZero() {
        assertThrows(IllegalArgumentException.class,
                () -> service.generateMonthlyReport(2026, 0));
    }

    @Test
    void ensureRejectsInvalidYear() {
        assertThrows(IllegalArgumentException.class,
                () -> service.generateMonthlyReport(1500, 6));
    }

    // ── countByStatus ───────────────────────────────────────────────────────

    @Test
    void ensureCountsByStatus() {
        final List<FlightPlan> plans = List.of(
                planFor("TP100", FlightPlanStatus.DRAFT),
                planFor("TP200", FlightPlanStatus.DRAFT),
                planFor("TP300", FlightPlanStatus.VALIDATED)
        );

        final Map<String, Integer> counts = service.countByStatus(plans);

        assertEquals(2, counts.get("DRAFT"));
        assertEquals(1, counts.get("VALIDATED"));
    }

    @Test
    void ensureCountByStatusIsEmptyForNoPlans() {
        assertTrue(service.countByStatus(List.of()).isEmpty());
    }

    // ── countByRoute ────────────────────────────────────────────────────────

    @Test
    void ensureCountsByRoute() {
        final List<FlightPlan> plans = List.of(
                planFor("TP100", FlightPlanStatus.DRAFT),
                planFor("TP100", FlightPlanStatus.DRAFT),
                planFor("TP200", FlightPlanStatus.DRAFT)
        );

        final Map<String, Integer> counts = service.countByRoute(plans);

        assertEquals(2, counts.get("TP100"));
        assertEquals(1, counts.get("TP200"));
    }

    // ── filterByMonth ───────────────────────────────────────────────────────

    @Test
    void ensureFilterKeepsPlansOfCurrentMonth() {
        final LocalDateTime now = LocalDateTime.now();

        final List<FlightPlan> plans = List.of(
                planFor("TP100", FlightPlanStatus.DRAFT),
                planFor("TP200", FlightPlanStatus.DRAFT)
        );

        final List<FlightPlan> filtered =
                service.filterByMonth(plans, now.getYear(), now.getMonthValue());

        assertEquals(2, filtered.size());
    }

    @Test
    void ensureFilterExcludesOtherMonths() {
        final LocalDateTime now = LocalDateTime.now();

        final List<FlightPlan> plans = List.of(
                planFor("TP100", FlightPlanStatus.DRAFT)
        );

        final int otherMonth = now.getMonthValue() == 1 ? 2 : 1;

        final List<FlightPlan> filtered =
                service.filterByMonth(plans, now.getYear(), otherMonth);

        assertTrue(filtered.isEmpty());
    }

    // ── End-to-end for the current month ────────────────────────────────────

    @Test
    void ensureGeneratesReportForCurrentMonth() {
        final LocalDateTime now = LocalDateTime.now();

        repo.addPlan(planFor("TP100", FlightPlanStatus.DRAFT));
        repo.addPlan(planFor("TP100", FlightPlanStatus.VALIDATED));

        final MonthlyReportDTO dto =
                service.generateMonthlyReport(now.getYear(), now.getMonthValue());

        assertEquals(2, dto.totalFlightPlans);
        assertEquals(2, dto.countByRoute.get("TP100"));
        assertTrue(dto.renderedReport.contains("AISafe"));
        assertTrue(dto.renderedReport.contains("Monthly Flight Plan Statistics"));
    }

    @Test
    void ensureEmptyMonthYieldsZeroTotal() {
        final LocalDateTime now = LocalDateTime.now();
        final int otherMonth = now.getMonthValue() == 1 ? 2 : 1;

        final MonthlyReportDTO dto =
                service.generateMonthlyReport(now.getYear(), otherMonth);

        assertEquals(0, dto.totalFlightPlans);
        assertTrue(dto.countByStatus.isEmpty());
        assertTrue(dto.countByRoute.isEmpty());
    }
}