package aisafe.enginemodel.domain;

import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerID;
import aisafe.maker.domain.MakerType;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EngineModelTest {

    // --- helpers ---

    private Maker validMaker() {
        return new Maker(
                MakerID.of("CFM"),
                "CFM International",
                new Country(IsoCode.of("FR"), "France"),
                Set.of(MakerType.ENGINE)
        );
    }

    private EngineModel validEngineModel() {
        return new EngineModel(
                EngineModelID.of("EM001"),
                "CFM56-7B",
                EngineType.TURBOFAN,
                121000.0,
                FuelType.JET_A1,
                0.37,
                validMaker()
        );
    }

    // --- constructor validation ---

    @Test
    void ensureEngineModelCannotHaveNullID() {
        assertThrows(IllegalArgumentException.class, () ->
                new EngineModel(null, "CFM56-7B", EngineType.TURBOFAN,
                        121000.0, FuelType.JET_A1, 0.37, validMaker()));
    }

    @Test
    void ensureEngineModelCannotHaveNullName() {
        assertThrows(IllegalArgumentException.class, () ->
                new EngineModel(EngineModelID.of("EM001"), null, EngineType.TURBOFAN,
                        121000.0, FuelType.JET_A1, 0.37, validMaker()));
    }

    @Test
    void ensureEngineModelCannotHaveEmptyName() {
        assertThrows(IllegalArgumentException.class, () ->
                new EngineModel(EngineModelID.of("EM001"), "", EngineType.TURBOFAN,
                        121000.0, FuelType.JET_A1, 0.37, validMaker()));
    }

    @Test
    void ensureEngineModelCannotHaveNullType() {
        assertThrows(IllegalArgumentException.class, () ->
                new EngineModel(EngineModelID.of("EM001"), "CFM56-7B", null,
                        121000.0, FuelType.JET_A1, 0.37, validMaker()));
    }

    @Test
    void ensureEngineModelCannotHaveNullFuel() {
        assertThrows(IllegalArgumentException.class, () ->
                new EngineModel(EngineModelID.of("EM001"), "CFM56-7B", EngineType.TURBOFAN,
                        121000.0, null, 0.37, validMaker()));
    }

    @Test
    void ensureEngineModelCannotHaveNullMaker() {
        assertThrows(IllegalArgumentException.class, () ->
                new EngineModel(EngineModelID.of("EM001"), "CFM56-7B", EngineType.TURBOFAN,
                        121000.0, FuelType.JET_A1, 0.37, null));
    }

    @Test
    void ensureEngineModelCannotHaveZeroPower() {
        assertThrows(IllegalArgumentException.class, () ->
                new EngineModel(EngineModelID.of("EM001"), "CFM56-7B", EngineType.TURBOFAN,
                        0.0, FuelType.JET_A1, 0.37, validMaker()));
    }

    @Test
    void ensureEngineModelCannotHaveNegativePower() {
        assertThrows(IllegalArgumentException.class, () ->
                new EngineModel(EngineModelID.of("EM001"), "CFM56-7B", EngineType.TURBOFAN,
                        -1.0, FuelType.JET_A1, 0.37, validMaker()));
    }

    @Test
    void ensureEngineModelCannotHaveZeroTsfc() {
        assertThrows(IllegalArgumentException.class, () ->
                new EngineModel(EngineModelID.of("EM001"), "CFM56-7B", EngineType.TURBOFAN,
                        121000.0, FuelType.JET_A1, 0.0, validMaker()));
    }

    @Test
    void ensureEngineModelCannotHaveNegativeTsfc() {
        assertThrows(IllegalArgumentException.class, () ->
                new EngineModel(EngineModelID.of("EM001"), "CFM56-7B", EngineType.TURBOFAN,
                        121000.0, FuelType.JET_A1, -0.1, validMaker()));
    }

    // --- getters ---

    @Test
    void ensureEngineModelIsCreatedWithValidValues() {
        // Arrange
        final var maker = validMaker();
        final var engineModel = new EngineModel(
                EngineModelID.of("EM001"), "CFM56-7B",
                EngineType.TURBOFAN, 121000.0,
                FuelType.JET_A1, 0.37, maker);

        // Act + Assert
        assertEquals("CFM56-7B", engineModel.name());
        assertEquals(EngineType.TURBOFAN, engineModel.type());
        assertEquals(121000.0, engineModel.power());
        assertEquals(FuelType.JET_A1, engineModel.fuel());
        assertEquals(0.37, engineModel.tsfc());
        assertEquals(maker, engineModel.maker());
    }

    // --- identity and sameAs ---

    @Test
    void ensureIdentityReturnsCorrectEngineModelID() {
        assertEquals(EngineModelID.of("EM001"), validEngineModel().identity());
    }

    @Test
    void ensureSameAsReturnsTrueForSameNameAndMaker() {
        // Arrange
        final var maker = validMaker();
        final var em1 = new EngineModel(EngineModelID.of("EM001"), "CFM56-7B",
                EngineType.TURBOFAN, 121000.0, FuelType.JET_A1, 0.37, maker);
        final var em2 = new EngineModel(EngineModelID.of("EM999"), "CFM56-7B",
                EngineType.TURBOFAN, 121000.0, FuelType.JET_A1, 0.37, maker);

        assertTrue(em1.sameAs(em2));
    }

    @Test
    void ensureSameAsReturnsFalseForDifferentName() {
        // Arrange
        final var maker = validMaker();
        final var em1 = new EngineModel(EngineModelID.of("EM001"), "CFM56-7B",
                EngineType.TURBOFAN, 121000.0, FuelType.JET_A1, 0.37, maker);
        final var em2 = new EngineModel(EngineModelID.of("EM002"), "CFM56-5B",
                EngineType.TURBOFAN, 121000.0, FuelType.JET_A1, 0.37, maker);

        assertFalse(em1.sameAs(em2));
    }

    @Test
    void ensureSameAsReturnsFalseForDifferentMaker() {
        // Arrange
        final var maker1 = validMaker();
        final var maker2 = new Maker(MakerID.of("GE"), "GE Aviation",
                new Country(IsoCode.of("US"), "United States"), Set.of(MakerType.ENGINE));
        final var em1 = new EngineModel(EngineModelID.of("EM001"), "CFM56-7B",
                EngineType.TURBOFAN, 121000.0, FuelType.JET_A1, 0.37, maker1);
        final var em2 = new EngineModel(EngineModelID.of("EM002"), "CFM56-7B",
                EngineType.TURBOFAN, 121000.0, FuelType.JET_A1, 0.37, maker2);

        assertFalse(em1.sameAs(em2));
    }

    @Test
    void ensureSameAsReturnsFalseForNonEngineModelObject() {
        assertFalse(validEngineModel().sameAs("not an engine model"));
    }
}