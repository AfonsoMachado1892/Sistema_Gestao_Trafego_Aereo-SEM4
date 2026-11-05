package aisafe.aircraft.domain;


import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AircraftRegistration implements ValueObject, Serializable, Comparable<AircraftRegistration> {

    private static final long serialVersionUID = 1L;

    private String code;

    protected AircraftRegistration(){

        //

    }

    public AircraftRegistration(String code){

        Preconditions.nonEmpty(code, "Insert a valid code");

        this.code = code;

    }

    public static AircraftRegistration of(String code){

        return new AircraftRegistration(code);

    }

    public String code(){

        return code;

    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AircraftRegistration)) return false;
        final AircraftRegistration that = (AircraftRegistration) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public int compareTo(final AircraftRegistration other) {
        return this.code.compareTo(other.code);
    }


}
