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
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DeactivateFlightRouteController} (US074).
 * Uses the AAA convention (Arrange, Act, Assert).
 */
class DeactivateFlightRouteControllerTest {

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

    // ── Setup ────────────────────────────────────────────────────────────────

    private FlightRouteRepositoryStub routeRepo;
    private AirportRepositoryStub airportRepo;
    private FlightPlanRepositoryStub flightPlanRepo;
    private DeactivateFlightRouteController controller;
    private FlightRouteService service;

    private static final AirTransportCompanyCode TP =
            AirTransportCompanyCode.of("TP");

    private RegularSchedule regularSchedule() {
        return RegularSchedule.of(List.of(
                ScheduleEntry.of(DayOfWeek.MONDAY, LocalTime.of(9, 0))
        ));
    }

    @BeforeEach
    void setUp() {
        routeRepo = new FlightRouteRepositoryStub();
        airportRepo = new AirportRepositoryStub();
        flightPlanRepo = new FlightPlanRepositoryStub();
        service = new FlightRouteService(routeRepo, airportRepo, flightPlanRepo);

        controller = new DeactivateFlightRouteController(
                new AuthorizationServiceStub(),
                routeRepo,
                airportRepo,
                flightPlanRepo,
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

        service.createRegularFlightRoute(
                "TP101", "LIS", "OPO", TP, regularSchedule());
    }

    // ── deactivateRoute — success ─────────────────────────────────────────────

    @Test
    void ensureRouteIsDeactivatedSuccessfully() {
        final FlightRouteDTO dto = controller.deactivateRoute(
                "TP101", LocalDate.now(), "TP");

        assertNotNull(dto);
        assertEquals("TP101", dto.routeCode);
        assertEquals("INACTIVE", dto.routeStatus);
        assertEquals("REGULAR", dto.flightType);
    }

    @Test
    void ensureDeactivatedRouteIsPersistedInRepository() {
        controller.deactivateRoute("TP101", LocalDate.now(), "TP");

        final FlightRoute route = routeRepo.store.get("TP101");
        assertFalse(route.isActive());
    }

    @Test
    void ensureDeactivationDateIsStored() {
        final LocalDate date = LocalDate.now().plusDays(7);

        controller.deactivateRoute("TP101", date, "TP");

        final FlightRoute route = routeRepo.store.get("TP101");
        assertEquals(date, route.deactivationDate());
    }

    @Test
    void ensureFutureDateIsAccepted() {
        assertDoesNotThrow(() ->
                controller.deactivateRoute(
                        "TP101", LocalDate.now().plusDays(30), "TP"));
    }

    // ── deactivateRoute — failures ────────────────────────────────────────────

    @Test
    void ensureNonExistentRouteIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.deactivateRoute("TP999", LocalDate.now(), "TP"));
    }

    @Test
    void ensurePastDateIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.deactivateRoute(
                        "TP101", LocalDate.now().minusDays(1), "TP"));
    }

    @Test
    void ensureAlreadyInactiveRouteIsRejected() {
        controller.deactivateRoute("TP101", LocalDate.now(), "TP");

        assertThrows(IllegalArgumentException.class, () ->
                controller.deactivateRoute("TP101", LocalDate.now(), "TP"));
    }

    @Test
    void ensureRouteWithActiveFlightPlansAfterDateIsRejected() {
        // Arrange — simular planos ativos após a data
        flightPlanRepo.setHasActivePlans(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                controller.deactivateRoute("TP101", LocalDate.now(), "TP"));
    }

    @Test
    void ensureDeactivatingRouteFromAnotherCompanyIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.deactivateRoute("TP101", LocalDate.now(), "FR"));
    }

    // ── listActiveRoutes ──────────────────────────────────────────────────────

    @Test
    void ensureListActiveRoutesReturnsActiveRoutes() {
        final List<FlightRouteDTO> list = new ArrayList<>();
        service.listActiveRoutesByCompany(TP).forEach(list::add);

        assertEquals(1, list.size());
        assertEquals("TP101", list.get(0).routeCode);
        assertEquals("REGULAR", list.get(0).flightType);
    }

    @Test
    void ensureDeactivatedRouteDoesNotAppearInActiveList() {
        controller.deactivateRoute("TP101", LocalDate.now(), "TP");

        final List<FlightRouteDTO> list = new ArrayList<>();
        service.listActiveRoutesByCompany(TP).forEach(list::add);

        assertTrue(list.isEmpty());
    }

    @Test
    void ensureOnlyActiveRoutesAreReturned() {
        service.createRegularFlightRoute(
                "TP202", "OPO", "LIS", TP, regularSchedule());

        controller.deactivateRoute("TP101", LocalDate.now(), "TP");

        final List<FlightRouteDTO> list = new ArrayList<>();
        service.listActiveRoutesByCompany(TP).forEach(list::add);

        assertEquals(1, list.size());
        assertEquals("TP202", list.get(0).routeCode);
    }
}