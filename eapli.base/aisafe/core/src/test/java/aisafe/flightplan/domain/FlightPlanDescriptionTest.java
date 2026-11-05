package aisafe.flightplan.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanDescriptionTest {

    @Test
    void ensureDescriptionIsAccepted() {
        final FlightPlanDescription description =
                FlightPlanDescription.of("RAW DSL TEXT");

        assertEquals("RAW DSL TEXT", description.text());
    }

    @Test
    void ensureEmptyDescriptionIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                FlightPlanDescription.of(""));
    }

    @Test
    void ensureBlankDescriptionIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                FlightPlanDescription.of("   "));
    }

    @Test
    void ensureNullDescriptionIsRejected() {
        assertThrows(Exception.class, () ->
                FlightPlanDescription.of(null));
    }

    @Test
    void ensureEqualsWorksForSameText() {
        assertEquals(
                FlightPlanDescription.of("TEXT"),
                FlightPlanDescription.of("TEXT")
        );
    }

    @Test
    void ensureDifferentTextIsNotEqual() {
        assertNotEquals(
                FlightPlanDescription.of("TEXT A"),
                FlightPlanDescription.of("TEXT B")
        );
    }

    @Test
    void ensureToStringReturnsText() {
        assertEquals("TEXT", FlightPlanDescription.of("TEXT").toString());
    }
}