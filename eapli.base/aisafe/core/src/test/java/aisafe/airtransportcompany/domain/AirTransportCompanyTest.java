package aisafe.airtransportcompany.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AirTransportCompany}.
 *
 * <p>Uses the AAA convention (Arrange, Act, Assert).</p>
 */
class AirTransportCompanyTest {

    private static final AirTransportCompanyCode IATA_TP =
            AirTransportCompanyCode.of("TP");

    // -----------------------------------------------------------------------
    // Valid construction
    // -----------------------------------------------------------------------

    @Test
    void ensureValidCompanyIsCreatedSuccessfully() {
        // Act
        final AirTransportCompany company =
                new AirTransportCompany(IATA_TP, "TAP", "TAP Air Portugal");

        // Assert
        assertEquals("TP", company.iataCode().toString());
        assertEquals("TAP", company.icaoCode());
        assertEquals("TAP Air Portugal", company.name());
    }

    @Test
    void ensureIcaoCodeIsNormalizedToUpperCase() {
        // Act
        final AirTransportCompany company =
                new AirTransportCompany(IATA_TP, "tap", "TAP Air Portugal");

        // Assert
        assertEquals("TAP", company.icaoCode());
    }

    @Test
    void ensureNameIsTrimmed() {
        // Act
        final AirTransportCompany company =
                new AirTransportCompany(IATA_TP, "TAP", "  TAP Air Portugal  ");

        // Assert
        assertEquals("TAP Air Portugal", company.name());
    }

    @Test
    void ensureTwoLetterIcaoCodeIsAccepted() {
        // Act & Assert — no exception
        assertDoesNotThrow(() ->
                new AirTransportCompany(IATA_TP, "TP", "TAP Air Portugal"));
    }

    @Test
    void ensureThreeLetterIcaoCodeIsAccepted() {
        // Act & Assert — no exception
        assertDoesNotThrow(() ->
                new AirTransportCompany(IATA_TP, "TAP", "TAP Air Portugal"));
    }

    // -----------------------------------------------------------------------
    // Invalid construction
    // -----------------------------------------------------------------------

    @Test
    void ensureNullIataCodeIsRejected() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> new AirTransportCompany(null, "TAP", "TAP Air Portugal"));
    }

    @Test
    void ensureEmptyIcaoCodeIsRejected() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> new AirTransportCompany(IATA_TP, "", "TAP Air Portugal"));
    }

    @Test
    void ensureNullIcaoCodeIsRejected() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> new AirTransportCompany(IATA_TP, null, "TAP Air Portugal"));
    }

    @Test
    void ensureEmptyNameIsRejected() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> new AirTransportCompany(IATA_TP, "TAP", ""));
    }

    @Test
    void ensureNullNameIsRejected() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> new AirTransportCompany(IATA_TP, "TAP", null));
    }

    @Test
    void ensureIcaoCodeShorterThanTwoLettersIsRejected() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> new AirTransportCompany(IATA_TP, "T", "TAP Air Portugal"));
    }

    @Test
    void ensureIcaoCodeLongerThanThreeLettersIsRejected() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> new AirTransportCompany(IATA_TP, "TAPP", "TAP Air Portugal"));
    }

    // -----------------------------------------------------------------------
    // Identity / sameAs
    // -----------------------------------------------------------------------

    @Test
    void ensureIdentityReturnsIataCode() {
        // Arrange
        final AirTransportCompany company =
                new AirTransportCompany(IATA_TP, "TAP", "TAP Air Portugal");

        // Act & Assert
        assertEquals(IATA_TP, company.identity());
    }

    @Test
    void ensureSameAsReturnsTrueForSameIataCode() {
        // Arrange
        final AirTransportCompany a = new AirTransportCompany(IATA_TP, "TAP", "TAP Air Portugal");
        final AirTransportCompany b = new AirTransportCompany(IATA_TP, "TAP", "Different Name");

        // Act & Assert
        assertTrue(a.sameAs(b));
    }

    @Test
    void ensureSameAsReturnsFalseForDifferentIataCode() {
        // Arrange
        final AirTransportCompany a = new AirTransportCompany(IATA_TP, "TAP", "TAP Air Portugal");
        final AirTransportCompany b = new AirTransportCompany(
                AirTransportCompanyCode.of("FR"), "RYR", "Ryanair");

        // Act & Assert
        assertFalse(a.sameAs(b));
    }

    @Test
    void ensureSameAsReturnsFalseForNonCompanyObject() {
        // Arrange
        final AirTransportCompany company =
                new AirTransportCompany(IATA_TP, "TAP", "TAP Air Portugal");

        // Act & Assert
        assertFalse(company.sameAs("not a company"));
    }
}