package aisafe.infrastructure.bootstrappers;

import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.persistence.PersistenceContext;
import aisafe.sharedkernel.Coordinates;
import aisafe.weather.domain.BoundingBox;
import aisafe.weather.domain.WeatherData;
import aisafe.weather.domain.WeatherDataRepository;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@SuppressWarnings("squid:S106")
public class WeatherDataBootstrapper implements Action {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(WeatherDataBootstrapper.class);

    private final WeatherDataRepository weatherDataRepository =
            PersistenceContext.repositories().weatherData();

    @Override
    public boolean execute() {

        final List<LocalDate> demoDates = List.of(
                LocalDate.of(2025, 4, 4),
                LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 15),
                LocalDate.of(2026, 6, 16),
                LocalDate.of(2026, 7, 14)
        );

        for (final LocalDate date : demoDates) {
            registerAllAreasFor(date);
        }

        return true;
    }

    private void registerAllAreasFor(final LocalDate day) {
        /*
         * Format:
         * code, southwestLat, southwestLon, northeastLat, northeastLon,
         * windSpeedKts, windDirectionDeg
         *
         * The geographical limits mirror AirControlAreaBootstrapper.
         */

        register("LPPC", day, 30.0, -32.0, 43.0, -5.0, 22.10, 245.00);
        register("LFFF", day, 44.0, -4.5, 51.5, 7.5, 18.60, 260.00);
        register("EDGG", day, 47.0, 7.6, 53.5, 14.5, 16.20, 235.00);
        register("KZNY", day, 24.0, -85.0, 47.0, -65.0, 28.40, 280.00);
        register("KZKC", day, 30.0, -115.0, 53.0, -86.0, 24.90, 310.00);
        register("KZLA", day, 18.0, -160.0, 42.0, -115.0, 19.30, 250.00);
        register("SBAZ", day, -30.0, -50.0, -15.0, -35.0, 14.80, 135.00);
        register("FAJA", day, -35.0, 10.0, 5.0, 35.0, 21.70, 160.00);
        register("UUWV", day, 40.0, 30.0, 60.0, 80.0, 26.10, 285.00);
        register("RJTG", day, 20.0, 85.0, 45.0, 145.0, 31.40, 95.00);
        register("WSJC", day, -2.0, 100.0, 5.0, 110.0, 12.70, 210.00);
        register("YMMM", day, -45.0, 115.0, -10.0, 155.0, 34.20, 275.00);
        register("LECM", day, 35.0, -4.9, 42.9, 4.9, 17.90, 240.00);
        register("GCCC", day, 26.0, -18.5, 30.0, -13.0, 29.60, 45.00);
        register("EGPX", day, 52.0, -25.0, 66.0, -1.0, 36.80, 295.00);
        register("ENOR", day, 60.0, 5.0, 72.0, 32.0, 32.50, 315.00);
        register("EHAA", day, 51.6, 3.0, 53.8, 7.5, 20.30, 255.00);
        register("LIMM", day, 35.0, 7.6, 47.0, 18.5, 15.60, 225.00);
        register("MMFR", day, 14.0, -114.9, 29.9, -86.0, 18.40, 80.00);
        register("SKED", day, -5.0, -82.0, 13.0, -66.0, 11.90, 120.00);
        register("SBRE", day, -15.0, -40.0, 0.0, -30.0, 16.80, 110.00);
    }

    private void register(final String areaCodeValue,
                          final LocalDate day,
                          final double southwestLatitude,
                          final double southwestLongitude,
                          final double northeastLatitude,
                          final double northeastLongitude,
                          final double baseWindSpeedKts,
                          final double baseWindDirectionDegrees) {
        try {
            final AirControlAreaCode areaCode = AirControlAreaCode.of(areaCodeValue);

            final double windSpeed = adjustedWindSpeed(baseWindSpeedKts, day);
            final double windDirection = adjustedWindDirection(baseWindDirectionDegrees, day);

            /*
             * Full-day forecast window.
             * The repository search is by area + date, so this gives US082 one useful
             * weather option regardless of the flight's exact departure time.
             */
            final LocalTime startTime = LocalTime.of(0, 0);
            final LocalTime endTime = LocalTime.of(23, 59);

            final int minAltitude = 0;
            final int maxAltitude = 41000;

            if (alreadyExists(areaCode, day, startTime, endTime, minAltitude, maxAltitude)) {
                LOGGER.debug("»»» WeatherData already exists for area {} on {}",
                        areaCode, day);
                return;
            }

            final BoundingBox boundingBox = new BoundingBox(
                    Coordinates.of(southwestLatitude, southwestLongitude),
                    Coordinates.of(northeastLatitude, northeastLongitude)
            );

            final WeatherData weatherData = new WeatherData(
                    areaCode,
                    day,
                    startTime,
                    endTime,
                    boundingBox,
                    minAltitude,
                    maxAltitude,
                    windSpeed,
                    windDirection
            );

            weatherDataRepository.save(weatherData);

            LOGGER.debug("»»» WeatherData registered: {} | area {} | day {} | wind {} kts {}°",
                    weatherData.identity(),
                    areaCode,
                    day,
                    windSpeed,
                    windDirection);

        } catch (final Exception e) {
            LOGGER.warn("WeatherData skipped for area [{}] and day [{}]: {}",
                    areaCodeValue,
                    day,
                    e.getMessage());
        }
    }

    private boolean alreadyExists(final AirControlAreaCode areaCode,
                                  final LocalDate day,
                                  final LocalTime startTime,
                                  final LocalTime endTime,
                                  final int minAltitude,
                                  final int maxAltitude) {
        for (final WeatherData weatherData : weatherDataRepository.findByAreaAndDate(areaCode, day)) {
            final boolean sameTime =
                    weatherData.startTime().equals(startTime)
                            && weatherData.endTime().equals(endTime);

            final boolean sameAltitude =
                    weatherData.minAltitude() == minAltitude
                            && weatherData.maxAltitude() == maxAltitude;

            if (sameTime && sameAltitude) {
                return true;
            }
        }

        return false;
    }

    private double adjustedWindSpeed(final double baseWindSpeedKts,
                                     final LocalDate day) {
        /*
         * Small deterministic variation so every demo date does not look identical.
         */
        final int dayOffset = day.getDayOfMonth() % 5;
        final double variation = (dayOffset - 2) * 1.35;

        final double value = baseWindSpeedKts + variation;

        return Math.max(3.0, round2(value));
    }

    private double adjustedWindDirection(final double baseWindDirectionDegrees,
                                         final LocalDate day) {
        /*
         * Small deterministic variation by day-of-month.
         */
        final int dayOffset = day.getDayOfMonth() % 7;
        final double value = baseWindDirectionDegrees + (dayOffset - 3) * 7.5;

        return round2(normalizeDegrees(value));
    }

    private double normalizeDegrees(final double degrees) {
        final double normalized = degrees % 360.0;
        return normalized < 0 ? normalized + 360.0 : normalized;
    }

    private double round2(final double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}