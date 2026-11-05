package aisafe.flightplan.domain;

import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.airport.domain.Airport;
import aisafe.airport.domain.AirportIataCode;
import aisafe.airport.domain.AirportIcaoCode;
import aisafe.airport.domain.AirportRepository;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.flightplan.application.InsertWeatherDataInFlightController;
import aisafe.flightroute.domain.*;
import aisafe.pilot.domain.Pilot;
import aisafe.pilot.domain.PilotID;
import aisafe.pilot.domain.PilotRepository;
import aisafe.sharedkernel.Coordinates;
import aisafe.weather.domain.BoundingBox;
import aisafe.weather.domain.WeatherData;
import aisafe.weather.domain.WeatherDataID;
import aisafe.weather.domain.WeatherDataRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InsertWeatherDataInFlightControllerTest {

    // --- Stubs ---

    static class AuthorizationServiceStub extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) {
            // Authorized by default for controller unit tests.
        }
    }

    private static class FlightPlanRepositoryStub implements FlightPlanRepository {

        private final Map<FlightPlanID, FlightPlan> flightPlans = new HashMap<>();

        void add(final FlightPlan flightPlan) {
            flightPlans.put(flightPlan.identity(), flightPlan);
        }

        @Override
        public Optional<FlightPlan> findById(final FlightPlanID id) {
            return Optional.ofNullable(flightPlans.get(id));
        }

        public Optional<FlightPlan> ofIdentity(final FlightPlanID id) {
            return Optional.ofNullable(flightPlans.get(id));
        }

        @Override
        public <S extends FlightPlan> S save(final S entity) {
            flightPlans.put(entity.identity(), entity);
            return entity;
        }

        @Override
        public Iterable<FlightPlan> findAll() {
            return flightPlans.values();
        }

        @Override
        public Iterable<FlightPlan> findByPilotID(final PilotID pilotID) {
            return flightPlans.values().stream()
                    .filter(fp -> fp.pilotID().equals(pilotID))
                    .toList();
        }

        @Override
        public Iterable<FlightPlan> findByRouteCode(final RouteCode routeCode) {
            return flightPlans.values().stream()
                    .filter(fp -> fp.routeCode().equals(routeCode))
                    .toList();
        }

        @Override
        public Iterable<FlightPlan> findByAircraftRegistration(final AircraftRegistration registration) {
            return flightPlans.values().stream()
                    .filter(fp -> fp.aircraftRegistration().equals(registration))
                    .toList();
        }

        @Override
        public Iterable<FlightPlan> findByStatus(final FlightPlanStatus status) {
            return flightPlans.values().stream()
                    .filter(fp -> fp.status() == status)
                    .toList();
        }

        @Override
        public boolean existsActiveByRouteCodeAfter(final RouteCode routeCode,
                                                    final LocalDate date) {
            return flightPlans.values().stream()
                    .anyMatch(fp -> fp.routeCode().equals(routeCode)
                            && fp.plannedDepartureDateTime().toLocalDate().isAfter(date)
                            && fp.status() != FlightPlanStatus.CANCELLED
                            && fp.status() != FlightPlanStatus.REJECTED);
        }

        @Override
        public void delete(final FlightPlan entity) {
            flightPlans.remove(entity.identity());
        }

        @Override
        public void deleteById(final FlightPlanID id) {
            flightPlans.remove(id);
        }

        @Override
        public long count() {
            return flightPlans.size();
        }
    }

    private static class WeatherDataRepositoryStub implements WeatherDataRepository {

        private final Map<WeatherDataID, WeatherData> weatherDataMap = new HashMap<>();

        void add(final WeatherData weatherData) {
            weatherDataMap.put(weatherData.identity(), weatherData);
        }

        @Override
        public Optional<WeatherData> findByID(final WeatherDataID id) {
            return Optional.ofNullable(weatherDataMap.get(id));
        }

        @Override
        public Iterable<WeatherData> findByAreaAndDate(final AirControlAreaCode code,
                                                       final LocalDate date) {
            return weatherDataMap.values().stream()
                    .filter(w -> w.airControlAreaCode().equals(code)
                            && w.day().equals(date))
                    .toList();
        }

        @Override
        public <S extends WeatherData> S save(final S entity) {
            weatherDataMap.put(entity.identity(), entity);
            return entity;
        }

        @Override
        public Iterable<WeatherData> findAll() {
            return weatherDataMap.values();
        }

        @Override
        public Optional<WeatherData> ofIdentity(final WeatherDataID id) {
            return Optional.ofNullable(weatherDataMap.get(id));
        }

        @Override
        public void delete(final WeatherData entity) {
            weatherDataMap.remove(entity.identity());
        }

        @Override
        public void deleteOfIdentity(final WeatherDataID id) {
            weatherDataMap.remove(id);
        }

        @Override
        public long count() {
            return weatherDataMap.size();
        }
    }

    private static class PilotRepositoryStub implements PilotRepository {

        private final Map<PilotID, Pilot> pilots = new HashMap<>();

        void add(final Pilot pilot) {
            pilots.put(pilot.identity(), pilot);
        }

        @Override
        public Optional<Pilot> findBySystemUser(final SystemUser systemUser) {
            return pilots.values().stream()
                    .filter(p -> p.systemUser() != null && p.systemUser().equals(systemUser))
                    .findFirst();
        }

        @Override
        public Iterable<Pilot> findByCompany(final AirTransportCompanyCode code) {
            return List.of();
        }

        @Override
        public Iterable<Pilot> findActiveByCompany(final AirTransportCompanyCode code) {
            return List.of();
        }

        @Override
        public Optional<Pilot> findByPilotID(final PilotID pilotID) {
            return Optional.ofNullable(pilots.get(pilotID));
        }

        @Override
        public <S extends Pilot> S save(final S entity) {
            pilots.put(entity.identity(), entity);
            return entity;
        }

        @Override
        public Iterable<Pilot> findAll() {
            return pilots.values();
        }

        @Override
        public Optional<Pilot> findById(final PilotID id) {
            return Optional.ofNullable(pilots.get(id));
        }

        public Optional<Pilot> ofIdentity(final PilotID id) {
            return Optional.ofNullable(pilots.get(id));
        }

        @Override
        public void delete(final Pilot entity) {
            pilots.remove(entity.identity());
        }

        @Override
        public void deleteById(final PilotID entityId) {
            pilots.remove(entityId);
        }

        public void deleteOfIdentity(final PilotID id) {
            pilots.remove(id);
        }

        @Override
        public long count() {
            return pilots.size();
        }
    }

    private static class FlightRouteRepositoryStub implements FlightRouteRepository {

        private final Map<RouteCode, FlightRoute> routes = new HashMap<>();

        void add(final FlightRoute route) {
            routes.put(route.identity(), route);
        }

        @Override
        public Optional<FlightRoute> findByRouteCode(final RouteCode routeCode) {
            return Optional.ofNullable(routes.get(routeCode));
        }

        @Override
        public Iterable<FlightRoute> findByCompany(final AirTransportCompanyCode companyIataCode) {
            return List.of();
        }

        @Override
        public Iterable<FlightRoute> findActiveByCompany(final AirTransportCompanyCode companyIataCode) {
            return List.of();
        }

        @Override
        public <S extends FlightRoute> S save(final S entity) {
            routes.put(entity.identity(), entity);
            return entity;
        }

        @Override
        public Iterable<FlightRoute> findAll() {
            return routes.values();
        }

        @Override
        public Optional<FlightRoute> findById(final RouteCode id) {
            return Optional.ofNullable(routes.get(id));
        }

        public Optional<FlightRoute> ofIdentity(final RouteCode id) {
            return Optional.ofNullable(routes.get(id));
        }

        @Override
        public void delete(final FlightRoute entity) {
            routes.remove(entity.identity());
        }

        @Override
        public void deleteById(final RouteCode id) {
            routes.remove(id);
        }

        @Override
        public long count() {
            return routes.size();
        }
    }

    private static class AirportRepositoryStub implements AirportRepository {

        private final Map<AirportIataCode, Airport> airports = new HashMap<>();

        void add(final Airport airport) {
            airports.put(airport.identity(), airport);
        }

        @Override
        public Optional<Airport> findByIataCode(final AirportIataCode iataCode) {
            return Optional.ofNullable(airports.get(iataCode));
        }

        @Override
        public Optional<Airport> findByIcaoCode(final AirportIcaoCode icaoCode) {
            return airports.values().stream()
                    .filter(airport -> airport.icaoCode().equals(icaoCode))
                    .findFirst();
        }

        @Override
        public <S extends Airport> S save(final S entity) {
            airports.put(entity.identity(), entity);
            return entity;
        }

        @Override
        public Iterable<Airport> findAll() {
            return airports.values();
        }

        @Override
        public Optional<Airport> findById(final AirportIataCode id) {
            return Optional.ofNullable(airports.get(id));
        }

        public Optional<Airport> ofIdentity(final AirportIataCode id) {
            return Optional.ofNullable(airports.get(id));
        }

        @Override
        public void delete(final Airport entity) {
            airports.remove(entity.identity());
        }

        @Override
        public void deleteById(final AirportIataCode id) {
            airports.remove(id);
        }

        @Override
        public long count() {
            return airports.size();
        }
    }

    // --- Fake domain objects ---

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

    // --- Setup ---

    private FlightPlanRepositoryStub flightPlanRepo;
    private WeatherDataRepositoryStub weatherDataRepo;
    private PilotRepositoryStub pilotRepo;
    private FlightRouteRepositoryStub flightRouteRepo;
    private AirportRepositoryStub airportRepo;
    private InsertWeatherDataInFlightController controller;

    private static final AirTransportCompanyCode COMPANY_CODE = AirTransportCompanyCode.of("TP");
    private static final RouteCode ROUTE_CODE = RouteCode.of("TP123");
    private static final AircraftRegistration AIRCRAFT_REGISTRATION = AircraftRegistration.of("CS-ABC");

    private static final AirControlAreaCode ACA_CODE = AirControlAreaCode.of("LPPC");
    private static final AirControlAreaCode OTHER_CODE = AirControlAreaCode.of("SBGL");
    private static final LocalDate DAY = LocalDate.of(2026, 6, 22);
    private static final LocalTime START_TIME = LocalTime.of(5, 0);
    private static final LocalTime END_TIME = LocalTime.of(8, 15);

    private static final BoundingBox BOUNDING_BOX = new BoundingBox(
            Coordinates.of(37.05, -9.79),
            Coordinates.of(43.84, 4.93)
    );

    private RegularSchedule regularSchedule() {
        return RegularSchedule.of(List.of(
                ScheduleEntry.of(DayOfWeek.MONDAY, LocalTime.of(10, 0))
        ));
    }

    private FlightRoute validRoute() {
        return new FlightRouteFactory()
                .withRouteCode(ROUTE_CODE)
                .withDepartureAirport(AirportIataCode.of("LIS"))
                .withArrivalAirport(AirportIataCode.of("OPO"))
                .withCompany(COMPANY_CODE)
                .asRegular(regularSchedule())
                .build();
    }

    private Aircraft validAircraft() {
        return new FakeAircraft(AIRCRAFT_REGISTRATION, true, COMPANY_CODE);
    }

    private Pilot validPilot(final PilotID pilotID) {
        return new FakePilot(pilotID, COMPANY_CODE, true);
    }

    private FlightPlan validFlightPlanForPilot(final PilotID pilotID) {
        return FlightPlan.draft(
                FlightPlanID.newID(),
                validRoute(),
                validAircraft(),
                validPilot(pilotID),
                LocalDateTime.of(2026, 6, 22, 10, 0),
                LocalDateTime.of(2026, 6, 22, 12, 0),
                FuelQuantity.of(5000.0, FuelUnit.KG),
                FlightPlanDescription.of("Test flight")
        );
    }

    private WeatherData validWeatherData() {
        return new WeatherData(
                ACA_CODE,
                DAY,
                START_TIME,
                END_TIME,
                BOUNDING_BOX,
                0,
                1000,
                28.75,
                90.0
        );
    }

    @BeforeEach
    void setUp() {
        flightPlanRepo = new FlightPlanRepositoryStub();
        weatherDataRepo = new WeatherDataRepositoryStub();
        pilotRepo = new PilotRepositoryStub();
        flightRouteRepo = new FlightRouteRepositoryStub();
        airportRepo = new AirportRepositoryStub();

        controller = new InsertWeatherDataInFlightController(
                new AuthorizationServiceStub(),
                flightPlanRepo,
                weatherDataRepo,
                pilotRepo,
                flightRouteRepo,
                airportRepo
        );
    }

    // --- insertWeatherData ---

    @Test
    void insertWeatherDataSuccessfully() {
        final PilotID pilotID = PilotID.newID();
        final FlightPlan flightPlan = validFlightPlanForPilot(pilotID);
        final WeatherData weatherData = validWeatherData();

        flightPlanRepo.add(flightPlan);
        weatherDataRepo.add(weatherData);

        assertDoesNotThrow(() ->
                controller.insertWeatherData(flightPlan.identity(), weatherData.identity()));

        final FlightPlan updated = flightPlanRepo.ofIdentity(flightPlan.identity()).orElseThrow();

        assertEquals(weatherData.identity(), updated.weatherDataID());
        assertEquals(weatherData, updated.weatherData());
    }

    @Test
    void insertWeatherDataWithInvalidFlightPlanThrows() {
        final WeatherData weatherData = validWeatherData();

        weatherDataRepo.add(weatherData);

        assertThrows(IllegalArgumentException.class, () ->
                controller.insertWeatherData(FlightPlanID.newID(), weatherData.identity()));
    }

    @Test
    void insertWeatherDataWithInvalidWeatherDataThrows() {
        final PilotID pilotID = PilotID.newID();
        final FlightPlan flightPlan = validFlightPlanForPilot(pilotID);

        flightPlanRepo.add(flightPlan);

        assertThrows(IllegalArgumentException.class, () ->
                controller.insertWeatherData(flightPlan.identity(), WeatherDataID.newID()));
    }

    @Test
    void insertWeatherDataVoidsTestIfFlightPlanIsTested() {
        final PilotID pilotID = PilotID.newID();
        final FlightPlan flightPlan = validFlightPlanForPilot(pilotID);
        final WeatherData weatherData = validWeatherData();

        flightPlan.markTested();

        flightPlanRepo.add(flightPlan);
        weatherDataRepo.add(weatherData);

        controller.insertWeatherData(flightPlan.identity(), weatherData.identity());

        final FlightPlan updated = flightPlanRepo.ofIdentity(flightPlan.identity()).orElseThrow();

        assertEquals(FlightPlanStatus.DRAFT, updated.status());
        assertEquals(weatherData.identity(), updated.weatherDataID());
    }

    @Test
    void insertWeatherDataInCancelledFlightPlanThrows() {
        final PilotID pilotID = PilotID.newID();
        final FlightPlan flightPlan = validFlightPlanForPilot(pilotID);
        final WeatherData weatherData = validWeatherData();

        flightPlan.cancel();

        flightPlanRepo.add(flightPlan);
        weatherDataRepo.add(weatherData);

        assertThrows(IllegalArgumentException.class, () ->
                controller.insertWeatherData(flightPlan.identity(), weatherData.identity()));
    }

    @Test
    void insertWeatherDataInRejectedFlightPlanThrows() {
        final PilotID pilotID = PilotID.newID();
        final FlightPlan flightPlan = validFlightPlanForPilot(pilotID);
        final WeatherData weatherData = validWeatherData();

        flightPlan.reject();

        flightPlanRepo.add(flightPlan);
        weatherDataRepo.add(weatherData);

        assertThrows(IllegalArgumentException.class, () ->
                controller.insertWeatherData(flightPlan.identity(), weatherData.identity()));
    }

    // --- getMyFlightPlans ---

    @Test
    void getMyFlightPlansReturnsOnlyPilotFlightPlans() {
        final PilotID pilotID = PilotID.newID();
        final PilotID otherPilotID = PilotID.newID();

        flightPlanRepo.add(validFlightPlanForPilot(pilotID));
        flightPlanRepo.add(validFlightPlanForPilot(pilotID));
        flightPlanRepo.add(validFlightPlanForPilot(otherPilotID));

        final List<FlightPlan> result = new ArrayList<>();

        controller.getMyFlightPlans(pilotID).forEach(result::add);

        assertEquals(2, result.size());
    }

    @Test
    void getMyFlightPlansReturnsEmptyWhenNoneExist() {
        final List<FlightPlan> result = new ArrayList<>();

        controller.getMyFlightPlans(PilotID.newID()).forEach(result::add);

        assertTrue(result.isEmpty());
    }

    // --- getWeatherDataByAreaAndDate ---

    @Test
    void getWeatherDataByAreaAndDateReturnsMatching() {
        final WeatherData weatherData = validWeatherData();

        weatherDataRepo.add(weatherData);

        final List<WeatherData> result = new ArrayList<>();

        controller.getWeatherDataByAreaAndDate(ACA_CODE.toString(), DAY).forEach(result::add);

        assertEquals(1, result.size());
    }

    @Test
    void getWeatherDataByAreaAndDateReturnsEmptyWhenNoMatch() {
        final List<WeatherData> result = new ArrayList<>();

        controller.getWeatherDataByAreaAndDate(ACA_CODE.toString(), DAY).forEach(result::add);

        assertTrue(result.isEmpty());
    }

    @Test
    void getWeatherDataByAreaAndDateDoesNotReturnOtherAreas() {
        weatherDataRepo.add(validWeatherData());

        final List<WeatherData> result = new ArrayList<>();

        controller.getWeatherDataByAreaAndDate(OTHER_CODE.toString(), DAY).forEach(result::add);

        assertTrue(result.isEmpty());
    }
}