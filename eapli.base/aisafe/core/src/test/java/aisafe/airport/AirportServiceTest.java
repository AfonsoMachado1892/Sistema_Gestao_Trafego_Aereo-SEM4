package aisafe.airport;

import aisafe.aircontrolarea.domain.*;
import aisafe.airport.application.AirportService;
import aisafe.airport.domain.*;
import aisafe.sharedkernel.Coordinates;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AirportServiceTest {

    // --- Stubs ---

    private static class AirportRepositoryStub implements AirportRepository {

        private final Map<AirportIataCode, Airport> airports = new HashMap<>();

        @Override
        public Optional<Airport> findByIataCode(final AirportIataCode iataCode) {
            return Optional.ofNullable(airports.get(iataCode));
        }

        @Override
        public Optional<Airport> findByIcaoCode(final AirportIcaoCode icaoCode) {
            return airports.values().stream()
                    .filter(a -> a.icaoCode().equals(icaoCode))
                    .findFirst();
        }

        @Override
        public <S extends Airport> S save(final S entity) {
            airports.put(entity.identity(), entity);
            return entity;
        }

        @Override public Iterable<Airport> findAll() { return airports.values(); }
        @Override public Optional<Airport> findById(final AirportIataCode id) { return Optional.ofNullable(airports.get(id)); }
        @Override public void delete(final Airport entity) { airports.remove(entity.identity()); }
        @Override public void deleteById(final AirportIataCode id) { airports.remove(id); }
        @Override public long count() { return airports.size(); }
    }

    private static class AirControlAreaRepositoryStub implements AirControlAreaRepository {

        private final Map<AirControlAreaCode, AirControlArea> areas = new HashMap<>();

        void add(final AirControlArea area) {
            areas.put(area.identity(), area);
        }

        @Override
        public Iterable<AirControlArea> findAreasContaining(final Coordinates coordinates) {
            return areas.values().stream()
                    .filter(a -> a.contains(coordinates))
                    .toList();
        }

        @Override
        public Optional<AirControlArea> findByCode(final AirControlAreaCode code) {
            return Optional.ofNullable(areas.get(code));
        }

        @Override
        public Optional<AirControlArea> findByName(final AirControlAreaName name) {
            return areas.values().stream()
                    .filter(a -> a.name().equals(name))
                    .findFirst();
        }

        @Override
        public <S extends AirControlArea> S save(final S entity) {
            areas.put(entity.identity(), entity);
            return entity;
        }

        @Override public Iterable<AirControlArea> findAll() { return areas.values(); }
        @Override public Optional<AirControlArea> findById(final AirControlAreaCode id) { return Optional.ofNullable(areas.get(id)); }
        @Override public void delete(final AirControlArea entity) { areas.remove(entity.identity()); }
        @Override public void deleteById(final AirControlAreaCode id) { areas.remove(id); }
        @Override public long count() { return areas.size(); }
    }

    // --- Setup ---

    private AirportRepositoryStub airportRepo;
    private AirControlAreaRepositoryStub acaRepo;
    private AirportService service;
    private AirControlArea airControlArea;

    private static final AirControlAreaCode ACA_CODE = AirControlAreaCode.of("LPPC");

    @BeforeEach
    void setUp() {
        airportRepo = new AirportRepositoryStub();
        acaRepo = new AirControlAreaRepositoryStub();
        service = new AirportService(airportRepo, acaRepo);

        airControlArea = new AirControlArea(
                ACA_CODE,
                GeographicBoundary.of(
                        Coordinates.of(36.0, -10.0),
                        Coordinates.of(42.0, -6.0)
                ),
                AirControlAreaName.of("Lisboa")
        );
        acaRepo.add(airControlArea);
    }

    // --- Helpers ---

    private AirportIataCode iata() { return AirportIataCode.of("OPO"); }
    private AirportIcaoCode icao() { return AirportIcaoCode.of("LPPR"); }
    private AirportName name() { return AirportName.of("Francisco de Sá Carneiro Airport"); }
    private Locality locality() {
        return Locality.of("Porto", Country.of(IsoCode.of("PT"), "Portugal"));
    }
    private Coordinates coordinates() { return Coordinates.of(41.2481, -8.6813); }
    private Elevation elevation() { return Elevation.of(69); }

    // --- Testes ---

    @Test
    void registerAirportSuccessfully() {
        final Airport airport = service.registerAirport(
                iata(), icao(), locality(), coordinates(), name(), elevation(), airControlArea);

        assertNotNull(airport);
        assertEquals(iata(), airport.identity());
    }

    @Test
    void registeredAirportIsStoredInRepository() {
        service.registerAirport(
                iata(), icao(), locality(), coordinates(), name(), elevation(), airControlArea);

        assertTrue(airportRepo.findByIataCode(iata()).isPresent());
    }

    @Test
    void duplicateIataCodeThrows() {
        service.registerAirport(
                iata(), icao(), locality(), coordinates(), name(), elevation(), airControlArea);

        assertThrows(IllegalArgumentException.class, () ->
                service.registerAirport(
                        iata(),
                        AirportIcaoCode.of("LPPT"),
                        locality(), coordinates(), name(), elevation(), airControlArea));
    }

    @Test
    void duplicateIcaoCodeThrows() {
        service.registerAirport(
                iata(), icao(), locality(), coordinates(), name(), elevation(), airControlArea);

        assertThrows(IllegalArgumentException.class, () ->
                service.registerAirport(
                        AirportIataCode.of("LIS"),
                        icao(),
                        locality(), coordinates(), name(), elevation(), airControlArea));
    }

    @Test
    void airControlAreaNotFoundThrows() {
        final AirControlArea acaNotSaved = new AirControlArea(
                AirControlAreaCode.of("XXXX"),
                GeographicBoundary.of(
                        Coordinates.of(36.0, -10.0),
                        Coordinates.of(42.0, -6.0)
                ),
                AirControlAreaName.of("Inexistente")
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.registerAirport(
                        iata(), icao(), locality(), coordinates(), name(), elevation(), acaNotSaved));
    }

    @Test
    void coordinatesOutsideAcaThrows() {
        final Coordinates outsideCoordinates = Coordinates.of(51.4706, -0.4619);

        assertThrows(IllegalArgumentException.class, () ->
                service.registerAirport(
                        iata(), icao(), locality(), outsideCoordinates, name(), elevation(), airControlArea));
    }

    @Test
    void nullAirportRepositoryThrows() {
        assertThrows(Exception.class, () -> new AirportService(null, acaRepo));
    }

    @Test
    void nullAcaRepositoryThrows() {
        assertThrows(Exception.class, () -> new AirportService(airportRepo, null));
    }

    @Test
    void listAirportsReturnsRegisteredAirports() {
        service.registerAirport(
                iata(), icao(), locality(), coordinates(), name(), elevation(), airControlArea);

        final Iterable<Airport> airports = service.listAirports();
        assertTrue(airports.iterator().hasNext());
    }

    @Test
    void listAirportsIsEmptyWhenNoneRegistered() {
        final Iterable<Airport> airports = service.listAirports();
        assertFalse(airports.iterator().hasNext());
    }

    @Test
    void twoAirportsWithDifferentCodesCanBeRegistered() {
        service.registerAirport(
                iata(), icao(), locality(), coordinates(), name(), elevation(), airControlArea);

        assertDoesNotThrow(() ->
                service.registerAirport(
                        AirportIataCode.of("LIS"),
                        AirportIcaoCode.of("LPPT"),
                        Locality.of("Lisboa", Country.of(IsoCode.of("PT"), "Portugal")),
                        Coordinates.of(38.7813, -9.1359),
                        AirportName.of("Humberto Delgado Airport"),
                        Elevation.of(114),
                        airControlArea));
    }
}