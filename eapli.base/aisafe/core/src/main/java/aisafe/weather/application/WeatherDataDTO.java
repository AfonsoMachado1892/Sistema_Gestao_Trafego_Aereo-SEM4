package aisafe.weather.application;

import java.time.LocalDate;
import java.time.LocalTime;

public record WeatherDataDTO(
        String areaCode,
        LocalDate day,
        LocalTime startTime,
        LocalTime endTime,
        double latStart,
        double lonStart,
        double latEnd,
        double lonEnd,
        int minAltitude,
        int maxAltitude,
        double windSpeed,
        double windDirection
) {}
