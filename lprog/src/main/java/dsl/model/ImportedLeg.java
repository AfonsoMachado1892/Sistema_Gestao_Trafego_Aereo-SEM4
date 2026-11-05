package dsl.model;

import java.time.LocalDateTime;
import java.util.List;

public class ImportedLeg {

    private final String departureAirportCode;
    private final LocalDateTime departureDateTime;
    private final int departureLine;
    private final int departureColumn;

    private final String arrivalAirportCode;
    private final LocalDateTime arrivalDateTime;
    private final int arrivalLine;
    private final int arrivalColumn;

    private final List<ImportedSegment> segments;
    private final ImportedFuel fuel;

    public ImportedLeg(
            String departureAirportCode,
            LocalDateTime departureDateTime,
            int departureLine,
            int departureColumn,
            String arrivalAirportCode,
            LocalDateTime arrivalDateTime,
            int arrivalLine,
            int arrivalColumn,
            List<ImportedSegment> segments,
            ImportedFuel fuel) {

        this.departureAirportCode = departureAirportCode;
        this.departureDateTime = departureDateTime;
        this.departureLine = departureLine;
        this.departureColumn = departureColumn;

        this.arrivalAirportCode = arrivalAirportCode;
        this.arrivalDateTime = arrivalDateTime;
        this.arrivalLine = arrivalLine;
        this.arrivalColumn = arrivalColumn;

        this.segments = List.copyOf(segments);
        this.fuel = fuel;
    }

    public String departureAirportCode() {
        return departureAirportCode;
    }

    public LocalDateTime departureDateTime() {
        return departureDateTime;
    }

    public int departureLine() {
        return departureLine;
    }

    public int departureColumn() {
        return departureColumn;
    }

    public String arrivalAirportCode() {
        return arrivalAirportCode;
    }

    public LocalDateTime arrivalDateTime() {
        return arrivalDateTime;
    }

    public int arrivalLine() {
        return arrivalLine;
    }

    public int arrivalColumn() {
        return arrivalColumn;
    }

    public List<ImportedSegment> segments() {
        return segments;
    }

    public ImportedFuel fuel() {
        return fuel;
    }
}