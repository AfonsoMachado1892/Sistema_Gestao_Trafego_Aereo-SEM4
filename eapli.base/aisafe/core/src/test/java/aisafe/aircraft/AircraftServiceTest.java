package aisafe.aircraft;

import aisafe.aircraft.application.AircraftService;
import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.aircraft.domain.AircraftRepository;
import aisafe.aircraft.domain.CabinConfiguration;
import aisafe.aircraft.domain.OperationalStatus;
import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.aircraftmodel.domain.AircraftModelID;
import aisafe.aircraftmodel.domain.AircraftType;
import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.enginemodel.domain.EngineModel;
import aisafe.enginemodel.domain.EngineModelID;
import aisafe.enginemodel.domain.EngineType;
import aisafe.enginemodel.domain.FuelType;
import aisafe.flight.domain.Flight;
import aisafe.flight.domain.FlightRepository;
import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerID;
import aisafe.maker.domain.MakerType;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AircraftServiceTest {

    // --- Stubs ---

    private static class AircraftRepositoryStub implements AircraftRepository {

        private final Map<AircraftRegistration, Aircraft> aircrafts = new HashMap<>();

        @Override
        public Optional<Aircraft> findByRegistration(final AircraftRegistration registration) {
            return Optional.ofNullable(aircrafts.get(registration));
        }

        @Override
        public Iterable<Aircraft> findByAirTransportCompany(final AirTransportCompany company) {
            return aircrafts.values().stream()
                    .filter(a -> a.airCompany().equals(company))
                    .toList();
        }

        @Override
        public <S extends Aircraft> S save(final S entity) {
            aircrafts.put(entity.identity(), entity);
            return entity;
        }

        @Override
        public Iterable<Aircraft> findAll() {
            return aircrafts.values();
        }

        @Override
        public Optional<Aircraft> findById(final AircraftRegistration id) {
            return Optional.ofNullable(aircrafts.get(id));
        }

        @Override
        public void delete(final Aircraft entity) {
            aircrafts.remove(entity.identity());
        }

        @Override
        public void deleteById(final AircraftRegistration id) {
            aircrafts.remove(id);
        }

        @Override
        public long count() {
            return aircrafts.size();
        }
    }

    private static class FlightRepositoryStub implements FlightRepository {

        private boolean pendingFlightsExist;

        void setPendingFlightsExist(final boolean pendingFlightsExist) {
            this.pendingFlightsExist = pendingFlightsExist;
        }

        @Override
        public boolean existsPendingFlightsFor(final AircraftRegistration registration) {
            return pendingFlightsExist;
        }

        @Override
        public <S extends Flight> S save(final S entity) {
            return entity;
        }

        @Override
        public Iterable<Flight> findAll() {
            return Collections.emptyList();
        }

        @Override
        public Optional<Flight> ofIdentity(final Long id) {
            return Optional.empty();
        }

        @Override
        public void delete(final Flight entity) {
            // not needed for these tests
        }

        @Override
        public void deleteOfIdentity(final Long id) {
            // not needed for these tests
        }

        @Override
        public long count() {
            return 0;
        }

        @Override
        public boolean contains(final Flight entity) {
            return false;
        }

        @Override
        public boolean containsOfIdentity(final Long id) {
            return false;
        }
    }

    // --- Setup ---

    private AircraftRepositoryStub aircraftRepo;
    private FlightRepositoryStub flightRepo;
    private AircraftService service;
    private AircraftModel aircraftModel;
    private AirTransportCompany airTransportCompany;
    private Country country;

    @BeforeEach
    void setUp() {
        aircraftRepo = new AircraftRepositoryStub();
        flightRepo = new FlightRepositoryStub();
        service = new AircraftService(aircraftRepo);

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

    // --- Helpers ---

    private CabinConfiguration cabin() {
        return CabinConfiguration.of(150, 20, 10);
    }

    private Aircraft addValidAircraft() {
        return service.addAircraft(
                "CS-TTV",
                aircraftModel,
                airTransportCompany,
                cabin(),
                country,
                2
        );
    }

    // --- addAircraft tests ---

    @Test
    void addAircraftSuccessfully() {
        final Aircraft aircraft = service.addAircraft(
                "CS-TTV", aircraftModel, airTransportCompany, cabin(), country, 2);

        assertNotNull(aircraft);
        assertEquals("CS-TTV", aircraft.identity().code());
    }

    @Test
    void aircraftIsCreatedWithActiveStatus() {
        final Aircraft aircraft = service.addAircraft(
                "CS-TTV", aircraftModel, airTransportCompany, cabin(), country, 2);

        assertEquals(OperationalStatus.ACTIVE, aircraft.status());
    }

    @Test
    void duplicateRegistrationThrows() {
        service.addAircraft("CS-TTV", aircraftModel, airTransportCompany, cabin(), country, 2);

        assertThrows(IllegalArgumentException.class, () ->
                service.addAircraft("CS-TTV", aircraftModel, airTransportCompany, cabin(), country, 2));
    }

    @Test
    void cabinExceedingCapacityThrows() {
        final CabinConfiguration overCapacity = CabinConfiguration.of(150, 20, 20);

        assertThrows(IllegalArgumentException.class, () ->
                service.addAircraft("CS-TTV", aircraftModel, airTransportCompany,
                        overCapacity, country, 2));
    }

    @Test
    void nullAircraftRepositoryThrows() {
        assertThrows(Exception.class, () -> new AircraftService(null));
    }

    @Test
    void listAircraftByCompanyReturnsCorrectAircrafts() {
        service.addAircraft("CS-TTV", aircraftModel, airTransportCompany, cabin(), country, 2);

        final Iterable<Aircraft> result = service.listAircraftByCompany(airTransportCompany);

        assertTrue(result.iterator().hasNext());
    }

    @Test
    void listAircraftByCompanyReturnsEmptyForOtherCompany() {
        service.addAircraft("CS-TTV", aircraftModel, airTransportCompany, cabin(), country, 2);

        final AirTransportCompany otherCompany = new AirTransportCompany(
                AirTransportCompanyCode.of("RY"),
                "RYR",
                "Ryanair"
        );

        final Iterable<Aircraft> result = service.listAircraftByCompany(otherCompany);

        assertFalse(result.iterator().hasNext());
    }

    @Test
    void nullAirTransportCompanyInListThrows() {
        assertThrows(Exception.class, () -> service.listAircraftByCompany(null));
    }

    @Test
    void twoAircraftsWithDifferentRegistrationsCanBeAdded() {
        service.addAircraft("CS-TTV", aircraftModel, airTransportCompany, cabin(), country, 2);

        assertDoesNotThrow(() ->
                service.addAircraft("CS-TTX", aircraftModel, airTransportCompany, cabin(), country, 2));
    }

    // --- decommissionAircraft tests ---

    @Test
    void decommissionAircraftSuccessfully() {
        addValidAircraft();
        flightRepo.setPendingFlightsExist(false);

        final Aircraft result = service.decommissionAircraft(
                "CS-TTV",
                airTransportCompany,
                flightRepo
        );

        assertNotNull(result);
        assertEquals(OperationalStatus.DECOMMISSIONED, result.status());

        final Aircraft saved = aircraftRepo.findByRegistration(AircraftRegistration.of("CS-TTV"))
                .orElseThrow();

        assertEquals(OperationalStatus.DECOMMISSIONED, saved.status());
    }

    @Test
    void decommissionNonExistentAircraftThrows() {
        flightRepo.setPendingFlightsExist(false);

        assertThrows(IllegalArgumentException.class, () ->
                service.decommissionAircraft(
                        "CS-XXX",
                        airTransportCompany,
                        flightRepo
                ));
    }

    @Test
    void decommissionAircraftFromDifferentCompanyThrows() {
        addValidAircraft();

        final AirTransportCompany otherCompany = new AirTransportCompany(
                AirTransportCompanyCode.of("RY"),
                "RYR",
                "Ryanair"
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.decommissionAircraft(
                        "CS-TTV",
                        otherCompany,
                        flightRepo
                ));
    }

    @Test
    void decommissionAircraftWithPendingFlightsThrows() {
        addValidAircraft();
        flightRepo.setPendingFlightsExist(true);

        assertThrows(IllegalStateException.class, () ->
                service.decommissionAircraft(
                        "CS-TTV",
                        airTransportCompany,
                        flightRepo
                ));
    }

    @Test
    void decommissionAircraftWithNullRegistrationThrows() {
        assertThrows(Exception.class, () ->
                service.decommissionAircraft(
                        null,
                        airTransportCompany,
                        flightRepo
                ));
    }

    @Test
    void decommissionAircraftWithNullCompanyThrows() {
        assertThrows(Exception.class, () ->
                service.decommissionAircraft(
                        "CS-TTV",
                        null,
                        flightRepo
                ));
    }

    @Test
    void decommissionAircraftWithNullFlightRepositoryThrows() {
        assertThrows(Exception.class, () ->
                service.decommissionAircraft(
                        "CS-TTV",
                        airTransportCompany,
                        null
                ));
    }
}