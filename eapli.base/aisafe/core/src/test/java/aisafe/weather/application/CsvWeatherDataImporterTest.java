package aisafe.weather.application;

import aisafe.weather.application.CsvWeatherDataImporter;
import aisafe.weather.application.WeatherDataDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvWeatherDataImporterTest {

    private static final String VALID_FILE = "src/test/java/aisafe/weather/testWeather.csv";
    private static final String INVALID_FILE = "src/test/resources/weather/nonexistent.csv";

    private final CsvWeatherDataImporter importer = new CsvWeatherDataImporter();

    @Test
    void readWeatherDataReturnsCorrectNumberOfRecords() {
        final List<WeatherDataDTO> result = importer.readWeatherData(VALID_FILE);

        assertEquals(2, result.size());
    }

    @Test
    void readWeatherDataParsesAreaCodeCorrectly() {
        final List<WeatherDataDTO> result = importer.readWeatherData(VALID_FILE);

        assertEquals("121", result.get(0).areaCode());
    }

    @Test
    void readWeatherDataParsesDayCorrectly() {
        final List<WeatherDataDTO> result = importer.readWeatherData(VALID_FILE);

        assertEquals(LocalDate.of(2026, 6, 22), result.get(0).day());
    }

    @Test
    void readWeatherDataParsesStartTimeCorrectly() {
        final List<WeatherDataDTO> result = importer.readWeatherData(VALID_FILE);

        assertEquals(LocalTime.of(5, 0), result.get(0).startTime());
    }

    @Test
    void readWeatherDataParsesEndTimeCorrectly() {
        final List<WeatherDataDTO> result = importer.readWeatherData(VALID_FILE);

        assertEquals(LocalTime.of(8, 15), result.get(0).endTime());
    }

    @Test
    void readWeatherDataParsesLatStartCorrectly() {
        final List<WeatherDataDTO> result = importer.readWeatherData(VALID_FILE);

        assertEquals(43.840454, result.get(0).latStart(), 0.000001);
    }

    @Test
    void readWeatherDataParsesLonStartCorrectly() {
        final List<WeatherDataDTO> result = importer.readWeatherData(VALID_FILE);

        assertEquals(-9.795711, result.get(0).lonStart(), 0.000001);
    }

    @Test
    void readWeatherDataParsesAltitudesCorrectly() {
        final List<WeatherDataDTO> result = importer.readWeatherData(VALID_FILE);

        assertEquals(0, result.get(0).minAltitude());
        assertEquals(1000, result.get(0).maxAltitude());
    }

    @Test
    void readWeatherDataParsesWindSpeedCorrectly() {
        final List<WeatherDataDTO> result = importer.readWeatherData(VALID_FILE);

        assertEquals(28.75, result.get(0).windSpeed(), 0.001);
    }

    @Test
    void readWeatherDataParsesWindDirectionCorrectly() {
        final List<WeatherDataDTO> result = importer.readWeatherData(VALID_FILE);

        assertEquals(90.0, result.get(0).windDirection(), 0.001);
    }

    @Test
    void readWeatherDataParsesSecondRecordCorrectly() {
        final List<WeatherDataDTO> result = importer.readWeatherData(VALID_FILE);

        assertEquals(1001, result.get(1).minAltitude());
        assertEquals(2000, result.get(1).maxAltitude());
        assertEquals(20.7, result.get(1).windSpeed(), 0.001);
        assertEquals(105.0, result.get(1).windDirection(), 0.001);
    }

    @Test
    void readWeatherDataWithNonExistentFileThrows() {
        assertThrows(RuntimeException.class, () ->
                importer.readWeatherData(INVALID_FILE));
    }
}