package aisafe.weather.application;

import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.weather.domain.*;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ImportBulkWeatherDataControllerTest {

    // --- Stubs ---

    static class AuthorizationServiceStub extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) { }
    }

    private static class WeatherDataRepositoryStub implements WeatherDataRepository {

        private final Map<WeatherDataID, WeatherData> weatherDataMap = new HashMap<>();

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

    private static class WeatherDataImporterFactoryStub extends WeatherDataImporterFactory {

        private final List<WeatherDataDTO> dtos;

        WeatherDataImporterFactoryStub(final List<WeatherDataDTO> dtos) {
            this.dtos = dtos;
        }

        @Override
        public WeatherDataImporter getImporter(final String filePath) {
            return path -> dtos;
        }
    }

    // --- Setup ---

    private WeatherDataRepositoryStub weatherRepo;
    private ImportBulkWeatherDataController controller;

    private static final AirControlAreaCode ACA_CODE = AirControlAreaCode.of("LPPC");
    private static final LocalDate DAY = LocalDate.of(2026, 6, 22);
    private static final LocalTime START_TIME = LocalTime.of(5, 0);
    private static final LocalTime END_TIME = LocalTime.of(8, 15);

    private WeatherDataDTO validDTO() {
        return new WeatherDataDTO(
                ACA_CODE.toString(),
                DAY, START_TIME, END_TIME,
                43.84, -9.79, 37.05, 4.93,
                0, 1000, 28.75, 90.0
        );
    }

    @BeforeEach
    void setUp() {
        weatherRepo = new WeatherDataRepositoryStub();
    }

    private ImportBulkWeatherDataController controllerWith(final List<WeatherDataDTO> dtos) {
        return new ImportBulkWeatherDataController(
                new AuthorizationServiceStub(),
                weatherRepo,
                new WeatherDataImporterFactoryStub(dtos)
        );
    }

    // --- previewImport ---

    @Test
    void previewImportReturnsCorrectNumberOfRecords() {
        final List<WeatherDataDTO> dtos = List.of(validDTO(), validDTO(), validDTO());
        controller = controllerWith(dtos);

        final List<WeatherDataDTO> result = controller.previewImport("test.csv");

        assertEquals(3, result.size());
    }

    @Test
    void previewImportReturnsEmptyListWhenFileIsEmpty() {
        controller = controllerWith(List.of());

        final List<WeatherDataDTO> result = controller.previewImport("test.csv");

        assertTrue(result.isEmpty());
    }

    @Test
    void previewImportWithUnsupportedFormatThrows() {
        controller = new ImportBulkWeatherDataController(
                new AuthorizationServiceStub(),
                weatherRepo,
                new WeatherDataImporterFactory()
        );

        assertThrows(IllegalArgumentException.class, () ->
                controller.previewImport("test.xml"));
    }

    // --- confirmImport ---

    @Test
    void confirmImportSavesAllRecords() {
        final List<WeatherDataDTO> dtos = List.of(validDTO(), validDTO(), validDTO());
        controller = controllerWith(dtos);

        final int count = controller.confirmImport(dtos);

        assertEquals(3, count);
        assertEquals(3, weatherRepo.count());
    }

    @Test
    void confirmImportReturnsZeroForEmptyList() {
        controller = controllerWith(List.of());

        final int count = controller.confirmImport(List.of());

        assertEquals(0, count);
        assertEquals(0, weatherRepo.count());
    }

    @Test
    void confirmImportWithSingleRecordSavesCorrectly() {
        final WeatherDataDTO dto = validDTO();
        controller = controllerWith(List.of(dto));

        controller.confirmImport(List.of(dto));

        assertEquals(1, weatherRepo.count());

        final WeatherData saved = weatherRepo.findAll().iterator().next();
        assertEquals(ACA_CODE, saved.airControlAreaCode());
        assertEquals(DAY, saved.day());
        assertEquals(START_TIME, saved.startTime());
        assertEquals(END_TIME, saved.endTime());
        assertEquals(0, saved.minAltitude());
        assertEquals(1000, saved.maxAltitude());
        assertEquals(28.75, saved.windSpeed());
        assertEquals(90.0, saved.windDirection());
    }
}