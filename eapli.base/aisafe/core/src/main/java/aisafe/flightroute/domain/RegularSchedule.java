package aisafe.flightroute.domain;

import eapli.framework.validations.Preconditions;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Schedule for a regular (recurring) flight route.
 * Contains at least one {@link ScheduleEntry}.
 */
@Embeddable
public class RegularSchedule extends Schedule {

    private static final long serialVersionUID = 1L;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "FLIGHT_ROUTE_ENTRIES",
            joinColumns = @JoinColumn(name = "route_code")
    )
    private List<ScheduleEntry> entries = new ArrayList<>();

    protected RegularSchedule() {
        // for ORM
    }

    private RegularSchedule(final List<ScheduleEntry> entries) {
        Preconditions.nonEmpty(entries, "A regular schedule must have at least one entry.");
        this.entries = new ArrayList<>(entries);
    }

    public static RegularSchedule of(final List<ScheduleEntry> entries) {
        return new RegularSchedule(entries);
    }

    public List<ScheduleEntry> entries() {
        return Collections.unmodifiableList(entries);
    }

    @Override
    public String toString() {
        return "RegularSchedule" + entries;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof RegularSchedule)) return false;
        return Objects.equals(entries, ((RegularSchedule) o).entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries);
    }

    @Override
    public boolean allows(final LocalDateTime departureDateTime) {
        Preconditions.noneNull(departureDateTime, "Departure date/time must not be null.");

        return entries.stream().anyMatch(entry ->
                entry.dayOfWeek().equals(departureDateTime.getDayOfWeek())
                        && entry.departureTime().equals(departureDateTime.toLocalTime())
        );
    }
}