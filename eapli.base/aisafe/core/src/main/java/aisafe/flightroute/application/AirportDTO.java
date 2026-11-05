package aisafe.flightroute.application;

/**
 * DTO carrying the display data for an airport in the context of route creation.
 * Presented to the user so they can choose departure and arrival airports.
 */
public class AirportDTO {

    public final String iataCode;
    public final String icaoCode;
    public final String name;
    public final String city;

    public AirportDTO(final String iataCode,
                      final String icaoCode,
                      final String name,
                      final String city) {
        this.iataCode = iataCode;
        this.icaoCode = icaoCode;
        this.name = name;
        this.city = city;
    }

    @Override
    public String toString() {
        return iataCode + " / " + icaoCode + " - " + name + " (" + city + ")";
    }
}
