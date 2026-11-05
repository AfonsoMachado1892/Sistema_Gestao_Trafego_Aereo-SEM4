package dsl.model;

import java.util.List;

public class ImportedFlightPlan {

    private final String flightId;
    private final String flightType;
    private final ImportedRoute route;
    private final String aircraftRegistration;
    private final String pilotId;
    private final List<ImportedLeg> legs;

    public ImportedFlightPlan(
            String flightId,
            String flightType,
            ImportedRoute route,
            String aircraftRegistration,
            String pilotId,
            List<ImportedLeg> legs) {

        this.flightId = flightId;
        this.flightType = flightType;
        this.route = route;
        this.aircraftRegistration = aircraftRegistration;
        this.pilotId = pilotId;
        this.legs = List.copyOf(legs);
    }

    public String flightId() {
        return flightId;
    }

    public String flightType() {
        return flightType;
    }

    public ImportedRoute route() {
        return route;
    }

    public String aircraftRegistration() {
        return aircraftRegistration;
    }

    public String pilotId() {
        return pilotId;
    }

    public List<ImportedLeg> legs() {
        return legs;
    }
}