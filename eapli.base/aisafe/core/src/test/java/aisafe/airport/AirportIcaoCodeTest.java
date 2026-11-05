package aisafe.airport;

import aisafe.airport.domain.AirportIcaoCode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AirportIcaoCodeTest {

    @Test
    void validIcaoCode() {
        assertDoesNotThrow(() -> AirportIcaoCode.of("LPPR"));
    }

    @Test
    void validIcaoCodeIsNormalizedToUppercase() {
        assertEquals("LPPR", AirportIcaoCode.of("lppr").code());
    }

    @Test
    void nullIcaoCodeThrows() {
        assertThrows(Exception.class, () -> AirportIcaoCode.of(null));
    }

    @Test
    void emptyIcaoCodeThrows() {
        assertThrows(Exception.class, () -> AirportIcaoCode.of(""));
    }

    @Test
    void threeLetterIcaoCodeThrows() {
        assertThrows(Exception.class, () -> AirportIcaoCode.of("LPP"));
    }

    @Test
    void fiveLetterIcaoCodeThrows() {
        assertThrows(Exception.class, () -> AirportIcaoCode.of("LPPRA"));
    }

    @Test
    void icaoCodeWithDigitsThrows() {
        assertThrows(Exception.class, () -> AirportIcaoCode.of("LPP1"));
    }

    @Test
    void equalIcaoCodesAreEqual() {
        assertEquals(AirportIcaoCode.of("LPPR"), AirportIcaoCode.of("LPPR"));
    }

    @Test
    void differentIcaoCodesAreNotEqual() {
        assertNotEquals(AirportIcaoCode.of("LPPR"), AirportIcaoCode.of("LPPT"));
    }

    @Test
    void toStringReturnsCode() {
        assertEquals("LPPR", AirportIcaoCode.of("LPPR").toString());
    }

    @Test
    void hashCodeIsConsistent() {
        final AirportIcaoCode code = AirportIcaoCode.of("LPPR");
        assertEquals(code.hashCode(), code.hashCode());
    }

    @Test
    void codeReturnsCorrectValue() {
        assertEquals("LPPR", AirportIcaoCode.of("LPPR").code());
    }

    @Test
    void icaoCodeNotEqualToNull() {
        assertNotEquals(null, AirportIcaoCode.of("LPPR"));
    }

    @Test
    void icaoCodeNotEqualToDifferentType() {
        assertNotEquals("LPPR", AirportIcaoCode.of("LPPR"));
    }
}