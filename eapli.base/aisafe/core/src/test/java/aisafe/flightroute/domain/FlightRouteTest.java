package aisafe.flightroute.domain;

import aisafe.airport.domain.AirportIataCode;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FlightRoute}.
 * Uses the AAA convention (Arrange, Act, Assert).
 */
class FlightRouteTest {

    // ── Shared fixtures ──────────────────────────────────────────────────────

    private static final AirportIataCode LIS = AirportIataCode.of("LIS");
    private static final AirportIataCode OPO = AirportIataCode.of("OPO");
    private static final AirTransportCompanyCode TP = AirTransportCompanyCode.of("TP");

    private RegularSchedule regularSchedule() {
        return RegularSchedule.of(List.of(
                ScheduleEntry.of(DayOfWeek.MONDAY, LocalTime.of(9, 0))
        ));
    }

    private CharterSchedule charterSchedule() {
        return CharterSchedule.of(
                LocalDateTime.of(2026, 6, 15, 9, 0),
                LocalDateTime.of(2026, 6, 15, 11, 0)
        );
    }

    private FlightRoute validRoute() {
        return new FlightRouteFactory()
                .withRouteCode(RouteCode.of("TP101"))
                .withDepartureAirport(LIS)
                .withArrivalAirport(OPO)
                .withCompany(TP)
                .asRegular(regularSchedule())
                .build();
    }

    // ── Valid construction ───────────────────────────────────────────────────

    @Test
    void ensureRouteIsCreatedWithActiveStatus() {
        final FlightRoute route = validRoute();

        assertEquals(RouteStatus.ACTIVE, route.routeStatus());
        assertTrue(route.isActive());
    }

    @Test
    void ensureRouteCodeIsStored() {
        assertEquals("TP101", validRoute().routeCode().value());
    }

    @Test
    void ensureDepartureAirportIsStored() {
        assertEquals(LIS, validRoute().departureAirportIataCode());
    }

    @Test
    void ensureArrivalAirportIsStored() {
        assertEquals(OPO, validRoute().arrivalAirportIataCode());
    }

    @Test
    void ensureCompanyIataCodeIsStored() {
        assertEquals(TP, validRoute().airTransportCompanyIataCode());
    }

    @Test
    void ensureIdentityReturnsRouteCode() {
        assertEquals(RouteCode.of("TP101"), validRoute().identity());
    }

    @Test
    void ensureRegularRouteStoresFlightType() {
        final FlightRoute route = validRoute();

        assertEquals(FlightType.REGULAR, route.flightType());
        assertTrue(route.isRegular());
        assertFalse(route.isCharter());
    }

    @Test
    void ensureCharterRouteStoresFlightType() {
        final FlightRoute route = new FlightRouteFactory()
                .withRouteCode(RouteCode.of("TP202"))
                .withDepartureAirport(LIS)
                .withArrivalAirport(OPO)
                .withCompany(TP)
                .asCharter(charterSchedule())
                .build();

        assertEquals(FlightType.CHARTER, route.flightType());
        assertTrue(route.isCharter());
        assertFalse(route.isRegular());
    }

    // ── Schedule validation ──────────────────────────────────────────────────

    @Test
    void ensureRegularRouteAllowsScheduledDeparture() {
        final FlightRoute route = validRoute();

        assertTrue(route.allowsDepartureAt(
                LocalDateTime.of(2026, 6, 15, 9, 0) // Monday
        ));
    }

    @Test
    void ensureRegularRouteRejectsUnscheduledDepartureTime() {
        final FlightRoute route = validRoute();

        assertFalse(route.allowsDepartureAt(
                LocalDateTime.of(2026, 6, 15, 10, 0) // Monday, wrong time
        ));
    }

    @Test
    void ensureRegularRouteRejectsUnscheduledDepartureDay() {
        final FlightRoute route = validRoute();

        assertFalse(route.allowsDepartureAt(
                LocalDateTime.of(2026, 6, 16, 9, 0) // Tuesday
        ));
    }

    @Test
    void ensureCharterRouteAllowsExactDeparture() {
        final FlightRoute route = new FlightRouteFactory()
                .withRouteCode(RouteCode.of("TP202"))
                .withDepartureAirport(LIS)
                .withArrivalAirport(OPO)
                .withCompany(TP)
                .asCharter(charterSchedule())
                .build();

        assertTrue(route.allowsDepartureAt(
                LocalDateTime.of(2026, 6, 15, 9, 0)
        ));
    }

    @Test
    void ensureCharterRouteRejectsDifferentDeparture() {
        final FlightRoute route = new FlightRouteFactory()
                .withRouteCode(RouteCode.of("TP202"))
                .withDepartureAirport(LIS)
                .withArrivalAirport(OPO)
                .withCompany(TP)
                .asCharter(charterSchedule())
                .build();

        assertFalse(route.allowsDepartureAt(
                LocalDateTime.of(2026, 6, 15, 10, 0)
        ));
    }

    // ── Invariant: departure != arrival ──────────────────────────────────────

