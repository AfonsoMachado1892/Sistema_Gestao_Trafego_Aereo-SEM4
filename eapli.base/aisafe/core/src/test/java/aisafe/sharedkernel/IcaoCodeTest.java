package aisafe.sharedkernel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IcaoCodeTest {

    @Test
    void ensureIcaoCodeCannotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> new IcaoCode(null));
    }

    @Test
    void ensureIcaoCodeCannotBeEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new IcaoCode(""));
    }

    @Test
    void ensureIcaoCodeCannotHaveMoreThanFourCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new IcaoCode("ABCDE"));
    }

    @Test
    void ensureIcaoCodeCannotHaveLessThanTwoCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new IcaoCode("A"));
    }

    @Test
    void ensureIcaoCodeIsCreatedWithTwoCharacters() {
        // Arrange + Act
        final var icaoCode = IcaoCode.of("tp");

        // Assert
        assertEquals("TP", icaoCode.code());
    }

    @Test
    void ensureIcaoCodeIsCreatedWithFourCharacters() {
        // Arrange + Act
        final var icaoCode = IcaoCode.of("lppt");

        // Assert
        assertEquals("LPPT", icaoCode.code());
    }

    @Test
    void ensureIcaoCodeIsStoredUpperCase() {
        // Arrange + Act
        final var icaoCode = IcaoCode.of("lppt");

        // Assert
        assertEquals("LPPT", icaoCode.toString());
    }

    @Test
    void ensureTwoIcaoCodesWithSameValueAreEqual() {
        // Arrange
        final var icaoCode1 = IcaoCode.of("LPPT");
        final var icaoCode2 = IcaoCode.of("lppt");

        // Act + Assert
        assertEquals(icaoCode1, icaoCode2);
    }

    @Test
    void ensureTwoIcaoCodesWithDifferentValuesAreNotEqual() {
        // Arrange
        final var icaoCode1 = IcaoCode.of("LPPT");
        final var icaoCode2 = IcaoCode.of("LPPR");

        // Act + Assert
        assertNotEquals(icaoCode1, icaoCode2);
    }
}