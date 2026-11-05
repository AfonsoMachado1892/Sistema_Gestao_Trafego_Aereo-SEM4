package aisafe.aircraft;

import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.aircraft.domain.AircraftRepository;
import aisafe.aircraft.domain.CabinConfiguration;
import aisafe.aircraft.domain.OperationalStatus;
import aisafe.aircraft.application.AircraftService;
import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.aircraftmodel.domain.AircraftModelID;
import aisafe.aircraftmodel.domain.AircraftType;
import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.enginemodel.domain.EngineModelID;
import aisafe.maker.domain.MakerID;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import aisafe.enginemodel.domain.EngineModel;
import aisafe.enginemodel.domain.EngineModelID;
import aisafe.enginemodel.domain.EngineType;
import aisafe.enginemodel.domain.FuelType;
import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerID;
import aisafe.maker.domain.MakerType;


import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AircraftTest {

    private AircraftModel aircraftModel;
    private AirTransportCompany airTransportCompany;
    private Country country;

    @BeforeEach
    void setUp() {
        aircraftModel = new AircraftModel(
                AircraftModelID.of("A320"),
                new Maker(
                        MakerID.of("AIRBUS"),
                        "Airbus",
                        Country.of(IsoCode.of("FR"), "France"),
                        Set.of(MakerType.AIRCRAFT)
                ),
                AircraftType.PASSENGER,
                Set.of(new EngineModel(
                        EngineModelID.of("CFM56"),
                        "CFM56",
                        EngineType.TURBOFAN,
                        120000.0,
                        FuelType.JET_A1,
                        0.36,
                        new Maker(
                                MakerID.of("CFM"),
                                "CFM International",
                                Country.of(IsoCode.of("FR"), "France"),
                                Set.of(MakerType.ENGINE)
                        )
                )),
                42000, 78000, 70000,
                26000, 12000, 833,
                122.6, 0.025, 1.5,
                6300, 180, 2
        );

        airTransportCompany = new AirTransportCompany(
                AirTransportCompanyCode.of("TP"),
                "TAP",
                "TAP Air Portugal"
        );

        country = Country.of(IsoCode.of("PT"), "Portugal");
    }

    private AircraftRegistration registration() { return AircraftRegistration.of("CS-TTV"); }
    private CabinConfiguration cabin() { return CabinConfiguration.of(150, 20, 10); }

    private Aircraft validAircraft() {
        return new Aircraft(registration(), cabin(), airTransportCompany, aircraftModel, country, 2);
    }

    @Test
    void validAircraftCreation() {
        assertDoesNotThrow(this::validAircraft);
    }

    @Test
    void nullRegistrationThrows() {
        assertThrows(Exception.class, () ->
                new Aircraft(null, cabin(), airTransportCompany, aircraftModel, country, 2));
    }

    @Test
    void nullCabinConfigurationThrows() {
        assertThrows(Exception.class, () ->
                new Aircraft(registration(), null, airTransportCompany, aircraftModel, country, 2));
    }

    @Test
    void nullAirTransportCompanyThrows() {
        assertThrows(Exception.class, () ->
                new Aircraft(registration(), cabin(), null, aircraftModel, country, 2));
    }

    @Test
    void nullAircraftModelThrows() {
        assertThrows(Exception.class, () ->
                new Aircraft(registration(), cabin(), airTransportCompany, null, country, 2));
    }

    @Test
    void nullCountryThrows() {
        assertThrows(Exception.class, () ->
                new Aircraft(registration(), cabin(), airTransportCompany, aircraftModel, null, 2));
    }

    @Test
    void zeroCrewElementsThrows() {
        assertThrows(Exception.class, () ->
                new Aircraft(registration(), cabin(), airTransportCompany, aircraftModel, country, 0));
    }

    @Test
    void negativeCrewElementsThrows() {
        assertThrows(Exception.class, () ->
                new Aircraft(registration(), cabin(), airTransportCompany, aircraftModel, country, -1));
    }

    @Test
    void initialStatusIsActive() {
        assertEquals(OperationalStatus.ACTIVE, validAircraft().status());
    }

    @Test
    void decommissionChangesStatus() {
        final Aircraft aircraft = validAircraft();
        aircraft.decommission();
        assertEquals(OperationalStatus.DECOMMISSIONED, aircraft.status());
    }

    @Test
    void decommissionAlreadyDecommissionedThrows() {
        final Aircraft aircraft = validAircraft();
        aircraft.decommission();
        assertThrows(Exception.class, aircraft::decommission);
    }

    @Test
    void identityReturnsRegistration() {
        assertEquals(registration(), validAircraft().identity());
    }

    @Test
    void aircraftCodeReturnsCorrectValue() {
        assertEquals(registration(), validAircraft().aircraftCode());
    }

    @Test
    void cabinConfigurationReturnsCorrectValue() {
        assertEquals(cabin(), validAircraft().cabinConfiguration());
    }

    @Test
    void airCompanyReturnsCorrectValue() {
        assertEquals(airTransportCompany, validAircraft().airCompany());
    }

    @Test
    void aircraftModelReturnsCorrectValue() {
        assertEquals(aircraftModel, validAircraft().aircraftModel());
    }

    @Test
    void countryReturnsCorrectValue() {
        assertEquals(country, validAircraft().country());
    }

    @Test
    void numCrewElementsReturnsCorrectValue() {
        assertEquals(2, validAircraft().numCrewElements());
    }

    @Test
    void sameAsReturnsTrueForIdenticalAircraft() {
        final Aircraft a1 = validAircraft();
        final Aircraft a2 = validAircraft();
        assertTrue(a1.sameAs(a2));
    }

    @Test
    void sameAsReturnsFalseForDifferentRegistration() {
        final Aircraft a1 = validAircraft();
        final Aircraft a2 = new Aircraft(
                AircraftRegistration.of("CS-TTX"),
                cabin(), airTransportCompany, aircraftModel, country, 2);
        assertFalse(a1.sameAs(a2));
    }

    @Test
    void sameAsReturnsFalseForNonAircraft() {
        assertFalse(validAircraft().sameAs("not an aircraft"));
    }

    @Test
    void equalAircraftsAreEqual() {
        assertEquals(validAircraft(), validAircraft());
    }

    @Test
    void hashCodeIsConsistent() {
        final Aircraft a = validAircraft();
        assertEquals(a.hashCode(), a.hashCode());
    }

    @Test
    void ofFactoryMethodCreatesAircraft() {
        assertDoesNotThrow(() -> Aircraft.of(
                registration(), cabin(), airTransportCompany, aircraftModel, country, 2));
    }
}