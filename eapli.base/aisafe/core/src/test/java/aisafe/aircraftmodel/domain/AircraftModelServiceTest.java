package aisafe.aircraftmodel.domain;

import aisafe.enginemodel.domain.EngineModel;
import aisafe.enginemodel.domain.EngineModelID;
import aisafe.enginemodel.domain.EngineType;
import aisafe.enginemodel.domain.FuelType;
import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerID;
import aisafe.maker.domain.MakerType;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class AircraftModelServiceTest {

    // --- stub ---

    private static class AircraftModelRepositoryStub implements AircraftModelRepository {
        private final Map<AircraftModelID, AircraftModel> aircraftModels = new HashMap<>();

        @Override
        public Optional<AircraftModel> findByModelIDAndMaker(final AircraftModelID modelID,
                                                             final MakerID makerID) {
            return aircraftModels.values().stream()
                    .filter(a -> a.identity().equals(modelID)
                            && a.maker().identity().equals(makerID))
                    .findFirst();
        }

        @Override
        public <S extends AircraftModel> S save(final S entity) {
            aircraftModels.put(entity.identity(), entity);
            return entity;
        }

        @Override
        public Optional<AircraftModel> findByModelID(final AircraftModelID modelID) {
            return Optional.ofNullable(aircraftModels.get(modelID));
        }

        @Override
        public Iterable<AircraftModel> findAll() {
            return aircraftModels.values();
        }

        @Override
        public Optional<AircraftModel> findById(final AircraftModelID id) {
            return Optional.ofNullable(aircraftModels.get(id));
        }

        @Override
        public void delete(final AircraftModel entity) {
            aircraftModels.remove(entity.identity());
        }

        @Override
        public void deleteById(final AircraftModelID id) {
            aircraftModels.remove(id);
        }

        @Override
        public long count() {
            return aircraftModels.size();
        }
    }

    // --- setup ---

    private AircraftModelRepositoryStub aircraftModelRepo;
    private AircraftModelService service;

    private Maker airbusMaker;
    private Maker boeingMaker;
    private Maker engineOnlyMaker;

    private EngineModel cfmEngine;
    private EngineModel iaeEngine;
    private EngineModel pwTurboprop;

    private static final AircraftModelID AIRCRAFT_MODEL_ID = AircraftModelID.of("A320");

    @BeforeEach
    void setUp() {
        aircraftModelRepo = new AircraftModelRepositoryStub();
        service = new AircraftModelService(aircraftModelRepo);

        final Maker cfmMaker = new Maker(
                MakerID.of("CFM"),
                "CFM International",
                new Country(IsoCode.of("FR"), "France"),
                Set.of(MakerType.ENGINE)
        );

        airbusMaker = new Maker(
                MakerID.of("AIRBUS"),
                "Airbus",
                new Country(IsoCode.of("FR"), "France"),
                Set.of(MakerType.AIRCRAFT)
        );

        boeingMaker = new Maker(
                MakerID.of("BOEING"),
                "Boeing",
                new Country(IsoCode.of("US"), "United States"),
                Set.of(MakerType.AIRCRAFT)
        );

        engineOnlyMaker = new Maker(
                MakerID.of("GE"),
                "GE Aviation",
                new Country(IsoCode.of("US"), "United States"),
                Set.of(MakerType.ENGINE)
        );

        cfmEngine = new EngineModel(
                EngineModelID.of("CFM56-5B"),
                "CFM56-5B",
                EngineType.TURBOFAN,
                120000.0,
                FuelType.JET_A1,
                0.36,
                cfmMaker
        );

        iaeEngine = new EngineModel(
                EngineModelID.of("IAE-V2500"),
                "IAE V2500",
                EngineType.TURBOFAN,
                111000.0,
                FuelType.JET_A1,
                0.35,
                new Maker(
                        MakerID.of("IAE"),
                        "IAE",
                        new Country(IsoCode.of("US"), "United States"),
                        Set.of(MakerType.ENGINE)
                )
        );

        pwTurboprop = new EngineModel(
                EngineModelID.of("PW100"),
                "PW100",
                EngineType.TURBOPROP,
                3500.0,
                FuelType.AVGAS,
                0.28,
                new Maker(
                        MakerID.of("PW"),
                        "Pratt & Whitney",
                        new Country(IsoCode.of("US"), "United States"),
                        Set.of(MakerType.ENGINE)
                )
        );
    }

    // --- registerAircraftModel tests ---

    @Test
    void ensureAircraftModelIsRegisteredSuccessfully() {
        final var result = service.registerAircraftModel(
                AIRCRAFT_MODEL_ID, airbusMaker, AircraftType.PASSENGER,
                Set.of(cfmEngine),
                42600.0, 78000.0, 61000.0, 26730.0,
                12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2);

        assertNotNull(result);
        assertEquals(AIRCRAFT_MODEL_ID, result.identity());
        assertEquals(airbusMaker, result.maker());
    }

    @Test
    void ensureAircraftModelCannotBeRegisteredWithMakerThatIsNotAircraft() {
        assertThrows(IllegalArgumentException.class, () ->
                service.registerAircraftModel(
                        AIRCRAFT_MODEL_ID, engineOnlyMaker, AircraftType.PASSENGER,
                        Set.of(cfmEngine),
                        42600.0, 78000.0, 61000.0, 26730.0,
                        12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2));
    }

    @Test
    void ensureAircraftModelCannotBeRegisteredWithNoEngineModels() {
        assertThrows(IllegalArgumentException.class, () ->
                service.registerAircraftModel(
                        AIRCRAFT_MODEL_ID, airbusMaker, AircraftType.PASSENGER,
                        Set.of(),
                        42600.0, 78000.0, 61000.0, 26730.0,
                        12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2));
    }

    @Test
    void ensureAircraftModelCannotBeRegisteredWithDuplicateModelIDAndMaker() {
        service.registerAircraftModel(AIRCRAFT_MODEL_ID, airbusMaker, AircraftType.PASSENGER,
                Set.of(cfmEngine), 42600.0, 78000.0, 61000.0, 26730.0,
                12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2);

        assertThrows(IllegalStateException.class, () ->
                service.registerAircraftModel(AIRCRAFT_MODEL_ID, airbusMaker, AircraftType.PASSENGER,
                        Set.of(cfmEngine), 42600.0, 78000.0, 61000.0, 26730.0,
                        12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2));
    }

    @Test
    void ensureSameModelIDWithDifferentMakerIsAllowed() {
        service.registerAircraftModel(AIRCRAFT_MODEL_ID, airbusMaker, AircraftType.PASSENGER,
                Set.of(cfmEngine), 42600.0, 78000.0, 61000.0, 26730.0,
                12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2);

        assertDoesNotThrow(() ->
                service.registerAircraftModel(AIRCRAFT_MODEL_ID, boeingMaker, AircraftType.PASSENGER,
                        Set.of(cfmEngine), 42600.0, 78000.0, 61000.0, 26730.0,
                        12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2));
    }

    @Test
    void ensureAircraftModelCanHaveMultipleCertifiedEngineModels() {
        assertDoesNotThrow(() ->
                service.registerAircraftModel(AIRCRAFT_MODEL_ID, airbusMaker, AircraftType.PASSENGER,
                        Set.of(cfmEngine, iaeEngine), 42600.0, 78000.0, 61000.0, 26730.0,
                        12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2));
    }

    // --- addEngineModel tests ---

    @Test
    void ensureEngineModelIsAddedSuccessfully() {
        service.registerAircraftModel(AIRCRAFT_MODEL_ID, airbusMaker, AircraftType.PASSENGER,
                Set.of(cfmEngine), 42600.0, 78000.0, 61000.0, 26730.0,
                12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2);

        final var result = service.addEngineModel(AIRCRAFT_MODEL_ID, iaeEngine);

        assertNotNull(result);
        assertTrue(result.hasCertifiedEngine(iaeEngine));
        assertTrue(result.hasCertifiedEngine(cfmEngine));
    }

    @Test
    void ensureCannotAddEngineModelToNonExistentAircraftModel() {
        assertThrows(IllegalArgumentException.class, () ->
                service.addEngineModel(AircraftModelID.of("DOES_NOT_EXIST"), iaeEngine));
    }

    @Test
    void ensureCannotAddAlreadyCertifiedEngineModel() {
        service.registerAircraftModel(AIRCRAFT_MODEL_ID, airbusMaker, AircraftType.PASSENGER,
                Set.of(cfmEngine), 42600.0, 78000.0, 61000.0, 26730.0,
                12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2);

        assertThrows(IllegalArgumentException.class, () ->
                service.addEngineModel(AIRCRAFT_MODEL_ID, cfmEngine));
    }

    @Test
    void ensureCannotAddEngineModelWithIncompatibleType() {
        service.registerAircraftModel(AIRCRAFT_MODEL_ID, airbusMaker, AircraftType.PASSENGER,
                Set.of(cfmEngine), 42600.0, 78000.0, 61000.0, 26730.0,
                12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2);

        assertThrows(IllegalArgumentException.class, () ->
                service.addEngineModel(AIRCRAFT_MODEL_ID, pwTurboprop));
    }
}