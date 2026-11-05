package aisafe.flightroute.application;

import aisafe.aircontrolarea.domain.*;
import aisafe.airport.domain.*;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.flightplan.domain.*;
import aisafe.flightroute.domain.*;
import aisafe.sharedkernel.Coordinates;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import aisafe.weather.domain.WeatherDataID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FlightRouteService} (US073).
 * Uses the AAA convention (Arrange, Act, Assert).
 */
class FlightRouteServiceTest {

    // ── Stubs ─────────────────────────────────────────────────────────────────

    static class FlightRouteRepositoryStub implements FlightRouteRepository {

        final Map<String, FlightRoute> store = new LinkedHashMap<>();

        @Override
        public Optional<FlightRoute> findByRouteCode(final RouteCode routeCode) {
            return Optional.ofNullable(store.get(routeCode.value()));
        }

        @Override
        public Iterable<FlightRoute> findByCompany(final AirTransportCompanyCode code) {
            return store.values().stream()
                    .filter(r -> r.airTransportCompanyIataCode().equals(code))
                    .toList();
        }

        @Override
        public Iterable<FlightRoute> findActiveByCompany(final AirTransportCompanyCode code) {
            return store.values().stream()
                    .filter(r -> r.airTransportCompanyIataCode().equals(code) && r.isActive())
                    .toList();
        }

        @Override
        @SuppressWarnings("unchecked")
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

    static class AirportRepositoryStub implements AirportRepository {

        final Map<String, Airport> store = new LinkedHashMap<>();

        void add(final Airport airport) {
            store.put(airport.iataCode().toString(), airport);
        }

        @Override
        public Optional<Airport> findByIataCode(final AirportIataCode iataCode) {
            return Optional.ofNullable(store.get(iataCode.toString()));
        }

        @Override
        public Optional<Airport> findByIcaoCode(final AirportIcaoCode icaoCode) {
            return store.values().stream()
                    .filter(a -> a.icaoCode().equals(icaoCode))
                    .findFirst();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S extends Airport> S save(final S entity) {
            store.put(entity.iataCode().toString(), entity);
            return entity;
        }

        @Override
        public Iterable<Airport> findAll() {
            return Collections.unmodifiableCollection(store.values());
        }

        @Override
        public Optional<Airport> findById(final AirportIataCode id) {
            return findByIataCode(id);
        }

        @Override
        public void delete(final Airport entity) {
            store.remove(entity.iataCode().toString());
        }

        @Override
        public void deleteById(final AirportIataCode id) {
            store.remove(id.toString());
        }

        @Override
        public long count() {
            return store.size();
        }
    }

    static class FlightPlanRepositoryStub implements FlightPlanRepository {

        private boolean hasActivePlans = false;

        void setHasActivePlans(final boolean hasActivePlans) {
            this.hasActivePlans = hasActivePlans;
        }

        @Override
        public boolean existsActiveByRouteCodeAfter(
                final RouteCode routeCode, final LocalDate date) {
            return hasActivePlans;
        }

        @Override
        public Iterable<FlightPlan> findByRouteCode(final RouteCode routeCode) {
            return Collections.emptyList();
        }

        @Override
        public Iterable<FlightPlan> findByAircraftRegistration(
                final aisafe.aircraft.domain.AircraftRegistration registration) {
            return Collections.emptyList();
        }

        @Override
        public Iterable<FlightPlan> findByPilotID(
                final aisafe.pilot.domain.PilotID pilotID) {
            return Collections.emptyList();
        }

        @Override
        public Iterable<FlightPlan> findByStatus(final FlightPlanStatus status) {
            return Collections.emptyList();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S extends FlightPlan> S save(final S entity) { return entity; }

        @Override
        public Iterable<FlightPlan> findAll() { return Collections.emptyList(); }

        @Override
        public Optional<FlightPlan> findById(final FlightPlanID id) {
            return Optional.empty();
        }

        @Override
        public void delete(final FlightPlan entity) { }

        @Override
        public void deleteById(final FlightPlanID id) { }

        @Override
        public long count() { return 0; }
    }

    // ── Setup ─────────────────────────────────────────────────────────────────

    private FlightRouteRepositoryStub routeRepo;
    private AirportRepositoryStub airportRepo;
    private FlightPlanRepositoryStub flightPlanRepo;
    private FlightRouteService service;

    private static final AirTransportCompanyCode TP = AirTransportCompanyCode.of("TP");

    private RegularSchedule regularSchedule() {
        return RegularSchedule.of(List.of(
                ScheduleEntry.of(DayOfWeek.MONDAY, LocalTime.of(9, 0))
        ));
    }

