package aisafe.weather.domain;


import aisafe.aircontrolarea.domain.AirControlAreaCode;
import eapli.framework.domain.repositories.DomainRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface WeatherDataRepository extends DomainRepository<WeatherDataID, WeatherData> {

    /**
     * Finds all weather data entries for a given air control area on a specific date.
     *
     * @param airControlAreaCode the area code to filter by
     * @param date               the date to filter by (matches the day of collectedAt)
     * @return iterable of matching WeatherData
     */
    Iterable<WeatherData> findByAreaAndDate(AirControlAreaCode airControlAreaCode, LocalDate date);

    Optional <WeatherData> findByID(WeatherDataID weatherDataID);
}