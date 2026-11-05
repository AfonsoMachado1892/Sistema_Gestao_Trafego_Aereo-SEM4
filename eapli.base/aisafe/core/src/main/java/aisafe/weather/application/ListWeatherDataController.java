package aisafe.weather.application;


import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.usermanagement.domain.AiSafeRoles;
import aisafe.weather.domain.WeatherData;
import aisafe.weather.domain.WeatherDataID;
import aisafe.weather.domain.WeatherDataRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

import java.time.LocalDate;
import java.util.Optional;

public class ListWeatherDataController {

    private final AuthorizationService authz;

    private final WeatherDataRepository weatherDataRepository;

    public ListWeatherDataController(final AuthorizationService authz , final WeatherDataRepository weatherDataRepository) {
        this.authz = authz;
        this.weatherDataRepository = weatherDataRepository;

    }

    public Iterable<WeatherData> allWeatherData() {
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.WEATHER_PERSON, AiSafeRoles.ADMIN, AiSafeRoles.PILOT,AiSafeRoles.FLIGHT_CONTROL_OPERATOR);
        return weatherDataRepository.findAll();
    }

    public Iterable<WeatherData> filteredWeatherData(final String areaCode, final LocalDate date){
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.WEATHER_PERSON, AiSafeRoles.ADMIN, AiSafeRoles.PILOT,AiSafeRoles.FLIGHT_CONTROL_OPERATOR);
        return weatherDataRepository.findByAreaAndDate(AirControlAreaCode.of(areaCode),date);
    }

    public Optional<WeatherData> getWeatherDataByID(WeatherDataID weatherDataID){
        authz.ensureAuthenticatedUserHasAnyOf(AiSafeRoles.WEATHER_PERSON,AiSafeRoles.ADMIN,AiSafeRoles.PILOT);
        return weatherDataRepository.findByID(weatherDataID);
    }
}