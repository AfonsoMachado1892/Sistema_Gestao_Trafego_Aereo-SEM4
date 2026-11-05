package aisafe.weather.application;

import java.util.List;

public interface WeatherDataImporter {

    List<WeatherDataDTO> readWeatherData(String filepath);
}
