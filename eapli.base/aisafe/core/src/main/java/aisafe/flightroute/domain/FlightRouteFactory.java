package aisafe.flightroute.domain;

import aisafe.airport.domain.AirportIataCode;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import eapli.framework.domain.model.DomainFactory;
import eapli.framework.validations.Preconditions;

public class FlightRouteFactory implements DomainFactory<FlightRoute> {

    private RouteCode routeCode;
    private AirportIataCode departureAirportIataCode;
    private AirportIataCode arrivalAirportIataCode;
    private AirTransportCompanyCode companyIataCode;
    private FlightType flightType;
    private RegularSchedule regularSchedule;
    private CharterSchedule charterSchedule;

    public FlightRouteFactory withRouteCode(final RouteCode routeCode) {
        this.routeCode = routeCode;
        return this;
    }

    public FlightRouteFactory withDepartureAirport(final AirportIataCode departureAirportIataCode) {
        this.departureAirportIataCode = departureAirportIataCode;
        return this;
    }

    public FlightRouteFactory withArrivalAirport(final AirportIataCode arrivalAirportIataCode) {
        this.arrivalAirportIataCode = arrivalAirportIataCode;
        return this;
    }

    public FlightRouteFactory withCompany(final AirTransportCompanyCode companyIataCode) {
        this.companyIataCode = companyIataCode;
        return this;
    }

    public FlightRouteFactory asRegular(final RegularSchedule regularSchedule) {
        Preconditions.noneNull(regularSchedule, "Regular schedule must not be null.");

        this.flightType = FlightType.REGULAR;
        this.regularSchedule = regularSchedule;
        this.charterSchedule = null;

        return this;
    }

    public FlightRouteFactory asCharter(final CharterSchedule charterSchedule) {
        Preconditions.noneNull(charterSchedule, "Charter schedule must not be null.");

        this.flightType = FlightType.CHARTER;
        this.charterSchedule = charterSchedule;
        this.regularSchedule = null;

        return this;
    }

    @Override
    public FlightRoute build() {
        Preconditions.noneNull(routeCode, "Route code must not be null.");
        Preconditions.noneNull(departureAirportIataCode, "Departure airport must not be null.");
        Preconditions.noneNull(arrivalAirportIataCode, "Arrival airport must not be null.");
        Preconditions.noneNull(companyIataCode, "Company IATA code must not be null.");
        Preconditions.noneNull(flightType, "Flight type must not be null.");

        return new FlightRoute(
                routeCode,
                departureAirportIataCode,
                arrivalAirportIataCode,
                companyIataCode,
                flightType,
                regularSchedule,
                charterSchedule
        );
    }
}