    private CharterSchedule charterSchedule() {
        return CharterSchedule.of(
                LocalDateTime.of(2026, 6, 15, 9, 0),
                LocalDateTime.of(2026, 6, 15, 11, 0)
        );
    }

    @BeforeEach
    void setUp() {
        routeRepo = new FlightRouteRepositoryStub();
        airportRepo = new AirportRepositoryStub();
        flightPlanRepo = new FlightPlanRepositoryStub();
        service = new FlightRouteService(routeRepo, airportRepo, flightPlanRepo);

        final AirControlArea area = new AirControlArea(
                AirControlAreaCode.of("LPPC"),
                GeographicBoundary.of(
                        Coordinates.of(36.0, -10.0),
                        Coordinates.of(44.0, -6.0)
                ),
                AirControlAreaName.of("Lisbon FIR")
        );

        final Country portugal = Country.of(IsoCode.of("PT"), "Portugal");

        airportRepo.add(new Airport(
                AirportIataCode.of("LIS"),
                AirportIcaoCode.of("LPPT"),
                Locality.of("Lisbon", portugal),
                Coordinates.of(38.7813, -9.1359),
                AirportName.of("Humberto Delgado Airport"),
                Elevation.of(113.0),
                area
        ));

        airportRepo.add(new Airport(
                AirportIataCode.of("OPO"),
                AirportIcaoCode.of("LPPR"),
                Locality.of("Porto", portugal),
                Coordinates.of(41.2481, -8.6814),
                AirportName.of("Francisco Sá Carneiro Airport"),
                Elevation.of(69.0),
                area
        ));
    }

    // ── createRegularFlightRoute — success ────────────────────────────────────

    @Test
    void ensureRegularRouteIsCreatedSuccessfully() {
        final FlightRoute route = service.createRegularFlightRoute(
                "TP101", "LIS", "OPO", TP, regularSchedule());

        assertNotNull(route);
        assertEquals("TP101", route.routeCode().value());
        assertEquals(RouteStatus.ACTIVE, route.routeStatus());
        assertEquals(FlightType.REGULAR, route.flightType());
    }

    @Test
    void ensureRegularRouteIsSavedInRepository() {
        service.createRegularFlightRoute(
                "TP101", "LIS", "OPO", TP, regularSchedule());

        assertTrue(routeRepo.store.containsKey("TP101"));
    }

    @Test
    void ensureRouteCodeIsNormalized() {
        final FlightRoute route = service.createRegularFlightRoute(
                "tp101", "LIS", "OPO", TP, regularSchedule());

        assertEquals("TP101", route.routeCode().value());
    }

    @Test
    void ensureTwoDifferentRegularRoutesCanBeCreated() {
        service.createRegularFlightRoute(
                "TP101", "LIS", "OPO", TP, regularSchedule());

        assertDoesNotThrow(() ->
                service.createRegularFlightRoute(
                        "TP202", "OPO", "LIS", TP, regularSchedule()));
    }

    // ── createCharterFlightRoute — success ────────────────────────────────────

    @Test
    void ensureCharterRouteIsCreatedSuccessfully() {
        final FlightRoute route = service.createCharterFlightRoute(
                "TP101", "LIS", "OPO", TP, charterSchedule());

        assertNotNull(route);
        assertEquals("TP101", route.routeCode().value());
        assertEquals(RouteStatus.ACTIVE, route.routeStatus());
        assertEquals(FlightType.CHARTER, route.flightType());
    }

    @Test
    void ensureCharterRouteIsSavedInRepository() {
        service.createCharterFlightRoute(
                "TP101", "LIS", "OPO", TP, charterSchedule());

        assertTrue(routeRepo.store.containsKey("TP101"));
    }

    // ── duplicate route code ──────────────────────────────────────────────────

    @Test
    void ensureDuplicateRegularRouteCodeIsRejected() {
        service.createRegularFlightRoute(
                "TP101", "LIS", "OPO", TP, regularSchedule());

        assertThrows(IllegalStateException.class, () ->
                service.createRegularFlightRoute(
                        "TP101", "LIS", "OPO", TP, regularSchedule()));
    }

    @Test
    void ensureDuplicateRouteCodeBetweenRegularAndCharterIsRejected() {
        service.createRegularFlightRoute(
                "TP101", "LIS", "OPO", TP, regularSchedule());

        assertThrows(IllegalStateException.class, () ->
                service.createCharterFlightRoute(
                        "TP101", "LIS", "OPO", TP, charterSchedule()));
    }

