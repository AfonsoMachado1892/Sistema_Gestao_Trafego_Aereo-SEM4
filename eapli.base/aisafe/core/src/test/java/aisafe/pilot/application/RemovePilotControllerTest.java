package aisafe.pilot.application;

import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.aircraftmodel.domain.AircraftModelID;
import aisafe.aircraftmodel.domain.AircraftModelRepository;
import aisafe.aircraftmodel.domain.AircraftType;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.domain.CustomerCollaboratorID;
import aisafe.costumerCollaborator.domain.CustomerReference;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.enginemodel.domain.EngineModel;
import aisafe.enginemodel.domain.EngineModelID;
import aisafe.enginemodel.domain.EngineType;
import aisafe.enginemodel.domain.FuelType;
import aisafe.flightplan.domain.*;
import aisafe.flightroute.domain.*;
import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerID;
import aisafe.maker.domain.MakerType;
import aisafe.pilot.domain.*;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import aisafe.usermanagement.domain.UserBuilderHelper;
import aisafe.usermanagement.domain.legacyFiles.Email;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RemovePilotController} (US077).
 * Uses the AAA convention (Arrange, Act, Assert).
 *
 * <p>Tests use the package-private {@code removePilot(pilotID, companyCode)}
 * to bypass authentication, mirroring DeactivateFlightRouteControllerTest.
 */
class RemovePilotControllerTest {

    // ── Stubs ────────────────────────────────────────────────────────────────

