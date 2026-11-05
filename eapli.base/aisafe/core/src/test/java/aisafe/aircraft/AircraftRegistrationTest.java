package aisafe.aircraft;

import aisafe.aircraft.domain.AircraftRegistration;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AircraftRegistrationTest {

    @Test
    void validAircraftRegistration() {
        assertDoesNotThrow(() -> AircraftRegistration.of("CS-TTV"));
    }

    @Test
    void emptyRegistrationThrows() {
        assertThrows(Exception.class, () -> AircraftRegistration.of(""));
    }

    @Test
    void nullRegistrationThrows() {
        assertThrows(Exception.class, () -> AircraftRegistration.of(null));
    }

    @Test
    void codeReturnsCorrectValue() {
        assertEquals("CS-TTV", AircraftRegistration.of("CS-TTV").code());
    }

    @Test
    void equalRegistrationsAreEqual() {
        assertEquals(AircraftRegistration.of("CS-TTV"), AircraftRegistration.of("CS-TTV"));
    }

    @Test
    void differentRegistrationsAreNotEqual() {
        assertNotEquals(AircraftRegistration.of("CS-TTV"), AircraftRegistration.of("CS-TTX"));
    }

    @Test
    void hashCodeIsConsistent() {
        final AircraftRegistration reg = AircraftRegistration.of("CS-TTV");
        assertEquals(reg.hashCode(), reg.hashCode());
    }

    @Test
    void compareToReturnsZeroForEqualRegistrations() {
        assertEquals(0, AircraftRegistration.of("CS-TTV").compareTo(AircraftRegistration.of("CS-TTV")));
    }

    @Test
    void compareToReturnsNonZeroForDifferentRegistrations() {
        assertNotEquals(0, AircraftRegistration.of("CS-TTV").compareTo(AircraftRegistration.of("CS-TTX")));
    }
}