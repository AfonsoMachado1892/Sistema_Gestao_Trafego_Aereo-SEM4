package aisafe.flightroute.domain;

import aisafe.airport.domain.AirportIataCode;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "FLIGHT_ROUTE")
public class FlightRoute implements AggregateRoot<RouteCode>, Serializable {

    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @EmbeddedId
    private RouteCode routeCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RouteStatus routeStatus;

    @Embedded
    @AttributeOverride(name = "code", column = @Column(name = "departure_airport_iata"))
    private AirportIataCode departureAirportIataCode;

    @Embedded
    @AttributeOverride(name = "code", column = @Column(name = "arrival_airport_iata"))
    private AirportIataCode arrivalAirportIataCode;

    @Embedded
    @AttributeOverride(name = "iataCode", column = @Column(name = "company_iata"))
    private AirTransportCompanyCode airTransportCompanyIataCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightType flightType;

    @Embedded
    private RegularSchedule regularSchedule;

    @Embedded
    private CharterSchedule charterSchedule;

    @Column(name = "deactivation_date")
    private LocalDate deactivationDate;

    protected FlightRoute() {
        // for ORM
    }

    FlightRoute(final RouteCode routeCode,
                final AirportIataCode departureAirportIataCode,
                final AirportIataCode arrivalAirportIataCode,
                final AirTransportCompanyCode companyIataCode,
                final FlightType flightType,
                final RegularSchedule regularSchedule,
                final CharterSchedule charterSchedule) {

        Preconditions.noneNull(routeCode, "Route code must not be null.");
        Preconditions.noneNull(departureAirportIataCode, "Departure airport must not be null.");
        Preconditions.noneNull(arrivalAirportIataCode, "Arrival airport must not be null.");
        Preconditions.noneNull(companyIataCode, "Company IATA code must not be null.");
        Preconditions.noneNull(flightType, "Flight type must not be null.");

        Preconditions.ensure(
                !departureAirportIataCode.equals(arrivalAirportIataCode),
                "Departure and arrival airports must be different."
        );

        Preconditions.ensure(
                routeCode.companyPrefix().equals(companyIataCode.toString()),
                "Route code prefix must match the company IATA code."
        );

        ensureScheduleMatchesFlightType(flightType, regularSchedule, charterSchedule);

        this.routeCode = routeCode;
        this.departureAirportIataCode = departureAirportIataCode;
        this.arrivalAirportIataCode = arrivalAirportIataCode;
        this.airTransportCompanyIataCode = companyIataCode;
        this.flightType = flightType;
        this.regularSchedule = regularSchedule;
        this.charterSchedule = charterSchedule;
        this.routeStatus = RouteStatus.ACTIVE;
    }

    private void ensureScheduleMatchesFlightType(final FlightType flightType,
                                                 final RegularSchedule regularSchedule,
                                                 final CharterSchedule charterSchedule) {
        if (flightType == FlightType.REGULAR) {
            Preconditions.noneNull(
                    regularSchedule,
                    "Regular schedule must be provided for regular routes."
            );
            Preconditions.ensure(
                    charterSchedule == null,
                    "Charter schedule must not be provided for regular routes."
            );
        }

        if (flightType == FlightType.CHARTER) {
            Preconditions.noneNull(
                    charterSchedule,
                    "Charter schedule must be provided for charter routes."
            );
            Preconditions.ensure(
                    regularSchedule == null,
                    "Regular schedule must not be provided for charter routes."
            );
        }
    }

    public RouteCode routeCode() {
        return routeCode;
    }

    public RouteStatus routeStatus() {
        return routeStatus;
    }

    public AirportIataCode departureAirportIataCode() {
        return departureAirportIataCode;
    }

    public AirportIataCode arrivalAirportIataCode() {
        return arrivalAirportIataCode;
    }

    public AirTransportCompanyCode airTransportCompanyIataCode() {
        return airTransportCompanyIataCode;
    }

    public FlightType flightType() {
        return flightType;
    }

    public LocalDate deactivationDate() {
        return deactivationDate;
    }

    public boolean isActive() {
        return isActiveOn(LocalDate.now());
    }

    public boolean isActiveOn(final LocalDate date) {
        Preconditions.noneNull(date, "Date must not be null.");

        return routeStatus == RouteStatus.ACTIVE
                && (deactivationDate == null || date.isBefore(deactivationDate));
    }

    public RouteStatus effectiveStatus() {
        return effectiveStatusOn(LocalDate.now());
    }

    public RouteStatus effectiveStatusOn(final LocalDate date) {
        return isActiveOn(date) ? RouteStatus.ACTIVE : RouteStatus.INACTIVE;
    }


    public boolean isRegular() {
        return flightType == FlightType.REGULAR;
    }

    public boolean isCharter() {
        return flightType == FlightType.CHARTER;
    }

    public RegularSchedule regularSchedule() {
        return regularSchedule;
    }

    public CharterSchedule charterSchedule() {
        return charterSchedule;
    }

    /**
     * Exposes the route schedule for presentation/DTO purposes.
     * Business rules should prefer allowsDepartureAt(...) to avoid exposing internals.
     */
    public Schedule schedule() {
        return isRegular() ? regularSchedule : charterSchedule;
    }

    /**
     * Business intention method.
     * Checks whether this route allows a flight plan departure at the given date/time.
     */
    public boolean allowsDepartureAt(final LocalDateTime departureDateTime) {
        Preconditions.noneNull(departureDateTime, "Departure date/time must not be null.");
        return schedule().allows(departureDateTime);
    }

    public void deactivate(final LocalDate deactivationDate) {
        Preconditions.noneNull(deactivationDate, "Deactivation date must not be null.");
        Preconditions.ensure(
                !deactivationDate.isBefore(LocalDate.now()),
                "Deactivation date cannot be in the past."
        );

        Preconditions.ensure(
                this.deactivationDate == null,
                "Flight route already has a deactivation date."
        );

        this.deactivationDate = deactivationDate;

        if (deactivationDate.equals(LocalDate.now())) {
            this.routeStatus = RouteStatus.INACTIVE;
        }
    }

    @Override
    public RouteCode identity() {
        return routeCode;
    }

    @Override
    public boolean sameAs(final Object other) {
        if (!(other instanceof FlightRoute)) return false;
        return this.identity().equals(((FlightRoute) other).identity());
    }

    @Override
    public boolean equals(final Object o) {
        return DomainEntities.areEqual(this, o);
    }

    @Override
    public int hashCode() {
        return DomainEntities.hashCode(this);
    }

    @Override
    public String toString() {
        return routeCode + " " + departureAirportIataCode + " → " + arrivalAirportIataCode
                + " (" + routeStatus + ", " + flightType + ")";
    }
}