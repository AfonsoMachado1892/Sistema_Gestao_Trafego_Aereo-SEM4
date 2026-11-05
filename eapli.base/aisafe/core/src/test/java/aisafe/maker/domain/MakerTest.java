package aisafe.maker.domain;

import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MakerTest {

    // --- helpers ---

    private Maker validEngineMaker() {
        return new Maker(
                MakerID.of("CFM"),
                "CFM International",
                new Country(IsoCode.of("FR"), "France"),
                Set.of(MakerType.ENGINE)
        );
    }

    private Maker validAircraftMaker() {
        return new Maker(
                MakerID.of("AIRBUS"),
                "Airbus",
                new Country(IsoCode.of("FR"), "France"),
                Set.of(MakerType.AIRCRAFT)
        );
    }

    private Maker validDualMaker() {
        return new Maker(
                MakerID.of("RR"),
                "Rolls-Royce",
                new Country(IsoCode.of("GB"), "United Kingdom"),
                Set.of(MakerType.ENGINE, MakerType.AIRCRAFT)
        );
    }

    // --- constructor validation ---

    @Test
    void ensureMakerCannotHaveNullID() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                new Maker(null, "CFM International",
                        new Country(IsoCode.of("FR"), "France"),
                        Set.of(MakerType.ENGINE)));
    }

    @Test
    void ensureMakerCannotHaveNullName() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                new Maker(MakerID.of("CFM"), null,
                        new Country(IsoCode.of("FR"), "France"),
                        Set.of(MakerType.ENGINE)));
    }

    @Test
    void ensureMakerCannotHaveEmptyName() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                new Maker(MakerID.of("CFM"), "",
                        new Country(IsoCode.of("FR"), "France"),
                        Set.of(MakerType.ENGINE)));
    }

    @Test
    void ensureMakerCannotHaveNullCountry() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                new Maker(MakerID.of("CFM"), "CFM International",
                        null,
                        Set.of(MakerType.ENGINE)));
    }

    @Test
    void ensureMakerCannotHaveNullTypes() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                new Maker(MakerID.of("CFM"), "CFM International",
                        new Country(IsoCode.of("FR"), "France"),
                        null));
    }

    @Test
    void ensureMakerCannotHaveEmptyTypes() {
        // Arrange + Act + Assert
        assertThrows(IllegalArgumentException.class, () ->
                new Maker(MakerID.of("CFM"), "CFM International",
                        new Country(IsoCode.of("FR"), "France"),
                        Set.of()));
    }

    // --- hasType ---

    @Test
    void ensureMakerOfTypeEngineHasTypeEngine() {
        // Arrange
        final var maker = validEngineMaker();

        // Act + Assert
        assertTrue(maker.hasType(MakerType.ENGINE));
    }

    @Test
    void ensureMakerOfTypeEngineDoesNotHaveTypeAircraft() {
        // Arrange
        final var maker = validEngineMaker();

        // Act + Assert
        assertFalse(maker.hasType(MakerType.AIRCRAFT));
    }

    @Test
    void ensureMakerOfTypeAircraftHasTypeAircraft() {
        // Arrange
        final var maker = validAircraftMaker();

        // Act + Assert
        assertTrue(maker.hasType(MakerType.AIRCRAFT));
    }

    @Test
    void ensureDualTypeMakerHasBothTypes() {
        // Arrange
        final var maker = validDualMaker();

        // Act + Assert
        assertTrue(maker.hasType(MakerType.ENGINE));
        assertTrue(maker.hasType(MakerType.AIRCRAFT));
    }

    // --- identity and sameAs ---

    @Test
    void ensureIdentityReturnsCorrectMakerID() {
        // Arrange
        final var maker = validEngineMaker();

        // Act + Assert
        assertEquals(MakerID.of("CFM"), maker.identity());
    }

    @Test
    void ensureSameAsReturnsTrueForSameMakerID() {
        // Arrange
        final var maker1 = validEngineMaker();
        final var maker2 = new Maker(
                MakerID.of("CFM"),
                "CFM International",
                new Country(IsoCode.of("FR"), "France"),
                Set.of(MakerType.ENGINE)
        );

        // Act + Assert
        assertTrue(maker1.sameAs(maker2));
    }

    @Test
    void ensureSameAsReturnsFalseForDifferentMakerID() {
        // Arrange
        final var maker1 = validEngineMaker();
        final var maker2 = validAircraftMaker();

        // Act + Assert
        assertFalse(maker1.sameAs(maker2));
    }

    @Test
    void ensureSameAsReturnsFalseForNonMakerObject() {
        // Arrange
        final var maker = validEngineMaker();

        // Act + Assert
        assertFalse(maker.sameAs("not a maker"));
    }
}