    static class AuthorizationServiceStub extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) { }
    }

    static class PilotRepositoryStub implements PilotRepository {
        final Map<PilotID, Pilot> store = new LinkedHashMap<>();

        void add(final Pilot pilot) {
            store.put(pilot.identity(), pilot);
        }

        @Override
        public Optional<Pilot> findBySystemUser(final SystemUser u) {
            return store.values().stream()
                    .filter(p -> p.systemUser().equals(u))
                    .findFirst();
        }

        @Override
        public Iterable<Pilot> findByCompany(final AirTransportCompanyCode c) {
            return store.values().stream()
                    .filter(p -> p.currentCompanyIataCode().equals(c))
                    .toList();
        }

        @Override
        public Iterable<Pilot> findActiveByCompany(final AirTransportCompanyCode c) {
            return store.values().stream()
                    .filter(p -> p.currentCompanyIataCode().equals(c) && p.isActive())
                    .toList();
        }

        @Override
        public Optional<Pilot> findByPilotID(final PilotID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S extends Pilot> S save(final S entity) {
            store.put(entity.identity(), entity);
            return entity;
        }

        @Override
        public Iterable<Pilot> findAll() {
            return Collections.unmodifiableCollection(store.values());
        }

        @Override
        public Optional<Pilot> findById(final PilotID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public void delete(final Pilot entity) {
            store.remove(entity.identity());
        }

        @Override
        public void deleteById(final PilotID id) {
            store.remove(id);
        }

        @Override
        public long count() {
            return store.size();
        }
    }

    static class AircraftModelRepositoryStub implements AircraftModelRepository {
        @Override
        public Optional<AircraftModel> findByModelIDAndMaker(final AircraftModelID id,
                                                             final MakerID m) {
            return Optional.empty();
        }

        @Override
        public Optional<AircraftModel> findByModelID(final AircraftModelID id) {
            return Optional.empty();
        }

        @Override
        public <S extends AircraftModel> S save(final S e) {
            return e;
        }

        @Override
        public Iterable<AircraftModel> findAll() {
            return Collections.emptyList();
        }

        @Override
        public Optional<AircraftModel> findById(final AircraftModelID id) {
            return Optional.empty();
        }

        @Override
        public void delete(final AircraftModel e) { }

        @Override
        public void deleteById(final AircraftModelID id) { }

        @Override
        public long count() {
            return 0;
        }
    }

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

    static class CustomerCollaboratorRepositoryStub implements CustomerCollaboratorRepository {
        @Override
        public Optional<CustomerCollaborator> findBySystemUser(final SystemUser u) {
            return Optional.empty();
        }

        @Override
        public Optional<CustomerCollaborator> findByUsername(final Username u) {
            return Optional.empty();
        }

        @Override
        public Optional<CustomerCollaborator> findByEmail(final Email e) {
            return Optional.empty();
        }

        @Override
        public Iterable<CustomerCollaborator> findByCustomerReference(final CustomerReference r) {
            return Collections.emptyList();
        }

        @Override
        public <S extends CustomerCollaborator> S save(final S entity) {
            return entity;
        }

        @Override
        public Iterable<CustomerCollaborator> findAll() {
            return Collections.emptyList();
        }

        @Override
        public Optional<CustomerCollaborator> findById(final CustomerCollaboratorID id) {
            return Optional.empty();
        }

        @Override
        public void delete(final CustomerCollaborator entity) { }

        @Override
        public void deleteById(final CustomerCollaboratorID id) { }

        @Override
        public Iterable<CustomerCollaborator> findAllActive() {
            return Collections.emptyList();
        }

        @Override
        public long count() {
            return 0;
        }
    }

    // ── Fake domain objects ──────────────────────────────────────────────────

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

    // ── Setup ────────────────────────────────────────────────────────────────

    private PilotRepositoryStub pilotRepo;
    private FlightPlanRepositoryStub flightPlanRepo;
    private RemovePilotController controller;

    private static final AirTransportCompanyCode TP = AirTransportCompanyCode.of("TP");
    private static final RouteCode ROUTE_CODE = RouteCode.of("TP101");
    private static final AircraftRegistration AIRCRAFT_REGISTRATION =
            AircraftRegistration.of("CS-TTP");

    private static final LocalDate ISSUED = LocalDate.of(2023, 1, 1);
    private static final LocalDate VALID_UNTIL = LocalDate.of(2028, 1, 1);

    private AircraftModel a320;
    private SystemUser systemUser1;

    private AircraftModel buildAircraftModel(final String modelID) {
        final Maker engineMaker = new Maker(
                MakerID.of("CFM"),
                "CFM International",
                new Country(IsoCode.of("FR"), "France"),
                Set.of(MakerType.ENGINE)
        );

        final EngineModel engine = new EngineModel(
                EngineModelID.of("CFM56-5B"),
                "CFM56-5B",
                EngineType.TURBOFAN,
                120000.0,
                FuelType.JET_A1,
                0.36,
                engineMaker
        );

        final Maker airbus = new Maker(
                MakerID.of("AIRBUS"),
                "Airbus",
                new Country(IsoCode.of("FR"), "France"),
                Set.of(MakerType.AIRCRAFT)
        );

        return new AircraftModel(
                AircraftModelID.of(modelID),
                airbus,
                AircraftType.PASSENGER,
                Set.of(engine),
                42000,
                78000,
                70000,
                26000,
                12000,
                833,
                122.6,
                0.025,
                1.5,
                6300,
                180,
                2
        );
    }

    private Pilot buildAndStorePilot(final SystemUser user,
                                     final AirTransportCompanyCode company) {
        final Pilot pilot = new PilotFactory()
                .withSystemUser(user)
                .withCompany(company)
                .withCertifications(Set.of(
                        Certification.of(a320, FlightHours.of(500), ISSUED, VALID_UNTIL)))
                .build();

        pilotRepo.add(pilot);

        return pilot;
    }

    private RegularSchedule regularSchedule() {
        return RegularSchedule.of(List.of(
                ScheduleEntry.of(DayOfWeek.MONDAY, LocalTime.of(9, 0))
        ));
    }

    private FlightRoute validRoute() {
        return new FlightRouteFactory()
                .withRouteCode(ROUTE_CODE)
                .withDepartureAirport(aisafe.airport.domain.AirportIataCode.of("LIS"))
                .withArrivalAirport(aisafe.airport.domain.AirportIataCode.of("OPO"))
                .withCompany(TP)
                .asRegular(regularSchedule())
                .build();
    }

    private Aircraft validAircraft() {
        return new FakeAircraft(AIRCRAFT_REGISTRATION, TP);
    }

    private FlightPlan planFor(final Pilot pilot,
                               final FlightPlanStatus targetStatus) {
        final FlightPlan plan = FlightPlan.draft(
                FlightPlanID.newID(),
                validRoute(),
                validAircraft(),
                pilot,
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

    @BeforeEach
    void setUp() {
        pilotRepo = new PilotRepositoryStub();
        flightPlanRepo = new FlightPlanRepositoryStub();

        controller = new RemovePilotController(
                new AuthorizationServiceStub(),
                pilotRepo,
                new AircraftModelRepositoryStub(),
                flightPlanRepo,
                new CustomerCollaboratorRepositoryStub()
        );

        a320 = buildAircraftModel("A320");

        systemUser1 = UserBuilderHelper.builder()
                .with("p1", "Password1", "Ana", "Silva", "ana@tp.pt")
                .withRoles(Set.of())
                .build();
    }

    // ── removePilot — success ─────────────────────────────────────────────────

    @Test
    void ensurePilotIsSuccessfullyRemoved() {
        final Pilot pilot = buildAndStorePilot(systemUser1, TP);

        final PilotDTO result = controller.removePilot(pilot.identity().number(), "TP");

        assertNotNull(result);
        assertEquals("INACTIVE", result.status);
    }

    @Test
    void ensureRemovedPilotIsPersistedAsInactive() {
        final Pilot pilot = buildAndStorePilot(systemUser1, TP);

        controller.removePilot(pilot.identity().number(), "TP");

        final Pilot persisted = pilotRepo.findByPilotID(pilot.identity()).orElseThrow();

        assertFalse(persisted.isActive());
    }

    @Test
    void ensureRemoveSucceedsWithNoPlans() {
        final Pilot pilot = buildAndStorePilot(systemUser1, TP);

        assertDoesNotThrow(() ->
                controller.removePilot(pilot.identity().number(), "TP"));
    }

    @Test
    void ensureRemoveSucceedsWithCancelledPlan() {
        final Pilot pilot = buildAndStorePilot(systemUser1, TP);

        flightPlanRepo.addPlan(planFor(pilot, FlightPlanStatus.CANCELLED));

        assertDoesNotThrow(() ->
                controller.removePilot(pilot.identity().number(), "TP"));
    }

    @Test
    void ensureRemoveSucceedsWithRejectedPlan() {
        final Pilot pilot = buildAndStorePilot(systemUser1, TP);

        flightPlanRepo.addPlan(planFor(pilot, FlightPlanStatus.REJECTED));

        assertDoesNotThrow(() ->
                controller.removePilot(pilot.identity().number(), "TP"));
    }

    // ── removePilot — failures ────────────────────────────────────────────────

    @Test
    void ensureRemoveFailsForNonExistentPilot() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.removePilot(PilotID.newID().number(), "TP"));
    }

    @Test
    void ensureRemoveFailsWhenPilotBelongsToAnotherCompany() {
        final Pilot pilot = buildAndStorePilot(systemUser1, TP);

        assertThrows(IllegalArgumentException.class, () ->
                controller.removePilot(pilot.identity().number(), "FR"));
    }

    @Test
    void ensureRemoveFailsWithDraftPlan() {
        final Pilot pilot = buildAndStorePilot(systemUser1, TP);

        flightPlanRepo.addPlan(planFor(pilot, FlightPlanStatus.DRAFT));

        assertThrows(IllegalArgumentException.class, () ->
                controller.removePilot(pilot.identity().number(), "TP"));
    }

    @Test
    void ensureRemoveFailsWithTestedPlan() {
        final Pilot pilot = buildAndStorePilot(systemUser1, TP);

        flightPlanRepo.addPlan(planFor(pilot, FlightPlanStatus.TESTED));

        assertThrows(IllegalArgumentException.class, () ->
                controller.removePilot(pilot.identity().number(), "TP"));
    }

    @Test
    void ensureRemoveFailsWithValidatedPlan() {
        final Pilot pilot = buildAndStorePilot(systemUser1, TP);

        flightPlanRepo.addPlan(planFor(pilot, FlightPlanStatus.VALIDATED));

        assertThrows(IllegalArgumentException.class, () ->
                controller.removePilot(pilot.identity().number(), "TP"));
    }

    @Test
    void ensureAlreadyInactivePilotCannotBeRemovedAgain() {
        final Pilot pilot = buildAndStorePilot(systemUser1, TP);

        controller.removePilot(pilot.identity().number(), "TP");

        assertThrows(IllegalArgumentException.class, () ->
                controller.removePilot(pilot.identity().number(), "TP"));
    }
}