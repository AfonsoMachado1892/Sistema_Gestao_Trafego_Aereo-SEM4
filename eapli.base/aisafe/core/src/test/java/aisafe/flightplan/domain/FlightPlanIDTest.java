package aisafe.flightplan.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanIDTest {

    @Test
    void ensureFlightPlanIDIsAccepted() {
        final FlightPlanID id = FlightPlanID.of("FP-001");

        assertEquals("FP-001", id.number());
    }

    @Test
    void ensureEmptyFlightPlanIDIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                FlightPlanID.of(""));
    }

    @Test
    void ensureBlankFlightPlanIDIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                FlightPlanID.of("   "));
    }

    @Test
    void ensureNullFlightPlanIDIsRejected() {
        assertThrows(Exception.class, () ->
                FlightPlanID.of(null));
    }

    @Test
    void ensureNewIDGeneratesValue() {
        final FlightPlanID id = FlightPlanID.newID();

        assertNotNull(id);
        assertNotNull(id.number());
        assertFalse(id.number().isBlank());
    }

    @Test
    void ensureNewIDGeneratesDifferentValues() {
        final FlightPlanID first = FlightPlanID.newID();
        final FlightPlanID second = FlightPlanID.newID();

        assertNotEquals(first, second);
    }

    @Test
    void ensureEqualsWorksForSameValue() {
        assertEquals(
                FlightPlanID.of("FP-001"),
                FlightPlanID.of("FP-001")
        );
    }

    @Test
    void ensureDifferentIDsAreNotEqual() {
        assertNotEquals(
                FlightPlanID.of("FP-001"),
                FlightPlanID.of("FP-002")
        );
    }

    @Test
    void ensureToStringReturnsIDValue() {
        assertEquals("FP-001", FlightPlanID.of("FP-001").toString());
    }
}