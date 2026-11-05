package aisafe.aircraftmodel.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftModelIDTest {

    @Test
    void ensureAircraftModelIDCannotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> new AircraftModelID(null));
    }

    @Test
    void ensureAircraftModelIDCannotBeEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new AircraftModelID(""));
    }

    @Test
    void ensureAircraftModelIDIsCreatedWithValidValue() {
        // Arrange
        final var number = "A320";

        // Act
        final var id = new AircraftModelID(number);

        // Assert
        assertEquals(number, id.toString());
    }

    @Test
    void ensureTwoAircraftModelIDsWithSameValueAreEqual() {
        // Arrange
        final var id1 = AircraftModelID.of("A320");
        final var id2 = AircraftModelID.of("A320");

        // Act + Assert
        assertEquals(id1, id2);
    }

    @Test
    void ensureTwoAircraftModelIDsWithDifferentValuesAreNotEqual() {
        // Arrange
        final var id1 = AircraftModelID.of("A320");
        final var id2 = AircraftModelID.of("B737");

        // Act + Assert
        assertNotEquals(id1, id2);
    }

    @Test
    void ensureCompareToWorksCorrectly() {
        // Arrange
        final var id1 = AircraftModelID.of("A320");
        final var id2 = AircraftModelID.of("B737");

        // Act + Assert
        assertTrue(id1.compareTo(id2) < 0);
        assertTrue(id2.compareTo(id1) > 0);
        assertEquals(0, id1.compareTo(AircraftModelID.of("A320")));
    }
}