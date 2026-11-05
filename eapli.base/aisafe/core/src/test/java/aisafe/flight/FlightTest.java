package aisafe.flight;

import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.flight.domain.Flight;
import aisafe.flight.domain.FlightStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class FlightTest {

    @Test
    void aircraftRegistrationReturnsCorrectValue() {
        final AircraftRegistration registration = AircraftRegistration.of("CS-TAP");
        final Flight flight = createFlight(1L, registration, FlightStatus.DRAFT);

        assertEquals(registration, flight.aircraftRegistration());
    }

    @Test
    void statusReturnsCorrectValue() {
        final Flight flight = createFlight(
                1L,
                AircraftRegistration.of("CS-TAP"),
                FlightStatus.PENDING
        );

        assertEquals(FlightStatus.PENDING, flight.status());
    }

    @Test
    void isPendingOrDraftReturnsTrueWhenStatusIsPending() {
        final Flight flight = createFlight(
                1L,
                AircraftRegistration.of("CS-TAP"),
                FlightStatus.PENDING
        );

        assertTrue(flight.isPendingOrDraft());
    }

    @Test
    void isPendingOrDraftReturnsTrueWhenStatusIsDraft() {
        final Flight flight = createFlight(
                1L,
                AircraftRegistration.of("CS-TAP"),
                FlightStatus.DRAFT
        );

        assertTrue(flight.isPendingOrDraft());
    }

    @Test
    void isPendingOrDraftReturnsFalseWhenStatusIsNeitherPendingNorDraft() {
        final Flight flight = createFlightWithFirstNonPendingOrDraftStatus();

        assertFalse(flight.isPendingOrDraft());
    }

    @Test
    void identityReturnsCorrectValue() {
        final Flight flight = createFlight(
                10L,
                AircraftRegistration.of("CS-TAP"),
                FlightStatus.DRAFT
        );

        assertEquals(10L, flight.identity());
    }

    @Test
    void sameAsReturnsTrueForSameIdentity() {
        final Flight first = createFlight(
                1L,
                AircraftRegistration.of("CS-TAP"),
                FlightStatus.DRAFT
        );

        final Flight second = createFlight(
                1L,
                AircraftRegistration.of("CS-ABC"),
                FlightStatus.PENDING
        );

        assertTrue(first.sameAs(second));
    }

    @Test
    void sameAsReturnsFalseForDifferentIdentity() {
        final Flight first = createFlight(
                1L,
                AircraftRegistration.of("CS-TAP"),
                FlightStatus.DRAFT
        );

        final Flight second = createFlight(
                2L,
                AircraftRegistration.of("CS-TAP"),
                FlightStatus.DRAFT
        );

        assertFalse(first.sameAs(second));
    }

    @Test
    void sameAsReturnsFalseWhenIdentityIsNull() {
        final Flight first = createFlight(
                null,
                AircraftRegistration.of("CS-TAP"),
                FlightStatus.DRAFT
        );

        final Flight second = createFlight(
                null,
                AircraftRegistration.of("CS-TAP"),
                FlightStatus.DRAFT
        );

        assertFalse(first.sameAs(second));
    }

    @Test
    void sameAsReturnsFalseForDifferentObjectType() {
        final Flight flight = createFlight(
                1L,
                AircraftRegistration.of("CS-TAP"),
                FlightStatus.DRAFT
        );

        assertFalse(flight.sameAs("not a flight"));
    }

    @Test
    void sameAsReturnsFalseForNull() {
        final Flight flight = createFlight(
                1L,
                AircraftRegistration.of("CS-TAP"),
                FlightStatus.DRAFT
        );

        assertFalse(flight.sameAs(null));
    }

    private Flight createFlight(final Long id,
                                final AircraftRegistration aircraftRegistration,
                                final FlightStatus status) {
        try {
            final Constructor<Flight> constructor = Flight.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            final Flight flight = constructor.newInstance();

            setField(flight, "id", id);
            setField(flight, "aircraftRegistration", aircraftRegistration);
            setField(flight, "status", status);

            return flight;
        } catch (final Exception e) {
            throw new RuntimeException("Could not create Flight for test.", e);
        }
    }

    private Flight createFlightWithFirstNonPendingOrDraftStatus() {
        for (final FlightStatus status : FlightStatus.values()) {
            if (status != FlightStatus.PENDING && status != FlightStatus.DRAFT) {
                return createFlight(
                        1L,
                        AircraftRegistration.of("CS-TAP"),
                        status
                );
            }
        }

        return createFlight(
                1L,
                AircraftRegistration.of("CS-TAP"),
                null
        );
    }

    private void setField(final Object target, final String fieldName, final Object value) {
        try {
            final Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (final Exception e) {
            throw new RuntimeException("Could not set field " + fieldName + " for test.", e);
        }
    }
}