package aisafe.maker.domain;

import aisafe.sharedkernel.Country;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(name = "MAKER")
public class Maker implements AggregateRoot<MakerID>, Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private MakerID makerID;

    private String name;


    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "country_name")),
            @AttributeOverride(name = "isoCode.code", column = @Column(name = "country_iso_code"))
    })
    private Country country;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "MAKER_TYPE", joinColumns = @JoinColumn(name = "maker_id"))
    @Column(name = "type")
    private Set<MakerType> makerType;

    protected Maker() {
        // for ORM
    }

    public Maker(final MakerID makerID, final String name,
                 final Country country, final Set<MakerType> makerType) {
        Preconditions.noneNull(makerID, name, country, makerType);
        Preconditions.nonEmpty(name, "Maker name cannot be empty");
        Preconditions.nonEmpty(makerType, "Maker must have at least one type");
        this.makerID = makerID;
        this.name = name;
        this.country = country;
        this.makerType = EnumSet.copyOf(makerType);
    }

    public boolean hasType(final MakerType type) {
        return makerType.contains(type);
    }

    public MakerID makerID() {
        return makerID;
    }

    public String name() {
        return name;
    }

    public Country country() {
        return country;
    }

    public Set<MakerType> makerTypes() {
        return Collections.unmodifiableSet(makerType);
    }

    @Override
    public MakerID identity() {
        return makerID;
    }

    @Override
    public boolean sameAs(Object other) {
        if (!(other instanceof Maker)) return false;
        final Maker that = (Maker) other;
        return this.makerID.equals(that.makerID);
    }

    @Override
    public String toString() {
        return name + " [" + makerID + "] - " + country;
    }
}