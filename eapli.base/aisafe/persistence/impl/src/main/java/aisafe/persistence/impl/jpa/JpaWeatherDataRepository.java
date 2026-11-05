package aisafe.persistence.impl.jpa;

import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.weather.domain.WeatherData;
import aisafe.weather.domain.WeatherDataID;
import aisafe.weather.domain.WeatherDataRepository;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

public class JpaWeatherDataRepository
        extends AiSafeJpaRepositoryBase<WeatherData, WeatherDataID, WeatherDataID>
        implements WeatherDataRepository {

    public JpaWeatherDataRepository(final String persistenceUnitName, final Map extendedProperties) {
        super(persistenceUnitName, extendedProperties, "weatherDataID");
    }

    @Override
    public Iterable<WeatherData> findByAreaAndDate(final AirControlAreaCode airControlAreaCode,
                                                   final LocalDate date) {
        final TypedQuery<WeatherData> query = entityManager().createQuery(
                "SELECT w FROM WeatherData w WHERE w.airControlAreaCode = :code " +
                        "AND  w.day = :date",
                WeatherData.class);
        query.setParameter("code", airControlAreaCode);
        query.setParameter("date", date);
        return query.getResultList();
    }

    @Override
    public Optional<WeatherData> findByID(WeatherDataID weatherDataID) {
        final TypedQuery<WeatherData> query = entityManager().createQuery(
                "SELECT w FROM WeatherData w WHERE w.weatherDataID = :id",
                WeatherData.class);
        query.setParameter("id", weatherDataID);
        return query.getResultStream().findFirst();
    }

}