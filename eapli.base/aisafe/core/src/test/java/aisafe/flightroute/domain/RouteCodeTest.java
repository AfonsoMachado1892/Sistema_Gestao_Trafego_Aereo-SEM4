package aisafe.flightroute.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RouteCode}.
 * Uses the AAA convention (Arrange, Act, Assert).
 */
class RouteCodeTest {

    // ── Valid codes ──────────────────────────────────────────────────────────

    @Test
    void ensureValidCodeWithOneDigitIsCreated() {
        final RouteCode code = RouteCode.of("TP1");
        assertEquals("TP1", code.value());
    }

    @Test
    void ensureValidCodeWithFourDigitsIsCreated() {
        final RouteCode code = RouteCode.of("TP1234");
        assertEquals("TP1234", code.value());
    }

    @Test
    void ensureCodeIsNormalizedToUpperCase() {
        final RouteCode code = RouteCode.of("tp123");
        assertEquals("TP123", code.value());
    }

    @Test
    void ensureCodeWithWhitespaceIsTrimmedAndAccepted() {
        final RouteCode code = RouteCode.of(" TP123 ");
        assertEquals("TP123", code.value());
    }

    // ── Invalid codes ────────────────────────────────────────────────────────

    @Test
    void ensureNullCodeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> RouteCode.of(null));
    }

    @Test
    void ensureEmptyCodeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> RouteCode.of(""));
    }

    @Test
    void ensureCodeWithOnlyLettersIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> RouteCode.of("TP"));
    }

    @Test
    void ensureCodeWithOneLetterAndDigitsIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> RouteCode.of("T123"));
    }

    @Test
    void ensureCodeWithThreeLettersAndDigitsIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> RouteCode.of("TAP123"));
    }

    @Test
    void ensureCodeWithFiveDigitsIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> RouteCode.of("TP12345"));
    }

    // ── companyPrefix ────────────────────────────────────────────────────────

    @Test
    void ensureCompanyPrefixReturnsTwoLetterPrefix() {
        assertEquals("TP", RouteCode.of("TP123").companyPrefix());
    }

    @Test
    void ensureCompanyPrefixMatchesNormalizedCode() {
        assertEquals("FR", RouteCode.of("fr42").companyPrefix());
    }

    // ── Equality and comparison ──────────────────────────────────────────────

    @Test
    void ensureTwoCodesWithSameValueAreEqual() {
        final RouteCode a = RouteCode.of("TP123");
        final RouteCode b = RouteCode.of("tp123");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void ensureTwoCodesWithDifferentValuesAreNotEqual() {
        assertNotEquals(RouteCode.of("TP123"), RouteCode.of("FR42"));
    }

    @Test
    void ensureCompareToReturnsCorrectOrder() {
        final RouteCode fr = RouteCode.of("FR1");
        final RouteCode tp = RouteCode.of("TP1");
        assertTrue(fr.compareTo(tp) < 0);
        assertTrue(tp.compareTo(fr) > 0);
        assertEquals(0, fr.compareTo(RouteCode.of("FR1")));
    }

    @Test
    void ensureToStringReturnsValue() {
        assertEquals("TP123", RouteCode.of("TP123").toString());
    }
}
