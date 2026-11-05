package aisafe.airport;

import aisafe.airport.domain.AirportName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AirportNameTest {

    @Test
    void validAirportName() {
        assertDoesNotThrow(() -> AirportName.of("Francisco de Sá Carneiro Airport"));
    }

    @Test
    void nullNameThrows() {
        assertThrows(Exception.class, () -> AirportName.of(null));
    }

    @Test
    void emptyNameThrows() {
        assertThrows(Exception.class, () -> AirportName.of(""));
    }

    @Test
    void nameReturnsCorrectValue() {
        assertEquals("Francisco de Sá Carneiro Airport",
                AirportName.of("Francisco de Sá Carneiro Airport").name());
    }

    @Test
    void toStringReturnsName() {
        assertEquals("Francisco de Sá Carneiro Airport",
                AirportName.of("Francisco de Sá Carneiro Airport").toString());
    }

    @Test
    void equalNamesAreEqual() {
        assertEquals(AirportName.of("Porto Airport"), AirportName.of("Porto Airport"));
    }

    @Test
    void differentNamesAreNotEqual() {
        assertNotEquals(AirportName.of("Porto Airport"), AirportName.of("Lisbon Airport"));
    }

    @Test
    void hashCodeIsConsistent() {
        final AirportName name = AirportName.of("Porto Airport");
        assertEquals(name.hashCode(), name.hashCode());
    }

    @Test
    void compareToReturnsZeroForEqualNames() {
        assertEquals(0, AirportName.of("Porto Airport").compareTo(AirportName.of("Porto Airport")));
    }

    @Test
    void compareToReturnsNonZeroForDifferentNames() {
        assertNotEquals(0, AirportName.of("Porto Airport").compareTo(AirportName.of("Lisbon Airport")));
    }
}