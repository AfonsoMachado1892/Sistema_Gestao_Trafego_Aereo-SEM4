package aisafe.flightplan.application;

import aisafe.flightplan.domain.FlightPlan;

import java.time.LocalDateTime;

/**
 * Application-layer representation of a {@link FlightPlan}, used to avoid
 * leaking the aggregate root (and its JPA-managed associations/mutators)
 * to the presentation layer.
 */
public final class FlightPlanDTO {

    private final String flightPlanID;
    private final String routeCode;
    private final String aircraftRegistration;
    private final String pilotID;
    private final LocalDateTime plannedDepartureDateTime;
    private final LocalDateTime plannedArrivalDateTime;
    private final double fuelAmount;
    private final String fuelUnit;
    private final String description;
    private final String status;
    private final LocalDateTime createdAt;
    private final String weatherDataID;

    public FlightPlanDTO(final String flightPlanID,
                         final String routeCode,
                         final String aircraftRegistration,
                         final String pilotID,
                         final LocalDateTime plannedDepartureDateTime,
                         final LocalDateTime plannedArrivalDateTime,
                         final double fuelAmount,
                         final String fuelUnit,
                         final String description,
                         final String status,
                         final LocalDateTime createdAt,
                         final String weatherDataID) {
        this.flightPlanID = flightPlanID;
        this.routeCode = routeCode;
        this.aircraftRegistration = aircraftRegistration;
        this.pilotID = pilotID;
        this.plannedDepartureDateTime = plannedDepartureDateTime;
        this.plannedArrivalDateTime = plannedArrivalDateTime;
        this.fuelAmount = fuelAmount;
        this.fuelUnit = fuelUnit;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.weatherDataID = weatherDataID;
    }

    public static FlightPlanDTO from(final FlightPlan flightPlan) {
        return new FlightPlanDTO(
                flightPlan.identity().toString(),
                flightPlan.routeCode().toString(),
                flightPlan.aircraftRegistration().code(),
                flightPlan.pilotID().toString(),
                flightPlan.plannedDepartureDateTime(),
                flightPlan.plannedArrivalDateTime(),
                flightPlan.fuelQuantity().amount(),
                flightPlan.fuelQuantity().unit().name(),
                flightPlan.description().text(),
                flightPlan.status().name(),
                flightPlan.createdAt(),
                flightPlan.weatherDataID() == null ? null : flightPlan.weatherDataID().toString()
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

    public LocalDateTime plannedDepartureDateTime() {
        return plannedDepartureDateTime;
    }

    public LocalDateTime plannedArrivalDateTime() {
        return plannedArrivalDateTime;
    }

    public double fuelAmount() {
        return fuelAmount;
    }

    public String fuelUnit() {
        return fuelUnit;
    }

    public String description() {
        return description;
    }

    public String status() {
        return status;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public String weatherDataID() {
        return weatherDataID;
    }

    @Override
    public String toString() {
        return flightPlanID + " - " + routeCode + " - " + status;
    }
}