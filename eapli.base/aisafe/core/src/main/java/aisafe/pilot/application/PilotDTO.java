package aisafe.pilot.application;

/**
 * DTO carrying display data for a Pilot.
 */
public class PilotDTO {

    public final String pilotID;
    public final String name;
    public final String companyIata;
    public final String status;
    public final int certificationCount;

    public PilotDTO(final String pilotID,
                    final String name,
                    final String companyIata,
                    final String status,
                    final int certificationCount) {
        this.pilotID = pilotID;
        this.name = name;
        this.companyIata = companyIata;
        this.status = status;
        this.certificationCount = certificationCount;
    }

    @Override
    public String toString() {
        return name + " [" + companyIata + "] (" + status + ") - "
                + certificationCount + " certification(s)";
    }
}
