package aisafe.airport.domain;


import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class Elevation implements Serializable, ValueObject {

    private static final long serialVersionUID = 1L;

    private double metersAboveTheSea;

    protected Elevation(){

        //

    }

    //Nota do programador: A altitude máxima que um aeroporto pode estar em relação ao nivel do mar é 9000 metros.

    public Elevation(double metersAboveTheSea){

        Preconditions.ensure(metersAboveTheSea >= -500 && metersAboveTheSea <= 9000, "Insere um valor válido.");

        this.metersAboveTheSea = metersAboveTheSea;

    }

    public static Elevation of(double metersAboveTheSea){

        return new Elevation(metersAboveTheSea);

    }

    public double metersAboveTheSea(){

        return metersAboveTheSea;

    }

    @Override
    public String toString() {
        return metersAboveTheSea + " m";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Elevation)) return false;
        Elevation that = (Elevation) o;
        return Double.compare(this.metersAboveTheSea, that.metersAboveTheSea) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(metersAboveTheSea);
    }

}
