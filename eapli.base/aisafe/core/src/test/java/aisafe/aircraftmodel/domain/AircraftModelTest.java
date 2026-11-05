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
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AircraftModelTest {

    private Maker validMaker() {
        return new Maker(MakerID.of("AIRBUS"), "Airbus",
                new Country(IsoCode.of("FR"), "France"), Set.of(MakerType.AIRCRAFT));
    }

    private EngineModel validEngineModel() {
        return new EngineModel(EngineModelID.of("CFM56-5B"), "CFM56-5B",
                EngineType.TURBOFAN, 120000.0, FuelType.JET_A1, 0.36,
                new Maker(MakerID.of("CFM"), "CFM International",
                        new Country(IsoCode.of("FR"), "France"), Set.of(MakerType.ENGINE)));
    }

    private AircraftModel validAircraftModel() {
        return new AircraftModel(
                AircraftModelID.of("A320"),
                validMaker(),
                AircraftType.PASSENGER,
                Set.of(validEngineModel()),
                42600.0, 78000.0, 61000.0, 26730.0,
                12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2
        );
    }

    @Test
    void ensureAircraftModelCannotHaveNullModelID() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(null, validMaker(), AircraftType.PASSENGER,
                        Set.of(validEngineModel()),
                        42600.0, 78000.0, 61000.0, 26730.0,
                        12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2));
    }

    @Test
    void ensureAircraftModelCannotHaveNullMaker() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(AircraftModelID.of("A320"), null, AircraftType.PASSENGER,
                        Set.of(validEngineModel()),
                        42600.0, 78000.0, 61000.0, 26730.0,
                        12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2));
    }

    @Test
    void ensureAircraftModelCannotHaveNullType() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(AircraftModelID.of("A320"), validMaker(), null,
                        Set.of(validEngineModel()),
                        42600.0, 78000.0, 61000.0, 26730.0,
                        12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2));
    }

    @Test
    void ensureAircraftModelCannotHaveNullEngineModels() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(AircraftModelID.of("A320"), validMaker(), AircraftType.PASSENGER,
                        null,
                        42600.0, 78000.0, 61000.0, 26730.0,
                        12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2));
    }

    @Test
    void ensureAircraftModelCannotHaveEmptyEngineModels() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(AircraftModelID.of("A320"), validMaker(), AircraftType.PASSENGER,
                        Set.of(),
                        42600.0, 78000.0, 61000.0, 26730.0,
                        12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2));
    }

    @Test
    void ensureAircraftModelCannotHaveMtowLessThanEmptyWeight() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(AircraftModelID.of("A320"), validMaker(), AircraftType.PASSENGER,
                        Set.of(validEngineModel()),
                        78000.0, 42600.0, 61000.0, 26730.0,
                        12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2));
    }

    @Test
    void ensureAircraftModelCannotHaveMtowEqualToEmptyWeight() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(AircraftModelID.of("A320"), validMaker(), AircraftType.PASSENGER,
                        Set.of(validEngineModel()),
                        78000.0, 78000.0, 61000.0, 26730.0,
                        12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2));
    }

    @Test
    void ensureAircraftModelCannotHaveZeroEmptyWeight() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(AircraftModelID.of("A320"), validMaker(), AircraftType.PASSENGER,
                        Set.of(validEngineModel()),
                        0.0, 78000.0, 61000.0, 26730.0,
                        12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2));
    }

    @Test
    void ensureAircraftModelCannotHaveNegativeDragCoefficient() {
        assertThrows(IllegalArgumentException.class, () ->
                new AircraftModel(AircraftModelID.of("A320"), validMaker(), AircraftType.PASSENGER,
                        Set.of(validEngineModel()),
                        42600.0, 78000.0, 61000.0, 26730.0,
                        12131.0, 257.0, 122.6, -0.025, 0.52, 6150.0, 180, 2));
    }

    @Test
    void ensureAircraftModelIsCreatedWithValidValues() {
        final var model = validAircraftModel();

        assertEquals(MakerID.of("AIRBUS"), model.maker().identity());
        assertEquals(AircraftType.PASSENGER, model.type());
        assertEquals(42600.0, model.emptyWeight());
        assertEquals(78000.0, model.mtow());
        assertTrue(model.hasCertifiedEngine(validEngineModel()));
    }

    @Test
    void ensureIdentityReturnsCorrectModelID() {
        assertEquals(AircraftModelID.of("A320"), validAircraftModel().identity());
    }

    @Test
    void ensureSameAsReturnsTrueForSameModelIDAndMaker() {
        assertTrue(validAircraftModel().sameAs(validAircraftModel()));
    }

    @Test
    void ensureSameAsReturnsFalseForDifferentModelID() {
        final var model1 = validAircraftModel();
        final var model2 = new AircraftModel(AircraftModelID.of("A321"), validMaker(),
                AircraftType.PASSENGER, Set.of(validEngineModel()),
                42600.0, 78000.0, 61000.0, 26730.0,
                12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2);

        assertFalse(model1.sameAs(model2));
    }

    @Test
    void ensureSameAsReturnsFalseForDifferentMaker() {
        final var model1 = validAircraftModel();
        final var boeingMaker = new Maker(MakerID.of("BOEING"), "Boeing",
                new Country(IsoCode.of("US"), "United States"), Set.of(MakerType.AIRCRAFT));
        final var model2 = new AircraftModel(AircraftModelID.of("A320"), boeingMaker,
                AircraftType.PASSENGER, Set.of(validEngineModel()),
                42600.0, 78000.0, 61000.0, 26730.0,
                12131.0, 257.0, 122.6, 0.025, 0.52, 6150.0, 180, 2);

        assertFalse(model1.sameAs(model2));
    }

    @Test
    void ensureSameAsReturnsFalseForNonAircraftModelObject() {
        assertFalse(validAircraftModel().sameAs("not an aircraft model"));
    }

    @Test
    void ensureHasCertifiedEngineReturnsTrueForCertifiedEngine() {
        assertTrue(validAircraftModel().hasCertifiedEngine(validEngineModel()));
    }

    @Test
    void ensureHasCertifiedEngineReturnsFalseForNonCertifiedEngine() {
        final var otherEngine = new EngineModel(EngineModelID.of("GEnx-1B"), "GEnx-1B",
                EngineType.TURBOFAN, 339000.0, FuelType.JET_A1, 0.51,
                new Maker(MakerID.of("GE"), "GE Aviation",
                        new Country(IsoCode.of("US"), "United States"), Set.of(MakerType.ENGINE)));

        assertFalse(validAircraftModel().hasCertifiedEngine(otherEngine));
    }
}