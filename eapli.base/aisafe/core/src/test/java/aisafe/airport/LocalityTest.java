package aisafe.airport;

import aisafe.airport.domain.Locality;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LocalityTest {

    private Country portugal() {
        return Country.of(IsoCode.of("PT"), "Portugal");
    }

    @Test
    void validLocality() {
        assertDoesNotThrow(() -> Locality.of("Porto", portugal()));
    }

    @Test
    void emptyCityNameThrows() {
        assertThrows(Exception.class, () -> Locality.of("", portugal()));
    }

    @Test
    void nullCityNameThrows() {
        assertThrows(Exception.class, () -> Locality.of(null, portugal()));
    }

    @Test
    void nullCountryThrows() {
        assertThrows(Exception.class, () -> Locality.of("Porto", null));
    }

    @Test
    void equalLocalitiesAreEqual() {
        assertEquals(
                Locality.of("Porto", portugal()),
                Locality.of("Porto", portugal())
        );
    }

    @Test
    void localityEqualityIsCaseInsensitiveForCity() {
        assertEquals(
                Locality.of("porto", portugal()),
                Locality.of("PORTO", portugal())
        );
    }

    @Test
    void toStringContainsCityAndCountry() {
        final String result = Locality.of("Porto", portugal()).toString();
        assertTrue(result.contains("Porto"));
        assertTrue(result.contains("Portugal"));
    }

    @Test
    void hashCodeIsConsistent() {
        final Locality locality = Locality.of("Porto", portugal());
        assertEquals(locality.hashCode(), locality.hashCode());
    }

    @Test
    void cityNameReturnsCorrectValue() {
        assertEquals("Porto", Locality.of("Porto", portugal()).cityName());
    }

    @Test
    void countryReturnsCorrectValue() {
        assertEquals(portugal(), Locality.of("Porto", portugal()).country());
    }

    @Test
    void differentLocalitiesAreNotEqual() {
        assertNotEquals(
                Locality.of("Porto", portugal()),
                Locality.of("Lisboa", portugal())
        );
    }
}
