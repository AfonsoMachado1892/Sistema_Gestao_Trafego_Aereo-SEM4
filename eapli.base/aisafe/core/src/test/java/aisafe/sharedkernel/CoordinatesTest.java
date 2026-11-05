package aisafe.sharedkernel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoordinatesTest {

    @Test
    void ensureLatitudeCannotBeAbove90() {
        assertThrows(IllegalArgumentException.class, () ->
                new Coordinates(91, 0));
    }

    @Test
    void ensureLatitudeCannotBeBelow90Negative() {
        assertThrows(IllegalArgumentException.class, () ->
                new Coordinates(-91, 0));
    }

    @Test
    void ensureLongitudeCannotBeAbove180() {
        assertThrows(IllegalArgumentException.class, () ->
                new Coordinates(0, 181));
    }

    @Test
    void ensureLongitudeCannotBeBelow180Negative() {
        assertThrows(IllegalArgumentException.class, () ->
                new Coordinates(0, -181));
    }

    @Test
    void ensureCoordinatesAreCreatedWithValidValues() {
        // Arrange
        final var latitude = 38.7;
        final var longitude = -9.1;

        // Act
        final var coordinates = new Coordinates(latitude, longitude);

        // Assert
        assertEquals(latitude, coordinates.latitude());
        assertEquals(longitude, coordinates.longitude());
    }

    @Test
    void ensureBoundaryValuesAreValid() {
        // Arrange + Act + Assert
        assertDoesNotThrow(() -> new Coordinates(90, 180));
        assertDoesNotThrow(() -> new Coordinates(-90, -180));
        assertDoesNotThrow(() -> new Coordinates(0, 0));
    }

    @Test
    void ensureTwoCoordinatesWithSameValuesAreEqual() {
        // Arrange
        final var coord1 = new Coordinates(38.7, -9.1);
        final var coord2 = new Coordinates(38.7, -9.1);

        // Act + Assert
        assertEquals(coord1, coord2);
    }

    @Test
    void ensureTwoCoordinatesWithDifferentValuesAreNotEqual() {
        // Arrange
        final var coord1 = new Coordinates(38.7, -9.1);
        final var coord2 = new Coordinates(41.1, -8.6);

        // Act + Assert
        assertNotEquals(coord1, coord2);
    }
}