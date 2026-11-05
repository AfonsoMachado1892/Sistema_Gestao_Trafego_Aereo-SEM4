package aisafe.airport;

import aisafe.aircontrolarea.domain.*;
import aisafe.airport.domain.*;
import aisafe.sharedkernel.Coordinates;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AirportTest {

    private AirportIataCode iata() { return AirportIataCode.of("OPO"); }
    private AirportIcaoCode icao() { return AirportIcaoCode.of("LPPR"); }
    private AirportName name() { return AirportName.of("Francisco de Sá Carneiro Airport"); }
    private Locality locality() {
        return Locality.of("Porto", Country.of(IsoCode.of("PT"), "Portugal"));
    }
    private Coordinates coordinates() { return Coordinates.of(41.2481, -8.6813); }
    private Elevation elevation() { return Elevation.of(69); }
    private AirControlArea aca() {
        return new AirControlArea(
                AirControlAreaCode.of("LPPC"),
                GeographicBoundary.of(
                        Coordinates.of(36.0, -10.0),
                        Coordinates.of(42.0, -6.0)
                ),
                AirControlAreaName.of("Lisboa")
        );
    }

    private Airport validAirport() {
        return new Airport(iata(), icao(), locality(), coordinates(), name(), elevation(), aca());
    }

    @Test
    void validAirportCreation() {
        assertDoesNotThrow(this::validAirport);
    }

    @Test
    void nullIataCodeThrows() {
        assertThrows(Exception.class, () ->
                new Airport(null, icao(), locality(), coordinates(), name(), elevation(), aca()));
    }

    @Test
    void nullIcaoCodeThrows() {
        assertThrows(Exception.class, () ->
                new Airport(iata(), null, locality(), coordinates(), name(), elevation(), aca()));
    }

    @Test
    void nullLocalityThrows() {
        assertThrows(Exception.class, () ->
                new Airport(iata(), icao(), null, coordinates(), name(), elevation(), aca()));
    }

    @Test
    void nullCoordinatesThrows() {
        assertThrows(Exception.class, () ->
                new Airport(iata(), icao(), locality(), null, name(), elevation(), aca()));
    }

    @Test
    void nullNameThrows() {
        assertThrows(Exception.class, () ->
                new Airport(iata(), icao(), locality(), coordinates(), null, elevation(), aca()));
    }

    @Test
    void nullElevationThrows() {
        assertThrows(Exception.class, () ->
                new Airport(iata(), icao(), locality(), coordinates(), name(), null, aca()));
    }

    @Test
    void nullAirControlAreaThrows() {
        assertThrows(Exception.class, () ->
                new Airport(iata(), icao(), locality(), coordinates(), name(), elevation(), null));
    }

    @Test
    void identityReturnsIataCode() {
        assertEquals(iata(), validAirport().identity());
    }

    @Test
    void sameAirportIsEqual() {
        final Airport a = validAirport();
        assertTrue(a.sameAs(a));
    }

    @Test
    void airportsWithSameIataAreEqual() {
        assertEquals(validAirport(), validAirport());
    }

    @Test
    void airportsWithDifferentIataAreNotEqual() {
        final Airport a1 = new Airport(
                AirportIataCode.of("OPO"), icao(), locality(), coordinates(), name(), elevation(), aca());
        final Airport a2 = new Airport(
                AirportIataCode.of("LIS"), AirportIcaoCode.of("LPPT"), locality(), coordinates(), name(), elevation(), aca());

        assertNotEquals(a1, a2);
    }

    @Test
    void toStringContainsIataAndName() {
        final String result = validAirport().toString();
        assertTrue(result.contains("OPO"));
        assertTrue(result.contains("Francisco de Sá Carneiro Airport"));
    }

    @Test
    void hashCodeIsConsistent() {
        final Airport a = validAirport();
        assertEquals(a.hashCode(), a.hashCode());
    }

    @Test
    void airControlAreaIsCorrect() {
        assertEquals(aca(), validAirport().airControlArea());
    }

    @Test
    void localityIsCorrect() {
        assertEquals(locality(), validAirport().locality());
    }

    @Test
    void coordinatesAreCorrect() {
        assertEquals(coordinates(), validAirport().coordinates());
    }

    @Test
    void elevationIsCorrect() {
        assertEquals(elevation(), validAirport().elevation());
    }

    @Test
    void nameIsCorrect() {
        assertEquals(name(), validAirport().name());
    }

    @Test
    void icaoCodeIsCorrect() {
        assertEquals(icao(), validAirport().icaoCode());
    }

    @Test
    void sameAsReturnsFalseForDifferentAirport() {
        final Airport a1 = validAirport();
        final Airport a2 = new Airport(
                AirportIataCode.of("LIS"),
                AirportIcaoCode.of("LPPT"),
                locality(), coordinates(), name(), elevation(), aca());
        assertFalse(a1.sameAs(a2));
    }

    @Test
    void sameAsReturnsFalseForNonAirport() {
        assertFalse(validAirport().sameAs("not an airport"));
    }

    @Test
    void ofFactoryMethodCreatesAirport() {
        assertDoesNotThrow(() -> Airport.of(
                iata(), icao(), locality(), coordinates(), name(), elevation(), aca()));
    }
}