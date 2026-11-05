package aisafe.sharedkernel;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class Coordinates implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    private double latitude;
    private double longitude;

    protected Coordinates() {
        // for ORM
    }

    public Coordinates(final double latitude, final double longitude) {
        Preconditions.ensure(latitude >= -90 && latitude <= 90,
                "Latitude must be between -90 and 90");
        Preconditions.ensure(longitude >= -180 && longitude <= 180,
                "Longitude must be between -180 and 180");
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static Coordinates of(final double latitude, final double longitude) {
        return new Coordinates(latitude, longitude);
    }

    public double latitude() {
        return latitude;
    }

    public double longitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "(" + latitude + ", " + longitude + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Coordinates)) return false;
        Coordinates that = (Coordinates) o;
        return Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
}