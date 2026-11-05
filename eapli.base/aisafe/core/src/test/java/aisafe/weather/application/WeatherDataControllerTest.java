package aisafe.weather.application;

import aisafe.aircontrolarea.domain.AirControlArea;
import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.aircontrolarea.domain.AirControlAreaName;
import aisafe.aircontrolarea.domain.AirControlAreaRepository;
import aisafe.aircontrolarea.domain.GeographicBoundary;
import aisafe.sharedkernel.Coordinates;
import aisafe.weather.domain.BoundingBox;
import aisafe.weather.domain.WeatherData;
import aisafe.weather.domain.WeatherDataID;
import aisafe.weather.domain.WeatherDataRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class WeatherDataControllerTest {

    // --- Stubs ---

    static class AuthorizationServiceStub extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) { }
    }

    private static class WeatherDataRepositoryStub implements WeatherDataRepository {

        private final Map<WeatherDataID, WeatherData> weatherData = new HashMap<>();

        @Override
        public Iterable<WeatherData> findByAreaAndDate(final AirControlAreaCode airControlAreaCode,
                                                       final LocalDate date) {
            return weatherData.values().stream()
                    .filter(w -> w.airControlAreaCode().equals(airControlAreaCode)
                            && w.day().equals(date))
                    .toList();
        }

        @Override
        public Optional<WeatherData> findByID(final WeatherDataID id) {
            return Optional.ofNullable(weatherData.get(id));
        }

        @Override
        public <S extends WeatherData> S save(final S entity) {
            weatherData.put(entity.identity(), entity);
            return entity;
        }

        @Override
        public Iterable<WeatherData> findAll() { return weatherData.values(); }

        @Override
        public Optional<WeatherData> ofIdentity(final WeatherDataID id) {
            return Optional.ofNullable(weatherData.get(id));
        }

        @Override
        public void delete(final WeatherData entity) { weatherData.remove(entity.identity()); }

        @Override
        public void deleteOfIdentity(final WeatherDataID id) { weatherData.remove(id); }

        @Override
        public long count() { return weatherData.size(); }
    }

    private static class AirControlAreaRepositoryStub implements AirControlAreaRepository {

        private final Map<AirControlAreaCode, AirControlArea> areas = new HashMap<>();

        void add(final AirControlArea area) {
            areas.put(area.identity(), area);
        }

        @Override
        public Iterable<AirControlArea> findAreasContaining(final Coordinates coordinates) {
            return areas.values().stream()
                    .filter(a -> a.contains(coordinates))
                    .toList();
        }

        @Override
        public Optional<AirControlArea> findByCode(final AirControlAreaCode code) {
            return Optional.ofNullable(areas.get(code));
        }

        @Override
        public Optional<AirControlArea> findByName(final AirControlAreaName name) {
            return areas.values().stream()
                    .filter(a -> a.name().equals(name))
                    .findFirst();
        }

        @Override
        public <S extends AirControlArea> S save(final S entity) {
            areas.put(entity.identity(), entity);
            return entity;
        }

        @Override
        public Iterable<AirControlArea> findAll() { return areas.values(); }

        @Override
        public Optional<AirControlArea> findById(final AirControlAreaCode id) {
            return Optional.ofNullable(areas.get(id));
        }

        @Override
        public void delete(final AirControlArea entity) { areas.remove(entity.identity()); }

        @Override
        public void deleteById(final AirControlAreaCode id) { areas.remove(id); }

        @Override
        public long count() { return areas.size(); }
    }

    // --- Setup ---

    private WeatherDataRepositoryStub weatherRepo;
    private AirControlAreaRepositoryStub acaRepo;
    private RegisterWeatherDataController controller;

    private static final AirControlAreaCode ACA_CODE = AirControlAreaCode.of("LPPC");
    private static final AirControlAreaCode INVALID_CODE = AirControlAreaCode.of("XXXX");
    private static final LocalDate DAY = LocalDate.of(2026, 5, 17);
    private static final LocalDate OTHER_DAY = LocalDate.of(2026, 5, 18);
    private static final LocalTime START_TIME = LocalTime.of(5, 0);
    private static final LocalTime END_TIME = LocalTime.of(8, 15);
    private static final BoundingBox BOUNDING_BOX = new BoundingBox(
            Coordinates.of(43.84, -9.79),
            Coordinates.of(37.05, 4.93)
    );
    private static final int MIN_ALTITUDE = 0;
    private static final int MAX_ALTITUDE = 1000;
    private static final double WIND_SPEED = 28.75;
    private static final double WIND_DIRECTION = 90.0;

    @BeforeEach
    void setUp() {
        weatherRepo = new WeatherDataRepositoryStub();
        acaRepo = new AirControlAreaRepositoryStub();

        acaRepo.add(new AirControlArea(
                ACA_CODE,
                GeographicBoundary.of(
                        Coordinates.of(36.0, -10.0),
                        Coordinates.of(42.0, -6.0)
                ),
                AirControlAreaName.of("Lisboa")
        ));

        controller = new RegisterWeatherDataController(
                new AuthorizationServiceStub(),
                acaRepo,
                weatherRepo
        );
    }

    // --- registerWeatherData ---

    @Test
    void registerWeatherDataSuccessfully() {
        controller.registerWeatherData(
                ACA_CODE.toString(), DAY, START_TIME, END_TIME,
                43.84, -9.79, 37.05, 4.93,
                MIN_ALTITUDE, MAX_ALTITUDE,
                WIND_SPEED, WIND_DIRECTION
        );

        assertEquals(1, weatherRepo.count());
    }

    @Test
    void registerWeatherDataWithInvalidAreaCodeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.registerWeatherData(
                        INVALID_CODE.toString(), DAY, START_TIME, END_TIME,
                        43.84, -9.79, 37.05, 4.93,
                        MIN_ALTITUDE, MAX_ALTITUDE,
                        WIND_SPEED, WIND_DIRECTION
                ));

        assertEquals(0, weatherRepo.count());
    }

    @Test
    void registerWeatherDataWithNullDayThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.registerWeatherData(
                        ACA_CODE.toString(), null, START_TIME, END_TIME,
                        43.84, -9.79, 37.05, 4.93,
                        MIN_ALTITUDE, MAX_ALTITUDE,
                        WIND_SPEED, WIND_DIRECTION
                ));

        assertEquals(0, weatherRepo.count());
    }

    @Test
    void registerWeatherDataWithNullStartTimeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.registerWeatherData(
                        ACA_CODE.toString(), DAY, null, END_TIME,
                        43.84, -9.79, 37.05, 4.93,
                        MIN_ALTITUDE, MAX_ALTITUDE,
                        WIND_SPEED, WIND_DIRECTION
                ));

        assertEquals(0, weatherRepo.count());
    }

    @Test
    void registerWeatherDataWithNullEndTimeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.registerWeatherData(
                        ACA_CODE.toString(), DAY, START_TIME, null,
                        43.84, -9.79, 37.05, 4.93,
                        MIN_ALTITUDE, MAX_ALTITUDE,
                        WIND_SPEED, WIND_DIRECTION
                ));

        assertEquals(0, weatherRepo.count());
    }

    @Test
    void registerWeatherDataWithNegativeMinAltitudeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.registerWeatherData(
                        ACA_CODE.toString(), DAY, START_TIME, END_TIME,
                        43.84, -9.79, 37.05, 4.93,
                        -1, MAX_ALTITUDE,
                        WIND_SPEED, WIND_DIRECTION
                ));

        assertEquals(0, weatherRepo.count());
    }

    @Test
    void registerWeatherDataWithMaxAltitudeLessThanMinThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.registerWeatherData(
                        ACA_CODE.toString(), DAY, START_TIME, END_TIME,
                        43.84, -9.79, 37.05, 4.93,
                        1000, 500,
                        WIND_SPEED, WIND_DIRECTION
                ));

        assertEquals(0, weatherRepo.count());
    }

    @Test
    void registerWeatherDataWithNegativeWindSpeedThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                controller.registerWeatherData(
                        ACA_CODE.toString(), DAY, START_TIME, END_TIME,
                        43.84, -9.79, 37.05, 4.93,
                        MIN_ALTITUDE, MAX_ALTITUDE,
                        -1.0, WIND_DIRECTION
                ));

        assertEquals(0, weatherRepo.count());
    }

    @Test
    void registerMultipleWeatherDataSuccessfully() {
        controller.registerWeatherData(
                ACA_CODE.toString(), DAY, START_TIME, END_TIME,
                43.84, -9.79, 37.05, 4.93,
                MIN_ALTITUDE, MAX_ALTITUDE,
                WIND_SPEED, WIND_DIRECTION
        );

        controller.registerWeatherData(
                ACA_CODE.toString(), OTHER_DAY, START_TIME, END_TIME,
                43.84, -9.79, 37.05, 4.93,
                MIN_ALTITUDE, MAX_ALTITUDE,
                WIND_SPEED, WIND_DIRECTION
        );

        assertEquals(2, weatherRepo.count());
    }

    // --- getAirControlAreas ---

    @Test
    void getAirControlAreasReturnsAll() {
        final List<AirControlArea> result = new ArrayList<>();
        controller.getAirControlAreas().forEach(result::add);

        assertEquals(1, result.size());
        assertEquals(ACA_CODE, result.get(0).identity());
    }

    // --- findByAreaAndDate ---

    @Test
    void findByAreaAndDateReturnsMatchingWeatherData() {
        controller.registerWeatherData(
                ACA_CODE.toString(), DAY, START_TIME, END_TIME,
                43.84, -9.79, 37.05, 4.93,
                MIN_ALTITUDE, MAX_ALTITUDE,
                WIND_SPEED, WIND_DIRECTION
        );

        controller.registerWeatherData(
                ACA_CODE.toString(), OTHER_DAY, START_TIME, END_TIME,
                43.84, -9.79, 37.05, 4.93,
                MIN_ALTITUDE, MAX_ALTITUDE,
                WIND_SPEED, WIND_DIRECTION
        );

        final List<WeatherData> result = new ArrayList<>();
        weatherRepo.findByAreaAndDate(ACA_CODE, DAY).forEach(result::add);

        assertEquals(1, result.size());
        assertEquals(DAY, result.get(0).day());
    }

    @Test
    void findByAreaAndDateReturnsEmptyWhenNoMatchExists() {
        controller.registerWeatherData(
                ACA_CODE.toString(), DAY, START_TIME, END_TIME,
                43.84, -9.79, 37.05, 4.93,
                MIN_ALTITUDE, MAX_ALTITUDE,
                WIND_SPEED, WIND_DIRECTION
        );

        final List<WeatherData> result = new ArrayList<>();
        weatherRepo.findByAreaAndDate(ACA_CODE, OTHER_DAY).forEach(result::add);

        assertTrue(result.isEmpty());
    }
}