package aisafe.flightroute.application;

import aisafe.aircontrolarea.domain.*;
import aisafe.airport.domain.*;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.domain.CustomerCollaboratorID;
import aisafe.costumerCollaborator.domain.CustomerReference;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.flightplan.domain.*;
import aisafe.flightroute.domain.*;
import aisafe.sharedkernel.Coordinates;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import aisafe.usermanagement.domain.legacyFiles.Email;
import aisafe.weather.domain.WeatherDataID;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CreateFlightRouteController} (US073).
 * Uses the AAA convention (Arrange, Act, Assert).
 */
class CreateFlightRouteControllerTest {

    // ── Stubs ────────────────────────────────────────────────────────────────

    static class AuthorizationServiceStub extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) { }
    }

    static class CustomerCollaboratorRepositoryStub
            implements CustomerCollaboratorRepository {

        @Override
        public Optional<CustomerCollaborator> findBySystemUser(
                final SystemUser systemUser) {
            return Optional.empty();
        }

        @Override
        public Optional<CustomerCollaborator> findByUsername(
                final Username username) {
            return Optional.empty();
        }

        @Override
        public Optional<CustomerCollaborator> findByEmail(final Email email) {
            return Optional.empty();
        }

        @Override
        public Iterable<CustomerCollaborator> findByCustomerReference(
                final CustomerReference customerReference) {
            return Collections.emptyList();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S extends CustomerCollaborator> S save(final S entity) {
            return entity;
        }

        @Override
        public Iterable<CustomerCollaborator> findAll() {
            return Collections.emptyList();
        }

        @Override
        public Optional<CustomerCollaborator> findById(
                final CustomerCollaboratorID id) {
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
        public long count() { return 0; }
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
        public long count() { return store.size(); }
    }

    static class FlightRouteRepositoryStub implements FlightRouteRepository {

        final Map<String, FlightRoute> store = new LinkedHashMap<>();

        @Override
        public Optional<FlightRoute> findByRouteCode(final RouteCode routeCode) {
            return Optional.ofNullable(store.get(routeCode.value()));
        }

        @Override
        public Iterable<FlightRoute> findByCompany(
                final AirTransportCompanyCode code) {
            return store.values().stream()
                    .filter(r -> r.airTransportCompanyIataCode().equals(code))
                    .toList();
        }

        @Override
        public Iterable<FlightRoute> findActiveByCompany(
                final AirTransportCompanyCode code) {
            return store.values().stream()
                    .filter(r -> r.airTransportCompanyIataCode().equals(code)
                            && r.isActive())
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
        public long count() { return store.size(); }
    }

    static class FlightPlanRepositoryStub implements FlightPlanRepository {

        @Override
        public boolean existsActiveByRouteCodeAfter(
                final RouteCode routeCode, final LocalDate date) {
            return false;
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

    // ── Setup ────────────────────────────────────────────────────────────────

    private FlightRouteRepositoryStub routeRepo;
    private AirportRepositoryStub airportRepo;
    private FlightPlanRepositoryStub planRepo;
    private CreateFlightRouteController controller;

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
        planRepo = new FlightPlanRepositoryStub();

        controller = new CreateFlightRouteController(
                new AuthorizationServiceStub(),
                routeRepo,
                airportRepo,
                planRepo,
                new CustomerCollaboratorRepositoryStub()
        );

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
        final FlightRouteDTO dto = controller.createRegularFlightRoute(
                "TP101", "LIS", "OPO", "TP", regularSchedule());

        assertNotNull(dto);
        assertEquals("TP101", dto.routeCode);
        assertEquals("ACTIVE", dto.routeStatus);
        assertEquals("REGULAR", dto.flightType);
    }

    @Test
    void ensureRegularResultIsReturnedAsDto() {
        final FlightRouteDTO dto = controller.createRegularFlightRoute(
                "TP101", "LIS", "OPO", "TP", regularSchedule());

        assertEquals("LIS", dto.departureAirportIata);
        assertEquals("OPO", dto.arrivalAirportIata);
        assertEquals("TP", dto.companyIata);
        assertEquals("REGULAR", dto.flightType);
        assertNotNull(dto.schedule);
    }

    @Test
    void ensureRegularRouteIsSavedInRepository() {
        controller.createRegularFlightRoute(
                "TP101", "LIS", "OPO", "TP", regularSchedule());

        assertTrue(routeRepo.store.containsKey("TP101"));
    }

    @Test
    void ensureTwoDifferentRegularRoutesCanBeCreated() {
        controller.createRegularFlightRoute(
                "TP101", "LIS", "OPO", "TP", regularSchedule());

        assertDoesNotThrow(() ->
                controller.createRegularFlightRoute(
                        "TP202", "OPO", "LIS", "TP", regularSchedule()));
    }

    // ── createCharterFlightRoute — success ────────────────────────────────────

    @Test
    void ensureCharterRouteIsCreatedSuccessfully() {
        final FlightRouteDTO dto = controller.createCharterFlightRoute(
                "TP101", "LIS", "OPO", "TP", charterSchedule());

        assertNotNull(dto);
        assertEquals("TP101", dto.routeCode);
        assertEquals("ACTIVE", dto.routeStatus);
        assertEquals("CHARTER", dto.flightType);
    }

    @Test
    void ensureCharterResultIsReturnedAsDto() {
        final FlightRouteDTO dto = controller.createCharterFlightRoute(
                "TP101", "LIS", "OPO", "TP", charterSchedule());

        assertEquals("LIS", dto.departureAirportIata);
        assertEquals("OPO", dto.arrivalAirportIata);
        assertEquals("TP", dto.companyIata);
        assertEquals("CHARTER", dto.flightType);
        assertNotNull(dto.schedule);
    }

    // ── business rule violations ──────────────────────────────────────────────

    @Test
    void ensureDuplicateRegularRouteCodeIsRejected() {
        controller.createRegularFlightRoute(
                "TP101", "LIS", "OPO", "TP", regularSchedule());

        assertThrows(IllegalStateException.class, () ->
                controller.createRegularFlightRoute(
                        "TP101", "LIS", "OPO", "TP", regularSchedule()));
    }

    @Test
    void ensureDuplicateBetweenRegularAndCharterRouteCodeIsRejected() {
        controller.createRegularFlightRoute(
                "TP101", "LIS", "OPO", "TP", regularSchedule());

        assertThrows(IllegalStateException.class, () ->
                controller.createCharterFlightRoute(
                        "TP101", "LIS", "OPO", "TP", charterSchedule()));
    }

    @Test
    void ensureSameAirportIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.createRegularFlightRoute(
                        "TP101", "LIS", "LIS", "TP", regularSchedule()));
    }

    @Test
    void ensureRoutePrefixMismatchIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.createRegularFlightRoute(
                        "FR101", "LIS", "OPO", "TP", regularSchedule()));
    }

    @Test
    void ensureNonExistentDepartureAirportIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.createRegularFlightRoute(
                        "TP101", "XXX", "OPO", "TP", regularSchedule()));
    }

    @Test
    void ensureNonExistentArrivalAirportIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.createRegularFlightRoute(
                        "TP101", "LIS", "XXX", "TP", regularSchedule()));
    }

    @Test
    void ensureNullRegularScheduleIsRejected() {
        assertThrows(Exception.class, () ->
                controller.createRegularFlightRoute(
                        "TP101", "LIS", "OPO", "TP", null));
    }

    @Test
    void ensureNullCharterScheduleIsRejected() {
        assertThrows(Exception.class, () ->
                controller.createCharterFlightRoute(
                        "TP101", "LIS", "OPO", "TP", null));
    }

    // ── listAirports ──────────────────────────────────────────────────────────

    @Test
    void ensureListAirportsReturnsDtos() {
        final List<AirportDTO> list = new ArrayList<>();
        controller.listAirports().forEach(list::add);

        assertEquals(2, list.size());
    }

    @Test
    void ensureListAirportsDtoContainsCorrectIataCodes() {
        final List<String> codes = new ArrayList<>();
        controller.listAirports().forEach(d -> codes.add(d.iataCode));

        assertTrue(codes.contains("LIS"));
        assertTrue(codes.contains("OPO"));
    }
}