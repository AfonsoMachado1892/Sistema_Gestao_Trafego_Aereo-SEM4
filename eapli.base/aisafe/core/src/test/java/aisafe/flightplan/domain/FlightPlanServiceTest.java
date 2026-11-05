package aisafe.flightplan.domain;

import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.aircraft.domain.AircraftRepository;
import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.flightplan.application.FlightPlanDTO;
import aisafe.flightroute.application.FlightRouteDTO;
import aisafe.flightroute.domain.*;
import aisafe.pilot.domain.Pilot;
import aisafe.pilot.domain.PilotID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanServiceTest {

    // ── Stub repositories ────────────────────────────────────────────────────

    static class FlightPlanRepositoryStub implements FlightPlanRepository {

        final Map<String, FlightPlan> store = new LinkedHashMap<>();

        @Override
        public boolean existsActiveByRouteCodeAfter(final RouteCode routeCode,
                                                    final LocalDate date) {
            return store.values().stream()
                    .anyMatch(fp ->
                            fp.routeCode().equals(routeCode)
                                    && isActive(fp.status())
                                    && fp.plannedDepartureDateTime()
                                    .toLocalDate()
                                    .isAfter(date));
        }

        private boolean isActive(final FlightPlanStatus status) {
            return status == FlightPlanStatus.DRAFT
                    || status == FlightPlanStatus.TESTED
                    || status == FlightPlanStatus.VALIDATED;
        }

        @Override
        public Iterable<FlightPlan> findByRouteCode(final RouteCode routeCode) {
            return store.values().stream()
                    .filter(fp -> fp.routeCode().equals(routeCode))
                    .toList();
        }

        @Override
        public Iterable<FlightPlan> findByAircraftRegistration(
                final AircraftRegistration aircraftRegistration) {
            return store.values().stream()
                    .filter(fp -> fp.aircraftRegistration().equals(aircraftRegistration))
                    .toList();
        }

        @Override
        public Iterable<FlightPlan> findByPilotID(final PilotID pilotID) {
            return store.values().stream()
                    .filter(fp -> fp.pilotID().equals(pilotID))
                    .toList();
        }

        @Override
        public Iterable<FlightPlan> findByStatus(final FlightPlanStatus status) {
            return store.values().stream()
                    .filter(fp -> fp.status() == status)
                    .toList();
        }

        @Override
        public <S extends FlightPlan> S save(final S entity) {
            store.put(entity.identity().toString(), entity);
            return entity;
        }

        @Override
        public Iterable<FlightPlan> findAll() {
            return Collections.unmodifiableCollection(store.values());
        }

        @Override
        public Optional<FlightPlan> findById(final FlightPlanID id) {
            return Optional.ofNullable(store.get(id.toString()));
        }

        @Override
        public void delete(final FlightPlan entity) {
            store.remove(entity.identity().toString());
        }

        @Override
        public void deleteById(final FlightPlanID id) {
            store.remove(id.toString());
        }

        @Override
        public long count() {
            return store.size();
        }
    }

    static class FlightRouteRepositoryStub implements FlightRouteRepository {

        final Map<String, FlightRoute> store = new LinkedHashMap<>();

        void add(final FlightRoute route) {
            store.put(route.routeCode().value(), route);
        }

        @Override
        public Optional<FlightRoute> findByRouteCode(final RouteCode routeCode) {
            return Optional.ofNullable(store.get(routeCode.value()));
        }

        @Override
        public Iterable<FlightRoute> findByCompany(final AirTransportCompanyCode companyIataCode) {
            return store.values().stream()
                    .filter(r -> r.airTransportCompanyIataCode().equals(companyIataCode))
                    .toList();
        }

        @Override
        public Iterable<FlightRoute> findActiveByCompany(final AirTransportCompanyCode companyIataCode) {
            return store.values().stream()
                    .filter(r -> r.airTransportCompanyIataCode().equals(companyIataCode))
                    .filter(FlightRoute::isActive)
                    .toList();
        }

        @Override
        public <S extends FlightRoute> S save(final S entity) {
            store.put(entity.routeCode().value(), entity);
            return entity;
        }

        @Override
        public Iterable<FlightRoute> findAll() {
            return Collections.unmodifiableCollection(store.values());
        }

        @Override
        public Optional<FlightRoute> findById(final RouteCode id) {
            return findByRouteCode(id);
        }

        @Override
        public void delete(final FlightRoute entity) {
            store.remove(entity.routeCode().value());
        }

        @Override
        public void deleteById(final RouteCode id) {
            store.remove(id.value());
        }

        @Override
        public long count() {
            return store.size();
        }
    }

    static class AircraftRepositoryStub implements AircraftRepository {

        final Map<String, Aircraft> store = new LinkedHashMap<>();

        void add(final AircraftRegistration registration, final Aircraft aircraft) {
            store.put(registration.code(), aircraft);
        }

        @Override
        public Optional<Aircraft> findByRegistration(final AircraftRegistration registration) {
            return Optional.ofNullable(store.get(registration.code()));
        }

        @Override
        public Iterable<Aircraft> findByAirTransportCompany(final AirTransportCompany airTransportCompany) {
            return Collections.emptyList();
        }

        @Override
        public Iterable<Aircraft> findAll() {
            return Collections.unmodifiableCollection(store.values());
        }

        @Override
        public Optional<Aircraft> findById(final AircraftRegistration id) {
            return findByRegistration(id);
        }

        @Override
        public <S extends Aircraft> S save(final S entity) {
            store.put(entity.identity().code(), entity);
            return entity;
        }

        @Override
        public void delete(final Aircraft entity) {
            store.remove(entity.identity().code());
        }

        @Override
        public void deleteById(final AircraftRegistration id) {
            store.remove(id.code());
        }

        @Override
        public long count() {
            return store.size();
        }
    }

    // ── Fake domain objects ──────────────────────────────────────────────────

    static class FakePilot extends Pilot {

        private final PilotID pilotID;
        private final AirTransportCompanyCode companyCode;
        private final boolean active;

        FakePilot(final PilotID pilotID,
                  final AirTransportCompanyCode companyCode,
                  final boolean active) {
            this.pilotID = pilotID;
            this.companyCode = companyCode;
            this.active = active;
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
            return active;
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

    static class FakeAircraft extends Aircraft {

        private final AircraftRegistration registration;
        private final boolean operational;
        private final AirTransportCompanyCode companyCode;

        FakeAircraft(final AircraftRegistration registration,
                     final boolean operational,
                     final AirTransportCompanyCode companyCode) {
            this.registration = registration;
            this.operational = operational;
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
            return operational;
        }

        @Override
        public boolean belongsTo(final AirTransportCompanyCode companyCode) {
            return this.companyCode.equals(companyCode);
        }
    }

    // ── Fixtures ─────────────────────────────────────────────────────────────

    private FlightPlanRepositoryStub flightPlanRepository;
    private FlightRouteRepositoryStub flightRouteRepository;
    private AircraftRepositoryStub aircraftRepository;
    private FlightPlanService service;

    private static final AirTransportCompanyCode TP = AirTransportCompanyCode.of("TP");
    private static final AirTransportCompanyCode FR = AirTransportCompanyCode.of("FR");

    private static final RouteCode ROUTE_CODE = RouteCode.of("TP101");
    private static final AircraftRegistration AIRCRAFT_REGISTRATION = AircraftRegistration.of("CS-TAB");
    private static final PilotID PILOT_ID = PilotID.of("PILOT-001");

    private static final LocalDateTime VALID_DEPARTURE =
            LocalDateTime.of(2026, 6, 15, 9, 0); // Monday

    private static final LocalDateTime INVALID_DEPARTURE =
            LocalDateTime.of(2026, 6, 15, 10, 0); // Monday, wrong time

    private static final LocalDateTime ARRIVAL =
            LocalDateTime.of(2026, 6, 15, 11, 0);

    @BeforeEach
    void setUp() {
        flightPlanRepository = new FlightPlanRepositoryStub();
        flightRouteRepository = new FlightRouteRepositoryStub();
        aircraftRepository = new AircraftRepositoryStub();

        service = new FlightPlanService(
                flightPlanRepository,
                flightRouteRepository,
                aircraftRepository
        );

        flightRouteRepository.add(validRegularRoute());

        aircraftRepository.add(
                AIRCRAFT_REGISTRATION,
                new FakeAircraft(AIRCRAFT_REGISTRATION, true, TP)
        );
    }

    private RegularSchedule regularSchedule() {
        return RegularSchedule.of(List.of(
                ScheduleEntry.of(DayOfWeek.MONDAY, LocalTime.of(9, 0))
        ));
    }

    private FlightRoute validRegularRoute() {
        return new FlightRouteFactory()
                .withRouteCode(ROUTE_CODE)
                .withDepartureAirport(aisafe.airport.domain.AirportIataCode.of("LIS"))
                .withArrivalAirport(aisafe.airport.domain.AirportIataCode.of("OPO"))
                .withCompany(TP)
                .asRegular(regularSchedule())
                .build();
    }

    private Pilot activePilotFromTP() {
        return new FakePilot(PILOT_ID, TP, true);
    }

    // ── Success ──────────────────────────────────────────────────────────────

    @Test
    void ensureDraftFlightPlanIsCreatedSuccessfully() {
        final FlightPlanDTO result = service.createDraftFlightPlan(
                activePilotFromTP(),
                "TP101",
                "CS-TAB",
                VALID_DEPARTURE,
                ARRIVAL,
                12000,
                FuelUnit.KG,
                "RAW FLIGHT PLAN DESCRIPTION"
        );

        assertNotNull(result);
        assertEquals(ROUTE_CODE.toString(), result.routeCode());
        assertEquals(AIRCRAFT_REGISTRATION.code(), result.aircraftRegistration());
        assertEquals(PILOT_ID.toString(), result.pilotID());
        assertEquals(VALID_DEPARTURE, result.plannedDepartureDateTime());
        assertEquals(ARRIVAL, result.plannedArrivalDateTime());
        assertEquals(FlightPlanStatus.DRAFT.name(), result.status());
        assertEquals(1, flightPlanRepository.count());
    }

    @Test
    void ensureCreatedFlightPlanHasFuelQuantity() {
        final FlightPlanDTO result = service.createDraftFlightPlan(
                activePilotFromTP(),
                "TP101",
                "CS-TAB",
                VALID_DEPARTURE,
                ARRIVAL,
                12000,
                FuelUnit.KG,
                "RAW FLIGHT PLAN DESCRIPTION"
        );

        assertEquals(12000, result.fuelAmount());
        assertEquals(FuelUnit.KG.name(), result.fuelUnit());
    }

    @Test
    void ensureCreatedFlightPlanHasDescription() {
        final FlightPlanDTO result = service.createDraftFlightPlan(
                activePilotFromTP(),
                "TP101",
                "CS-TAB",
                VALID_DEPARTURE,
                ARRIVAL,
                12000,
                FuelUnit.KG,
                "RAW FLIGHT PLAN DESCRIPTION"
        );

        assertEquals("RAW FLIGHT PLAN DESCRIPTION", result.description());
    }

    // ── Route validation ─────────────────────────────────────────────────────

    @Test
    void ensureNonExistingRouteIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createDraftFlightPlan(
                        activePilotFromTP(),
                        "TP999",
                        "CS-TAB",
                        VALID_DEPARTURE,
                        ARRIVAL,
                        12000,
                        FuelUnit.KG,
                        "RAW FLIGHT PLAN DESCRIPTION"
                ));

        assertEquals(0, flightPlanRepository.count());
    }

    @Test
    void ensureInactiveRouteIsRejected() {
        final FlightRoute route = flightRouteRepository.findByRouteCode(ROUTE_CODE).orElseThrow();
        route.deactivate(LocalDate.now());

        assertThrows(IllegalArgumentException.class, () ->
                service.createDraftFlightPlan(
                        activePilotFromTP(),
                        "TP101",
                        "CS-TAB",
                        VALID_DEPARTURE,
                        ARRIVAL,
                        12000,
                        FuelUnit.KG,
                        "RAW FLIGHT PLAN DESCRIPTION"
                ));

        assertEquals(0, flightPlanRepository.count());
    }

    @Test
    void ensureDepartureNotAllowedByRouteScheduleIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createDraftFlightPlan(
                        activePilotFromTP(),
                        "TP101",
                        "CS-TAB",
                        INVALID_DEPARTURE,
                        ARRIVAL,
                        12000,
                        FuelUnit.KG,
                        "RAW FLIGHT PLAN DESCRIPTION"
                ));

        assertEquals(0, flightPlanRepository.count());
    }

    // ── Pilot validation ─────────────────────────────────────────────────────

    @Test
    void ensureInactivePilotIsRejected() {
        final Pilot inactivePilot = new FakePilot(PILOT_ID, TP, false);

        assertThrows(IllegalArgumentException.class, () ->
                service.createDraftFlightPlan(
                        inactivePilot,
                        "TP101",
                        "CS-TAB",
                        VALID_DEPARTURE,
                        ARRIVAL,
                        12000,
                        FuelUnit.KG,
                        "RAW FLIGHT PLAN DESCRIPTION"
                ));

        assertEquals(0, flightPlanRepository.count());
    }

    @Test
    void ensurePilotFromAnotherCompanyIsRejected() {
        final Pilot pilotFromAnotherCompany = new FakePilot(PILOT_ID, FR, true);

        assertThrows(IllegalArgumentException.class, () ->
                service.createDraftFlightPlan(
                        pilotFromAnotherCompany,
                        "TP101",
                        "CS-TAB",
                        VALID_DEPARTURE,
                        ARRIVAL,
                        12000,
                        FuelUnit.KG,
                        "RAW FLIGHT PLAN DESCRIPTION"
                ));

        assertEquals(0, flightPlanRepository.count());
    }

    // ── Aircraft validation ──────────────────────────────────────────────────

    @Test
    void ensureNonExistingAircraftIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createDraftFlightPlan(
                        activePilotFromTP(),
                        "TP101",
                        "CS-XXX",
                        VALID_DEPARTURE,
                        ARRIVAL,
                        12000,
                        FuelUnit.KG,
                        "RAW FLIGHT PLAN DESCRIPTION"
                ));

        assertEquals(0, flightPlanRepository.count());
    }

    @Test
    void ensureNonOperationalAircraftIsRejected() {
        aircraftRepository.add(
                AIRCRAFT_REGISTRATION,
                new FakeAircraft(AIRCRAFT_REGISTRATION, false, TP)
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.createDraftFlightPlan(
                        activePilotFromTP(),
                        "TP101",
                        "CS-TAB",
                        VALID_DEPARTURE,
                        ARRIVAL,
                        12000,
                        FuelUnit.KG,
                        "RAW FLIGHT PLAN DESCRIPTION"
                ));

        assertEquals(0, flightPlanRepository.count());
    }

    @Test
    void ensureAircraftFromAnotherCompanyIsRejected() {
        aircraftRepository.add(
                AIRCRAFT_REGISTRATION,
                new FakeAircraft(AIRCRAFT_REGISTRATION, true, FR)
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.createDraftFlightPlan(
                        activePilotFromTP(),
                        "TP101",
                        "CS-TAB",
                        VALID_DEPARTURE,
                        ARRIVAL,
                        12000,
                        FuelUnit.KG,
                        "RAW FLIGHT PLAN DESCRIPTION"
                ));

        assertEquals(0, flightPlanRepository.count());
    }

    // ── Value object validation ──────────────────────────────────────────────

    @Test
    void ensureNegativeFuelQuantityIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createDraftFlightPlan(
                        activePilotFromTP(),
                        "TP101",
                        "CS-TAB",
                        VALID_DEPARTURE,
                        ARRIVAL,
                        -1,
                        FuelUnit.KG,
                        "RAW FLIGHT PLAN DESCRIPTION"
                ));

        assertEquals(0, flightPlanRepository.count());
    }

    @Test
    void ensureBlankDescriptionIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createDraftFlightPlan(
                        activePilotFromTP(),
                        "TP101",
                        "CS-TAB",
                        VALID_DEPARTURE,
                        ARRIVAL,
                        12000,
                        FuelUnit.KG,
                        "   "
                ));

        assertEquals(0, flightPlanRepository.count());
    }

    @Test
    void ensureArrivalBeforeDepartureIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createDraftFlightPlan(
                        activePilotFromTP(),
                        "TP101",
                        "CS-TAB",
                        VALID_DEPARTURE,
                        VALID_DEPARTURE.minusHours(1),
                        12000,
                        FuelUnit.KG,
                        "RAW FLIGHT PLAN DESCRIPTION"
                ));

        assertEquals(0, flightPlanRepository.count());
    }

    // ── listAvailableRoutesFor ───────────────────────────────────────────────

    @Test
    void ensureListAvailableRoutesForPilotReturnsRoutesFromPilotCompany() {
        final List<FlightRouteDTO> result = new ArrayList<>();
        service.listAvailableRoutesFor(activePilotFromTP()).forEach(result::add);

        assertEquals(1, result.size());
        assertEquals("TP101", result.get(0).routeCode);
        assertEquals("ACTIVE", result.get(0).routeStatus);
        assertEquals("REGULAR", result.get(0).flightType);
    }

    @Test
    void ensureListAvailableRoutesRejectsInactivePilot() {
        final Pilot inactivePilot = new FakePilot(PILOT_ID, TP, false);

        assertThrows(IllegalArgumentException.class, () ->
                service.listAvailableRoutesFor(inactivePilot));
    }
}