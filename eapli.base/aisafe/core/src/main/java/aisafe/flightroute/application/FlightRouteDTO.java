package aisafe.flightroute.application;

public class FlightRouteDTO {

    public final String routeCode;
    public final String routeStatus;
    public final String departureAirportIata;
    public final String arrivalAirportIata;
    public final String companyIata;
    public final String flightType;
    public final String deactivationDate;
    public final String schedule;

    public FlightRouteDTO(final String routeCode,
                          final String routeStatus,
                          final String departureAirportIata,
                          final String arrivalAirportIata,
                          final String companyIata,
                          final String flightType,
                          final String deactivationDate,
                          final String schedule) {
        this.routeCode = routeCode;
        this.routeStatus = routeStatus;
        this.departureAirportIata = departureAirportIata;
        this.arrivalAirportIata = arrivalAirportIata;
        this.companyIata = companyIata;
        this.flightType = flightType;
        this.deactivationDate = deactivationDate;
        this.schedule = schedule;
    }

    @Override
    public String toString() {
        return routeCode + " " + departureAirportIata + " → " + arrivalAirportIata
                + " (" + routeStatus + ", " + flightType
                + ", deactivation: " + deactivationDate
                + ", " + schedule + ")";
    }
}