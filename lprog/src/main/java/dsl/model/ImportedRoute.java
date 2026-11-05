package dsl.model;

public class ImportedRoute {

    private final String originAirportCode;
    private final String destinationAirportCode;

    public ImportedRoute(String originAirportCode, String destinationAirportCode) {
        this.originAirportCode = originAirportCode;
        this.destinationAirportCode = destinationAirportCode;
    }

    public String originAirportCode() {
        return originAirportCode;
    }

    public String destinationAirportCode() {
        return destinationAirportCode;
    }
}