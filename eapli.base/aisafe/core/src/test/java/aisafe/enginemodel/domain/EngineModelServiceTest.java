package aisafe.enginemodel.domain;

import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerID;
import aisafe.maker.domain.MakerType;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class EngineModelServiceTest {

    // --- stub ---

    private static class EngineModelRepositoryStub implements EngineModelRepository {

        private final Map<EngineModelID, EngineModel> engineModels = new HashMap<>();

        @Override
        public Optional<EngineModel> findByNameAndMaker(final String name, final MakerID makerID) {
            return engineModels.values().stream()
                    .filter(e -> e.name().equalsIgnoreCase(name)
                            && e.maker().identity().equals(makerID))
                    .findFirst();
        }

        @Override
        public Optional<EngineModel> findByEngineModelID(final EngineModelID engineModelID) {
            return engineModels.values().stream()
                    .filter(e -> e.identity().equals(engineModelID))
                    .findFirst();
        }

        @Override public <S extends EngineModel> S save(S entity) { engineModels.put(entity.identity(), entity); return entity; }
        @Override public Iterable<EngineModel> findAll() { return engineModels.values(); }
        @Override public Optional<EngineModel> findById(EngineModelID id) { return Optional.ofNullable(engineModels.get(id)); }
        @Override public void delete(EngineModel entity) { engineModels.remove(entity.identity()); }
        @Override public void deleteById(EngineModelID id) { engineModels.remove(id); }
        @Override public long count() { return engineModels.size(); }
    }

    // --- setup ---

    private EngineModelRepositoryStub engineModelRepo;
    private EngineModelService service;

    private Maker engineMaker;
    private Maker aircraftMaker;
    private Maker dualMaker;

    @BeforeEach
    void setUp() {
        engineModelRepo = new EngineModelRepositoryStub();
        service = new EngineModelService(engineModelRepo);

        engineMaker = new Maker(MakerID.of("CFM"), "CFM International",
                new Country(IsoCode.of("FR"), "France"), Set.of(MakerType.ENGINE));

        aircraftMaker = new Maker(MakerID.of("AIRBUS"), "Airbus",
                new Country(IsoCode.of("FR"), "France"), Set.of(MakerType.AIRCRAFT));

        dualMaker = new Maker(MakerID.of("RR"), "Rolls-Royce",
                new Country(IsoCode.of("GB"), "United Kingdom"),
                Set.of(MakerType.ENGINE, MakerType.AIRCRAFT));
    }

    // --- testes ---

    @Test
    void ensureEngineModelIsRegisteredSuccessfully() {
        // Act
        final var result = service.registerEngineModel(
                EngineModelID.of("EM001"), "CFM56-7B",
                EngineType.TURBOFAN, 121000.0, FuelType.JET_A1, 0.37, engineMaker);

        // Assert
        assertNotNull(result);
        assertEquals("CFM56-7B", result.name());
        assertEquals(engineMaker, result.maker());
    }

    @Test
    void ensureEngineModelCannotBeRegisteredWithMakerThatIsNotEngine() {
        // Act + Assert
        assertThrows(IllegalStateException.class, () ->
                service.registerEngineModel(
                        EngineModelID.of("EM001"), "CFM56-7B",
                        EngineType.TURBOFAN, 121000.0, FuelType.JET_A1, 0.37, aircraftMaker));
    }

    @Test
    void ensureEngineModelCannotBeRegisteredWithDuplicateNameAndMaker() {
        // Arrange
        service.registerEngineModel(EngineModelID.of("EM001"), "CFM56-7B",
                EngineType.TURBOFAN, 121000.0, FuelType.JET_A1, 0.37, engineMaker);

        // Act + Assert
        assertThrows(IllegalStateException.class, () ->
                service.registerEngineModel(EngineModelID.of("EM002"), "CFM56-7B",
                        EngineType.TURBOFAN, 121000.0, FuelType.JET_A1, 0.37, engineMaker));
    }

    @Test
    void ensureSameEngineModelNameWithDifferentMakerIsAllowed() {
        // Arrange
        service.registerEngineModel(EngineModelID.of("EM001"), "Trent 1000",
                EngineType.TURBOFAN, 320000.0, FuelType.JET_A1, 0.52, dualMaker);

        // Act + Assert
        assertDoesNotThrow(() ->
                service.registerEngineModel(EngineModelID.of("EM002"), "Trent 1000",
                        EngineType.TURBOFAN, 320000.0, FuelType.JET_A1, 0.52, engineMaker));
    }

    @Test
    void ensureDualTypeMakerCanRegisterEngineModel() {
        // Act + Assert
        assertDoesNotThrow(() ->
                service.registerEngineModel(EngineModelID.of("EM001"), "Trent 1000",
                        EngineType.TURBOFAN, 320000.0, FuelType.JET_A1, 0.52, dualMaker));
    }
}