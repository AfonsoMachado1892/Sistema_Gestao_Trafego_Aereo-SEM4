package aisafe.flightplan.application;

import aisafe.flightplan.domain.FlightPlan;
import aisafe.flightplan.domain.FlightPlanStatus;
import aisafe.flightplan.domain.FlightPlanTestResult;

import java.time.LocalDateTime;
import java.util.List;

public final class FlightPlanTestBoard {

    private final String flightPlanID;
    private final String routeCode;
    private final String aircraftRegistration;
    private final String pilotID;
    private final FlightPlanStatus finalStatus;
    private final boolean passed;
    private final LocalDateTime testedAt;
    private final String summary;
    private final List<String> messages;
    private final double totalDistanceKm;
    private final double loadedFuelKg;
    private final double estimatedFuelRequiredKg;
    private final double estimatedFuelRemainingKg;
    private final boolean weatherUsed;
    private final double weatherWindSpeed;
    private final double weatherWindDirection;
    private final double weatherFuelFactor;

    private FlightPlanTestBoard(final String flightPlanID,
                                final String routeCode,
                                final String aircraftRegistration,
                                final String pilotID,
                                final FlightPlanStatus finalStatus,
                                final boolean passed,
                                final LocalDateTime testedAt,
                                final String summary,
                                final List<String> messages,
                                final double totalDistanceKm,
                                final double loadedFuelKg,
                                final double estimatedFuelRequiredKg,
                                final double estimatedFuelRemainingKg,
                                final boolean weatherUsed,
                                final double weatherWindSpeed,
                                final double weatherWindDirection,
                                final double weatherFuelFactor) {
        this.flightPlanID = flightPlanID;
        this.routeCode = routeCode;
        this.aircraftRegistration = aircraftRegistration;
        this.pilotID = pilotID;
        this.finalStatus = finalStatus;
        this.passed = passed;
        this.testedAt = testedAt;
        this.summary = summary;
        this.messages = List.copyOf(messages);
        this.totalDistanceKm = totalDistanceKm;
        this.loadedFuelKg = loadedFuelKg;
        this.estimatedFuelRequiredKg = estimatedFuelRequiredKg;
        this.estimatedFuelRemainingKg = estimatedFuelRemainingKg;
        this.weatherUsed = weatherUsed;
        this.weatherWindSpeed = weatherWindSpeed;
        this.weatherWindDirection = weatherWindDirection;
        this.weatherFuelFactor = weatherFuelFactor;
    }

    public static FlightPlanTestBoard from(final FlightPlan flightPlan,
                                           final FlightPlanTestResult result) {
        return new FlightPlanTestBoard(
                flightPlan.identity().toString(),
                flightPlan.routeCode().toString(),
                flightPlan.aircraftRegistration().code(),
                flightPlan.pilotID().toString(),
                flightPlan.status(),
                result.passed(),
                result.testedAt(),
                result.summary(),
                result.messages(),
                result.totalDistanceKm(),
                result.loadedFuel(),
                result.estimatedFuelRequired(),
                result.estimatedFuelRemaining(),
                result.weatherUsed(),
                result.weatherWindSpeed(),
                result.weatherWindDirection(),
                result.weatherFuelFactor()
        );
    }

    public String flightPlanID() {
        return flightPlanID;
    }

    public String routeCode() {
        return routeCode;
    }

    public String aircraftRegistration() {
        return aircraftRegistration;
    }

    public String pilotID() {
        return pilotID;
    }

    public FlightPlanStatus finalStatus() {
        return finalStatus;
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
        return messages;
    }

    public double totalDistanceKm() {
        return totalDistanceKm;
    }

    public double loadedFuelKg() {
        return loadedFuelKg;
    }

    public double estimatedFuelRequiredKg() {
        return estimatedFuelRequiredKg;
    }

    public double estimatedFuelRemainingKg() {
        return estimatedFuelRemainingKg;
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

    public boolean simulationExecuted() {
        return totalDistanceKm > 0.0
                || loadedFuelKg > 0.0
                || estimatedFuelRequiredKg > 0.0;
    }
}