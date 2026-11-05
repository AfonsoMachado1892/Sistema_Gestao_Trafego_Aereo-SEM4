package aisafe.enginemodel.domain;

import aisafe.maker.domain.Maker;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ENGINE_MODEL")
public class EngineModel implements AggregateRoot<EngineModelID>, Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private EngineModelID engineModelID;

    private String name;

    @Enumerated(EnumType.STRING)
    private EngineType type;

    private double power;

    @Enumerated(EnumType.STRING)
    private FuelType fuel;

    private double tsfc;

    @ManyToOne
    @JoinColumn(name = "maker_id")
    private Maker maker;

    protected EngineModel() {
        // for ORM
    }

    public EngineModel(final EngineModelID engineModelID,
                       final String name,
                       final EngineType type,
                       final double power,
                       final FuelType fuel,
                       final double tsfc,
                       final Maker maker) {
        Preconditions.noneNull(engineModelID, name, type, fuel, maker);
        Preconditions.nonEmpty(name, "Engine model name cannot be empty");
        Preconditions.ensure(power > 0, "Power must be positive");
        Preconditions.ensure(tsfc > 0, "TSFC must be positive");
        this.engineModelID = engineModelID;
        this.name = name;
        this.type = type;
        this.power = power;
        this.fuel = fuel;
        this.tsfc = tsfc;
        this.maker = maker;
    }

    public static EngineModel of(final EngineModelID engineModelID,
                                 final String name,
                                 final EngineType type,
                                 final double power,
                                 final FuelType fuel,
                                 final double tsfc,
                                 final Maker maker) {
        return new EngineModel(engineModelID, name, type, power, fuel, tsfc, maker);
    }

    public String name() {
        return name;
    }

    public EngineType type() {
        return type;
    }

    public double power() {
        return power;
    }

    public FuelType fuel() {
        return fuel;
    }

    public double tsfc() {
        return tsfc;
    }

    public Maker maker() {
        return maker;
    }

    @Override
    public EngineModelID identity() {
        return engineModelID;
    }

    @Override
    public boolean sameAs(Object other) {
        if (!(other instanceof EngineModel)) return false;
        final EngineModel that = (EngineModel) other;
        return this.name.equals(that.name) && this.maker.identity().equals(that.maker.identity());
    }

    @Override
    public String toString() {
        return name + " [" + engineModelID + "] - " + type + " - " + fuel;
    }
}