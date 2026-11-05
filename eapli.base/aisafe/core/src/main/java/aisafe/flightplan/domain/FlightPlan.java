package aisafe.flightplan.domain;

import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.flightroute.domain.FlightRoute;
import aisafe.flightroute.domain.RouteCode;
import aisafe.pilot.domain.Pilot;
import aisafe.pilot.domain.PilotID;
import aisafe.weather.domain.WeatherData;
import aisafe.weather.domain.WeatherDataID;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "FLIGHT_PLAN")
public class FlightPlan implements AggregateRoot<FlightPlanID>, Serializable {

    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @EmbeddedId
    private FlightPlanID flightPlanID;

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_code", nullable = false)
    private FlightRoute route;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aircraft_registration", nullable = false)
    private Aircraft aircraft;

    @ManyToOne(optional = false)
    @JoinColumn(name = "pilot_id", nullable = false)
    private Pilot pilot;

    @Column(nullable = false)
    private LocalDateTime plannedDepartureDateTime;

    @Column(nullable = false)
    private LocalDateTime plannedArrivalDateTime;

    @Embedded
    private FuelQuantity fuelQuantity;

    @Embedded
    private FlightPlanDescription description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlightPlanStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "weather_data_id")
    private WeatherData weatherData;

    @Embedded
    private FlightPlanTestResult lastTestResult;

    protected FlightPlan() {
        // for ORM
    }

    private FlightPlan(final FlightPlanID flightPlanID,
                       final FlightRoute route,
                       final Aircraft aircraft,
                       final Pilot pilot,
                       final LocalDateTime plannedDepartureDateTime,
                       final LocalDateTime plannedArrivalDateTime,
                       final FuelQuantity fuelQuantity,
                       final FlightPlanDescription description) {

        Preconditions.noneNull(flightPlanID, "Flight plan ID must not be null.");
        Preconditions.noneNull(route, "Route must not be null.");
        Preconditions.noneNull(aircraft, "Aircraft must not be null.");
        Preconditions.noneNull(pilot, "Pilot must not be null.");
        Preconditions.noneNull(plannedDepartureDateTime, "Planned departure date/time must not be null.");
        Preconditions.noneNull(plannedArrivalDateTime, "Planned arrival date/time must not be null.");
        Preconditions.noneNull(fuelQuantity, "Fuel quantity must not be null.");
        Preconditions.noneNull(description, "Flight plan description must not be null.");

        Preconditions.ensure(
                plannedArrivalDateTime.isAfter(plannedDepartureDateTime),
                "Planned arrival date/time must be after planned departure date/time."
        );

        this.flightPlanID = flightPlanID;
        this.route = route;
        this.aircraft = aircraft;
        this.pilot = pilot;
        this.plannedDepartureDateTime = plannedDepartureDateTime;
        this.plannedArrivalDateTime = plannedArrivalDateTime;
        this.fuelQuantity = fuelQuantity;
        this.description = description;
        this.status = FlightPlanStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    public static FlightPlan draft(final FlightPlanID flightPlanID,
                                   final FlightRoute route,
                                   final Aircraft aircraft,
                                   final Pilot pilot,
                                   final LocalDateTime plannedDepartureDateTime,
                                   final LocalDateTime plannedArrivalDateTime,
                                   final FuelQuantity fuelQuantity,
                                   final FlightPlanDescription description) {
        return new FlightPlan(
                flightPlanID,
                route,
                aircraft,
                pilot,
                plannedDepartureDateTime,
                plannedArrivalDateTime,
                fuelQuantity,
                description
        );
    }

    public FlightRoute route() {
        return route;
    }

    public Aircraft aircraft() {
        return aircraft;
    }

    public Pilot pilot() {
        return pilot;
    }

    public RouteCode routeCode() {
        return route.routeCode();
    }

    public AircraftRegistration aircraftRegistration() {
        return aircraft.aircraftCode();
    }

    public PilotID pilotID() {
        return pilot.pilotID();
    }

    public WeatherData weatherData() {
        return weatherData;
    }

    public WeatherDataID weatherDataID() {
        return weatherData == null ? null : weatherData.identity();
    }

    public LocalDateTime plannedDepartureDateTime() {
        return plannedDepartureDateTime;
    }

    public LocalDateTime plannedArrivalDateTime() {
        return plannedArrivalDateTime;
    }

    public FuelQuantity fuelQuantity() {
        return fuelQuantity;
    }

    public FlightPlanDescription description() {
        return description;
    }

    public FlightPlanStatus status() {
        return status;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public boolean isDraft() {
        return status == FlightPlanStatus.DRAFT;
    }

    public boolean isValidated() {
        return status == FlightPlanStatus.VALIDATED;
    }

    public void markTested() {
        Preconditions.ensure(
                status == FlightPlanStatus.DRAFT,
                "Only draft flight plans can be marked as tested."
        );
        this.status = FlightPlanStatus.TESTED;
    }

    public void markValidated() {
        Preconditions.ensure(
                status == FlightPlanStatus.TESTED,
                "Only tested flight plans can be validated."
        );
        this.status = FlightPlanStatus.VALIDATED;
    }

    public void reject() {
        Preconditions.ensure(
                status != FlightPlanStatus.CANCELLED,
                "A cancelled flight plan cannot be rejected."
        );
        this.status = FlightPlanStatus.REJECTED;
    }

    public void cancel() {
        Preconditions.ensure(
                status != FlightPlanStatus.CANCELLED,
                "Flight plan is already cancelled."
        );
        this.status = FlightPlanStatus.CANCELLED;
    }
    public void insertWeatherData(final WeatherData weatherData) {
        Preconditions.noneNull(weatherData, "Weather data must not be null.");

        Preconditions.ensure(
                status != FlightPlanStatus.CANCELLED && status != FlightPlanStatus.REJECTED,
                "Cannot insert weather data in a cancelled or rejected flight plan."
        );

        this.weatherData = weatherData;

        if (status == FlightPlanStatus.TESTED || status == FlightPlanStatus.VALIDATED) {
            this.status = FlightPlanStatus.DRAFT;
            this.lastTestResult = null;
        }
    }

    public FlightPlanTestResult lastTestResult() {
        return lastTestResult;
    }

    public void recordTestResult(final FlightPlanTestResult result) {
        Preconditions.noneNull(result, "Flight plan test result must not be null.");
        this.lastTestResult = result;
    }

    @Override
    public FlightPlanID identity() {
        return flightPlanID;
    }

    @Override
    public boolean sameAs(final Object other) {
        if (!(other instanceof FlightPlan)) {
            return false;
        }

        final FlightPlan that = (FlightPlan) other;
        return this.identity().equals(that.identity());
    }

    @Override
    public boolean equals(final Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public int hashCode() {
        return DomainEntities.hashCode(this);
    }

    @Override
    public String toString() {
        return flightPlanID + " - " + routeCode() + " - " + status;
    }

}