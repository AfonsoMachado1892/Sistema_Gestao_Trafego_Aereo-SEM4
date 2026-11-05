package aisafe.flightplan.domain;

import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.flightroute.domain.*;
import aisafe.pilot.domain.Pilot;
import aisafe.pilot.domain.PilotID;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightPlanTest {

    private static final FlightPlanID FLIGHT_PLAN_ID =
            FlightPlanID.of("FP-001");

    private static final AirTransportCompanyCode COMPANY_CODE =
            AirTransportCompanyCode.of("TP");

    private static final RouteCode ROUTE_CODE =
            RouteCode.of("TP101");

    private static final AircraftRegistration AIRCRAFT_REGISTRATION =
            AircraftRegistration.of("CS-TAB");

    private static final PilotID PILOT_ID =
            PilotID.of("PILOT-001");

    private static final LocalDateTime DEPARTURE =
            LocalDateTime.of(2026, 6, 15, 9, 0);

    private static final LocalDateTime ARRIVAL =
            LocalDateTime.of(2026, 6, 15, 11, 0);

    private static final FuelQuantity FUEL =
            FuelQuantity.of(12000, FuelUnit.KG);

    private static final FlightPlanDescription DESCRIPTION =
            FlightPlanDescription.of("RAW FLIGHT PLAN DESCRIPTION");

    // ── Fake domain objects ──────────────────────────────────────────────────

    static class FakePilot extends Pilot {

        private final PilotID pilotID;
        private final AirTransportCompanyCode companyCode;
        private final boolean active;

        FakePilot(final PilotID pilotID,
                  final AirTransportCompanyCode companyCode,
                  final boolean active) {
            this.pilotID = pilotID;
            this.companyCode = companyCode;
            this.active = active;
        }

        @Override
        public PilotID identity() {
            return pilotID;
        }

        @Override
        public PilotID pilotID() {
            return pilotID;
        }

        @Override
        public boolean isActive() {
            return active;
        }

        @Override
        public AirTransportCompanyCode currentCompanyIataCode() {
            return companyCode;
        }

        @Override
        public boolean belongsTo(final AirTransportCompanyCode companyCode) {
            return this.companyCode.equals(companyCode);
        }
    }

    static class FakeAircraft extends Aircraft {

        private final AircraftRegistration registration;
        private final boolean operational;
        private final AirTransportCompanyCode companyCode;

        FakeAircraft(final AircraftRegistration registration,
                     final boolean operational,
                     final AirTransportCompanyCode companyCode) {
            this.registration = registration;
            this.operational = operational;
            this.companyCode = companyCode;
        }

        @Override
        public AircraftRegistration identity() {
            return registration;
        }

        @Override
        public AircraftRegistration aircraftCode() {
            return registration;
        }

        @Override
        public boolean isOperational() {
            return operational;
        }

        @Override
        public boolean belongsTo(final AirTransportCompanyCode companyCode) {
            return this.companyCode.equals(companyCode);
        }
    }

    // ── Fixtures ─────────────────────────────────────────────────────────────

    private RegularSchedule regularSchedule() {
        return RegularSchedule.of(List.of(
                ScheduleEntry.of(DayOfWeek.MONDAY, LocalTime.of(9, 0))
        ));
    }

    private FlightRoute validRoute() {
        return new FlightRouteFactory()
                .withRouteCode(ROUTE_CODE)
                .withDepartureAirport(aisafe.airport.domain.AirportIataCode.of("LIS"))
                .withArrivalAirport(aisafe.airport.domain.AirportIataCode.of("OPO"))
                .withCompany(COMPANY_CODE)
                .asRegular(regularSchedule())
                .build();
    }

    private Aircraft validAircraft() {
        return new FakeAircraft(AIRCRAFT_REGISTRATION, true, COMPANY_CODE);
    }

    private Pilot validPilot() {
        return new FakePilot(PILOT_ID, COMPANY_CODE, true);
    }

    private FlightPlan validFlightPlan() {
        return FlightPlan.draft(
                FLIGHT_PLAN_ID,
                validRoute(),
                validAircraft(),
                validPilot(),
                DEPARTURE,
                ARRIVAL,
                FUEL,
                DESCRIPTION
        );
    }

    // ── Creation ─────────────────────────────────────────────────────────────

    @Test
    void ensureFlightPlanIsCreatedAsDraft() {
        final FlightPlan flightPlan = validFlightPlan();

        assertEquals(FlightPlanStatus.DRAFT, flightPlan.status());
        assertTrue(flightPlan.isDraft());
        assertFalse(flightPlan.isValidated());
    }

    @Test
    void ensureIdentityIsStored() {
        final FlightPlan flightPlan = validFlightPlan();

        assertEquals(FLIGHT_PLAN_ID, flightPlan.identity());
    }

    @Test
    void ensureRouteCodeIsStored() {
        final FlightPlan flightPlan = validFlightPlan();

        assertEquals(ROUTE_CODE, flightPlan.routeCode());
    }

    @Test
    void ensureAircraftRegistrationIsStored() {
        final FlightPlan flightPlan = validFlightPlan();

        assertEquals(AIRCRAFT_REGISTRATION, flightPlan.aircraftRegistration());
    }

    @Test
    void ensurePilotIDIsStored() {
        final FlightPlan flightPlan = validFlightPlan();

        assertEquals(PILOT_ID, flightPlan.pilotID());
    }

    @Test
    void ensurePlannedDepartureDateTimeIsStored() {
        final FlightPlan flightPlan = validFlightPlan();

        assertEquals(DEPARTURE, flightPlan.plannedDepartureDateTime());
    }

    @Test
    void ensurePlannedArrivalDateTimeIsStored() {
        final FlightPlan flightPlan = validFlightPlan();

        assertEquals(ARRIVAL, flightPlan.plannedArrivalDateTime());
    }

    @Test
    void ensureFuelQuantityIsStored() {
        final FlightPlan flightPlan = validFlightPlan();

        assertEquals(FUEL, flightPlan.fuelQuantity());
    }

    @Test
    void ensureDescriptionIsStored() {
        final FlightPlan flightPlan = validFlightPlan();

        assertEquals(DESCRIPTION, flightPlan.description());
    }

    @Test
    void ensureCreatedAtIsSet() {
        final FlightPlan flightPlan = validFlightPlan();

        assertNotNull(flightPlan.createdAt());
    }

    // ── Planned time invariants ──────────────────────────────────────────────

    @Test
    void ensureArrivalAfterDepartureIsAccepted() {
        assertDoesNotThrow(this::validFlightPlan);
    }

    @Test
    void ensureArrivalEqualToDepartureIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                FlightPlan.draft(
                        FLIGHT_PLAN_ID,
                        validRoute(),
                        validAircraft(),
                        validPilot(),
                        DEPARTURE,
                        DEPARTURE,
                        FUEL,
                        DESCRIPTION
                )
        );
    }

    @Test
    void ensureArrivalBeforeDepartureIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                FlightPlan.draft(
                        FLIGHT_PLAN_ID,
                        validRoute(),
                        validAircraft(),
                        validPilot(),
                        DEPARTURE,
                        DEPARTURE.minusHours(1),
                        FUEL,
                        DESCRIPTION
                )
        );
    }

    // ── Null guards ──────────────────────────────────────────────────────────

    @Test
    void ensureNullFlightPlanIDIsRejected() {
        assertThrows(Exception.class, () ->
                FlightPlan.draft(
                        null,
                        validRoute(),
                        validAircraft(),
                        validPilot(),
                        DEPARTURE,
                        ARRIVAL,
                        FUEL,
                        DESCRIPTION
                )
        );
    }

    @Test
    void ensureNullRouteIsRejected() {
        assertThrows(Exception.class, () ->
                FlightPlan.draft(
                        FLIGHT_PLAN_ID,
                        null,
                        validAircraft(),
                        validPilot(),
                        DEPARTURE,
                        ARRIVAL,
                        FUEL,
                        DESCRIPTION
                )
        );
    }

    @Test
    void ensureNullAircraftIsRejected() {
        assertThrows(Exception.class, () ->
                FlightPlan.draft(
                        FLIGHT_PLAN_ID,
                        validRoute(),
                        null,
                        validPilot(),
                        DEPARTURE,
                        ARRIVAL,
                        FUEL,
                        DESCRIPTION
                )
        );
    }

    @Test
    void ensureNullPilotIsRejected() {
        assertThrows(Exception.class, () ->
                FlightPlan.draft(
                        FLIGHT_PLAN_ID,
                        validRoute(),
                        validAircraft(),
                        null,
                        DEPARTURE,
                        ARRIVAL,
                        FUEL,
                        DESCRIPTION
                )
        );
    }

    @Test
    void ensureNullPlannedDepartureDateTimeIsRejected() {
        assertThrows(Exception.class, () ->
                FlightPlan.draft(
                        FLIGHT_PLAN_ID,
                        validRoute(),
                        validAircraft(),
                        validPilot(),
                        null,
                        ARRIVAL,
                        FUEL,
                        DESCRIPTION
                )
        );
    }

    @Test
    void ensureNullPlannedArrivalDateTimeIsRejected() {
        assertThrows(Exception.class, () ->
                FlightPlan.draft(
                        FLIGHT_PLAN_ID,
                        validRoute(),
                        validAircraft(),
                        validPilot(),
                        DEPARTURE,
                        null,
                        FUEL,
                        DESCRIPTION
                )
        );
    }

    @Test
    void ensureNullFuelQuantityIsRejected() {
        assertThrows(Exception.class, () ->
                FlightPlan.draft(
                        FLIGHT_PLAN_ID,
                        validRoute(),
                        validAircraft(),
                        validPilot(),
                        DEPARTURE,
                        ARRIVAL,
                        null,
                        DESCRIPTION
                )
        );
    }

    @Test
    void ensureNullDescriptionIsRejected() {
        assertThrows(Exception.class, () ->
                FlightPlan.draft(
                        FLIGHT_PLAN_ID,
                        validRoute(),
                        validAircraft(),
                        validPilot(),
                        DEPARTURE,
                        ARRIVAL,
                        FUEL,
                        null
                )
        );
    }

    // ── Status transitions ───────────────────────────────────────────────────

    @Test
    void ensureDraftFlightPlanCanBeMarkedAsTested() {
        final FlightPlan flightPlan = validFlightPlan();

        flightPlan.markTested();

        assertEquals(FlightPlanStatus.TESTED, flightPlan.status());
    }

    @Test
    void ensureOnlyDraftFlightPlanCanBeMarkedAsTested() {
        final FlightPlan flightPlan = validFlightPlan();

        flightPlan.markTested();

        assertThrows(IllegalArgumentException.class, flightPlan::markTested);
    }

    @Test
    void ensureTestedFlightPlanCanBeValidated() {
        final FlightPlan flightPlan = validFlightPlan();

        flightPlan.markTested();
        flightPlan.markValidated();

        assertEquals(FlightPlanStatus.VALIDATED, flightPlan.status());
        assertTrue(flightPlan.isValidated());
    }

    @Test
    void ensureDraftFlightPlanCannotBeValidatedDirectly() {
        final FlightPlan flightPlan = validFlightPlan();

        assertThrows(IllegalArgumentException.class, flightPlan::markValidated);
    }

    @Test
    void ensureFlightPlanCanBeRejected() {
        final FlightPlan flightPlan = validFlightPlan();

        flightPlan.reject();

        assertEquals(FlightPlanStatus.REJECTED, flightPlan.status());
    }

    @Test
    void ensureFlightPlanCanBeCancelled() {
        final FlightPlan flightPlan = validFlightPlan();

        flightPlan.cancel();

        assertEquals(FlightPlanStatus.CANCELLED, flightPlan.status());
    }

    @Test
    void ensureCancelledFlightPlanCannotBeCancelledAgain() {
        final FlightPlan flightPlan = validFlightPlan();

        flightPlan.cancel();

        assertThrows(IllegalArgumentException.class, flightPlan::cancel);
    }

    @Test
    void ensureCancelledFlightPlanCannotBeRejected() {
        final FlightPlan flightPlan = validFlightPlan();

        flightPlan.cancel();

        assertThrows(IllegalArgumentException.class, flightPlan::reject);
    }

    // ── Identity / sameAs ────────────────────────────────────────────────────

    @Test
    void ensureSameAsReturnsTrueForSameIdentity() {
        final FlightPlan a = validFlightPlan();

        final FlightPlan b = FlightPlan.draft(
                FLIGHT_PLAN_ID,
                validRoute(),
                validAircraft(),
                validPilot(),
                DEPARTURE,
                ARRIVAL,
                FuelQuantity.of(12000, FuelUnit.KG),
                FlightPlanDescription.of("OTHER DESCRIPTION")
        );

        assertTrue(a.sameAs(b));
    }

    @Test
    void ensureSameAsReturnsFalseForDifferentIdentity() {
        final FlightPlan a = validFlightPlan();

        final FlightPlan b = FlightPlan.draft(
                FlightPlanID.of("FP-002"),
                validRoute(),
                validAircraft(),
                validPilot(),
                DEPARTURE,
                ARRIVAL,
                FUEL,
                DESCRIPTION
        );

        assertFalse(a.sameAs(b));
    }

    @Test
    void ensureSameAsReturnsFalseForNonFlightPlanObject() {
        assertFalse(validFlightPlan().sameAs("not a flight plan"));
    }

    @Test
    void ensureToStringContainsFlightPlanID() {
        assertTrue(validFlightPlan().toString().contains("FP-001"));
    }

    @Test
    void ensureToStringContainsRouteCode() {
        assertTrue(validFlightPlan().toString().contains("TP101"));
    }
}