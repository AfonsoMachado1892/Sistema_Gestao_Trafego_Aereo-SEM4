package aisafe.flightroute.domain;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Schedule for a charter flight route.
 * Contains a specific departure and arrival date/time.
 */
@Embeddable
public class CharterSchedule extends Schedule {

    private static final long serialVersionUID = 1L;

    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    protected CharterSchedule() {
        // for ORM
    }

    private CharterSchedule(final LocalDateTime departureTime, final LocalDateTime arrivalTime) {
        Preconditions.noneNull(departureTime, "Departure time must not be null.");
        Preconditions.noneNull(arrivalTime, "Arrival time must not be null.");
        Preconditions.ensure(
                arrivalTime.isAfter(departureTime),
                "Arrival time must be after departure time."
        );
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public static CharterSchedule of(final LocalDateTime departureTime,
                                     final LocalDateTime arrivalTime) {
        return new CharterSchedule(departureTime, arrivalTime);
    }

    public LocalDateTime departureTime() {
        return departureTime;
    }

    public LocalDateTime arrivalTime() {
        return arrivalTime;
    }

    @Override
    public String toString() {
        return "CharterSchedule{departure=" + departureTime + ", arrival=" + arrivalTime + "}";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof CharterSchedule)) return false;
        final CharterSchedule that = (CharterSchedule) o;
        return Objects.equals(departureTime, that.departureTime)
                && Objects.equals(arrivalTime, that.arrivalTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(departureTime, arrivalTime);
    }

    @Override
    public boolean allows(final LocalDateTime departureDateTime) {
        Preconditions.noneNull(departureDateTime, "Departure date/time must not be null.");
        return this.departureTime.equals(departureDateTime);
    }
}
