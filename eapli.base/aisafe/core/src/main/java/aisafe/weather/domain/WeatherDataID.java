package aisafe.weather.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.util.UUID;

@Embeddable
public class WeatherDataID implements ValueObject, Comparable<WeatherDataID> {

    @Serial
    private static final long serialVersionUID = 1L;

    private String number;

    protected WeatherDataID() {
        // for ORM
    }

    private WeatherDataID(final String number) {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("WeatherDataID cannot be null or blank.");
        }
        this.number = number;
    }

    public static WeatherDataID newID() {
        return new WeatherDataID(UUID.randomUUID().toString());
    }

    public static WeatherDataID of(final String number) {
        return new WeatherDataID(number);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WeatherDataID that)) return false;
        return number.equals(that.number);
    }

    @Override
    public int hashCode() {
        return number.hashCode();
    }

    @Override
    public String toString() {
        return number;
    }

    @Override
    public int compareTo(WeatherDataID o) {
       return this.number.compareTo(o.number);
    }
}