package aisafe.aircontrolarea;

import aisafe.aircontrolarea.domain.AirControlAreaName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AirControlAreaNameTest {

    @Test
    void validAirControlAreaName() {
        assertDoesNotThrow(() -> AirControlAreaName.of("Lisboa"));
    }

    @Test
    void nullNameThrows() {
        assertThrows(Exception.class, () -> AirControlAreaName.of(null));
    }

    @Test
    void emptyNameThrows() {
        assertThrows(Exception.class, () -> AirControlAreaName.of(""));
    }

    @Test
    void nameReturnsCorrectValue() {
        assertEquals("Lisboa", AirControlAreaName.of("Lisboa").name());
    }

    @Test
    void toStringReturnsName() {
        assertEquals("Lisboa", AirControlAreaName.of("Lisboa").toString());
    }

    @Test
    void equalNamesAreEqual() {
        assertEquals(AirControlAreaName.of("Lisboa"), AirControlAreaName.of("Lisboa"));
    }

    @Test
    void differentNamesAreNotEqual() {
        assertNotEquals(AirControlAreaName.of("Lisboa"), AirControlAreaName.of("Madrid"));
    }

    @Test
    void hashCodeIsConsistent() {
        final AirControlAreaName name = AirControlAreaName.of("Lisboa");
        assertEquals(name.hashCode(), name.hashCode());
    }

    @Test
    void compareToReturnsZeroForEqualNames() {
        assertEquals(0, AirControlAreaName.of("Lisboa").compareTo(AirControlAreaName.of("Lisboa")));
    }

    @Test
    void compareToReturnsNonZeroForDifferentNames() {
        assertNotEquals(0, AirControlAreaName.of("Lisboa").compareTo(AirControlAreaName.of("Madrid")));
    }
}