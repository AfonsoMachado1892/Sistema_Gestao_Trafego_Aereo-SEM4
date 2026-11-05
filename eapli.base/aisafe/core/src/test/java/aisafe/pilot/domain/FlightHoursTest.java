package aisafe.pilot.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FlightHours}.
 * Uses the AAA convention (Arrange, Act, Assert).
 */
class FlightHoursTest {

    @Test
    void ensureValidHoursIsCreated() {
        final FlightHours hours = FlightHours.of(500);
        assertEquals(500, hours.hours());
    }

    @Test
    void ensureZeroHoursIsAccepted() {
        assertDoesNotThrow(() -> FlightHours.of(0));
    }

    @Test
    void ensureNegativeHoursAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> FlightHours.of(-1));
    }

    @Test
    void ensureTwoHoursWithSameValueAreEqual() {
        assertEquals(FlightHours.of(500), FlightHours.of(500));
        assertEquals(FlightHours.of(500).hashCode(), FlightHours.of(500).hashCode());
    }

    @Test
    void ensureTwoHoursWithDifferentValuesAreNotEqual() {
        assertNotEquals(FlightHours.of(500), FlightHours.of(600));
    }

    @Test
    void ensureToStringContainsHours() {
        assertTrue(FlightHours.of(500).toString().contains("500"));
    }
}
