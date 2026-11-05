package aisafe.aircraftmodel.domain;

import aisafe.enginemodel.domain.EngineModel;
import aisafe.enginemodel.domain.EngineModelID;
import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerType;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.validations.Preconditions;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "AIRCRAFT_MODEL")
public class AircraftModel implements AggregateRoot<AircraftModelID>, Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private AircraftModelID modelID;

    @ManyToOne
    @JoinColumn(name = "maker_id")
    private Maker maker;

    @Enumerated(EnumType.STRING)
    private AircraftType type;

    private double emptyWeight;
    private double mtow;
    private double mzfw;
    private double maxFuelCapacity;
    private double serviceCeiling;
    private double cruiseSpeed;
    private double wingArea;
    private double dragCoefficient;
    private double liftCoefficient;
    private double maximumRange;
    private int maxPassengerCapacity;
    private int engineNumber;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "AIRCRAFT_MODEL_ENGINES",
            joinColumns = @JoinColumn(name = "aircraft_model_id"),
            inverseJoinColumns = @JoinColumn(name = "engine_model_id"))
    private Set<EngineModel> certifiedEngineModels;

    protected AircraftModel() {
        // for ORM
    }

    public AircraftModel(final AircraftModelID modelID,
                         final Maker maker,
                         final AircraftType type,
                         final Set<EngineModel> certifiedEngineModels,
                         final double emptyWeight,
                         final double mtow,
                         final double mzfw,
                         final double maxFuelCapacity,
                         final double serviceCeiling,
                         final double cruiseSpeed,
                         final double wingArea,
                         final double dragCoefficient,
                         final double liftCoefficient,
                         final double maximumRange,
                         final int maxPassengerCapacity,
                         final int engineNumber) {
        Preconditions.noneNull(modelID, maker, type, certifiedEngineModels);
        Preconditions.nonEmpty(certifiedEngineModels,
                "AircraftModel must have at least one certified engine model");
        final var engineType = certifiedEngineModels.iterator().next().type();
        Preconditions.ensure(
                certifiedEngineModels.stream().allMatch(e -> e.type().equals(engineType)),
                "All certified engine models must be of the same engine type");
        Preconditions.ensure(mtow > emptyWeight, "MTOW must be greater than empty weight");
        Preconditions.ensure(emptyWeight > 0, "Empty weight must be positive");
        Preconditions.ensure(mtow > 0, "MTOW must be positive");
        Preconditions.ensure(mzfw > 0, "MZFW must be positive");
        Preconditions.ensure(maxFuelCapacity > 0, "Max fuel capacity must be positive");
        Preconditions.ensure(serviceCeiling > 0, "Service ceiling must be positive");
        Preconditions.ensure(cruiseSpeed > 0, "Cruise speed must be positive");
        Preconditions.ensure(wingArea > 0, "Wing area must be positive");
        Preconditions.ensure(dragCoefficient > 0, "Drag coefficient must be positive");
        Preconditions.ensure(liftCoefficient > 0, "Lift coefficient must be positive");
        Preconditions.ensure(maximumRange > 0, "Maximum range must be positive");
        Preconditions.ensure(maxPassengerCapacity >= 0, "Max passenger capacity must be non-negative");
        Preconditions.ensure(engineNumber > 0, "Engine number must be positive");

        this.modelID = modelID;
        this.maker = maker;
        this.type = type;
        this.certifiedEngineModels = new HashSet<>(certifiedEngineModels);
        this.emptyWeight = emptyWeight;
        this.mtow = mtow;
        this.mzfw = mzfw;
        this.maxFuelCapacity = maxFuelCapacity;
        this.serviceCeiling = serviceCeiling;
        this.cruiseSpeed = cruiseSpeed;
        this.wingArea = wingArea;
        this.dragCoefficient = dragCoefficient;
        this.liftCoefficient = liftCoefficient;
        this.maximumRange = maximumRange;
        this.maxPassengerCapacity = maxPassengerCapacity;
        this.engineNumber = engineNumber;
    }

    public static AircraftModel of(final AircraftModelID modelID,
                                   final Maker maker,
                                   final AircraftType type,
                                   final Set<EngineModel> certifiedEngineModels,
                                   final double emptyWeight,
                                   final double mtow,
                                   final double mzfw,
                                   final double maxFuelCapacity,
                                   final double serviceCeiling,
                                   final double cruiseSpeed,
                                   final double wingArea,
                                   final double dragCoefficient,
                                   final double liftCoefficient,
                                   final double maximumRange,
                                   final int maxPassengerCapacity,
                                   final int engineNumber) {
        return new AircraftModel(modelID, maker, type, certifiedEngineModels,
                emptyWeight, mtow, mzfw, maxFuelCapacity, serviceCeiling, cruiseSpeed,
                wingArea, dragCoefficient, liftCoefficient, maximumRange,
                maxPassengerCapacity, engineNumber);
    }

    public void addCertifiedEngine(final EngineModel engineModel) {
        Preconditions.noneNull(engineModel);
        Preconditions.ensure(!hasCertifiedEngine(engineModel.identity()),
                "Engine model already certified for this aircraft model: " + engineModel.identity());
        final var existingEngineType = certifiedEngineModels.iterator().next().type();
        Preconditions.ensure(engineModel.type().equals(existingEngineType),
                "Engine type " + engineModel.type()
                        + " is not compatible with existing certified engine type "
                        + existingEngineType);
        certifiedEngineModels.add(engineModel);
    }

    public void removeCertifiedEngine(final EngineModel engineModel) {
        Preconditions.noneNull(engineModel);
        Preconditions.ensure(hasCertifiedEngine(engineModel.identity()),
                "Engine model is not certified for this aircraft model: " + engineModel.identity());
        Preconditions.ensure(certifiedEngineModels.size() > 1,
                "Cannot remove the last certified engine model from an aircraft model");
        certifiedEngineModels.removeIf(existing ->
                existing.identity().equals(engineModel.identity()));
    }

    public Maker maker() { return maker; }
    public AircraftType type() { return type; }
    public double emptyWeight() { return emptyWeight; }
    public double mtow() { return mtow; }
    public double mzfw() { return mzfw; }
    public double maxFuelCapacity() { return maxFuelCapacity; }
    public double serviceCeiling() { return serviceCeiling; }
    public double cruiseSpeed() { return cruiseSpeed; }
    public double wingArea() { return wingArea; }
    public double dragCoefficient() { return dragCoefficient; }
    public double liftCoefficient() { return liftCoefficient; }
    public double maximumRange() { return maximumRange; }
    public int maxPassengerCapacity() { return maxPassengerCapacity; }
    public int engineNumber() { return engineNumber; }

    public Set<EngineModel> certifiedEngineModels() {
        return Collections.unmodifiableSet(certifiedEngineModels);
    }

    public boolean hasCertifiedEngine(final EngineModel engineModel) {
        Preconditions.noneNull(engineModel);
        return hasCertifiedEngine(engineModel.identity());
    }

    public boolean hasCertifiedEngine(final EngineModelID engineModelID) {
        Preconditions.noneNull(engineModelID);
        return certifiedEngineModels.stream()
                .anyMatch(e -> e.identity().equals(engineModelID));
    }

    @Override
    public AircraftModelID identity() { return modelID; }

    @Override
    public boolean sameAs(Object other) {
        if (!(other instanceof AircraftModel)) return false;
        final AircraftModel that = (AircraftModel) other;
        return this.modelID.equals(that.modelID) && this.maker.identity().equals(that.maker.identity());
    }

    @Override
    public String toString() {
        return modelID + " [" + type + "] - Maker: " + maker.name();
    }
}