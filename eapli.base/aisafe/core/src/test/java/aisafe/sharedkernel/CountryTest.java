package aisafe.sharedkernel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CountryTest {

    @Test
    void ensureCountryCannotHaveNullIsoCode() {
        assertThrows(IllegalArgumentException.class, () ->
                new Country(null, "Portugal"));
    }

    @Test
    void ensureCountryCannotHaveNullName() {
        assertThrows(IllegalArgumentException.class, () ->
                new Country(IsoCode.of("PT"), null));
    }

    @Test
    void ensureCountryCannotHaveEmptyName() {
        assertThrows(IllegalArgumentException.class, () ->
                new Country(IsoCode.of("PT"), ""));
    }

    @Test
    void ensureCountryIsCreatedWithValidValues() {
        // Arrange
        final var isoCode = IsoCode.of("PT");
        final var name = "Portugal";

        // Act
        final var country = new Country(isoCode, name);

        // Assert
        assertEquals(isoCode, country.isoCode());
        assertEquals(name, country.name());
    }

    @Test
    void ensureTwoCountriesWithSameIsoCodeAreEqual() {
        // Arrange
        final var country1 = new Country(IsoCode.of("PT"), "Portugal");
        final var country2 = new Country(IsoCode.of("PT"), "Portugal");

        // Act + Assert
        assertEquals(country1, country2);
    }

    @Test
    void ensureTwoCountriesWithDifferentIsoCodesAreNotEqual() {
        // Arrange
        final var country1 = new Country(IsoCode.of("PT"), "Portugal");
        final var country2 = new Country(IsoCode.of("GB"), "United Kingdom");

        // Act + Assert
        assertNotEquals(country1, country2);
    }

    @Test
    void ensureToStringContainsNameAndCode() {
        // Arrange
        final var country = new Country(IsoCode.of("PT"), "Portugal");

        // Act
        final var result = country.toString();

        // Assert
        assertTrue(result.contains("Portugal"));
        assertTrue(result.contains("PT"));
    }
}