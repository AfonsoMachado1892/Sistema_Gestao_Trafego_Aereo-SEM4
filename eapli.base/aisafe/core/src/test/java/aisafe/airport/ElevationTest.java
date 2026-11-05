package aisafe.airport;

import aisafe.airport.domain.Elevation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ElevationTest {

    @Test
    void validPositiveElevation() {
        assertDoesNotThrow(() -> Elevation.of(69));
    }

    @Test
    void validNegativeElevationWithinBounds() {
        assertDoesNotThrow(() -> Elevation.of(-3));
    }

    @Test
    void zeroElevationIsValid() {
        assertDoesNotThrow(() -> Elevation.of(0));
    }

    @Test
    void elevationAtLowerBoundIsValid() {
        assertDoesNotThrow(() -> Elevation.of(-500));
    }

    @Test
    void elevationAtUpperBoundIsValid() {
        assertDoesNotThrow(() -> Elevation.of(9000));
    }

    @Test
    void elevationBelowMinThrows() {
        assertThrows(Exception.class, () -> Elevation.of(-501));
    }

    @Test
    void elevationAboveMaxThrows() {
        assertThrows(Exception.class, () -> Elevation.of(9001));
    }

    @Test
    void equalElevationsAreEqual() {
        assertEquals(Elevation.of(100), Elevation.of(100));
    }

    @Test
    void differentElevationsAreNotEqual() {
        assertNotEquals(Elevation.of(100), Elevation.of(200));
    }

    @Test
    void toStringContainsValue() {
        assertTrue(Elevation.of(69).toString().contains("69"));
    }

    @Test
    void hashCodeIsConsistent() {
        final Elevation e = Elevation.of(69);
        assertEquals(e.hashCode(), e.hashCode());
    }

    @Test
    void metersAboveTheSeaReturnsCorrectValue() {
        assertEquals(69.0, Elevation.of(69).metersAboveTheSea());
    }

    @Test
    void elevationNotEqualToNull() {
        assertNotEquals(null, Elevation.of(69));
    }

    @Test
    void elevationNotEqualToDifferentType() {
        assertNotEquals("69", Elevation.of(69));
    }
}