    @Test
    void ensureSameAirportForDepartureAndArrivalIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new FlightRouteFactory()
                        .withRouteCode(RouteCode.of("TP101"))
                        .withDepartureAirport(LIS)
                        .withArrivalAirport(LIS)
                        .withCompany(TP)
                        .asRegular(regularSchedule())
                        .build()
        );
    }

    // ── Invariant: route code prefix must match company IATA ─────────────────

    @Test
    void ensureRoutePrefixMismatchIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new FlightRouteFactory()
                        .withRouteCode(RouteCode.of("FR101"))
                        .withDepartureAirport(LIS)
                        .withArrivalAirport(OPO)
                        .withCompany(TP)
                        .asRegular(regularSchedule())
                        .build()
        );
    }

    // ── Invariant: flight type / schedule compatibility ──────────────────────

    @Test
    void ensureRegularRouteWithoutRegularScheduleIsRejected() {
        assertThrows(Exception.class, () ->
                new FlightRouteFactory()
                        .withRouteCode(RouteCode.of("TP101"))
                        .withDepartureAirport(LIS)
                        .withArrivalAirport(OPO)
                        .withCompany(TP)
                        .asRegular(null)
                        .build()
        );
    }

    @Test
    void ensureCharterRouteWithoutCharterScheduleIsRejected() {
        assertThrows(Exception.class, () ->
                new FlightRouteFactory()
                        .withRouteCode(RouteCode.of("TP101"))
                        .withDepartureAirport(LIS)
                        .withArrivalAirport(OPO)
                        .withCompany(TP)
                        .asCharter(null)
                        .build()
        );
    }

    @Test
    void ensureRouteWithoutFlightTypeIsRejected() {
        assertThrows(Exception.class, () ->
                new FlightRouteFactory()
                        .withRouteCode(RouteCode.of("TP101"))
                        .withDepartureAirport(LIS)
                        .withArrivalAirport(OPO)
                        .withCompany(TP)
                        .build()
        );
    }

    // ── Null guards ───────────────────────────────────────────────────────────

    @Test
    void ensureNullRouteCodeIsRejected() {
        assertThrows(Exception.class, () ->
                new FlightRouteFactory()
                        .withRouteCode(null)
                        .withDepartureAirport(LIS)
                        .withArrivalAirport(OPO)
                        .withCompany(TP)
                        .asRegular(regularSchedule())
                        .build()
        );
    }

    @Test
    void ensureNullDepartureAirportIsRejected() {
        assertThrows(Exception.class, () ->
                new FlightRouteFactory()
                        .withRouteCode(RouteCode.of("TP101"))
                        .withDepartureAirport(null)
                        .withArrivalAirport(OPO)
                        .withCompany(TP)
                        .asRegular(regularSchedule())
                        .build()
        );
    }

    @Test
    void ensureNullArrivalAirportIsRejected() {
        assertThrows(Exception.class, () ->
                new FlightRouteFactory()
                        .withRouteCode(RouteCode.of("TP101"))
                        .withDepartureAirport(LIS)
                        .withArrivalAirport(null)
                        .withCompany(TP)
                        .asRegular(regularSchedule())
                        .build()
        );
    }

    @Test
    void ensureNullCompanyIsRejected() {
        assertThrows(Exception.class, () ->
                new FlightRouteFactory()
                        .withRouteCode(RouteCode.of("TP101"))
                        .withDepartureAirport(LIS)
                        .withArrivalAirport(OPO)
                        .withCompany(null)
                        .asRegular(regularSchedule())
                        .build()
        );
    }

    // ── deactivate ────────────────────────────────────────────────────────────

    @Test
    void ensureDeactivateSetsRouteStatusToInactive() {
        final FlightRoute route = validRoute();

        route.deactivate(LocalDate.now());

        assertEquals(RouteStatus.INACTIVE, route.routeStatus());
        assertFalse(route.isActive());
    }

    @Test
    void ensureDeactivatingAlreadyInactiveRouteIsRejected() {
        final FlightRoute route = validRoute();

        route.deactivate(LocalDate.now());

        assertThrows(IllegalArgumentException.class,
                () -> route.deactivate(LocalDate.now()));
    }

    @Test
    void ensureDeactivatingWithPastDateIsRejected() {
        final FlightRoute route = validRoute();

        assertThrows(IllegalArgumentException.class,
                () -> route.deactivate(LocalDate.now().minusDays(1)));
    }

    @Test
    void ensureDeactivationDateIsStored() {
        final FlightRoute route = validRoute();
        final LocalDate date = LocalDate.now().plusDays(7);

        route.deactivate(date);

        assertEquals(date, route.deactivationDate());
    }

    // ── sameAs ────────────────────────────────────────────────────────────────

    @Test
    void ensureSameAsReturnsTrueForSameRouteCode() {
        final FlightRoute a = validRoute();

        final FlightRoute b = new FlightRouteFactory()
                .withRouteCode(RouteCode.of("TP101"))
                .withDepartureAirport(LIS)
                .withArrivalAirport(OPO)
                .withCompany(TP)
                .asRegular(regularSchedule())
                .build();

        assertTrue(a.sameAs(b));
    }

    @Test
    void ensureSameAsReturnsFalseForDifferentRouteCode() {
        final FlightRoute a = validRoute();

        final FlightRoute b = new FlightRouteFactory()
                .withRouteCode(RouteCode.of("TP202"))
                .withDepartureAirport(LIS)
                .withArrivalAirport(OPO)
                .withCompany(TP)
                .asRegular(regularSchedule())
                .build();

        assertFalse(a.sameAs(b));
    }

    @Test
    void ensureSameAsReturnsFalseForNonRouteObject() {
        assertFalse(validRoute().sameAs("not a route"));
    }

    @Test
    void ensureToStringContainsRouteCode() {
        assertTrue(validRoute().toString().contains("TP101"));
    }
}