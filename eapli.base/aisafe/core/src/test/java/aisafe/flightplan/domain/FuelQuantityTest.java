package aisafe.flightplan.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FuelQuantityTest {

    @Test
    void ensurePositiveFuelQuantityIsAccepted() {
        final FuelQuantity fuel = FuelQuantity.of(12000, FuelUnit.KG);

        assertEquals(12000, fuel.amount());
        assertEquals(FuelUnit.KG, fuel.unit());
    }

    @Test
    void ensureZeroFuelQuantityIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                FuelQuantity.of(0, FuelUnit.KG));
    }

    @Test
    void ensureNegativeFuelQuantityIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                FuelQuantity.of(-1, FuelUnit.KG));
    }

    @Test
    void ensureNullFuelUnitIsRejected() {
        assertThrows(Exception.class, () ->
                FuelQuantity.of(12000, null));
    }

    @Test
    void ensureEqualsWorksForSameValue() {
        assertEquals(
                FuelQuantity.of(12000, FuelUnit.KG),
                FuelQuantity.of(12000, FuelUnit.KG)
        );
    }

    @Test
    void ensureDifferentAmountIsNotEqual() {
        assertNotEquals(
                FuelQuantity.of(12000, FuelUnit.KG),
                FuelQuantity.of(13000, FuelUnit.KG)
        );
    }

    @Test
    void ensureDifferentUnitIsNotEqual() {
        assertNotEquals(
                FuelQuantity.of(12000, FuelUnit.KG),
                FuelQuantity.of(12000, FuelUnit.L)
        );
    }

    @Test
    void ensureToStringContainsAmountAndUnit() {
        final String text = FuelQuantity.of(12000, FuelUnit.KG).toString();

        assertTrue(text.contains("12000"));
        assertTrue(text.contains("KG"));
    }
}