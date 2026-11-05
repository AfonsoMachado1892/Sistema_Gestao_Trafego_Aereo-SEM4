package aisafe.pilot.application;

/**
 * DTO carrying display data for an AircraftModel, used in pilot certification selection.
 */
public class AircraftModelDTO {

    public final String modelID;
    public final String name;
    public final String makerName;
    public final String type;

    public AircraftModelDTO(final String modelID,
                            final String name,
                            final String makerName,
                            final String type) {
        this.modelID = modelID;
        this.name = name;
        this.makerName = makerName;
        this.type = type;
    }

    @Override
    public String toString() {
        return modelID + " - " + name + " (" + makerName + ") [" + type + "]";
    }
}
