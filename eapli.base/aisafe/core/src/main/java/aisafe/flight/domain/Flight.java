package aisafe.flight.domain;

import aisafe.aircraft.domain.AircraftRegistration;
import eapli.framework.domain.model.AggregateRoot;
import jakarta.persistence.*;

@Entity
public class Flight implements AggregateRoot<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private AircraftRegistration aircraftRegistration;

    @Enumerated(EnumType.STRING)
    private FlightStatus status;

    protected Flight() {
        // Required by ORM
    }

    public AircraftRegistration aircraftRegistration() {
        return aircraftRegistration;
    }

    public FlightStatus status() {
        return status;
    }

    public boolean isPendingOrDraft() {
        return status == FlightStatus.PENDING || status == FlightStatus.DRAFT;
    }

    @Override
    public boolean sameAs(final Object other) {
        if (!(other instanceof Flight)) {
            return false;
        }

        final Flight that = (Flight) other;
        return this.identity() != null && this.identity().equals(that.identity());
    }

    @Override
    public Long identity() {
        return this.id;
    }
}