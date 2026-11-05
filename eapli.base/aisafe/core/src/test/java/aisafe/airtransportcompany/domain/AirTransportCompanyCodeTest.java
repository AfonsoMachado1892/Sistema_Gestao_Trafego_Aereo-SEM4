package aisafe.airtransportcompany.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AirTransportCompanyCode}.
 *
 * <p>Uses the AAA convention (Arrange, Act, Assert).</p>
 */
class AirTransportCompanyCodeTest {

    // -----------------------------------------------------------------------
    // Valid codes
    // -----------------------------------------------------------------------

    @Test
    void ensureValidTwoLetterCodeIsCreated() {
        // Act
        final AirTransportCompanyCode code = AirTransportCompanyCode.of("TP");

        // Assert
        assertEquals("TP", code.toString());
    }

    @Test
    void ensureCodeIsNormalizedToUpperCase() {
        // Act
        final AirTransportCompanyCode code = AirTransportCompanyCode.of("tp");

        // Assert
        assertEquals("TP", code.toString());
    }

    @Test
    void ensureCodeWithWhitespaceIsTrimmedAndAccepted() {
        // Act
        final AirTransportCompanyCode code = AirTransportCompanyCode.of(" TP ");

        // Assert
        assertEquals("TP", code.toString());
    }

    // -----------------------------------------------------------------------
    // Invalid codes
    // -----------------------------------------------------------------------

    @Test
    void ensureEmptyCodeIsRejected() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> AirTransportCompanyCode.of(""));
    }

    @Test
    void ensureNullCodeIsRejected() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> AirTransportCompanyCode.of(null));
    }

    @Test
    void ensureSingleLetterCodeIsRejected() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> AirTransportCompanyCode.of("T"));
    }

    @Test
    void ensureThreeLetterCodeIsRejected() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> AirTransportCompanyCode.of("TAP"));
    }

    // -----------------------------------------------------------------------
    // Equality and comparison
    // -----------------------------------------------------------------------

    @Test
    void ensureTwoCodesWithSameValueAreEqual() {
        // Arrange
        final AirTransportCompanyCode a = AirTransportCompanyCode.of("TP");
        final AirTransportCompanyCode b = AirTransportCompanyCode.of("tp");

        // Act & Assert
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void ensureTwoCodesWithDifferentValuesAreNotEqual() {
        // Arrange
        final AirTransportCompanyCode a = AirTransportCompanyCode.of("TP");
        final AirTransportCompanyCode b = AirTransportCompanyCode.of("FR");

        // Act & Assert
        assertNotEquals(a, b);
    }

    @Test
    void ensureCompareToReturnsCorrectOrder() {
        // Arrange
        final AirTransportCompanyCode fr = AirTransportCompanyCode.of("FR");
        final AirTransportCompanyCode tp = AirTransportCompanyCode.of("TP");

        // Act & Assert
        assertTrue(fr.compareTo(tp) < 0);
        assertTrue(tp.compareTo(fr) > 0);
        assertEquals(0, fr.compareTo(AirTransportCompanyCode.of("FR")));
    }
}