    // ── business rule failures ────────────────────────────────────────────────

    @Test
    void ensureSameDepartureAndArrivalAirportIsRejectedForRegularRoute() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createRegularFlightRoute(
                        "TP101", "LIS", "LIS", TP, regularSchedule()));
    }

    @Test
    void ensureSameDepartureAndArrivalAirportIsRejectedForCharterRoute() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createCharterFlightRoute(
                        "TP101", "LIS", "LIS", TP, charterSchedule()));
    }

    @Test
    void ensureRoutePrefixMismatchWithCompanyIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createRegularFlightRoute(
                        "FR101", "LIS", "OPO", TP, regularSchedule()));
    }

    @Test
    void ensureNonExistentDepartureAirportIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createRegularFlightRoute(
                        "TP101", "XXX", "OPO", TP, regularSchedule()));
    }

    @Test
    void ensureNonExistentArrivalAirportIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                service.createRegularFlightRoute(
                        "TP101", "LIS", "XXX", TP, regularSchedule()));
    }

    @Test
    void ensureNullRegularScheduleIsRejected() {
        assertThrows(Exception.class, () ->
                service.createRegularFlightRoute(
                        "TP101", "LIS", "OPO", TP, null));
    }

    @Test
    void ensureNullCharterScheduleIsRejected() {
        assertThrows(Exception.class, () ->
                service.createCharterFlightRoute(
                        "TP101", "LIS", "OPO", TP, null));
    }

    // ── constructor guard ─────────────────────────────────────────────────────

    @Test
    void ensureNullFlightRouteRepositoryIsRejected() {
        assertThrows(Exception.class, () ->
                new FlightRouteService(null, airportRepo, flightPlanRepo));
    }

    @Test
    void ensureNullAirportRepositoryIsRejected() {
        assertThrows(Exception.class, () ->
                new FlightRouteService(routeRepo, null, flightPlanRepo));
    }

    @Test
    void ensureNullFlightPlanRepositoryIsRejected() {
        assertThrows(Exception.class, () ->
                new FlightRouteService(routeRepo, airportRepo, null));
    }

    // ── deactivateRoute ───────────────────────────────────────────────────────

    @Test
    void ensureRouteCanBeDeactivatedWithNoActivePlans() {
        service.createRegularFlightRoute(
                "TP101", "LIS", "OPO", TP, regularSchedule());

        assertDoesNotThrow(() ->
                service.deactivateRoute("TP101", LocalDate.now(), TP));
    }

    @Test
    void ensureRouteCannotBeDeactivatedWithActivePlansAfterDate() {
        service.createRegularFlightRoute(
                "TP101", "LIS", "OPO", TP, regularSchedule());

        flightPlanRepo.setHasActivePlans(true);

        assertThrows(IllegalArgumentException.class, () ->
                service.deactivateRoute("TP101", LocalDate.now(), TP));
    }

    // ── listAirports ──────────────────────────────────────────────────────────

    @Test
    void ensureListAirportsReturnsBothAirports() {
        final List<AirportDTO> list = new ArrayList<>();
        service.listAirports().forEach(list::add);

        assertEquals(2, list.size());
    }

    @Test
    void ensureListAirportsDtoContainsCorrectIataCodes() {
        final List<String> iataCodes = new ArrayList<>();
        service.listAirports().forEach(d -> iataCodes.add(d.iataCode));

        assertTrue(iataCodes.contains("LIS"));
        assertTrue(iataCodes.contains("OPO"));
    }

    @Test
    void ensureListAirportsDtoContainsCorrectCityName() {
        final List<AirportDTO> list = new ArrayList<>();
        service.listAirports().forEach(list::add);

        assertTrue(list.stream().anyMatch(d -> d.city.equals("Lisbon")));
        assertTrue(list.stream().anyMatch(d -> d.city.equals("Porto")));
    }

    // ── listActiveRoutesByCompany ─────────────────────────────────────────────

    @Test
    void ensureListActiveRoutesByCompanyReturnsDtosWithFlightTypeAndSchedule() {
        service.createRegularFlightRoute(
                "TP101", "LIS", "OPO", TP, regularSchedule());

        final List<FlightRouteDTO> list = new ArrayList<>();
        service.listActiveRoutesByCompany(TP).forEach(list::add);

        assertEquals(1, list.size());
        assertEquals("TP101", list.get(0).routeCode);
        assertEquals("REGULAR", list.get(0).flightType);
        assertNotNull(list.get(0).schedule);
    }
}