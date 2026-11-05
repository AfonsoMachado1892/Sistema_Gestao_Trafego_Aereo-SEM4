package aisafe.sharedkernel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IsoCodeTest {

    @Test
    void ensureIsoCodeCannotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> new IsoCode(null));
    }

    @Test
    void ensureIsoCodeCannotBeEmpty() {
        assertThrows(IllegalArgumentException.class, () -> new IsoCode(""));
    }

    @Test
    void ensureIsoCodeCannotHaveMoreThanTwoCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new IsoCode("PRT"));
    }

    @Test
    void ensureIsoCodeCannotHaveLessThanTwoCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new IsoCode("P"));
    }

    @Test
    void ensureIsoCodeIsCreatedWithValidValue() {
        // Arrange
        final var isoCode = new IsoCode("pt");

        // Act + Assert
        assertEquals("PT", isoCode.code());
    }

    @Test
    void ensureIsoCodeIsStoredUpperCase() {
        // Arrange
        final var isoCode = IsoCode.of("pt");

        // Act + Assert
        assertEquals("PT", isoCode.toString());
    }

    @Test
    void ensureTwoIsoCodesWithSameValueAreEqual() {
        // Arrange
        final var isoCode1 = IsoCode.of("PT");
        final var isoCode2 = IsoCode.of("pt");

        // Act + Assert
        assertEquals(isoCode1, isoCode2);
    }

    @Test
    void ensureTwoIsoCodesWithDifferentValuesAreNotEqual() {
        // Arrange
        final var isoCode1 = IsoCode.of("PT");
        final var isoCode2 = IsoCode.of("GB");

        // Act + Assert
        assertNotEquals(isoCode1, isoCode2);
    }
}