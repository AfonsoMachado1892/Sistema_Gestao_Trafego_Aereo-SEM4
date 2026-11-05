package aisafe.weather.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeatherDataIDTest {

    @Test
    void newIDCreatesValidWeatherDataID() {
        final WeatherDataID id = WeatherDataID.newID();

        assertNotNull(id);
        assertNotNull(id.toString());
        assertFalse(id.toString().isBlank());
    }

    @Test
    void newIDCreatesDifferentIDs() {
        final WeatherDataID first = WeatherDataID.newID();
        final WeatherDataID second = WeatherDataID.newID();

        assertNotEquals(first, second);
    }

    @Test
    void ofCreatesWeatherDataIDSuccessfully() {
        assertDoesNotThrow(() -> WeatherDataID.of("WD-001"));
    }

    @Test
    void ofReturnsCorrectToString() {
        final WeatherDataID id = WeatherDataID.of("WD-001");

        assertEquals("WD-001", id.toString());
    }

    @Test
    void nullNumberThrows() {
        final Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> WeatherDataID.of(null)
        );

        assertEquals("WeatherDataID cannot be null or blank.", exception.getMessage());
    }

    @Test
    void blankNumberThrows() {
        final Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> WeatherDataID.of("   ")
        );

        assertEquals("WeatherDataID cannot be null or blank.", exception.getMessage());
    }

    @Test
    void emptyNumberThrows() {
        final Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> WeatherDataID.of("")
        );

        assertEquals("WeatherDataID cannot be null or blank.", exception.getMessage());
    }

    @Test
    void equalIDsAreEqual() {
        assertEquals(WeatherDataID.of("WD-001"), WeatherDataID.of("WD-001"));
    }

    @Test
    void sameObjectIsEqual() {
        final WeatherDataID id = WeatherDataID.of("WD-001");

        assertEquals(id, id);
    }

    @Test
    void differentIDsAreNotEqual() {
        assertNotEquals(WeatherDataID.of("WD-001"), WeatherDataID.of("WD-002"));
    }

    @Test
    void idIsNotEqualToDifferentObjectType() {
        assertNotEquals(WeatherDataID.of("WD-001"), "WD-001");
    }

    @Test
    void idIsNotEqualToNull() {
        assertNotEquals(null, WeatherDataID.of("WD-001"));
    }

    @Test
    void hashCodeIsConsistent() {
        final WeatherDataID id = WeatherDataID.of("WD-001");

        assertEquals(id.hashCode(), id.hashCode());
    }

    @Test
    void equalIDsHaveSameHashCode() {
        final WeatherDataID first = WeatherDataID.of("WD-001");
        final WeatherDataID second = WeatherDataID.of("WD-001");

        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void compareToReturnsZeroForEqualIDs() {
        final WeatherDataID first = WeatherDataID.of("WD-001");
        final WeatherDataID second = WeatherDataID.of("WD-001");

        assertEquals(0, first.compareTo(second));
    }

    @Test
    void compareToReturnsNegativeForLowerID() {
        final WeatherDataID first = WeatherDataID.of("WD-001");
        final WeatherDataID second = WeatherDataID.of("WD-002");

        assertTrue(first.compareTo(second) < 0);
    }

    @Test
    void compareToReturnsPositiveForGreaterID() {
        final WeatherDataID first = WeatherDataID.of("WD-002");
        final WeatherDataID second = WeatherDataID.of("WD-001");

        assertTrue(first.compareTo(second) > 0);
    }
}
