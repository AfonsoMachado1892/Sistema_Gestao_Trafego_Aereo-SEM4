package aisafe.aircontrolarea;

import aisafe.aircontrolarea.application.AirControlAreaService;
import aisafe.aircontrolarea.domain.*;
import aisafe.sharedkernel.Coordinates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AirControlAreaServiceTest {

    // --- Stub ---

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

    private AirControlAreaRepositoryStub repo;
    private AirControlAreaService service;

    private AirControlAreaCode code() { return AirControlAreaCode.of("LPPC"); }
    private AirControlAreaName name() { return AirControlAreaName.of("Lisboa"); }
    private GeographicBoundary boundary() {
        return GeographicBoundary.of(
                Coordinates.of(36.0, -10.0),
                Coordinates.of(42.0, -6.0)
        );
    }

    @BeforeEach
    void setUp() {
        repo = new AirControlAreaRepositoryStub();
        service = new AirControlAreaService(repo);
    }

    // --- Testes ---

    @Test
    void registerAirControlAreaSuccessfully() {
        final AirControlArea area = service.registerAirControlArea(code(), boundary(), name());
        assertNotNull(area);
        assertEquals(code(), area.identity());
    }

    @Test
    void duplicateCodeThrows() {
        service.registerAirControlArea(code(), boundary(), name());

        assertThrows(IllegalArgumentException.class, () ->
                service.registerAirControlArea(
                        code(),
                        GeographicBoundary.of(
                                Coordinates.of(50.0, 0.0),
                                Coordinates.of(55.0, 5.0)
                        ),
                        AirControlAreaName.of("OutroNome")
                ));
    }

    @Test
    void duplicateNameThrows() {
        service.registerAirControlArea(code(), boundary(), name());

        assertThrows(IllegalArgumentException.class, () ->
                service.registerAirControlArea(
                        AirControlAreaCode.of("LECM"),
                        GeographicBoundary.of(
                                Coordinates.of(50.0, 0.0),
                                Coordinates.of(55.0, 5.0)
                        ),
                        name()
                ));
    }

    @Test
    void overlappingAreaThrows() {
        service.registerAirControlArea(code(), boundary(), name());

        assertThrows(IllegalArgumentException.class, () ->
                service.registerAirControlArea(
                        AirControlAreaCode.of("LECM"),
                        GeographicBoundary.of(
                                Coordinates.of(38.0, -9.0),
                                Coordinates.of(44.0, -4.0)
                        ),
                        AirControlAreaName.of("Madrid")
                ));
    }

    @Test
    void nonOverlappingAreaIsRegisteredSuccessfully() {
        service.registerAirControlArea(code(), boundary(), name());

        assertDoesNotThrow(() ->
                service.registerAirControlArea(
                        AirControlAreaCode.of("EDGG"),
                        GeographicBoundary.of(
                                Coordinates.of(50.0, 0.0),
                                Coordinates.of(55.0, 5.0)
                        ),
                        AirControlAreaName.of("Frankfurt")
                ));
    }

    @Test
    void nullRepositoryThrows() {
        assertThrows(Exception.class, () -> new AirControlAreaService(null));
    }

    @Test
    void listAirControlAreasReturnsRegisteredAreas() {
        service.registerAirControlArea(code(), boundary(), name());

        final Iterable<AirControlArea> areas = service.listAirControlAreas();
        assertTrue(areas.iterator().hasNext());
    }

    @Test
    void listAirControlAreasIsEmptyWhenNoneRegistered() {
        final Iterable<AirControlArea> areas = service.listAirControlAreas();
        assertFalse(areas.iterator().hasNext());
    }

    @Test
    void twoNonOverlappingAreasCanBeRegistered() {
        service.registerAirControlArea(code(), boundary(), name());

        assertDoesNotThrow(() ->
                service.registerAirControlArea(
                        AirControlAreaCode.of("EDGG"),
                        GeographicBoundary.of(
                                Coordinates.of(50.0, 0.0),
                                Coordinates.of(55.0, 5.0)
                        ),
                        AirControlAreaName.of("Frankfurt")
                ));
    }
}