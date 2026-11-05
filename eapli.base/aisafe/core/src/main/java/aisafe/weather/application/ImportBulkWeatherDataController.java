package aisafe.weather.application;

import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.sharedkernel.Coordinates;
import aisafe.usermanagement.domain.AiSafeRoles;
import aisafe.weather.domain.BoundingBox;
import aisafe.weather.domain.WeatherData;
import aisafe.weather.domain.WeatherDataRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.util.List;

public class ImportBulkWeatherDataController {

    private final AuthorizationService authz;
    private final WeatherDataRepository weatherDataRepository;
    private final WeatherDataImporterFactory factory;

    public ImportBulkWeatherDataController(final WeatherDataRepository weatherDataRepository) {
        this(
                AuthzRegistry.authorizationService(),
                weatherDataRepository,
                new WeatherDataImporterFactory()
        );
    }

    public ImportBulkWeatherDataController(final AuthorizationService authz,
                                           final WeatherDataRepository weatherDataRepository,
                                           final WeatherDataImporterFactory factory) {
        this.authz = authz;
        this.weatherDataRepository = weatherDataRepository;
        this.factory = factory;
    }

    public List<WeatherDataDTO> previewImport(final String filePath) {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.WEATHER_PERSON, AiSafeRoles.ADMIN);

        final WeatherDataImporter importer = factory.getImporter(filePath);
        return importer.readWeatherData(filePath);
    }

    public int confirmImport(final List<WeatherDataDTO> weatherDataDTOList) {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.WEATHER_PERSON, AiSafeRoles.ADMIN);

        int count = 0;

        for (final WeatherDataDTO dto : weatherDataDTOList) {
            final BoundingBox boundingBox = new BoundingBox(
                    Coordinates.of(dto.latStart(), dto.lonStart()),
                    Coordinates.of(dto.latEnd(), dto.lonEnd())
            );

            final WeatherData weatherData = new WeatherData(
                    AirControlAreaCode.of(dto.areaCode()),
                    dto.day(),
                    dto.startTime(),
                    dto.endTime(),
                    boundingBox,
                    dto.minAltitude(),
                    dto.maxAltitude(),
                    dto.windSpeed(),
                    dto.windDirection()
            );

            weatherDataRepository.save(weatherData);
            count++;
        }

        return count;
    }
}