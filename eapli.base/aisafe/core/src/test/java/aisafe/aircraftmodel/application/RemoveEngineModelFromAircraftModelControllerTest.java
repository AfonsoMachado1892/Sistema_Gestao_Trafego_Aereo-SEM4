package aisafe.aircraftmodel.application;

import aisafe.aircraftmodel.domain.*;
import aisafe.enginemodel.domain.*;
import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerID;
import aisafe.maker.domain.MakerType;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RemoveEngineModelFromAircraftModelController} (US058).
 */
class RemoveEngineModelFromAircraftModelControllerTest {

    // -----------------------------------------------------------------------
    // Stubs
    // -----------------------------------------------------------------------

    static class AuthorizationServiceStub extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) { }
    }

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

    static class EngineModelRepositoryStub implements EngineModelRepository {
        final Map<String, EngineModel> store = new LinkedHashMap<>();

        @Override
        public Optional<EngineModel> findByNameAndMaker(final String name,
                                                        final MakerID makerID) {
            return Optional.empty();
        }

        @Override
        public Optional<EngineModel> findByEngineModelID(final EngineModelID id) {
            return Optional.ofNullable(store.get(id.toString()));
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S extends EngineModel> S save(final S entity) {
            store.put(entity.identity().toString(), entity);
            return entity;
        }

        @Override
        public Iterable<EngineModel> findAll() {
            return Collections.unmodifiableCollection(store.values());
        }

        @Override
        public Optional<EngineModel> findById(final EngineModelID id) {
            return Optional.ofNullable(store.get(id.toString()));
        }

        @Override
        public void delete(final EngineModel entity) {
            store.remove(entity.identity().toString());
        }

        @Override
        public void deleteById(final EngineModelID id) {
            store.remove(id.toString());
        }

        @Override
        public long count() {
            return store.size();
        }
    }

    // -----------------------------------------------------------------------
    // Fixtures
    // -----------------------------------------------------------------------

    private static final AircraftModelID MODEL_ID = AircraftModelID.of("A320");

    private static Maker validMaker() {
        return new Maker(
                MakerID.of("AIRBUS"),
                "Airbus",
                new Country(IsoCode.of("FR"), "France"),
                Set.of(MakerType.AIRCRAFT)
        );
    }

    private static Maker engineMaker() {
        return new Maker(
                MakerID.of("CFM"),
                "CFM International",
                new Country(IsoCode.of("FR"), "France"),
                Set.of(MakerType.ENGINE)
        );
    }

    private static EngineModel engine1() {
        return new EngineModel(
                EngineModelID.of("CFM56-5B"),
                "CFM56-5B",
                EngineType.TURBOFAN,
                120000.0,
                FuelType.JET_A1,
                0.36,
                engineMaker()
        );
    }

    private static EngineModel engine2() {
        return new EngineModel(
                EngineModelID.of("CFM56-5C"),
                "CFM56-5C",
                EngineType.TURBOFAN,
                138000.0,
                FuelType.JET_A1,
                0.35,
                engineMaker()
        );
    }

    private AircraftModel buildModelWithTwoEngines() {
        return new AircraftModel(
                MODEL_ID,
                validMaker(),
                AircraftType.PASSENGER,
                new HashSet<>(Set.of(engine1(), engine2())),
                42600.0, 78000.0, 61000.0, 26730.0,
                12131.0, 257.0, 122.6, 0.025, 0.52,
                6150.0, 180, 2
        );
    }

    private AircraftModelRepositoryStub aircraftRepo;
    private EngineModelRepositoryStub engineRepo;
    private RemoveEngineModelFromAircraftModelController controller;

    @BeforeEach
    void setUp() {
        aircraftRepo = new AircraftModelRepositoryStub();
        engineRepo = new EngineModelRepositoryStub();

        engineRepo.store.put(engine1().identity().toString(), engine1());
        engineRepo.store.put(engine2().identity().toString(), engine2());

        controller = new RemoveEngineModelFromAircraftModelController(
                new AuthorizationServiceStub(),
                aircraftRepo,
                engineRepo
        );
    }

    // -----------------------------------------------------------------------
    // removeEngineModel — happy path
    // -----------------------------------------------------------------------

    @Test
    void ensureEngineIsRemovedSuccessfully() {
        // Arrange
        aircraftRepo.save(buildModelWithTwoEngines());

        // Act
        final AircraftModel result = controller.removeEngineModel("A320", "CFM56-5B");

        // Assert
        assertFalse(result.hasCertifiedEngine(EngineModelID.of("CFM56-5B")));
        assertTrue(result.hasCertifiedEngine(EngineModelID.of("CFM56-5C")));
    }

    @Test
    void ensureRemovedEngineIsPersistedInRepository() {
        // Arrange
        aircraftRepo.save(buildModelWithTwoEngines());

        // Act
        controller.removeEngineModel("A320", "CFM56-5B");

        // Assert
        final AircraftModel saved = aircraftRepo.aircraftModels.get(AircraftModelID.of("A320"));
        assertNotNull(saved);
        assertFalse(saved.hasCertifiedEngine(EngineModelID.of("CFM56-5B")));
        assertTrue(saved.hasCertifiedEngine(EngineModelID.of("CFM56-5C")));
    }

    // -----------------------------------------------------------------------
    // removeEngineModel — error cases
    // -----------------------------------------------------------------------

    @Test
    void ensureRemovingFromNonExistentAircraftModelThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.removeEngineModel("NONEXISTENT", "CFM56-5B"));
    }

    @Test
    void ensureRemovingNonExistentEngineModelThrows() {
        // Arrange
        aircraftRepo.save(buildModelWithTwoEngines());

        // Act & Assert — engine not in engineRepo
        assertThrows(IllegalArgumentException.class,
                () -> controller.removeEngineModel("A320", "UNKNOWN-ENGINE"));
    }

    @Test
    void ensureRemovingNonCertifiedEngineThrows() {
        // Arrange — add a third engine to repo but not certified on the model
        final EngineModel engine3 = new EngineModel(
                EngineModelID.of("IAE-V2500"),
                "IAE-V2500",
                EngineType.TURBOFAN,
                111000.0,
                FuelType.JET_A1,
                0.34,
                new Maker(
                        MakerID.of("IAE"),
                        "IAE",
                        new Country(IsoCode.of("US"), "United States"),
                        Set.of(MakerType.ENGINE)
                )
        );

        engineRepo.store.put(engine3.identity().toString(), engine3);
        aircraftRepo.save(buildModelWithTwoEngines());

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.removeEngineModel("A320", "IAE-V2500"));
    }

    @Test
    void ensureRemovingLastCertifiedEngineThrows() {
        // Arrange — model with only one engine
        final AircraftModel model = new AircraftModel(
                MODEL_ID,
                validMaker(),
                AircraftType.PASSENGER,
                Set.of(engine1()),
                42600.0, 78000.0, 61000.0, 26730.0,
                12131.0, 257.0, 122.6, 0.025, 0.52,
                6150.0, 180, 2
        );

        aircraftRepo.save(model);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> controller.removeEngineModel("A320", "CFM56-5B"));
    }

    // -----------------------------------------------------------------------
    // getAircraftModels / getEngineModels
    // -----------------------------------------------------------------------

    @Test
    void ensureGetAircraftModelsReturnsAllModels() {
        // Arrange
        aircraftRepo.save(buildModelWithTwoEngines());

        // Act
        final List<AircraftModel> result = new ArrayList<>();
        controller.getAircraftModels().forEach(result::add);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    void ensureGetEngineModelsReturnsRegisteredEngines() {
        // Act
        final List<EngineModel> result = new ArrayList<>();
        controller.getEngineModels().forEach(result::add);

        // Assert
        assertEquals(2, result.size());
    }
}