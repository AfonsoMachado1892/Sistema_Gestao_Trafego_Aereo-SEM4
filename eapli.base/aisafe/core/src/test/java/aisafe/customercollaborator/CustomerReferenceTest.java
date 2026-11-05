package aisafe.customercollaborator;

import aisafe.costumerCollaborator.domain.CustomerReference;
import aisafe.costumerCollaborator.domain.CustomerType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomerReferenceTest {

    @Test
    void validCustomerReferenceForATC() {
        assertDoesNotThrow(() -> CustomerReference.of(CustomerType.AIR_TRANSPORT_COMPANY, "TP"));
    }

    @Test
    void validCustomerReferenceForACA() {
        assertDoesNotThrow(() -> CustomerReference.of(CustomerType.AIR_CONTROL_AREA, "LPPC"));
    }

    @Test
    void nullCustomerTypeThrows() {
        assertThrows(Exception.class, () -> CustomerReference.of(null, "TP"));
    }

    @Test
    void emptyCustomerCodeThrows() {
        assertThrows(Exception.class, () ->
                CustomerReference.of(CustomerType.AIR_TRANSPORT_COMPANY, ""));
    }

    @Test
    void nullCustomerCodeThrows() {
        assertThrows(Exception.class, () ->
                CustomerReference.of(CustomerType.AIR_TRANSPORT_COMPANY, null));
    }

    @Test
    void customerTypeReturnsCorrectValue() {
        assertEquals(CustomerType.AIR_TRANSPORT_COMPANY,
                CustomerReference.of(CustomerType.AIR_TRANSPORT_COMPANY, "TP").customerType());
    }

    @Test
    void customerCodeReturnsCorrectValue() {
        assertEquals("TP",
                CustomerReference.of(CustomerType.AIR_TRANSPORT_COMPANY, "TP").customerCode());
    }

    @Test
    void toStringContainsTypeAndCode() {
        final String result = CustomerReference.of(CustomerType.AIR_TRANSPORT_COMPANY, "TP").toString();
        assertTrue(result.contains("AIR_TRANSPORT_COMPANY"));
        assertTrue(result.contains("TP"));
    }

    @Test
    void equalReferencesAreEqual() {
        assertEquals(
                CustomerReference.of(CustomerType.AIR_TRANSPORT_COMPANY, "TP"),
                CustomerReference.of(CustomerType.AIR_TRANSPORT_COMPANY, "TP")
        );
    }

    @Test
    void differentTypeReferencesAreNotEqual() {
        assertNotEquals(
                CustomerReference.of(CustomerType.AIR_TRANSPORT_COMPANY, "TP"),
                CustomerReference.of(CustomerType.AIR_CONTROL_AREA, "TP")
        );
    }

    @Test
    void differentCodeReferencesAreNotEqual() {
        assertNotEquals(
                CustomerReference.of(CustomerType.AIR_TRANSPORT_COMPANY, "TP"),
                CustomerReference.of(CustomerType.AIR_TRANSPORT_COMPANY, "RY")
        );
    }

    @Test
    void hashCodeIsConsistent() {
        final CustomerReference ref = CustomerReference.of(CustomerType.AIR_TRANSPORT_COMPANY, "TP");
        assertEquals(ref.hashCode(), ref.hashCode());
    }

    @Test
    void compareToReturnsZeroForEqualReferences() {
        assertEquals(0,
                CustomerReference.of(CustomerType.AIR_TRANSPORT_COMPANY, "TP")
                        .compareTo(CustomerReference.of(CustomerType.AIR_TRANSPORT_COMPANY, "TP")));
    }
}