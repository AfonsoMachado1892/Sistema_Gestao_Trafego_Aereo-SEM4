package aisafe.aircontrolarea;

import aisafe.aircontrolarea.domain.AirControlAreaDefaults;
import aisafe.aircontrolarea.domain.GeographicBoundary;
import aisafe.sharedkernel.Coordinates;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GeographicBoundaryTest {

    private Coordinates sw() { return Coordinates.of(36.0, -10.0); }
    private Coordinates ne() { return Coordinates.of(42.0, -6.0); }

    private GeographicBoundary validBoundary() {
        return GeographicBoundary.of(sw(), ne());
    }

    @Test
    void validGeographicBoundary() {
        assertDoesNotThrow(this::validBoundary);
    }

    @Test
    void nullSouthwestCornerThrows() {
        assertThrows(Exception.class, () -> GeographicBoundary.of(null, ne()));
    }

    @Test
    void nullNortheastCornerThrows() {
        assertThrows(Exception.class, () -> GeographicBoundary.of(sw(), null));
    }

    @Test
    void swLatitudeHigherThanNeLatitudeThrows() {
        assertThrows(Exception.class, () ->
                GeographicBoundary.of(Coordinates.of(42.0, -10.0), Coordinates.of(36.0, -6.0)));
    }

    @Test
    void swLongitudeHigherThanNeLongitudeThrows() {
        assertThrows(Exception.class, () ->
                GeographicBoundary.of(Coordinates.of(36.0, -6.0), Coordinates.of(42.0, -10.0)));
    }

    @Test
    void equalLatitudesThrows() {
        assertThrows(Exception.class, () ->
                GeographicBoundary.of(Coordinates.of(36.0, -10.0), Coordinates.of(36.0, -6.0)));
    }

    @Test
    void equalLongitudesThrows() {
        assertThrows(Exception.class, () ->
                GeographicBoundary.of(Coordinates.of(36.0, -10.0), Coordinates.of(42.0, -10.0)));
    }

    @Test
    void defaultLowerAltitudeIsZero() {
        assertEquals(AirControlAreaDefaults.DEFAULT_LOWER_ALTITUDE_METERS,
                validBoundary().lowerAltitudeMeters());
    }

    @Test
    void defaultUpperAltitudeIs14000() {
        assertEquals(AirControlAreaDefaults.DEFAULT_UPPER_ALTITUDE_METERS,
                validBoundary().upperAltitudeMeters());
    }

    @Test
    void containsReturnsTrueForCoordinatesInside() {
        assertTrue(validBoundary().contains(Coordinates.of(40.0, -8.0)));
    }

    @Test
    void containsReturnsFalseForCoordinatesOutside() {
        assertFalse(validBoundary().contains(Coordinates.of(51.4706, -0.4619)));
    }

    @Test
    void containsReturnsTrueForCornerCoordinates() {
        assertTrue(validBoundary().contains(sw()));
        assertTrue(validBoundary().contains(ne()));
    }

    @Test
    void containsNullCoordinatesThrows() {
        assertThrows(Exception.class, () -> validBoundary().contains(null));
    }

    @Test
    void overlapsReturnsTrueForOverlappingBoundaries() {
        final GeographicBoundary other = GeographicBoundary.of(
                Coordinates.of(38.0, -9.0),
                Coordinates.of(44.0, -4.0)
        );
        assertTrue(validBoundary().overlaps(other));
    }

    @Test
    void overlapsReturnsFalseForNonOverlappingBoundaries() {
        final GeographicBoundary other = GeographicBoundary.of(
                Coordinates.of(50.0, 0.0),
                Coordinates.of(55.0, 5.0)
        );
        assertFalse(validBoundary().overlaps(other));
    }

    @Test
    void overlapsNullBoundaryThrows() {
        assertThrows(Exception.class, () -> validBoundary().overlaps(null));
    }

    @Test
    void southwestCornerReturnsCorrectValue() {
        assertEquals(sw(), validBoundary().southwestCorner());
    }

    @Test
    void northeastCornerReturnsCorrectValue() {
        assertEquals(ne(), validBoundary().northeastCorner());
    }

    @Test
    void toStringContainsCorners() {
        final String result = validBoundary().toString();
        assertTrue(result.contains("SW"));
        assertTrue(result.contains("NE"));
    }

    @Test
    void equalBoundariesAreEqual() {
        assertEquals(validBoundary(), validBoundary());
    }

    @Test
    void differentBoundariesAreNotEqual() {
        assertNotEquals(validBoundary(),
                GeographicBoundary.of(Coordinates.of(50.0, 0.0), Coordinates.of(55.0, 5.0)));
    }

    @Test
    void hashCodeIsConsistent() {
        final GeographicBoundary boundary = validBoundary();
        assertEquals(boundary.hashCode(), boundary.hashCode());
    }
}