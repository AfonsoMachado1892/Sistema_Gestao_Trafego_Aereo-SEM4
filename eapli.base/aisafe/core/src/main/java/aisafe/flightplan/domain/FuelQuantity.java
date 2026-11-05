package aisafe.flightplan.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FuelQuantity implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    private double amount;

    @Enumerated(EnumType.STRING)
    private FuelUnit unit;

    protected FuelQuantity() {
        // for ORM
    }

    private FuelQuantity(final double amount, final FuelUnit unit) {
        Preconditions.ensure(amount > 0, "Fuel quantity must be strictly positive.");
        Preconditions.noneNull(unit, "Fuel unit must not be null.");

        this.amount = amount;
        this.unit = unit;
    }

    public static FuelQuantity of(final double amount, final FuelUnit unit) {
        return new FuelQuantity(amount, unit);
    }

    public double amount() {
        return amount;
    }

    public FuelUnit unit() {
        return unit;
    }

    @Override
    public String toString() {
        return amount + " " + unit;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof FuelQuantity)) return false;
        final FuelQuantity that = (FuelQuantity) o;
        return Double.compare(that.amount, amount) == 0
                && unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, unit);
    }
}