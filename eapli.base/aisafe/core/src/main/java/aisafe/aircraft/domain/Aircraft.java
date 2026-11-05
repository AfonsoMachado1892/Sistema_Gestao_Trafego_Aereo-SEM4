package aisafe.aircraft.domain;

import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.sharedkernel.Country;
import eapli.framework.domain.model.AggregateRoot;
import eapli.framework.domain.model.DomainEntities;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "AIRCRAFT")
public class Aircraft implements AggregateRoot<AircraftRegistration>, Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private AircraftRegistration aircraftCode;

    @Embedded
    private CabinConfiguration cabinConfiguration;

    @ManyToOne
    private AirTransportCompany airCompany;

    @ManyToOne
    private AircraftModel aircraftModel;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column = @Column(name = "country_name")),
            @AttributeOverride(name = "isoCode.code", column = @Column(name = "country_iso_code"))
    })
    private Country country;

    private int numCrewElements;

    @Enumerated(EnumType.STRING)
    private OperationalStatus status;

    protected Aircraft() {
        //
    }

    public Aircraft(AircraftRegistration aircraftCode, CabinConfiguration cabinConfiguration,
                    AirTransportCompany airCompany, AircraftModel aircraftModel,
                    Country country, int numCrewElements) {

        Preconditions.noneNull(aircraftCode, "Insert a valid aircraft code");
        Preconditions.noneNull(cabinConfiguration, "Insert a valid cabin configuration");
        Preconditions.noneNull(airCompany, "Insert a valid air company code");
        Preconditions.noneNull(aircraftModel, "Insert a valid aircraft model");
        Preconditions.noneNull(country, "Insert a valid country");
        Preconditions.ensure(numCrewElements > 0, "Insert a value > 0 in crew elements");

        this.aircraftCode = aircraftCode;
        this.cabinConfiguration = cabinConfiguration;
        this.airCompany = airCompany;
        this.aircraftModel = aircraftModel;
        this.country = country;
        this.numCrewElements = numCrewElements;
        this.status = OperationalStatus.ACTIVE;
    }

    public static Aircraft of(AircraftRegistration aircraftCode, CabinConfiguration cabinConfiguration,
                              AirTransportCompany airCompany, AircraftModel aircraftModel,
                              Country country, int numCrewElements) {
        return new Aircraft(aircraftCode, cabinConfiguration, airCompany, aircraftModel, country, numCrewElements);
    }

    /**
     * Decommissions this aircraft by transitioning its status to DECOMMISSIONED.
     *
     * The caller (controller/service) is responsible for verifying that no
     * pending flight plans exist before invoking this method.
     *
     * @throws IllegalStateException if the aircraft is already decommissioned
     */
    public void decommission() {
        Preconditions.ensure(
                this.status != OperationalStatus.DECOMMISSIONED,
                "Aircraft " + aircraftCode.code() + " is already decommissioned."
        );
        this.status = OperationalStatus.DECOMMISSIONED;
    }

    public AircraftRegistration aircraftCode() {
        return aircraftCode;
    }

    public CabinConfiguration cabinConfiguration() {
        return cabinConfiguration;
    }

    public AirTransportCompany airCompany() {
        return airCompany;
    }

    public AircraftModel aircraftModel() {
        return aircraftModel;
    }

    public Country country() {
        return country;
    }

    public int numCrewElements() {
        return numCrewElements;
    }

    public OperationalStatus status() {
        return status;
    }


    @Override
    public AircraftRegistration identity() {
        return aircraftCode;
    }

    @Override
    public boolean sameAs(final Object other) {
        if (!(other instanceof Aircraft that)) return false;
        if (this == that) return true;
        return this.aircraftCode.equals(that.aircraftCode)
                && this.airCompany.equals(that.airCompany)
                && this.aircraftModel.equals(that.aircraftModel)
                && this.cabinConfiguration.equals(that.cabinConfiguration)
                && this.country.equals(that.country)
                && this.numCrewElements == that.numCrewElements
                && this.status.equals(that.status);
    }

    @Override
    public boolean equals(final Object other) {
        return DomainEntities.areEqual(this, other);
    }

    @Override
    public int hashCode() {
        return DomainEntities.hashCode(this);
    }

    public boolean isOperational() {
        return status == OperationalStatus.ACTIVE;
    }

    public boolean belongsTo(final AirTransportCompanyCode companyCode) {
        Preconditions.noneNull(companyCode, "Company code must not be null.");
        return airCompany.identity().equals(companyCode);
    }
}