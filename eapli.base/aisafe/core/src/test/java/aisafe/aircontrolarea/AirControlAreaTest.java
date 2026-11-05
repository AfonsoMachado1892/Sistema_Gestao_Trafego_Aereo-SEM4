package aisafe.aircontrolarea;

import aisafe.aircontrolarea.domain.AirControlArea;
import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.aircontrolarea.domain.AirControlAreaName;
import aisafe.aircontrolarea.domain.GeographicBoundary;
import aisafe.sharedkernel.Coordinates;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AirControlAreaTest {

    private AirControlAreaCode code() { return AirControlAreaCode.of("LPPC"); }
    private AirControlAreaName name() { return AirControlAreaName.of("Lisboa"); }
    private GeographicBoundary boundary() {
        return GeographicBoundary.of(
                Coordinates.of(36.0, -10.0),
                Coordinates.of(42.0, -6.0)
        );
    }

    private AirControlArea validArea() {
        return new AirControlArea(code(), boundary(), name());
    }

    @Test
    void validAirControlAreaCreation() {
        assertDoesNotThrow(this::validArea);
    }

    @Test
    void nullCodeThrows() {
        assertThrows(Exception.class, () -> new AirControlArea(null, boundary(), name()));
    }

    @Test
    void nullBoundaryThrows() {
        assertThrows(Exception.class, () -> new AirControlArea(code(), null, name()));
    }

    @Test
    void nullNameThrows() {
        assertThrows(Exception.class, () -> new AirControlArea(code(), boundary(), null));
    }

    @Test
    void identityReturnsCode() {
        assertEquals(code(), validArea().identity());
    }

    @Test
    void codeReturnsCorrectValue() {
        assertEquals(code(), validArea().code());
    }

    @Test
    void nameReturnsCorrectValue() {
        assertEquals(name(), validArea().name());
    }

    @Test
    void containsReturnsTrueForCoordinatesInside() {
        assertTrue(validArea().contains(Coordinates.of(40.0, -8.0)));
    }

    @Test
    void containsReturnsFalseForCoordinatesOutside() {
        assertFalse(validArea().contains(Coordinates.of(51.4706, -0.4619)));
    }

    @Test
    void overlapsWithReturnsTrueForOverlappingArea() {
        final AirControlArea other = new AirControlArea(
                AirControlAreaCode.of("LECM"),
                GeographicBoundary.of(
                        Coordinates.of(38.0, -9.0),
                        Coordinates.of(44.0, -4.0)
                ),
                AirControlAreaName.of("Madrid")
        );
        assertTrue(validArea().overlapsWith(other));
    }

    @Test
    void overlapsWithReturnsFalseForNonOverlappingArea() {
        final AirControlArea other = new AirControlArea(
                AirControlAreaCode.of("EDGG"),
                GeographicBoundary.of(
                        Coordinates.of(50.0, 0.0),
                        Coordinates.of(55.0, 5.0)
                ),
                AirControlAreaName.of("Frankfurt")
        );
        assertFalse(validArea().overlapsWith(other));
    }

    @Test
    void sameAsReturnsTrueForSameCode() {
        assertTrue(validArea().sameAs(validArea()));
    }

    @Test
    void sameAsReturnsFalseForDifferentCode() {
        final AirControlArea other = new AirControlArea(
                AirControlAreaCode.of("LECM"),
                GeographicBoundary.of(
                        Coordinates.of(50.0, 0.0),
                        Coordinates.of(55.0, 5.0)
                ),
                AirControlAreaName.of("Madrid")
        );
        assertFalse(validArea().sameAs(other));
    }

    @Test
    void sameAsReturnsFalseForNonAirControlArea() {
        assertFalse(validArea().sameAs("not an area"));
    }

    @Test
    void toStringContainsCodeAndName() {
        final String result = validArea().toString();
        assertTrue(result.contains("LPPC"));
        assertTrue(result.contains("Lisboa"));
    }

    @Test
    void boundaryDescriptionIsNotEmpty() {
        assertFalse(validArea().boundaryDescription().isEmpty());
    }

    @Test
    void ofFactoryMethodCreatesArea() {
        assertDoesNotThrow(() -> AirControlArea.of(code(), boundary(), name()));
    }

    @Test
    void equalAreasAreEqual() {
        assertEquals(validArea(), validArea());
    }

    @Test
    void hashCodeIsConsistent() {
        final AirControlArea area = validArea();
        assertEquals(area.hashCode(), area.hashCode());
    }
}