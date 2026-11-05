package aisafe.aircontrolarea.domain;

import aisafe.sharedkernel.Coordinates;
import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class GeographicBoundary implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "southwest_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "southwest_longitude"))
    })
    private Coordinates southwestCorner;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "latitude", column = @Column(name = "northeast_latitude")),
            @AttributeOverride(name = "longitude", column = @Column(name = "northeast_longitude"))
    })
    private Coordinates northeastCorner;

    private double lowerAltitudeMeters;

    private double upperAltitudeMeters;

    protected GeographicBoundary() {
        // for ORM
    }

    public GeographicBoundary(final Coordinates southwestCorner, final Coordinates northeastCorner) {
        Preconditions.noneNull(southwestCorner, "Southwest corner cannot be null.");
        Preconditions.noneNull(northeastCorner, "Northeast corner cannot be null.");

        Preconditions.ensure(
                southwestCorner.latitude() < northeastCorner.latitude(),
                "Southwest latitude must be lower than northeast latitude."
        );

        Preconditions.ensure(
                southwestCorner.longitude() < northeastCorner.longitude(),
                "Southwest longitude must be lower than northeast longitude."
        );

        this.southwestCorner = southwestCorner;
        this.northeastCorner = northeastCorner;
        this.lowerAltitudeMeters = AirControlAreaDefaults.DEFAULT_LOWER_ALTITUDE_METERS;
        this.upperAltitudeMeters = AirControlAreaDefaults.DEFAULT_UPPER_ALTITUDE_METERS;
    }

    public static GeographicBoundary of(final Coordinates southwestCorner, final Coordinates northeastCorner) {
        return new GeographicBoundary(southwestCorner, northeastCorner);
    }

    public boolean contains(final Coordinates coordinates) {
        Preconditions.noneNull(coordinates, "Coordinates cannot be null.");

        return coordinates.latitude() >= southwestCorner.latitude()
                && coordinates.latitude() <= northeastCorner.latitude()
                && coordinates.longitude() >= southwestCorner.longitude()
                && coordinates.longitude() <= northeastCorner.longitude();
    }

    public boolean overlaps(final GeographicBoundary other) {
        Preconditions.noneNull(other, "Other geographic boundary cannot be null.");

        final boolean latitudeOverlaps =
                this.southwestCorner.latitude() < other.northeastCorner.latitude()
                        && this.northeastCorner.latitude() > other.southwestCorner.latitude();

        final boolean longitudeOverlaps =
                this.southwestCorner.longitude() < other.northeastCorner.longitude()
                        && this.northeastCorner.longitude() > other.southwestCorner.longitude();

        final boolean altitudeOverlaps =
                this.lowerAltitudeMeters < other.upperAltitudeMeters
                        && this.upperAltitudeMeters > other.lowerAltitudeMeters;

        return latitudeOverlaps && longitudeOverlaps && altitudeOverlaps;
    }

    public double lowerAltitudeMeters(){

        return lowerAltitudeMeters;

    }

    public double upperAltitudeMeters(){

        return upperAltitudeMeters;

    }

    public Coordinates southwestCorner() {
        return southwestCorner;
    }

    public Coordinates northeastCorner() {
        return northeastCorner;
    }

    @Override
    public String toString() {
        return "SW " + southwestCorner
                + " - NE " + northeastCorner
                + " - ALT " + lowerAltitudeMeters + "m to " + upperAltitudeMeters + "m";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof GeographicBoundary)) return false;
        final GeographicBoundary that = (GeographicBoundary) o;
        return Double.compare(that.lowerAltitudeMeters, lowerAltitudeMeters) == 0
                && Double.compare(that.upperAltitudeMeters, upperAltitudeMeters) == 0
                && Objects.equals(southwestCorner, that.southwestCorner)
                && Objects.equals(northeastCorner, that.northeastCorner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(southwestCorner, northeastCorner, lowerAltitudeMeters, upperAltitudeMeters);
    }
}