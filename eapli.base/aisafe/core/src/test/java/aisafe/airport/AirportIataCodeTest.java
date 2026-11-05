package aisafe.airport;

import aisafe.airport.domain.AirportIataCode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AirportIataCodeTest {

    @Test
    void validIataCode() {
        assertDoesNotThrow(() -> AirportIataCode.of("OPO"));
    }

    @Test
    void validIataCodeIsNormalizedToUppercase() {
        assertEquals("OPO", AirportIataCode.of("opo").code());
    }

    @Test
    void validIataCodeWithLeadingSpaces() {
        assertDoesNotThrow(() -> AirportIataCode.of(" OPO"));
    }

    @Test
    void nullIataCodeThrows() {
        assertThrows(Exception.class, () -> AirportIataCode.of(null));
    }

    @Test
    void emptyIataCodeThrows() {
        assertThrows(Exception.class, () -> AirportIataCode.of(""));
    }

    @Test
    void twoLetterIataCodeThrows() {
        assertThrows(Exception.class, () -> AirportIataCode.of("OP"));
    }

    @Test
    void fourLetterIataCodeThrows() {
        assertThrows(Exception.class, () -> AirportIataCode.of("OPOO"));
    }

    @Test
    void iataCodeWithDigitsThrows() {
        assertThrows(Exception.class, () -> AirportIataCode.of("O1O"));
    }

    @Test
    void iataCodeWithSpecialCharsThrows() {
        assertThrows(Exception.class, () -> AirportIataCode.of("O@O"));
    }

    @Test
    void equalIataCodesAreEqual() {
        assertEquals(AirportIataCode.of("OPO"), AirportIataCode.of("OPO"));
    }

    @Test
    void differentIataCodesAreNotEqual() {
        assertNotEquals(AirportIataCode.of("OPO"), AirportIataCode.of("LIS"));
    }

    @Test
    void compareToReturnsZeroForEqualCodes() {
        assertEquals(0, AirportIataCode.of("OPO").compareTo(AirportIataCode.of("OPO")));
    }

    @Test
    void compareToReturnsNonZeroForDifferentCodes() {
        assertNotEquals(0, AirportIataCode.of("OPO").compareTo(AirportIataCode.of("LIS")));
    }

    @Test
    void toStringReturnsCode() {
        assertEquals("OPO", AirportIataCode.of("OPO").toString());
    }

    @Test
    void iataCodeNotEqualToNull() {
        assertNotEquals(null, AirportIataCode.of("OPO"));
    }

    @Test
    void iataCodeNotEqualToDifferentType() {
        assertNotEquals("OPO", AirportIataCode.of("OPO"));
    }
}