package dsl.model;

import java.util.Objects;

public class ImportedCoordinate{

    private final double latitude;
    private final double longitude;

    public ImportedCoordinate(double latitude, double longitude){

        this.latitude = latitude;
        this.longitude = longitude;

    }

    public double getLatitude(){

        return latitude;

    }

    public double getLongitude(){

        return longitude;

    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ImportedCoordinate other)) {
            return false;
        }

        return Double.compare(latitude, other.latitude) == 0
                && Double.compare(longitude, other.longitude) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude);
    }
}