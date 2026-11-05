package aisafe.enginemodel.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EngineModelIDTest {

    @Test
    void ensureEngineModelIDCannotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> new EngineModelID(null));
    }

    @Test
    void ensureEngineModelIDCannotBeEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new EngineModelID(""));
    }

    @Test
    void ensureEngineModelIDIsCreatedWithValidValue() {
        // Arrange
        final var number = "EM001";

        // Act
        final var engineModelID = new EngineModelID(number);

        // Assert
        assertEquals(number, engineModelID.toString());
    }

    @Test
    void ensureTwoEngineModelIDsWithSameValueAreEqual() {
        // Arrange
        final var id1 = EngineModelID.of("EM001");
        final var id2 = EngineModelID.of("EM001");

        // Act + Assert
        assertEquals(id1, id2);
    }

    @Test
    void ensureTwoEngineModelIDsWithDifferentValuesAreNotEqual() {
        // Arrange
        final var id1 = EngineModelID.of("EM001");
        final var id2 = EngineModelID.of("EM002");

        // Act + Assert
        assertNotEquals(id1, id2);
    }

    @Test
    void ensureCompareToWorksCorrectly() {
        // Arrange
        final var id1 = EngineModelID.of("EM001");
        final var id2 = EngineModelID.of("EM002");

        // Act + Assert
        assertTrue(id1.compareTo(id2) < 0);
        assertTrue(id2.compareTo(id1) > 0);
        assertEquals(0, id1.compareTo(EngineModelID.of("EM001")));
    }
}