package aisafe.persistence.impl.inmemory;

import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.weather.domain.WeatherData;
import aisafe.weather.domain.WeatherDataID;
import aisafe.weather.domain.WeatherDataRepository;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.time.LocalDate;
import java.util.Optional;

public class InMemoryWeatherDataRepository
        extends InMemoryDomainRepository<WeatherData, WeatherDataID>
        implements WeatherDataRepository {

    @Override
    public Iterable<WeatherData> findByAreaAndDate(final AirControlAreaCode airControlAreaCode,
                                                   final LocalDate date) {
        return match(w -> w.airControlAreaCode().equals(airControlAreaCode)
                && w.day().equals(date));
    }

    @Override
    public Optional<WeatherData> findByID(WeatherDataID weatherDataID) {
        return ofIdentity(weatherDataID);
    }
}