package aisafe.airport.domain;


import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AirportIcaoCode implements Serializable, ValueObject {

    private static final long serialVersionUID = 1L;

    private String code;

    protected AirportIcaoCode(){

        //

    }

    public AirportIcaoCode(String code){

        Preconditions.nonEmpty(code, "Icao Code must be filled.");
        Preconditions.ensure(code.matches("[A-Za-z]{4}"), "Airport ICAO code must have exactly 4 letters.");
        this.code = code.trim().toUpperCase();

    }

    public static AirportIcaoCode of(String code){

        return new AirportIcaoCode(code);

    }

    public String code(){

        return code;

    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AirportIcaoCode)) return false;
        AirportIcaoCode that = (AirportIcaoCode) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }


}
