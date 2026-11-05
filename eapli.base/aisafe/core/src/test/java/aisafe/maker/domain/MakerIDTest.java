package aisafe.maker.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MakerIDTest {

    @Test
    void ensureMakerIDCannotBeNull() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () -> new MakerID(null));
    }

    @Test
    void ensureMakerIDCannotBeEmpty() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () -> new MakerID(""));
    }

    @Test
    void ensureMakerIDIsCreatedWithValidValue() {
        // Arrange
        final var number = "MAKER001";

        // Act
        final var makerID = new MakerID(number);

        // Assert
        assertEquals(number, makerID.toString());
    }

    @Test
    void ensureTwoMakerIDsWithSameValueAreEqual() {
        // Arrange
        final var makerID1 = MakerID.of("MAKER001");
        final var makerID2 = MakerID.of("MAKER001");

        // Act + Assert
        assertEquals(makerID1, makerID2);
    }

    @Test
    void ensureTwoMakerIDsWithDifferentValuesAreNotEqual() {
        // Arrange
        final var makerID1 = MakerID.of("MAKER001");
        final var makerID2 = MakerID.of("MAKER002");

        // Act + Assert
        assertNotEquals(makerID1, makerID2);
    }

    @Test
    void ensureCompareToWorksCorrectly() {
        // Arrange
        final var makerID1 = MakerID.of("MAKER001");
        final var makerID2 = MakerID.of("MAKER002");

        // Act + Assert
        assertTrue(makerID1.compareTo(makerID2) < 0);
        assertTrue(makerID2.compareTo(makerID1) > 0);
        assertEquals(0, makerID1.compareTo(MakerID.of("MAKER001")));
    }
}