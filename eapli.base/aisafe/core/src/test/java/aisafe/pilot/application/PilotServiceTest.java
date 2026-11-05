package aisafe.pilot.application;

import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.aircraftmodel.domain.AircraftModelID;
import aisafe.aircraftmodel.domain.AircraftModelRepository;
import aisafe.aircraftmodel.domain.AircraftType;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
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
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PilotService} (US075, US076, US077).
 * Uses the AAA convention (Arrange, Act, Assert).
 */
class PilotServiceTest {

    // ── Stubs ────────────────────────────────────────────────────────────────

    static class PilotRepositoryStub implements PilotRepository {
        final Map<PilotID, Pilot> store = new LinkedHashMap<>();

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
        public Optional<Pilot> findByPilotID(final PilotID pilotID) {
            return Optional.ofNullable(store.get(pilotID));
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
        final Map<AircraftModelID, AircraftModel> store = new LinkedHashMap<>();

        void add(final AircraftModel model) {
            store.put(model.identity(), model);
        }

        @Override
        public Optional<AircraftModel> findByModelIDAndMaker(
                final AircraftModelID id, final MakerID maker) {
            return store.values().stream()
                    .filter(m -> m.identity().equals(id)
                            && m.maker().identity().equals(maker))
                    .findFirst();
        }

        @Override
        public Optional<AircraftModel> findByModelID(final AircraftModelID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S extends AircraftModel> S save(final S entity) {
            store.put(entity.identity(), entity);
            return entity;
        }

        @Override
        public Iterable<AircraftModel> findAll() {
            return Collections.unmodifiableCollection(store.values());
        }

        @Override
        public Optional<AircraftModel> findById(final AircraftModelID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public void delete(final AircraftModel entity) {
            store.remove(entity.identity());
        }

        @Override
        public void deleteById(final AircraftModelID id) {
            store.remove(id);
        }

        @Override
        public long count() {
            return store.size();
        }
    }

    static class FlightPlanRepositoryStub implements FlightPlanRepository {
        final List<FlightPlan> store = new ArrayList<>();

        void addPlan(final FlightPlan fp) {
            store.add(fp);
        }

        @Override
        public Iterable<FlightPlan> findByPilotID(final PilotID pilotID) {
            return store.stream()
                    .filter(fp -> fp.pilotID().equals(pilotID))
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
        public boolean existsActiveByRouteCodeAfter(final RouteCode rc, final LocalDate d) {
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
    private AircraftModelRepositoryStub aircraftModelRepo;
    private FlightPlanRepositoryStub flightPlanRepo;
    private PilotService service;

    private static final AirTransportCompanyCode TP = AirTransportCompanyCode.of("TP");
    private static final AirTransportCompanyCode FR = AirTransportCompanyCode.of("FR");

    private static final RouteCode ROUTE_CODE = RouteCode.of("TP101");
    private static final AircraftRegistration AIRCRAFT_REGISTRATION =
            AircraftRegistration.of("CS-TTP");

    private static final LocalDate ISSUED = LocalDate.of(2023, 1, 1);
    private static final LocalDate VALID_UNTIL = LocalDate.of(2028, 1, 1);

    private AircraftModel a320;
    private SystemUser systemUser1;
    private SystemUser systemUser2;

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

    @BeforeEach
    void setUp() {
        pilotRepo = new PilotRepositoryStub();
        aircraftModelRepo = new AircraftModelRepositoryStub();
        flightPlanRepo = new FlightPlanRepositoryStub();

        service = new PilotService(pilotRepo, aircraftModelRepo);

        a320 = buildAircraftModel("A320");

        systemUser1 = UserBuilderHelper.builder()
                .with("pilot1", "Password1", "António", "Silva", "antonio.silva@tap.pt")
                .withRoles(Set.of())
                .build();

        systemUser2 = UserBuilderHelper.builder()
                .with("pilot2", "Password1", "Maria", "Santos", "maria.santos@tap.pt")
                .withRoles(Set.of())
                .build();
    }

    private Set<Certification> validCertifications() {
        return Set.of(Certification.of(a320, FlightHours.of(500), ISSUED, VALID_UNTIL));
    }

    private Pilot addPilotToTP(final SystemUser user) {
        return service.addPilot(user, TP, validCertifications());
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

    /**
     * Builds a flight plan assigned to the given pilot in the given status,
     * starting from DRAFT and using valid domain transitions.
     */
    private FlightPlan planFor(final Pilot pilot, final FlightPlanStatus targetStatus) {
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

    // ── US075 — addPilot ─────────────────────────────────────────────────────

    @Test
    void ensurePilotIsCreatedSuccessfully() {
        aircraftModelRepo.add(a320);

        final Pilot pilot = service.addPilot(systemUser1, TP, validCertifications());

        assertNotNull(pilot);
        assertEquals(PilotStatus.ACTIVE, pilot.status());
        assertEquals(TP, pilot.currentCompanyIataCode());
    }

    @Test
    void ensurePilotIsSavedInRepository() {
        aircraftModelRepo.add(a320);

        service.addPilot(systemUser1, TP, validCertifications());

        assertEquals(1, pilotRepo.store.size());
    }

    @Test
    void ensureTwoDifferentPilotsCanBeAdded() {
        aircraftModelRepo.add(a320);

        service.addPilot(systemUser1, TP, validCertifications());

        assertDoesNotThrow(() -> service.addPilot(systemUser2, TP, validCertifications()));
    }

    @Test
    void ensureDuplicateSystemUserIsRejected() {
        aircraftModelRepo.add(a320);

        service.addPilot(systemUser1, TP, validCertifications());

        assertThrows(IllegalStateException.class, () ->
                service.addPilot(systemUser1, TP, validCertifications()));
    }

    @Test
    void ensureEmptyCertificationsAreRejected() {
        assertThrows(Exception.class, () ->
                service.addPilot(systemUser1, TP, Collections.emptySet()));
    }

    @Test
    void ensureNullCertificationsAreRejected() {
        assertThrows(Exception.class, () ->
                service.addPilot(systemUser1, TP, null));
    }

    @Test
    void ensureNullPilotRepositoryIsRejected() {
        assertThrows(Exception.class, () -> new PilotService(null, aircraftModelRepo));
    }

    @Test
    void ensureNullAircraftModelRepositoryIsRejected() {
        assertThrows(Exception.class, () -> new PilotService(pilotRepo, null));
    }

    @Test
    void ensureListAircraftModelsReturnsRegisteredModels() {
        aircraftModelRepo.add(a320);

        final List<AircraftModelDTO> list = new ArrayList<>();
        service.listAircraftModels().forEach(list::add);

        assertEquals(1, list.size());
        assertEquals("A320", list.get(0).modelID);
    }

    @Test
    void ensureListAircraftModelsReturnsEmptyWhenNoneRegistered() {
        final List<AircraftModelDTO> list = new ArrayList<>();
        service.listAircraftModels().forEach(list::add);

        assertTrue(list.isEmpty());
    }

    @Test
    void ensureFindAircraftModelReturnsModelWhenExists() {
        aircraftModelRepo.add(a320);

        assertEquals(a320, service.findAircraftModel("A320"));
    }

    @Test
    void ensureFindAircraftModelThrowsWhenNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
                service.findAircraftModel("UNKNOWN"));
    }

    // ── US076 — listPilotsByCompany (all) ─────────────────────────────────────

    @Test
    void ensureListPilotsReturnsAllPilotsOfCompany() {
        aircraftModelRepo.add(a320);

        addPilotToTP(systemUser1);
        addPilotToTP(systemUser2);

        assertEquals(2, service.listPilotsByCompany(TP).size());
    }

    @Test
    void ensureListPilotsIncludesInactivePilots() {
        aircraftModelRepo.add(a320);

        addPilotToTP(systemUser1);
        final Pilot inactive = addPilotToTP(systemUser2);

        inactive.deactivate();
        pilotRepo.save(inactive);

        final List<PilotDTO> result = service.listPilotsByCompany(TP);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(d -> d.status.equals("ACTIVE")));
        assertTrue(result.stream().anyMatch(d -> d.status.equals("INACTIVE")));
    }

    @Test
    void ensureListPilotsReturnsEmptyWhenNoPilotsInCompany() {
        assertTrue(service.listPilotsByCompany(TP).isEmpty());
    }

    @Test
    void ensureListPilotsDoesNotReturnPilotsFromOtherCompany() {
        aircraftModelRepo.add(a320);

        service.addPilot(systemUser1, FR, validCertifications());

        assertTrue(service.listPilotsByCompany(TP).isEmpty());
    }

    @Test
    void ensureListPilotsWithNullCompanyCodeIsRejected() {
        assertThrows(Exception.class, () -> service.listPilotsByCompany(null));
    }

    // ── US076 — listActivePilotsByCompany (selection) ─────────────────────────

    @Test
    void ensureListActivePilotsExcludesInactive() {
        aircraftModelRepo.add(a320);

        addPilotToTP(systemUser1);
        final Pilot inactive = addPilotToTP(systemUser2);

        inactive.deactivate();
        pilotRepo.save(inactive);

        final List<PilotDTO> result = service.listActivePilotsByCompany(TP);

        assertEquals(1, result.size());
        assertEquals("ACTIVE", result.get(0).status);
    }

    @Test
    void ensureListActivePilotsReturnsCorrectDTO() {
        aircraftModelRepo.add(a320);

        addPilotToTP(systemUser1);

        final List<PilotDTO> result = service.listActivePilotsByCompany(TP);

        assertEquals(1, result.size());

        final PilotDTO dto = result.get(0);

        assertEquals("TP", dto.companyIata);
        assertEquals("ACTIVE", dto.status);
        assertEquals(1, dto.certificationCount);
    }

    // ── US077 — removePilot ───────────────────────────────────────────────────

    @Test
    void ensureRemovePilotSetsStatusToInactive() {
        aircraftModelRepo.add(a320);

        final Pilot pilot = addPilotToTP(systemUser1);

        final PilotDTO result = service.removePilot(pilot.identity(), TP, flightPlanRepo);

        assertEquals("INACTIVE", result.status);
    }

    @Test
    void ensureRemovePilotPersistsInactiveStatus() {
        aircraftModelRepo.add(a320);

        final Pilot pilot = addPilotToTP(systemUser1);

        service.removePilot(pilot.identity(), TP, flightPlanRepo);

        final Pilot persisted = pilotRepo.findByPilotID(pilot.identity()).orElseThrow();

        assertFalse(persisted.isActive());
    }

    @Test
    void ensureRemovePilotFailsWhenPilotNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
                service.removePilot(PilotID.newID(), TP, flightPlanRepo));
    }

    @Test
    void ensureRemovePilotFailsWhenPilotBelongsToAnotherCompany() {
        aircraftModelRepo.add(a320);

        final Pilot pilot = addPilotToTP(systemUser1);

        assertThrows(IllegalArgumentException.class, () ->
                service.removePilot(pilot.identity(), FR, flightPlanRepo));
    }

    @Test
    void ensureRemovePilotFailsWithDraftFlightPlan() {
        aircraftModelRepo.add(a320);

        final Pilot pilot = addPilotToTP(systemUser1);

        flightPlanRepo.addPlan(planFor(pilot, FlightPlanStatus.DRAFT));

        assertThrows(IllegalArgumentException.class, () ->
                service.removePilot(pilot.identity(), TP, flightPlanRepo));
    }

    @Test
    void ensureRemovePilotFailsWithTestedFlightPlan() {
        aircraftModelRepo.add(a320);

        final Pilot pilot = addPilotToTP(systemUser1);

        flightPlanRepo.addPlan(planFor(pilot, FlightPlanStatus.TESTED));

        assertThrows(IllegalArgumentException.class, () ->
                service.removePilot(pilot.identity(), TP, flightPlanRepo));
    }

    @Test
    void ensureRemovePilotFailsWithValidatedFlightPlan() {
        aircraftModelRepo.add(a320);

        final Pilot pilot = addPilotToTP(systemUser1);

        flightPlanRepo.addPlan(planFor(pilot, FlightPlanStatus.VALIDATED));

        assertThrows(IllegalArgumentException.class, () ->
                service.removePilot(pilot.identity(), TP, flightPlanRepo));
    }

    @Test
    void ensureRemovePilotSucceedsWithCancelledFlightPlan() {
        aircraftModelRepo.add(a320);

        final Pilot pilot = addPilotToTP(systemUser1);

        flightPlanRepo.addPlan(planFor(pilot, FlightPlanStatus.CANCELLED));

        assertDoesNotThrow(() ->
                service.removePilot(pilot.identity(), TP, flightPlanRepo));
    }

    @Test
    void ensureRemovePilotSucceedsWithRejectedFlightPlan() {
        aircraftModelRepo.add(a320);

        final Pilot pilot = addPilotToTP(systemUser1);

        flightPlanRepo.addPlan(planFor(pilot, FlightPlanStatus.REJECTED));

        assertDoesNotThrow(() ->
                service.removePilot(pilot.identity(), TP, flightPlanRepo));
    }

    @Test
    void ensureRemovePilotSucceedsWithNoFlightPlans() {
        aircraftModelRepo.add(a320);

        final Pilot pilot = addPilotToTP(systemUser1);

        assertDoesNotThrow(() ->
                service.removePilot(pilot.identity(), TP, flightPlanRepo));
    }

    @Test
    void ensureRemovePilotWithNullPilotIDIsRejected() {
        assertThrows(Exception.class, () ->
                service.removePilot(null, TP, flightPlanRepo));
    }

    @Test
    void ensureRemovePilotWithNullCompanyCodeIsRejected() {
        aircraftModelRepo.add(a320);

        final Pilot pilot = addPilotToTP(systemUser1);

        assertThrows(Exception.class, () ->
                service.removePilot(pilot.identity(), null, flightPlanRepo));
    }

    @Test
    void ensureRemovePilotWithNullFlightPlanRepositoryIsRejected() {
        aircraftModelRepo.add(a320);

        final Pilot pilot = addPilotToTP(systemUser1);

        assertThrows(Exception.class, () ->
                service.removePilot(pilot.identity(), TP, null));
    }
}