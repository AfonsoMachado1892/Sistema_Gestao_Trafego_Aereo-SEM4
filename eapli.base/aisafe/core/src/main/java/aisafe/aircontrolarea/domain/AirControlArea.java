package aisafe.aircontrolarea.domain;

import aisafe.sharedkernel.Coordinates;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "AIR_CONTROL_AREA")
public class AirControlArea implements Serializable, AggregateRoot<AirControlAreaCode> {

    private static final long serialVersionUID = 1L;

    @Version
    private Long version;

    @EmbeddedId
    private AirControlAreaCode code;

    @Embedded
    private GeographicBoundary geographicBoundary;

    @Embedded
    private AirControlAreaName name;

    protected AirControlArea() {
        // for ORM
    }

    public AirControlArea(
            final AirControlAreaCode code,
            final GeographicBoundary geographicBoundary,
            final AirControlAreaName name
    ) {
        Preconditions.noneNull(code, "Provide a valid air control area code.");
        Preconditions.noneNull(geographicBoundary, "Provide a valid geographic boundary.");
        Preconditions.noneNull(name, "Provide a valid name.");

        this.code = code;
        this.geographicBoundary = geographicBoundary;
        this.name = name;

    }

    public static AirControlArea of(
            final AirControlAreaCode code,
            final GeographicBoundary geographicBoundary,
            final AirControlAreaName name
    ) {
        return new AirControlArea(code, geographicBoundary, name);
    }

    public AirControlAreaName name() {
        return name;
    }

    public AirControlAreaCode code() {
        return code;
    }


    public boolean contains(final Coordinates coordinates) {
        return this.geographicBoundary.contains(coordinates);
    }

    public boolean overlapsWith(final AirControlArea other) {
        return this.geographicBoundary.overlaps(other.geographicBoundary);
    }

    public String boundaryDescription(){

        return geographicBoundary.toString();
    }

    @Override
    public boolean equals(final Object o) {
        return DomainEntities.areEqual(this, o);
    }

    @Override
    public int hashCode() {
        return DomainEntities.hashCode(this);
    }

    @Override
    public AirControlAreaCode identity() {
        return code;
    }

    @Override
    public boolean sameAs(final Object other) {
        if (!(other instanceof AirControlArea)) {
            return false;
        }

        final AirControlArea that = (AirControlArea) other;
        return this.identity().equals(that.identity());
    }

    @Override
    public String toString() {
        return code + " - " + name;
    }

}