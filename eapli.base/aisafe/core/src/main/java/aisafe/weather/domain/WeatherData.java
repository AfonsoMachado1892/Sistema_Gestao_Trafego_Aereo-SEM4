package aisafe.weather.domain;

import aisafe.aircontrolarea.domain.AirControlAreaCode;
import eapli.framework.domain.model.AggregateRoot;
import jakarta.persistence.*;

import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class WeatherData implements AggregateRoot<WeatherDataID> {

    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private WeatherDataID weatherDataID;

    @Embedded
    @AttributeOverride(name = "code", column = @Column(name = "air_control_area_code"))
    private AirControlAreaCode airControlAreaCode;

    @Column(name = "weather_day")
    private LocalDate day;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "start.latitude",  column = @Column(name = "bbox_start_lat")),
            @AttributeOverride(name = "start.longitude", column = @Column(name = "bbox_start_lon")),
            @AttributeOverride(name = "end.latitude",    column = @Column(name = "bbox_end_lat")),
            @AttributeOverride(name = "end.longitude",   column = @Column(name = "bbox_end_lon"))
    })
    private BoundingBox boundingBox;

    @Column(name = "min_altitude")
    private int minAltitude;

    @Column(name = "max_altitude")
    private int maxAltitude;

    @Column(name = "wind_speed")
    private double windSpeed;

    @Column(name = "wind_direction")
    private double windDirection;

    protected WeatherData() {
        // for ORM
    }

    public WeatherData(final AirControlAreaCode airControlAreaCode,
                       final LocalDate day,
                       final LocalTime startTime,
                       final LocalTime endTime,
                       final BoundingBox boundingBox,
                       final int minAltitude,
                       final int maxAltitude,
                       final double windSpeed,
                       final double windDirection) {
        if (airControlAreaCode == null) throw new IllegalArgumentException("Air control area code is required.");
        if (day == null) throw new IllegalArgumentException("Day is required.");
        if (startTime == null) throw new IllegalArgumentException("Start time is required.");
        if (endTime == null) throw new IllegalArgumentException("End time is required.");
        if (boundingBox == null) throw new IllegalArgumentException("Bounding box is required.");
        if (minAltitude < 0) throw new IllegalArgumentException("Min altitude cannot be negative.");
        if (maxAltitude < minAltitude) throw new IllegalArgumentException("Max altitude cannot be less than min altitude.");
        if (windSpeed < 0) throw new IllegalArgumentException("Wind speed cannot be negative.");

        this.weatherDataID      = WeatherDataID.newID();
        this.airControlAreaCode = airControlAreaCode;
        this.day                = day;
        this.startTime          = startTime;
        this.endTime            = endTime;
        this.boundingBox        = boundingBox;
        this.minAltitude        = minAltitude;
        this.maxAltitude        = maxAltitude;
        this.windSpeed          = windSpeed;
        this.windDirection      = windDirection;
    }

    @Override
    public WeatherDataID identity() {
        return weatherDataID;
    }

    @Override
    public boolean sameAs(Object other) {
        if (!(other instanceof WeatherData)) return false;
        WeatherData that = (WeatherData) other;
        return this.weatherDataID.equals(that.weatherDataID);
    }

    public AirControlAreaCode airControlAreaCode() { return airControlAreaCode; }
    public LocalDate day()                         { return day; }
    public LocalTime startTime()                   { return startTime; }
    public LocalTime endTime()                     { return endTime; }
    public BoundingBox boundingBox()               { return boundingBox; }
    public int minAltitude()                       { return minAltitude; }
    public int maxAltitude()                       { return maxAltitude; }
    public double windSpeed()                      { return windSpeed; }
    public double windDirection()                  { return windDirection; }

    @Override
    public String toString() {
        return String.format(
                "WeatherData{id=%s, area=%s, day=%s, start=%s, end=%s, bbox=%s, altMin=%d, altMax=%d, windSpeed=%.2f, windDir=%.2f}",
                weatherDataID, airControlAreaCode, day, startTime, endTime, boundingBox, minAltitude, maxAltitude, windSpeed, windDirection
        );
    }
}