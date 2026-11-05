package aisafe.flightplan.domain.simulation;

import aisafe.sharedkernel.Coordinates;
import eapli.framework.validations.Preconditions;

public final class SimulatedFlightSegment {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private final Coordinates start;
    private final Coordinates end;
    private final double altitudeMeters;

    private SimulatedFlightSegment(final Coordinates start,
                                  final Coordinates end,
                                  final double altitudeMeters) {
        Preconditions.noneNull(start, "Segment start coordinates must not be null.");
        Preconditions.noneNull(end, "Segment end coordinates must not be null.");
        Preconditions.ensure(!start.equals(end), "Segment start and end coordinates must be different.");
        Preconditions.ensure(altitudeMeters > 0, "Segment altitude must be positive.");

        this.start = start;
        this.end = end;
        this.altitudeMeters = altitudeMeters;
    }

    public static SimulatedFlightSegment of(final Coordinates start,
                                            final Coordinates end,
                                            final double altitudeMeters) {
        return new SimulatedFlightSegment(start, end, altitudeMeters);
    }

    public Coordinates start() {
        return start;
    }

    public Coordinates end() {
        return end;
    }

    public double altitudeMeters() {
        return altitudeMeters;
    }

    public double distanceKm() {
        final double lat1 = Math.toRadians(start.latitude());
        final double lat2 = Math.toRadians(end.latitude());

        final double deltaLat = Math.toRadians(end.latitude() - start.latitude());
        final double deltaLon = Math.toRadians(end.longitude() - start.longitude());

        final double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }
}