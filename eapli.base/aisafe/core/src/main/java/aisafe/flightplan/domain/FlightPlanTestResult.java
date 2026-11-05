package aisafe.flightplan.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Embeddable
public class FlightPlanTestResult implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "test_passed")
    private boolean passed;

    @Column(name = "tested_at")
    private LocalDateTime testedAt;

    @Column(name = "test_summary")
    private String summary;

    @Lob
    @Column(name = "test_messages")
    private String messages;

    @Column(name = "test_total_distance_km")
    private double totalDistanceKm;

    @Column(name = "test_loaded_fuel")
    private double loadedFuel;

    @Column(name = "test_estimated_fuel_required")
    private double estimatedFuelRequired;

    @Column(name = "test_estimated_fuel_remaining")
    private double estimatedFuelRemaining;

    @Column(name = "weather_used")
    private boolean weatherUsed;

    @Column(name = "weather_wind_speed")
    private double weatherWindSpeed;

    @Column(name = "weather_wind_direction")
    private double weatherWindDirection;

    @Column(name = "weather_fuel_factor")
    private double weatherFuelFactor;

    protected FlightPlanTestResult() {
        // for ORM
    }

    private FlightPlanTestResult(final boolean passed,
                                 final String summary,
                                 final List<String> messages,
                                 final double totalDistanceKm,
                                 final double loadedFuel,
                                 final double estimatedFuelRequired,
                                 final double estimatedFuelRemaining,
                                 final boolean weatherUsed,
                                 final double weatherWindSpeed,
                                 final double weatherWindDirection,
                                 final double weatherFuelFactor) {
        Preconditions.nonEmpty(summary, "Test result summary must not be empty.");
        Preconditions.noneNull(messages, "Test result messages must not be null.");

        this.passed = passed;
        this.testedAt = LocalDateTime.now();
        this.summary = summary;
        this.messages = String.join(System.lineSeparator(), messages);
        this.totalDistanceKm = totalDistanceKm;
        this.loadedFuel = loadedFuel;
        this.estimatedFuelRequired = estimatedFuelRequired;
        this.estimatedFuelRemaining = estimatedFuelRemaining;
        this.weatherUsed = weatherUsed;
        this.weatherWindSpeed = weatherWindSpeed;
        this.weatherWindDirection = weatherWindDirection;
        this.weatherFuelFactor = weatherFuelFactor;
    }

    public static FlightPlanTestResult passed(final String summary,
                                              final List<String> messages,
                                              final double totalDistanceKm,
                                              final double loadedFuel,
                                              final double estimatedFuelRequired,
                                              final double estimatedFuelRemaining) {
        return passed(
                summary,
                messages,
                totalDistanceKm,
                loadedFuel,
                estimatedFuelRequired,
                estimatedFuelRemaining,
                false,
                0.0,
                0.0,
                1.0
        );
    }

    public static FlightPlanTestResult failed(final String summary,
                                              final List<String> messages,
                                              final double totalDistanceKm,
                                              final double loadedFuel,
                                              final double estimatedFuelRequired,
                                              final double estimatedFuelRemaining) {
        return failed(
                summary,
                messages,
                totalDistanceKm,
                loadedFuel,
                estimatedFuelRequired,
                estimatedFuelRemaining,
                false,
                0.0,
                0.0,
                1.0
        );
    }

    public static FlightPlanTestResult passed(final String summary,
                                              final List<String> messages,
                                              final double totalDistanceKm,
                                              final double loadedFuel,
                                              final double estimatedFuelRequired,
                                              final double estimatedFuelRemaining,
                                              final boolean weatherUsed,
                                              final double weatherWindSpeed,
                                              final double weatherWindDirection,
                                              final double weatherFuelFactor) {
        return new FlightPlanTestResult(
                true,
                summary,
                messages,
                totalDistanceKm,
                loadedFuel,
                estimatedFuelRequired,
                estimatedFuelRemaining,
                weatherUsed,
                weatherWindSpeed,
                weatherWindDirection,
                weatherFuelFactor
        );
    }

    public static FlightPlanTestResult failed(final String summary,
                                              final List<String> messages,
                                              final double totalDistanceKm,
                                              final double loadedFuel,
                                              final double estimatedFuelRequired,
                                              final double estimatedFuelRemaining,
                                              final boolean weatherUsed,
                                              final double weatherWindSpeed,
                                              final double weatherWindDirection,
                                              final double weatherFuelFactor) {
        return new FlightPlanTestResult(
                false,
                summary,
                messages,
                totalDistanceKm,
                loadedFuel,
                estimatedFuelRequired,
                estimatedFuelRemaining,
                weatherUsed,
                weatherWindSpeed,
                weatherWindDirection,
                weatherFuelFactor
        );
    }

    public boolean passed() {
        return passed;
    }

    public boolean failed() {
        return !passed;
    }

    public LocalDateTime testedAt() {
        return testedAt;
    }

    public String summary() {
        return summary;
    }

    public List<String> messages() {
        if (messages == null || messages.isBlank()) {
            return List.of();
        }

        return List.of(messages.split("\\R"));
    }

    public double totalDistanceKm() {
        return totalDistanceKm;
    }

    public double loadedFuel() {
        return loadedFuel;
    }

    public double estimatedFuelRequired() {
        return estimatedFuelRequired;
    }

    public double estimatedFuelRemaining() {
        return estimatedFuelRemaining;
    }

    public boolean weatherUsed() {
        return weatherUsed;
    }

    public double weatherWindSpeed() {
        return weatherWindSpeed;
    }

    public double weatherWindDirection() {
        return weatherWindDirection;
    }

    public double weatherFuelFactor() {
        return weatherFuelFactor;
    }

    @Override
    public String toString() {
        return summary;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof FlightPlanTestResult that)) {
            return false;
        }

        return passed == that.passed
                && Double.compare(that.totalDistanceKm, totalDistanceKm) == 0
                && Double.compare(that.loadedFuel, loadedFuel) == 0
                && Double.compare(that.estimatedFuelRequired, estimatedFuelRequired) == 0
                && Double.compare(that.estimatedFuelRemaining, estimatedFuelRemaining) == 0
                && weatherUsed == that.weatherUsed
                && Double.compare(that.weatherWindSpeed, weatherWindSpeed) == 0
                && Double.compare(that.weatherWindDirection, weatherWindDirection) == 0
                && Double.compare(that.weatherFuelFactor, weatherFuelFactor) == 0
                && Objects.equals(testedAt, that.testedAt)
                && Objects.equals(summary, that.summary)
                && Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                passed,
                testedAt,
                summary,
                messages,
                totalDistanceKm,
                loadedFuel,
                estimatedFuelRequired,
                estimatedFuelRemaining,
                weatherUsed,
                weatherWindSpeed,
                weatherWindDirection,
                weatherFuelFactor
        );
    }
}