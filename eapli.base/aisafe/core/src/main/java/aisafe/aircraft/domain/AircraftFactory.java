package aisafe.aircraft.domain;

import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.sharedkernel.Country;
import eapli.framework.domain.model.DomainFactory;

public class AircraftFactory implements DomainFactory<Aircraft> {

    private AircraftRegistration registration;
    private CabinConfiguration cabinConfiguration;
    private AirTransportCompany airTransportCompany;
    private AircraftModel aircraftModel;
    private Country country;
    private int numCrewElements;

    public AircraftFactory withRegistration(final String registration){
        this.registration = AircraftRegistration.of(registration);
        return this;
    }

    public AircraftFactory withCabinConfiguration(final CabinConfiguration cabinConfiguration) {
        this.cabinConfiguration = cabinConfiguration;
        return this;
    }

    public AircraftFactory withAirTransportCompany(final AirTransportCompany airTransportCompany) {
        this.airTransportCompany = airTransportCompany;
        return this;
    }

    public AircraftFactory withAircraftModel(final AircraftModel aircraftModel) {
        this.aircraftModel = aircraftModel;
        return this;
    }

    public AircraftFactory withCountry(final Country country) {
        this.country = country;
        return this;
    }

    public AircraftFactory withNumCrewElements(final int numCrewElements) {
        this.numCrewElements = numCrewElements;
        return this;
    }

    @Override
    public Aircraft build() {
        return Aircraft.of(
                registration,
                cabinConfiguration,
                airTransportCompany,
                aircraftModel,
                country,
                numCrewElements
        );
    }

}
