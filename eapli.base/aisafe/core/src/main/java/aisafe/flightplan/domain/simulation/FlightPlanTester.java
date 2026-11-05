package aisafe.flightplan.domain.simulation;

import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.flightplan.domain.FlightPlanTestResult;
import aisafe.flightplan.domain.FuelQuantity;
import aisafe.flightplan.domain.FuelUnit;
import aisafe.weather.domain.WeatherData;
import eapli.framework.validations.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FlightPlanTester {

    private static final double JET_A1_DENSITY_KG_PER_LITER = 0.804;
    private static final double KNOT_TO_KMH = 1.852;

    public FlightPlanTestResult test(final SimulatedFlightPlan simulatedFlightPlan,
                                     final AircraftModel aircraftModel) {
        return test(simulatedFlightPlan, aircraftModel, Optional.empty());
    }

    public FlightPlanTestResult test(final SimulatedFlightPlan simulatedFlightPlan,
                                     final AircraftModel aircraftModel,
                                     final Optional<WeatherData> weatherData) {
        Preconditions.noneNull(simulatedFlightPlan, "Simulated flight plan must not be null.");
        Preconditions.noneNull(aircraftModel, "Aircraft model must not be null.");
        Preconditions.noneNull(weatherData, "Weather data optional must not be null.");

        final List<String> messages = new ArrayList<>();
        final List<String> violations = new ArrayList<>();

        final double totalDistanceKm = simulatedFlightPlan.totalDistanceKm();
        final double loadedFuelKg = toKilograms(simulatedFlightPlan.loadedFuel());
        final double maxFuelCapacityKg = aircraftModel.maxFuelCapacity();
        final double maximumRangeKm = aircraftModel.maximumRange();

        final double baseEstimatedFuelRequiredKg =
                estimateFuelRequired(totalDistanceKm, maximumRangeKm, maxFuelCapacityKg);

        final double weatherFuelFactor = weatherData
                .map(weather -> calculateWeatherFuelFactor(simulatedFlightPlan, aircraftModel, weather))
                .orElse(1.0);

        final double estimatedFuelRequiredKg =
                baseEstimatedFuelRequiredKg * weatherFuelFactor;

        final double estimatedFuelRemainingKg =
                loadedFuelKg - estimatedFuelRequiredKg;

        if (weatherData.isPresent()) {
            final WeatherData weather = weatherData.get();

            messages.add(String.format(
                    "Weather data used: wind %.2f kts at %.2f°; fuel factor %.2f.",
                    weather.windSpeed(),
                    weather.windDirection(),
                    weatherFuelFactor
            ));
        } else {
            messages.add("No persisted WeatherData is associated with this flight plan; standard no-weather assumptions were used.");
            messages.add("Weather values shown during DSL import are not automatically linked as persisted WeatherData.");
        }

        validateFuelCapacity(violations, loadedFuelKg, maxFuelCapacityKg);
        validateMaximumRange(violations, totalDistanceKm, maximumRangeKm);
        validateEstimatedFuel(violations, estimatedFuelRequiredKg, loadedFuelKg);
        validateAltitude(violations, simulatedFlightPlan, aircraftModel, weatherData);

        if (violations.isEmpty()) {
            messages.add("Flight plan passed all validation and simulation checks.");
            messages.add(String.format("Total distance: %.2f km.", totalDistanceKm));
            messages.add(String.format("Loaded fuel: %.2f kg.", loadedFuelKg));
            messages.add(String.format("Estimated fuel required: %.2f kg.", estimatedFuelRequiredKg));
            messages.add(String.format("Estimated fuel remaining: %.2f kg.", estimatedFuelRemainingKg));

            return FlightPlanTestResult.passed(
                    "Flight plan test passed.",
                    messages,
                    totalDistanceKm,
                    loadedFuelKg,
                    estimatedFuelRequiredKg,
                    estimatedFuelRemainingKg,
                    weatherData.isPresent(),
                    weatherData.map(WeatherData::windSpeed).orElse(0.0),
                    weatherData.map(WeatherData::windDirection).orElse(0.0),
                    weatherFuelFactor
            );
        }

        messages.addAll(violations);

        return FlightPlanTestResult.failed(
                "Flight plan test failed.",
                messages,
                totalDistanceKm,
                loadedFuelKg,
                estimatedFuelRequiredKg,
                estimatedFuelRemainingKg,
                weatherData.isPresent(),
                weatherData.map(WeatherData::windSpeed).orElse(0.0),
                weatherData.map(WeatherData::windDirection).orElse(0.0),
                weatherFuelFactor
        );
    }

    private double estimateFuelRequired(final double totalDistanceKm,
                                        final double maximumRangeKm,
                                        final double maxFuelCapacityKg) {
        if (maximumRangeKm <= 0) {
            return Double.POSITIVE_INFINITY;
        }

        return (totalDistanceKm / maximumRangeKm) * maxFuelCapacityKg;
    }

    private void validateFuelCapacity(final List<String> violations,
                                      final double loadedFuelKg,
                                      final double maxFuelCapacityKg) {
        if (loadedFuelKg > maxFuelCapacityKg) {
            violations.add(String.format(
                    "Loaded fuel %.2f kg exceeds aircraft maximum fuel capacity %.2f kg.",
                    loadedFuelKg,
                    maxFuelCapacityKg
            ));
        }
    }

    private void validateMaximumRange(final List<String> violations,
                                      final double totalDistanceKm,
                                      final double maximumRangeKm) {
        if (totalDistanceKm > maximumRangeKm) {
            violations.add(String.format(
                    "Total distance %.2f km exceeds aircraft maximum range %.2f km.",
                    totalDistanceKm,
                    maximumRangeKm
            ));
        }
    }

    private void validateEstimatedFuel(final List<String> violations,
                                       final double estimatedFuelRequiredKg,
                                       final double loadedFuelKg) {
        if (estimatedFuelRequiredKg > loadedFuelKg) {
            violations.add(String.format(
                    "Estimated fuel required %.2f kg exceeds loaded fuel %.2f kg.",
                    estimatedFuelRequiredKg,
                    loadedFuelKg
            ));
        }
    }

    private void validateAltitude(final List<String> violations,
                                  final SimulatedFlightPlan simulatedFlightPlan,
                                  final AircraftModel aircraftModel,
                                  final Optional<WeatherData> weatherData) {
        for (final SimulatedFlightSegment segment : simulatedFlightPlan.segments()) {
            if (segment.altitudeMeters() > aircraftModel.serviceCeiling()) {
                violations.add(String.format(
                        "Segment altitude %.2f m exceeds aircraft service ceiling %.2f m.",
                        segment.altitudeMeters(),
                        aircraftModel.serviceCeiling()
                ));
            }

            if (weatherData.isPresent()) {
                final WeatherData weather = weatherData.get();

                if (segment.altitudeMeters() < weather.minAltitude()) {
                    violations.add(String.format(
                            "Segment altitude %.2f m is below weather/airspace minimum altitude %d m.",
                            segment.altitudeMeters(),
                            weather.minAltitude()
                    ));
                }

                if (segment.altitudeMeters() > weather.maxAltitude()) {
                    violations.add(String.format(
                            "Segment altitude %.2f m exceeds weather/airspace maximum altitude %d m.",
                            segment.altitudeMeters(),
                            weather.maxAltitude()
                    ));
                }
            }
        }
    }

    private double calculateWeatherFuelFactor(final SimulatedFlightPlan simulatedFlightPlan,
                                              final AircraftModel aircraftModel,
                                              final WeatherData weatherData) {
        final SimulatedFlightSegment firstSegment = simulatedFlightPlan.segments().get(0);

        final double bearing = bearingDegrees(
                firstSegment.start().latitude(),
                firstSegment.start().longitude(),
                firstSegment.end().latitude(),
                firstSegment.end().longitude()
        );

        final double angleDifference = smallestAngleDifferenceDegrees(
                bearing,
                weatherData.windDirection()
        );

        final double windComponentKmh =
                weatherData.windSpeed() * KNOT_TO_KMH * Math.cos(Math.toRadians(angleDifference));

        final double cruiseSpeedKmh = aircraftModel.cruiseSpeed();

        final double effectiveGroundSpeedKmh = Math.max(
                cruiseSpeedKmh + windComponentKmh,
                cruiseSpeedKmh * 0.5
        );

        final double rawFactor = cruiseSpeedKmh / effectiveGroundSpeedKmh;

        return clamp(rawFactor, 0.85, 1.25);
    }

    private double bearingDegrees(final double lat1,
                                  final double lon1,
                                  final double lat2,
                                  final double lon2) {
        final double phi1 = Math.toRadians(lat1);
        final double phi2 = Math.toRadians(lat2);
        final double deltaLambda = Math.toRadians(lon2 - lon1);

        final double y = Math.sin(deltaLambda) * Math.cos(phi2);
        final double x = Math.cos(phi1) * Math.sin(phi2)
                - Math.sin(phi1) * Math.cos(phi2) * Math.cos(deltaLambda);

        return normalizeDegrees(Math.toDegrees(Math.atan2(y, x)));
    }

    private double smallestAngleDifferenceDegrees(final double a,
                                                  final double b) {
        final double diff = Math.abs(normalizeDegrees(a) - normalizeDegrees(b));
        return diff > 180.0 ? 360.0 - diff : diff;
    }

    private double normalizeDegrees(final double degrees) {
        final double normalized = degrees % 360.0;
        return normalized < 0 ? normalized + 360.0 : normalized;
    }

    private double clamp(final double value,
                         final double min,
                         final double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double toKilograms(final FuelQuantity fuelQuantity) {
        if (fuelQuantity.unit() == FuelUnit.KG) {
            return fuelQuantity.amount();
        }

        if (fuelQuantity.unit() == FuelUnit.L) {
            return fuelQuantity.amount() * JET_A1_DENSITY_KG_PER_LITER;
        }

        throw new IllegalArgumentException("Unsupported fuel unit: " + fuelQuantity.unit());
    }
}