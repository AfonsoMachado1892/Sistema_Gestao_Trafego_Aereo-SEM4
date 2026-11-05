package aisafe.pilot.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PilotID}.
 * Uses the AAA convention (Arrange, Act, Assert).
 */
class PilotIDTest {

    @Test
    void ensureValidPilotIDIsCreated() {
        final PilotID id = PilotID.of("abc-123");
        assertEquals("abc-123", id.number());
    }

    @Test
    void ensureNullIDIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> PilotID.of(null));
    }

    @Test
    void ensureEmptyIDIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> PilotID.of(""));
    }

    @Test
    void ensureNewIDGeneratesUniqueValues() {
        final PilotID a = PilotID.newID();
        final PilotID b = PilotID.newID();
        assertNotEquals(a, b);
    }

    @Test
    void ensureTwoIDsWithSameValueAreEqual() {
        final PilotID a = PilotID.of("same");
        final PilotID b = PilotID.of("same");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void ensureTwoIDsWithDifferentValuesAreNotEqual() {
        assertNotEquals(PilotID.of("aaa"), PilotID.of("bbb"));
    }

    @Test
    void ensureCompareToReturnsCorrectOrder() {
        final PilotID a = PilotID.of("aaa");
        final PilotID b = PilotID.of("bbb");
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
        assertEquals(0, a.compareTo(PilotID.of("aaa")));
    }

    @Test
    void ensureToStringReturnsNumber() {
        assertEquals("abc-123", PilotID.of("abc-123").toString());
    }
}
