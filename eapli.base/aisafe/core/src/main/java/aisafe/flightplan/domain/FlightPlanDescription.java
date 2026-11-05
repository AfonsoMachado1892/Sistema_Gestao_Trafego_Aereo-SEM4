package aisafe.flightplan.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FlightPlanDescription implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    @Lob
    @Column(nullable = false)
    private String text;

    protected FlightPlanDescription() {
        // for ORM
    }

    private FlightPlanDescription(final String text) {
        Preconditions.nonEmpty(text, "Flight plan description must not be empty.");
        this.text = text;
    }

    public static FlightPlanDescription of(final String text) {
        return new FlightPlanDescription(text);
    }

    public String text() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof FlightPlanDescription)) return false;
        final FlightPlanDescription that = (FlightPlanDescription) o;
        return Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }
}