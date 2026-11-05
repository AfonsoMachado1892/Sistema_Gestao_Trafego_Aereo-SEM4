package aisafe.weather.domain;

import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.sharedkernel.Coordinates;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class WeatherDataTest {

    private static final AirControlAreaCode ACA_CODE = AirControlAreaCode.of("LPPC");
    private static final LocalDate DAY = LocalDate.of(2026, 5, 17);
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

    // --- Valid construction ---

    @Test
    void validWeatherData() {
        assertDoesNotThrow(() -> validWeatherDataInstance());
    }

    @Test
    void identityIsGenerated() {
        assertNotNull(validWeatherDataInstance().identity());
    }

    // --- Getters ---

    @Test
    void airControlAreaCodeReturnsCorrectValue() {
        assertEquals(ACA_CODE, validWeatherDataInstance().airControlAreaCode());
    }

    @Test
    void dayReturnsCorrectValue() {
        assertEquals(DAY, validWeatherDataInstance().day());
    }

    @Test
    void startTimeReturnsCorrectValue() {
        assertEquals(START_TIME, validWeatherDataInstance().startTime());
    }

    @Test
    void endTimeReturnsCorrectValue() {
        assertEquals(END_TIME, validWeatherDataInstance().endTime());
    }

    @Test
    void boundingBoxReturnsCorrectValue() {
        assertEquals(BOUNDING_BOX, validWeatherDataInstance().boundingBox());
    }

    @Test
    void minAltitudeReturnsCorrectValue() {
        assertEquals(MIN_ALTITUDE, validWeatherDataInstance().minAltitude());
    }

    @Test
    void maxAltitudeReturnsCorrectValue() {
        assertEquals(MAX_ALTITUDE, validWeatherDataInstance().maxAltitude());
    }

    @Test
    void windSpeedReturnsCorrectValue() {
        assertEquals(WIND_SPEED, validWeatherDataInstance().windSpeed());
    }

    @Test
    void windDirectionReturnsCorrectValue() {
        assertEquals(WIND_DIRECTION, validWeatherDataInstance().windDirection());
    }

    // --- Null checks ---

    @Test
    void nullAirControlAreaCodeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new WeatherData(
                null, DAY, START_TIME, END_TIME,
                BOUNDING_BOX, MIN_ALTITUDE, MAX_ALTITUDE,
                WIND_SPEED, WIND_DIRECTION
        ));
    }

    @Test
    void nullAirControlAreaCodeThrowsCorrectMessage() {
        final Exception exception = assertThrows(IllegalArgumentException.class, () -> new WeatherData(
                null, DAY, START_TIME, END_TIME,
                BOUNDING_BOX, MIN_ALTITUDE, MAX_ALTITUDE,
                WIND_SPEED, WIND_DIRECTION
        ));

        assertEquals("Air control area code is required.", exception.getMessage());
    }

    @Test
    void nullDayThrows() {
        assertThrows(IllegalArgumentException.class, () -> new WeatherData(
                ACA_CODE, null, START_TIME, END_TIME,
                BOUNDING_BOX, MIN_ALTITUDE, MAX_ALTITUDE,
                WIND_SPEED, WIND_DIRECTION
        ));
    }

    @Test
    void nullDayThrowsCorrectMessage() {
        final Exception exception = assertThrows(IllegalArgumentException.class, () -> new WeatherData(
                ACA_CODE, null, START_TIME, END_TIME,
                BOUNDING_BOX, MIN_ALTITUDE, MAX_ALTITUDE,
                WIND_SPEED, WIND_DIRECTION
        ));

        assertEquals("Day is required.", exception.getMessage());
    }

    @Test
    void nullStartTimeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new WeatherData(
                ACA_CODE, DAY, null, END_TIME,
                BOUNDING_BOX, MIN_ALTITUDE, MAX_ALTITUDE,
                WIND_SPEED, WIND_DIRECTION
        ));
    }

    @Test
    void nullStartTimeThrowsCorrectMessage() {
        final Exception exception = assertThrows(IllegalArgumentException.class, () -> new WeatherData(
                ACA_CODE, DAY, null, END_TIME,
                BOUNDING_BOX, MIN_ALTITUDE, MAX_ALTITUDE,
                WIND_SPEED, WIND_DIRECTION
        ));

        assertEquals("Start time is required.", exception.getMessage());
    }

    @Test
    void nullEndTimeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new WeatherData(
                ACA_CODE, DAY, START_TIME, null,
                BOUNDING_BOX, MIN_ALTITUDE, MAX_ALTITUDE,
                WIND_SPEED, WIND_DIRECTION
        ));
    }

    @Test
    void nullEndTimeThrowsCorrectMessage() {
        final Exception exception = assertThrows(IllegalArgumentException.class, () -> new WeatherData(
                ACA_CODE, DAY, START_TIME, null,
                BOUNDING_BOX, MIN_ALTITUDE, MAX_ALTITUDE,
                WIND_SPEED, WIND_DIRECTION
        ));

        assertEquals("End time is required.", exception.getMessage());
    }

    @Test
    void nullBoundingBoxThrows() {
        assertThrows(IllegalArgumentException.class, () -> new WeatherData(
                ACA_CODE, DAY, START_TIME, END_TIME,
                null, MIN_ALTITUDE, MAX_ALTITUDE,
                WIND_SPEED, WIND_DIRECTION
        ));
    }

    @Test
    void nullBoundingBoxThrowsCorrectMessage() {
        final Exception exception = assertThrows(IllegalArgumentException.class, () -> new WeatherData(
                ACA_CODE, DAY, START_TIME, END_TIME,
                null, MIN_ALTITUDE, MAX_ALTITUDE,
                WIND_SPEED, WIND_DIRECTION
        ));

        assertEquals("Bounding box is required.", exception.getMessage());
    }

    // --- Altitude validation ---

    @Test
    void negativeMinAltitudeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new WeatherData(
                ACA_CODE, DAY, START_TIME, END_TIME,
                BOUNDING_BOX, -1, MAX_ALTITUDE,
                WIND_SPEED, WIND_DIRECTION
        ));
    }

    @Test
    void negativeMinAltitudeThrowsCorrectMessage() {
        final Exception exception = assertThrows(IllegalArgumentException.class, () -> new WeatherData(
                ACA_CODE, DAY, START_TIME, END_TIME,
                BOUNDING_BOX, -1, MAX_ALTITUDE,
                WIND_SPEED, WIND_DIRECTION
        ));

        assertEquals("Min altitude cannot be negative.", exception.getMessage());
    }

    @Test
    void maxAltitudeLessThanMinThrows() {
        assertThrows(IllegalArgumentException.class, () -> new WeatherData(
                ACA_CODE, DAY, START_TIME, END_TIME,
                BOUNDING_BOX, 1000, 500,
                WIND_SPEED, WIND_DIRECTION
        ));
    }

    @Test
    void maxAltitudeLessThanMinThrowsCorrectMessage() {
        final Exception exception = assertThrows(IllegalArgumentException.class, () -> new WeatherData(
                ACA_CODE, DAY, START_TIME, END_TIME,
                BOUNDING_BOX, 1000, 500,
                WIND_SPEED, WIND_DIRECTION
        ));

        assertEquals("Max altitude cannot be less than min altitude.", exception.getMessage());
    }

    // --- Wind speed validation ---

    @Test
    void negativeWindSpeedThrows() {
        assertThrows(IllegalArgumentException.class, () -> new WeatherData(
                ACA_CODE, DAY, START_TIME, END_TIME,
                BOUNDING_BOX, MIN_ALTITUDE, MAX_ALTITUDE,
                -1.0, WIND_DIRECTION
        ));
    }

    @Test
    void negativeWindSpeedThrowsCorrectMessage() {
        final Exception exception = assertThrows(IllegalArgumentException.class, () -> new WeatherData(
                ACA_CODE, DAY, START_TIME, END_TIME,
                BOUNDING_BOX, MIN_ALTITUDE, MAX_ALTITUDE,
                -1.0, WIND_DIRECTION
        ));

        assertEquals("Wind speed cannot be negative.", exception.getMessage());
    }

    // --- sameAs ---

    @Test
    void sameAsReturnsTrueForSameWeatherData() {
        final WeatherData weatherData = validWeatherDataInstance();

        assertTrue(weatherData.sameAs(weatherData));
    }

    @Test
    void sameAsReturnsFalseForDifferentWeatherData() {
        final WeatherData first = validWeatherDataInstance();
        final WeatherData second = validWeatherDataInstance();

        assertFalse(first.sameAs(second));
    }

    @Test
    void sameAsReturnsFalseForDifferentObject() {
        assertFalse(validWeatherDataInstance().sameAs("not weather data"));
    }

    @Test
    void sameAsReturnsFalseForNull() {
        assertFalse(validWeatherDataInstance().sameAs(null));
    }

    // --- toString ---

    @Test
    void toStringContainsWeatherDataInformation() {
        final String result = validWeatherDataInstance().toString();

        assertTrue(result.contains("WeatherData"));
        assertTrue(result.contains("id="));
        assertTrue(result.contains("area="));
        assertTrue(result.contains("day="));
        assertTrue(result.contains("start="));
        assertTrue(result.contains("end="));
        assertTrue(result.contains("bbox="));
        assertTrue(result.contains("altMin="));
        assertTrue(result.contains("altMax="));
        assertTrue(result.contains("windSpeed="));
        assertTrue(result.contains("windDir="));
    }

    // --- Helper ---

    private WeatherData validWeatherDataInstance() {
        return new WeatherData(
                ACA_CODE, DAY, START_TIME, END_TIME,
                BOUNDING_BOX, MIN_ALTITUDE, MAX_ALTITUDE,
                WIND_SPEED, WIND_DIRECTION
        );
    }
}