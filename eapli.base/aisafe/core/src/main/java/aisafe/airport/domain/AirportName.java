package aisafe.airport.domain;

import aisafe.aircontrolarea.domain.AirControlAreaName;
import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AirportName implements Serializable, ValueObject, Comparable<AirportName> {

    private static final long serialVersionUID = 1L;

    private String name;

    protected AirportName(){

        //

    }

    public AirportName(String name){

        Preconditions.nonEmpty(name, "Insert a valid name.");
        this.name = name.trim();

    }

    public static AirportName of(String name){

        return new AirportName(name);

    }

    public String name(){

        return name;

    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AirportName)) return false;
        final AirportName that = (AirportName) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(final AirportName other) {
        return this.name.compareTo(other.name);
    }



}
