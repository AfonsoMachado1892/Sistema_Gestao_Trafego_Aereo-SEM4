package aisafe.weather.application;

import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.sharedkernel.Coordinates;
import aisafe.weather.domain.*;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ListWeatherDataControllerTest {

    // --- Stubs ---

    static class AuthorizationServiceStub extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) { }
    }

    private static class WeatherDataRepositoryStub implements WeatherDataRepository {

        private final Map<WeatherDataID, WeatherData> weatherDataMap = new HashMap<>();

        void add(final WeatherData weatherData) {
            weatherDataMap.put(weatherData.identity(), weatherData);
        }

        @Override
        public Iterable<WeatherData> findByAreaAndDate(final AirControlAreaCode code,
                                                       final LocalDate date) {
            return weatherDataMap.values().stream()
                    .filter(w -> w.airControlAreaCode().equals(code) && w.day().equals(date))
                    .toList();
        }

        @Override
        public Optional<WeatherData> findByID(final WeatherDataID id) {
            return Optional.ofNullable(weatherDataMap.get(id));
        }

        @Override
        public <S extends WeatherData> S save(final S entity) {
            weatherDataMap.put(entity.identity(), entity);
            return entity;
        }

        @Override
        public Iterable<WeatherData> findAll() { return weatherDataMap.values(); }

        @Override
        public Optional<WeatherData> ofIdentity(final WeatherDataID id) {
            return Optional.ofNullable(weatherDataMap.get(id));
        }

        @Override
        public void delete(final WeatherData entity) { weatherDataMap.remove(entity.identity()); }

        @Override
        public void deleteOfIdentity(final WeatherDataID id) { weatherDataMap.remove(id); }

        @Override
        public long count() { return weatherDataMap.size(); }
    }

    // --- Setup ---

    private WeatherDataRepositoryStub weatherRepo;
    private ListWeatherDataController controller;

    private static final AirControlAreaCode ACA_CODE = AirControlAreaCode.of("LPPC");
    private static final AirControlAreaCode OTHER_CODE = AirControlAreaCode.of("SBGL");
    private static final LocalDate DAY = LocalDate.of(2026, 6, 22);
    private static final LocalDate OTHER_DAY = LocalDate.of(2026, 6, 23);
    private static final LocalTime START_TIME = LocalTime.of(5, 0);
    private static final LocalTime END_TIME = LocalTime.of(8, 15);
    private static final BoundingBox BOUNDING_BOX = new BoundingBox(
            Coordinates.of(43.84, -9.79),
            Coordinates.of(37.05, 4.93)
    );

    private WeatherData validWeatherData(final AirControlAreaCode code, final LocalDate day) {
        return new WeatherData(
                code, day, START_TIME, END_TIME,
                BOUNDING_BOX, 0, 1000, 28.75, 90.0
        );
    }

    @BeforeEach
    void setUp() {
        weatherRepo = new WeatherDataRepositoryStub();
        controller = new ListWeatherDataController(
                new AuthorizationServiceStub(),
                weatherRepo
        );
    }

    // --- allWeatherData ---

    @Test
    void allWeatherDataReturnsAllRecords() {
        weatherRepo.add(validWeatherData(ACA_CODE, DAY));
        weatherRepo.add(validWeatherData(ACA_CODE, OTHER_DAY));
        weatherRepo.add(validWeatherData(OTHER_CODE, DAY));

        final List<WeatherData> result = new ArrayList<>();
        controller.allWeatherData().forEach(result::add);

        assertEquals(3, result.size());
    }

    @Test
    void allWeatherDataReturnsEmptyWhenNoneRegistered() {
        final List<WeatherData> result = new ArrayList<>();
        controller.allWeatherData().forEach(result::add);

        assertTrue(result.isEmpty());
    }

    // --- filteredWeatherData ---

    @Test
    void filteredWeatherDataReturnsMatchingRecords() {
        weatherRepo.add(validWeatherData(ACA_CODE, DAY));
        weatherRepo.add(validWeatherData(ACA_CODE, OTHER_DAY));
        weatherRepo.add(validWeatherData(OTHER_CODE, DAY));

        final List<WeatherData> result = new ArrayList<>();
        controller.filteredWeatherData(ACA_CODE.toString(), DAY).forEach(result::add);

        assertEquals(1, result.size());
        assertEquals(ACA_CODE, result.get(0).airControlAreaCode());
        assertEquals(DAY, result.get(0).day());
    }

    @Test
    void filteredWeatherDataReturnsEmptyWhenNoMatch() {
        weatherRepo.add(validWeatherData(ACA_CODE, DAY));

        final List<WeatherData> result = new ArrayList<>();
        controller.filteredWeatherData(OTHER_CODE.toString(), DAY).forEach(result::add);

        assertTrue(result.isEmpty());
    }

    @Test
    void filteredWeatherDataReturnsEmptyWhenDateDoesNotMatch() {
        weatherRepo.add(validWeatherData(ACA_CODE, DAY));

        final List<WeatherData> result = new ArrayList<>();
        controller.filteredWeatherData(ACA_CODE.toString(), OTHER_DAY).forEach(result::add);

        assertTrue(result.isEmpty());
    }

    @Test
    void filteredWeatherDataReturnsMultipleRecordsForSameAreaAndDate() {
        weatherRepo.add(validWeatherData(ACA_CODE, DAY));
        weatherRepo.add(validWeatherData(ACA_CODE, DAY));

        final List<WeatherData> result = new ArrayList<>();
        controller.filteredWeatherData(ACA_CODE.toString(), DAY).forEach(result::add);

        assertEquals(2, result.size());
    }
}
