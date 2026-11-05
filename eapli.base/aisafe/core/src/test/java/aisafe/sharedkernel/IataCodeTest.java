package aisafe.sharedkernel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IataCodeTest {

    @Test
    void ensureIataCodeCannotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> new IataCode(null));
    }

    @Test
    void ensureIataCodeCannotBeEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new IataCode(""));
    }

    @Test
    void ensureIataCodeCannotHaveMoreThanThreeCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new IataCode("ABCD"));
    }

    @Test
    void ensureIataCodeCannotHaveLessThanTwoCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new IataCode("A"));
    }

    @Test
    void ensureIataCodeIsCreatedWithTwoCharacters() {
        // Arrange + Act
        final var iataCode = IataCode.of("tp");

        // Assert
        assertEquals("TP", iataCode.code());
    }

    @Test
    void ensureIataCodeIsCreatedWithThreeCharacters() {
        // Arrange + Act
        final var iataCode = IataCode.of("lis");

        // Assert
        assertEquals("LIS", iataCode.code());
    }

    @Test
    void ensureIataCodeIsStoredUpperCase() {
        // Arrange + Act
        final var iataCode = IataCode.of("lis");

        // Assert
        assertEquals("LIS", iataCode.toString());
    }

    @Test
    void ensureTwoIataCodesWithSameValueAreEqual() {
        // Arrange
        final var iataCode1 = IataCode.of("LIS");
        final var iataCode2 = IataCode.of("lis");

        // Act + Assert
        assertEquals(iataCode1, iataCode2);
    }

    @Test
    void ensureTwoIataCodesWithDifferentValuesAreNotEqual() {
        // Arrange
        final var iataCode1 = IataCode.of("LIS");
        final var iataCode2 = IataCode.of("OPO");

        // Act + Assert
        assertNotEquals(iataCode1, iataCode2);
    }
}