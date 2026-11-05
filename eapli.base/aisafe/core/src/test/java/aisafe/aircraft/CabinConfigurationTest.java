package aisafe.aircraft;

import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.aircraft.domain.AircraftRepository;
import aisafe.aircraft.domain.CabinConfiguration;
import aisafe.aircraft.domain.OperationalStatus;
import aisafe.aircraft.application.AircraftService;
import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.aircraftmodel.domain.AircraftModelID;
import aisafe.aircraftmodel.domain.AircraftType;
import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.enginemodel.domain.EngineModelID;
import aisafe.maker.domain.MakerID;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CabinConfigurationTest {

    @Test
    void validCabinConfiguration() {
        assertDoesNotThrow(() -> CabinConfiguration.of(150, 20, 10));
    }

    @Test
    void cabinWithOnlyEconomySeatsIsValid() {
        assertDoesNotThrow(() -> CabinConfiguration.of(150, 0, 0));
    }

    @Test
    void cabinWithOnlyBusinessSeatsIsValid() {
        assertDoesNotThrow(() -> CabinConfiguration.of(0, 20, 0));
    }

    @Test
    void cabinWithOnlyFirstSeatsIsValid() {
        assertDoesNotThrow(() -> CabinConfiguration.of(0, 0, 10));
    }

    @Test
    void allZeroSeatsThrows() {
        assertThrows(Exception.class, () -> CabinConfiguration.of(0, 0, 0));
    }

    @Test
    void negativeEconomySeatsThrows() {
        assertThrows(Exception.class, () -> CabinConfiguration.of(-1, 0, 0));
    }

    @Test
    void negativeBusinessSeatsThrows() {
        assertThrows(Exception.class, () -> CabinConfiguration.of(0, -1, 0));
    }

    @Test
    void negativeFirstSeatsThrows() {
        assertThrows(Exception.class, () -> CabinConfiguration.of(0, 0, -1));
    }

    @Test
    void totalSeatsReturnsCorrectSum() {
        assertEquals(180, CabinConfiguration.of(150, 20, 10).totalSeats());
    }

    @Test
    void exceedsCapacityReturnsTrueWhenOver() {
        assertTrue(CabinConfiguration.of(150, 20, 10).exceedsCapacity(100));
    }

    @Test
    void exceedsCapacityReturnsFalseWhenUnder() {
        assertFalse(CabinConfiguration.of(150, 20, 10).exceedsCapacity(200));
    }

    @Test
    void exceedsCapacityReturnsFalseWhenEqual() {
        assertFalse(CabinConfiguration.of(150, 20, 10).exceedsCapacity(180));
    }

    @Test
    void toStringContainsAllClasses() {
        final String result = CabinConfiguration.of(150, 20, 10).toString();
        assertTrue(result.contains("150"));
        assertTrue(result.contains("20"));
        assertTrue(result.contains("10"));
    }

    @Test
    void equalConfigurationsAreEqual() {
        assertEquals(CabinConfiguration.of(150, 20, 10), CabinConfiguration.of(150, 20, 10));
    }

    @Test
    void differentConfigurationsAreNotEqual() {
        assertNotEquals(CabinConfiguration.of(150, 20, 10), CabinConfiguration.of(100, 20, 10));
    }

    @Test
    void hashCodeIsConsistent() {
        final CabinConfiguration config = CabinConfiguration.of(150, 20, 10);
        assertEquals(config.hashCode(), config.hashCode());
    }

    @Test
    void economySeatsReturnsCorrectValue() {
        assertEquals(150, CabinConfiguration.of(150, 20, 10).economySeats());
    }

    @Test
    void businessSeatsReturnsCorrectValue() {
        assertEquals(20, CabinConfiguration.of(150, 20, 10).businessSeats());
    }

    @Test
    void firstSeatsReturnsCorrectValue() {
        assertEquals(10, CabinConfiguration.of(150, 20, 10).firstSeats());
    }
}