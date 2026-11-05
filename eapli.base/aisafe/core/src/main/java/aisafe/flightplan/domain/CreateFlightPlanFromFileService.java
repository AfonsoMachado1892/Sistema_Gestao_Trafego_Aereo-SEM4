package aisafe.flightplan.domain;

import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.aircraft.domain.AircraftRepository;
import aisafe.airport.domain.Airport;
import aisafe.airport.domain.AirportRepository;
import aisafe.flightplan.application.FlightPlanDTO;
import aisafe.flightroute.domain.FlightRoute;
import aisafe.flightroute.domain.FlightRouteRepository;
import aisafe.flightroute.domain.RouteCode;
import aisafe.pilot.domain.Pilot;
import aisafe.sharedkernel.Coordinates;
import aisafe.weather.domain.BoundingBox;
import aisafe.weather.domain.WeatherData;
import aisafe.weather.domain.WeatherDataRepository;
import dsl.model.ImportedFlightPlan;
import dsl.model.ImportedFuel;
import dsl.model.ImportedLeg;
import dsl.model.ImportedSegment;
import dsl.parser.FlightPlanImportService;
import eapli.framework.validations.Preconditions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CreateFlightPlanFromFileService {

    private static final double MPS_TO_KNOTS = 1.9438444924406;

    private final FlightPlanRepository flightPlanRepository;
    private final FlightRouteRepository flightRouteRepository;
    private final AircraftRepository aircraftRepository;
    private final AirportRepository airportRepository;
    private final WeatherDataRepository weatherDataRepository;
    private final FlightPlanImportService importService;

    public CreateFlightPlanFromFileService(final FlightPlanRepository flightPlanRepository,
                                           final FlightRouteRepository flightRouteRepository,
                                           final AircraftRepository aircraftRepository,
                                           final AirportRepository airportRepository,
                                           final WeatherDataRepository weatherDataRepository) {
        Preconditions.noneNull(flightPlanRepository, "Flight plan repository must not be null.");
        Preconditions.noneNull(flightRouteRepository, "Flight route repository must not be null.");
        Preconditions.noneNull(aircraftRepository, "Aircraft repository must not be null.");
        Preconditions.noneNull(airportRepository, "Airport repository must not be null.");
        Preconditions.noneNull(weatherDataRepository, "Weather data repository must not be null.");

        this.flightPlanRepository = flightPlanRepository;
        this.flightRouteRepository = flightRouteRepository;
        this.aircraftRepository = aircraftRepository;
        this.airportRepository = airportRepository;
        this.weatherDataRepository = weatherDataRepository;
        this.importService = new FlightPlanImportService();
    }

    public ImportFlightPlanResult createFromFile(final Pilot authenticatedPilot,
                                                 final String filePath) throws IOException {
        Preconditions.noneNull(authenticatedPilot, "Authenticated pilot must not be null.");

        final List<String> errors = new ArrayList<>();

        validateFileBeforeProcessing(filePath, errors);

        if (!errors.isEmpty()) {
            return ImportFlightPlanResult.failure(errors);
        }

        final Path path = Path.of(filePath);
        final String originalDslContent = Files.readString(path);

        final FlightPlanImportService.ImportResult importResult =
                importService.importFromFile(path);

        if (!importResult.isSuccess()) {
            return ImportFlightPlanResult.failure(importResult.errors());
        }

        final ImportedFlightPlan importedFlightPlan = importResult.flightPlan();

        final FlightPlan flightPlan;

        try {
            flightPlan = createDomainFlightPlan(
                    authenticatedPilot,
                    importedFlightPlan,
                    originalDslContent
            );
        } catch (final IllegalArgumentException ex) {
            return ImportFlightPlanResult.failure(List.of(ex.getMessage()));
        }

        return ImportFlightPlanResult.success(
                FlightPlanDTO.from(flightPlan),
                importedSegmentViews(importedFlightPlan)
        );
    }

    private void validateFileBeforeProcessing(final String filePath,
                                              final List<String> errors) {
        if (filePath == null || filePath.isBlank()) {
            errors.add("FILE_ERROR at line 0, column 0: File path cannot be empty.");
            return;
        }

        final Path path = Path.of(filePath);

        if (!Files.exists(path)) {
            errors.add("FILE_ERROR at line 0, column 0: File does not exist.");
            return;
        }

        if (!Files.isRegularFile(path)) {
            errors.add("FILE_ERROR at line 0, column 0: Path does not reference a regular file.");
            return;
        }

        if (!Files.isReadable(path)) {
            errors.add("FILE_ERROR at line 0, column 0: File is not readable.");
            return;
        }

        if (!path.getFileName().toString().toLowerCase().endsWith(".flight")) {
            errors.add("FILE_FORMAT_ERROR at line 0, column 0: Unsupported file format. Currently supported formats: .flight.");
            return;
        }

        try {
            if (Files.size(path) == 0) {
                errors.add("FILE_ERROR at line 0, column 0: File cannot be empty.");
            }
        } catch (IOException e) {
            errors.add("FILE_ERROR at line 0, column 0: Could not inspect file size.");
        }
    }

    private FlightPlan createDomainFlightPlan(final Pilot authenticatedPilot,
                                              final ImportedFlightPlan importedFlightPlan,
                                              final String originalDslContent) {
        validateAuthenticatedPilot(authenticatedPilot);
        validateImportedFlightPlanShape(importedFlightPlan);

        final RouteCode routeCode = resolveRouteCode(importedFlightPlan.flightId());

        final FlightRoute route = flightRouteRepository.findByRouteCode(routeCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "ROUTE_ERROR: No existing flight route was found for route code "
                                + routeCode + "."
                ));

        validateRouteCoherence(importedFlightPlan, route);
        validateRouteAvailability(importedFlightPlan, route);
        validatePilotCoherence(authenticatedPilot, route);

        final Aircraft aircraft = resolveAndValidateAircraft(importedFlightPlan, route);

        final ImportedLeg leg = importedFlightPlan.legs().get(0);
        final FuelQuantity fuelQuantity = mapFuel(leg.fuel());

        final FlightPlan flightPlan = FlightPlan.draft(
                FlightPlanID.newID(),
                route,
                aircraft,
                authenticatedPilot,
                leg.departureDateTime(),
                leg.arrivalDateTime(),
                fuelQuantity,
                FlightPlanDescription.of(originalDslContent)
        );

        final FlightPlan savedFlightPlan = flightPlanRepository.save(flightPlan);

        associateGeneratedWeatherDataIfPossible(
                savedFlightPlan,
                importedFlightPlan,
                route
        );

        return flightPlanRepository.save(savedFlightPlan);
    }

    private void validateAuthenticatedPilot(final Pilot authenticatedPilot) {
        Preconditions.ensure(
                authenticatedPilot.isActive(),
                "PILOT_ERROR: Authenticated pilot must be active."
        );
    }

    private void validateImportedFlightPlanShape(final ImportedFlightPlan importedFlightPlan) {
        Preconditions.noneNull(importedFlightPlan, "Imported flight plan must not be null.");

        Preconditions.ensure(
                importedFlightPlan.legs().size() == 1,
                "DOMAIN_INTEGRATION_ERROR: Only direct flight plans with exactly one leg can currently be imported into the system."
        );
    }

    private RouteCode resolveRouteCode(final String importedFlightId) {
        Preconditions.nonEmpty(importedFlightId, "ROUTE_ERROR: Imported flight identifier cannot be empty.");

        String candidate = importedFlightId.trim().toUpperCase();

        if (candidate.matches("^[A-Z]{2}\\d{1,4}[A-Z]$")) {
            candidate = candidate.substring(0, candidate.length() - 1);
        }

        return RouteCode.of(candidate);
    }

    private void validateRouteCoherence(final ImportedFlightPlan importedFlightPlan,
                                        final FlightRoute route) {
        final String importedOrigin = importedFlightPlan.route().originAirportCode();
        final String importedDestination = importedFlightPlan.route().destinationAirportCode();

        final String systemOrigin = route.departureAirportIataCode().toString();
        final String systemDestination = route.arrivalAirportIataCode().toString();

        Preconditions.ensure(
                systemOrigin.equalsIgnoreCase(importedOrigin)
                        && systemDestination.equalsIgnoreCase(importedDestination),
                "ROUTE_ERROR: Imported route "
                        + importedOrigin + " -> " + importedDestination
                        + " does not match system route "
                        + route.routeCode() + " ("
                        + systemOrigin + " -> " + systemDestination + ")."
        );

        Preconditions.ensure(
                route.flightType().name().equalsIgnoreCase(importedFlightPlan.flightType()),
                "ROUTE_ERROR: Imported flight type "
                        + importedFlightPlan.flightType().toUpperCase()
                        + " does not match system route type "
                        + route.flightType().name() + "."
        );
    }

    private void validateRouteAvailability(final ImportedFlightPlan importedFlightPlan,
                                           final FlightRoute route) {
        final ImportedLeg leg = importedFlightPlan.legs().get(0);
        final LocalDateTime departure = leg.departureDateTime();

        Preconditions.ensure(
                route.isActiveOn(departure.toLocalDate()),
                "ROUTE_ERROR: Flight route is not active on the planned departure date."
        );

        Preconditions.ensure(
                route.allowsDepartureAt(departure),
                "ROUTE_ERROR: The planned departure date/time is not allowed by the route schedule."
        );
    }

    private void validatePilotCoherence(final Pilot authenticatedPilot,
                                        final FlightRoute route) {
        Preconditions.ensure(
                authenticatedPilot.belongsTo(route.airTransportCompanyIataCode()),
                "PILOT_ERROR: Authenticated pilot must belong to the route's company."
        );
    }

    private Aircraft resolveAndValidateAircraft(final ImportedFlightPlan importedFlightPlan,
                                                final FlightRoute route) {
        final AircraftRegistration registration =
                AircraftRegistration.of(importedFlightPlan.aircraftRegistration());

        final Aircraft aircraft = aircraftRepository.findByRegistration(registration)
                .orElseThrow(() -> new IllegalArgumentException(
                        "AIRCRAFT_ERROR: Aircraft "
                                + importedFlightPlan.aircraftRegistration()
                                + " was not found."
                ));

        Preconditions.ensure(
                aircraft.isOperational(),
                "AIRCRAFT_ERROR: Aircraft "
                        + importedFlightPlan.aircraftRegistration()
                        + " must be operational."
        );

        Preconditions.ensure(
                aircraft.belongsTo(route.airTransportCompanyIataCode()),
                "AIRCRAFT_ERROR: Aircraft "
                        + importedFlightPlan.aircraftRegistration()
                        + " does not belong to the route's company."
        );

        return aircraft;
    }

    private FuelQuantity mapFuel(final ImportedFuel importedFuel) {
        final FuelUnit unit = mapFuelUnit(importedFuel.unit());
        return FuelQuantity.of(importedFuel.quantity(), unit);
    }

    private FuelUnit mapFuelUnit(final String importedUnit) {
        if ("kg".equalsIgnoreCase(importedUnit)) {
            return FuelUnit.KG;
        }

        if ("l".equalsIgnoreCase(importedUnit)
                || "lt".equalsIgnoreCase(importedUnit)
                || "liter".equalsIgnoreCase(importedUnit)
                || "litre".equalsIgnoreCase(importedUnit)) {
            return FuelUnit.L;
        }

        throw new IllegalArgumentException(
                "FUEL_ERROR: Unsupported fuel unit: " + importedUnit
        );
    }

    private void associateGeneratedWeatherDataIfPossible(final FlightPlan flightPlan,
                                                         final ImportedFlightPlan importedFlightPlan,
                                                         final FlightRoute route) {
        final List<ImportedSegment> segments = importedFlightPlan.legs().stream()
                .flatMap(leg -> leg.segments().stream())
                .toList();

        if (segments.isEmpty()) {
            return;
        }

        final Airport arrivalAirport = airportRepository.findByIataCode(route.arrivalAirportIataCode())
                .orElseThrow(() -> new IllegalArgumentException(
                        "AIRPORT_ERROR: Arrival airport not found: "
                                + route.arrivalAirportIataCode()
                ));

        final WeatherData generatedWeatherData = buildWeatherDataFromSegments(
                importedFlightPlan,
                arrivalAirport,
                segments
        );

        final WeatherData savedWeatherData =
                weatherDataRepository.save(generatedWeatherData);

        flightPlan.insertWeatherData(savedWeatherData);
    }

    private WeatherData buildWeatherDataFromSegments(final ImportedFlightPlan importedFlightPlan,
                                                     final Airport arrivalAirport,
                                                     final List<ImportedSegment> segments) {
        final ImportedLeg leg = importedFlightPlan.legs().get(0);

        final BoundingBox boundingBox = buildBoundingBox(segments);

        final int minAltitude = (int) Math.round(
                segments.stream()
                        .mapToDouble(ImportedSegment::altitude)
                        .min()
                        .orElse(0.0)
        );

        final int maxAltitude = (int) Math.round(
                segments.stream()
                        .mapToDouble(ImportedSegment::altitude)
                        .max()
                        .orElse(0.0)
        );

        final double averageWindSpeedKts = weightedAverageWindSpeedKts(segments);
        final double averageWindDirection = weightedAverageWindDirection(segments);

        return new WeatherData(
                arrivalAirport.airControlArea().identity(),
                leg.departureDateTime().toLocalDate(),
                leg.departureDateTime().toLocalTime(),
                leg.arrivalDateTime().toLocalTime(),
                boundingBox,
                minAltitude,
                maxAltitude,
                averageWindSpeedKts,
                averageWindDirection
        );
    }

    private BoundingBox buildBoundingBox(final List<ImportedSegment> segments) {
        double minLat = Double.POSITIVE_INFINITY;
        double maxLat = Double.NEGATIVE_INFINITY;
        double minLon = Double.POSITIVE_INFINITY;
        double maxLon = Double.NEGATIVE_INFINITY;

        for (final ImportedSegment segment : segments) {
            minLat = Math.min(minLat, segment.start().getLatitude());
            minLat = Math.min(minLat, segment.end().getLatitude());

            maxLat = Math.max(maxLat, segment.start().getLatitude());
            maxLat = Math.max(maxLat, segment.end().getLatitude());

            minLon = Math.min(minLon, segment.start().getLongitude());
            minLon = Math.min(minLon, segment.end().getLongitude());

            maxLon = Math.max(maxLon, segment.start().getLongitude());
            maxLon = Math.max(maxLon, segment.end().getLongitude());
        }

        return new BoundingBox(
                Coordinates.of(minLat, minLon),
                Coordinates.of(maxLat, maxLon)
        );
    }

    private double weightedAverageWindSpeedKts(final List<ImportedSegment> segments) {
        double weightedSum = 0.0;
        double totalWeight = 0.0;

        for (final ImportedSegment segment : segments) {
            final double weight = segmentDistanceKm(segment);
            weightedSum += segment.windSpeed() * MPS_TO_KNOTS * weight;
            totalWeight += weight;
        }

        if (totalWeight == 0.0) {
            return segments.stream()
                    .mapToDouble(segment -> segment.windSpeed() * MPS_TO_KNOTS)
                    .average()
                    .orElse(0.0);
        }

        return weightedSum / totalWeight;
    }

    private double weightedAverageWindDirection(final List<ImportedSegment> segments) {
        double x = 0.0;
        double y = 0.0;
        double totalWeight = 0.0;

        for (final ImportedSegment segment : segments) {
            final double weight = segmentDistanceKm(segment);
            final double radians = Math.toRadians(segment.windDirection());

            x += Math.cos(radians) * weight;
            y += Math.sin(radians) * weight;
            totalWeight += weight;
        }

        if (totalWeight == 0.0) {
            return segments.stream()
                    .mapToDouble(ImportedSegment::windDirection)
                    .average()
                    .orElse(0.0);
        }

        return normalizeDegrees(Math.toDegrees(Math.atan2(y, x)));
    }

    private double segmentDistanceKm(final ImportedSegment segment) {
        final double earthRadiusKm = 6371.0;

        final double lat1 = Math.toRadians(segment.start().getLatitude());
        final double lat2 = Math.toRadians(segment.end().getLatitude());

        final double deltaLat = Math.toRadians(segment.end().getLatitude()
                - segment.start().getLatitude());
        final double deltaLon = Math.toRadians(segment.end().getLongitude()
                - segment.start().getLongitude());

        final double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return earthRadiusKm * c;
    }

    private double normalizeDegrees(final double degrees) {
        final double normalized = degrees % 360.0;
        return normalized < 0 ? normalized + 360.0 : normalized;
    }

    private List<ImportedSegmentView> importedSegmentViews(final ImportedFlightPlan importedFlightPlan) {
        final List<ImportedSegmentView> views = new ArrayList<>();

        for (final ImportedLeg leg : importedFlightPlan.legs()) {
            for (final ImportedSegment segment : leg.segments()) {
                views.add(ImportedSegmentView.from(segment));
            }
        }

        return views;
    }

    public static final class ImportedSegmentView {

        private final double startLatitude;
        private final double startLongitude;
        private final double endLatitude;
        private final double endLongitude;
        private final double altitudeMeters;
        private final double widthMeters;
        private final double windDirectionDegrees;
        private final double windSpeedMetersPerSecond;
        private final int line;
        private final int column;

        private ImportedSegmentView(final double startLatitude,
                                    final double startLongitude,
                                    final double endLatitude,
                                    final double endLongitude,
                                    final double altitudeMeters,
                                    final double widthMeters,
                                    final double windDirectionDegrees,
                                    final double windSpeedMetersPerSecond,
                                    final int line,
                                    final int column) {
            this.startLatitude = startLatitude;
            this.startLongitude = startLongitude;
            this.endLatitude = endLatitude;
            this.endLongitude = endLongitude;
            this.altitudeMeters = altitudeMeters;
            this.widthMeters = widthMeters;
            this.windDirectionDegrees = windDirectionDegrees;
            this.windSpeedMetersPerSecond = windSpeedMetersPerSecond;
            this.line = line;
            this.column = column;
        }

        public static ImportedSegmentView from(final ImportedSegment segment) {
            return new ImportedSegmentView(
                    segment.start().getLatitude(),
                    segment.start().getLongitude(),
                    segment.end().getLatitude(),
                    segment.end().getLongitude(),
                    segment.altitude(),
                    segment.width(),
                    segment.windDirection(),
                    segment.windSpeed(),
                    segment.line(),
                    segment.column()
            );
        }

        public double startLatitude() {
            return startLatitude;
        }

        public double startLongitude() {
            return startLongitude;
        }

        public double endLatitude() {
            return endLatitude;
        }

        public double endLongitude() {
            return endLongitude;
        }

        public double altitudeMeters() {
            return altitudeMeters;
        }

        public double widthMeters() {
            return widthMeters;
        }

        public double windDirectionDegrees() {
            return windDirectionDegrees;
        }

        public double windSpeedMetersPerSecond() {
            return windSpeedMetersPerSecond;
        }

        public int line() {
            return line;
        }

        public int column() {
            return column;
        }
    }

    public static final class ImportFlightPlanResult {

        private final boolean success;
        private final FlightPlanDTO flightPlan;
        private final List<String> errors;
        private final List<ImportedSegmentView> segments;

        private ImportFlightPlanResult(final boolean success,
                                       final FlightPlanDTO flightPlan,
                                       final List<String> errors,
                                       final List<ImportedSegmentView> segments) {
            this.success = success;
            this.flightPlan = flightPlan;
            this.errors = errors == null ? List.of() : List.copyOf(errors);
            this.segments = segments == null ? List.of() : List.copyOf(segments);
        }

        public static ImportFlightPlanResult success(final FlightPlanDTO flightPlan,
                                                     final List<ImportedSegmentView> segments) {
            return new ImportFlightPlanResult(
                    true,
                    flightPlan,
                    List.of(),
                    segments
            );
        }

        public static ImportFlightPlanResult failure(final List<String> errors) {
            return new ImportFlightPlanResult(
                    false,
                    null,
                    errors,
                    List.of()
            );
        }

        public boolean isSuccess() {
            return success;
        }

        public FlightPlanDTO flightPlan() {
            return flightPlan;
        }

        public List<String> errors() {
            return errors;
        }

        public List<ImportedSegmentView> segments() {
            return segments;
        }
    }
}