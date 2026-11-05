package aisafe.aircontrolarea;

import aisafe.aircontrolarea.domain.AirControlAreaCode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AirControlAreaCodeTest {

    @Test
    void validAirControlAreaCode() {
        assertDoesNotThrow(() -> AirControlAreaCode.of("LPPC"));
    }

    @Test
    void codeIsNormalizedToUppercase() {
        assertEquals("LPPC", AirControlAreaCode.of("lppc").code());
    }

    @Test
    void codeWithLeadingSpacesIsValid() {
        assertDoesNotThrow(() -> AirControlAreaCode.of(" LPPC"));
    }

    @Test
    void nullCodeThrows() {
        assertThrows(Exception.class, () -> AirControlAreaCode.of(null));
    }

    @Test
    void emptyCodeThrows() {
        assertThrows(Exception.class, () -> AirControlAreaCode.of(""));
    }

    @Test
    void codeReturnsCorrectValue() {
        assertEquals("LPPC", AirControlAreaCode.of("LPPC").code());
    }

    @Test
    void toStringReturnsCode() {
        assertEquals("LPPC", AirControlAreaCode.of("LPPC").toString());
    }

    @Test
    void equalCodesAreEqual() {
        assertEquals(AirControlAreaCode.of("LPPC"), AirControlAreaCode.of("LPPC"));
    }

    @Test
    void differentCodesAreNotEqual() {
        assertNotEquals(AirControlAreaCode.of("LPPC"), AirControlAreaCode.of("LECM"));
    }

    @Test
    void hashCodeIsConsistent() {
        final AirControlAreaCode code = AirControlAreaCode.of("LPPC");
        assertEquals(code.hashCode(), code.hashCode());
    }

    @Test
    void compareToReturnsZeroForEqualCodes() {
        assertEquals(0, AirControlAreaCode.of("LPPC").compareTo(AirControlAreaCode.of("LPPC")));
    }

    @Test
    void compareToReturnsNonZeroForDifferentCodes() {
        assertNotEquals(0, AirControlAreaCode.of("LPPC").compareTo(AirControlAreaCode.of("LECM")));
    }
}