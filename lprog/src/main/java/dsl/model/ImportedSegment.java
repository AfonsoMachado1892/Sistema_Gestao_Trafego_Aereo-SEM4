package dsl.model;

public class ImportedSegment {

    private final ImportedCoordinate start;
    private final ImportedCoordinate end;
    private final double altitude;
    private final double width;
    private final double windDirection;
    private final double windSpeed;
    private final int line;
    private final int column;

    public ImportedSegment(
            ImportedCoordinate start,
            ImportedCoordinate end,
            double altitude,
            double width,
            double windDirection,
            double windSpeed,
            int line,
            int column) {

        this.start = start;
        this.end = end;
        this.altitude = altitude;
        this.width = width;
        this.windDirection = windDirection;
        this.windSpeed = windSpeed;
        this.line = line;
        this.column = column;
    }

    public ImportedCoordinate start() {
        return start;
    }

    public ImportedCoordinate end() {
        return end;
    }

    public double altitude() {
        return altitude;
    }

    public double width() {
        return width;
    }

    public double windDirection() {
        return windDirection;
    }

    public double windSpeed() {
        return windSpeed;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }
}