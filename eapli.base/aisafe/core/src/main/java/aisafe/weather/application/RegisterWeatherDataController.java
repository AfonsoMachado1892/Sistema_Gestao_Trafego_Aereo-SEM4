package aisafe.weather.application;

import aisafe.aircontrolarea.domain.AirControlArea;
import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.aircontrolarea.domain.AirControlAreaRepository;
import aisafe.sharedkernel.Coordinates;
import aisafe.usermanagement.domain.AiSafeRoles;
import aisafe.weather.domain.BoundingBox;
import aisafe.weather.domain.WeatherData;
import aisafe.weather.domain.WeatherDataRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.time.LocalDate;
import java.time.LocalTime;

public class RegisterWeatherDataController {

    private final AuthorizationService authz;
    private final AirControlAreaRepository airControlAreaRepository;
    private final WeatherDataRepository weatherDataRepository;

    public RegisterWeatherDataController(final AirControlAreaRepository airControlAreaRepository,
                                         final WeatherDataRepository weatherDataRepository) {
        this(
                AuthzRegistry.authorizationService(),
                airControlAreaRepository,
                weatherDataRepository
        );
    }

    public RegisterWeatherDataController(final AuthorizationService authz,
                                         final AirControlAreaRepository airControlAreaRepository,
                                         final WeatherDataRepository weatherDataRepository) {
        this.authz = authz;
        this.airControlAreaRepository = airControlAreaRepository;
        this.weatherDataRepository = weatherDataRepository;
    }

    public Iterable<AirControlArea> getAirControlAreas() {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.WEATHER_PERSON, AiSafeRoles.ADMIN);
        return airControlAreaRepository.findAll();
    }

    public void registerWeatherData(final String areaCode,
                                    final LocalDate day,
                                    final LocalTime startTime,
                                    final LocalTime endTime,
                                    final double latStart,
                                    final double lonStart,
                                    final double latEnd,
                                    final double lonEnd,
                                    final int minAltitude,
                                    final int maxAltitude,
                                    final double windSpeed,
                                    final double windDirection) {

        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.WEATHER_PERSON, AiSafeRoles.ADMIN);

        final AirControlArea airControlArea = getAirControlArea(areaCode);

        final BoundingBox boundingBox = new BoundingBox(
                Coordinates.of(latStart, lonStart),
                Coordinates.of(latEnd, lonEnd)
        );

        final WeatherData weatherData = new WeatherData(
                airControlArea.identity(),
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
    }

    private AirControlArea getAirControlArea(final String areaCode) {
        final AirControlAreaCode code = AirControlAreaCode.of(areaCode);
        return airControlAreaRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Air control area not found for code: " + areaCode));
    }
}