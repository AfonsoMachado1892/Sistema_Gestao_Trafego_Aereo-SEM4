package aisafe.aircontrolarea.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.io.Serializable;
import java.util.Objects;


@Embeddable
public class AirControlAreaName implements Serializable, ValueObject, Comparable<AirControlAreaName> {

    private static final long serialVersionUID = 1L;

     private String name;

    protected AirControlAreaName(){

        //

    }

    public AirControlAreaName(String name){

        Preconditions.nonEmpty(name, "Insert a valid name.");

        this.name = name.trim();

    }

    public static AirControlAreaName of(String name){

        return new AirControlAreaName(name);

    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof AirControlAreaName)) return false;
        final AirControlAreaName that = (AirControlAreaName) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(final AirControlAreaName other) {
        return this.name.compareTo(other.name);
    }

}
