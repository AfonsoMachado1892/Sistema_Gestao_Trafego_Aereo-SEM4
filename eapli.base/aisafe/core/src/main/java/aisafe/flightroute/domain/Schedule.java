package aisafe.flightroute.domain;

import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Abstract schedule for a flight route.
 * Concrete subtypes: {@link RegularSchedule} and {@link CharterSchedule}.
 */
@MappedSuperclass
public abstract class Schedule implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Checks whether this schedule allows a flight plan departure
     * at the given date/time.
     */
    public abstract boolean allows(LocalDateTime departureDateTime);

}
