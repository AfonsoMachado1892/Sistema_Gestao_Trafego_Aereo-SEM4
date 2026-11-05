package aisafe.flightroute.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

/**
 * A single entry in a regular (recurring) schedule:
 * a day of the week paired with a departure time.
 */
@Embeddable
public class ScheduleEntry implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;

    protected ScheduleEntry() {
        // for ORM
    }

    private ScheduleEntry(final DayOfWeek dayOfWeek, final LocalTime departureTime) {
        Preconditions.noneNull(dayOfWeek, "Day of week must not be null.");
        Preconditions.noneNull(departureTime, "Departure time must not be null.");
        this.dayOfWeek = dayOfWeek;
        this.departureTime = departureTime;
    }

    public static ScheduleEntry of(final DayOfWeek dayOfWeek, final LocalTime departureTime) {
        return new ScheduleEntry(dayOfWeek, departureTime);
    }

    public DayOfWeek dayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime departureTime() {
        return departureTime;
    }

    @Override
    public String toString() {
        return dayOfWeek + " at " + departureTime;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduleEntry)) return false;
        final ScheduleEntry that = (ScheduleEntry) o;
        return Objects.equals(dayOfWeek, that.dayOfWeek)
                && Objects.equals(departureTime, that.departureTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayOfWeek, departureTime);
    }
}