package aisafe.airport.domain;

import aisafe.sharedkernel.Country;
import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class Locality implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;
    private String cityName;
    private Country country;

    protected Locality(){

        //

    }

    public Locality(String cityName, Country country){

        Preconditions.nonEmpty(cityName, "City name must be filled.");
        Preconditions.noneNull(country, "Country must be filled.");

        this.cityName = cityName.trim();
        this.country = country;

    }

    public static Locality of(String cityName, Country country){

        return new Locality(cityName, country);

    }

    public String cityName(){

        return cityName;

    }

    public Country country(){

        return country;

    }

    @Override
    public String toString() {
        return cityName + ", " + country;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Locality)) {
            return false;
        }
        final Locality that = (Locality) o;
        return cityName.equalsIgnoreCase(that.cityName)
                && Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cityName.toLowerCase(), country);
    }
}
