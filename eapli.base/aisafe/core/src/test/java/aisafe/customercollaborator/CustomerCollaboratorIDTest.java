package aisafe.customercollaborator;

import aisafe.costumerCollaborator.domain.CustomerCollaboratorID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomerCollaboratorIDTest {

    @Test
    void validCustomerCollaboratorID() {
        assertDoesNotThrow(() -> CustomerCollaboratorID.of("123"));
    }

    @Test
    void emptyCodeThrows() {
        assertThrows(Exception.class, () -> CustomerCollaboratorID.of(""));
    }

    @Test
    void nullCodeThrows() {
        assertThrows(Exception.class, () -> CustomerCollaboratorID.of(null));
    }

    @Test
    void newIDGeneratesUniqueIDs() {
        final CustomerCollaboratorID id1 = CustomerCollaboratorID.newID();
        final CustomerCollaboratorID id2 = CustomerCollaboratorID.newID();
        assertNotEquals(id1, id2);
    }

    @Test
    void codeReturnsCorrectValue() {
        assertEquals("123", CustomerCollaboratorID.of("123").code());
    }

    @Test
    void toStringReturnsCode() {
        assertEquals("123", CustomerCollaboratorID.of("123").toString());
    }

    @Test
    void equalIDsAreEqual() {
        assertEquals(CustomerCollaboratorID.of("123"), CustomerCollaboratorID.of("123"));
    }

    @Test
    void differentIDsAreNotEqual() {
        assertNotEquals(CustomerCollaboratorID.of("123"), CustomerCollaboratorID.of("456"));
    }

    @Test
    void hashCodeIsConsistent() {
        final CustomerCollaboratorID id = CustomerCollaboratorID.of("123");
        assertEquals(id.hashCode(), id.hashCode());
    }

    @Test
    void compareToReturnsZeroForEqualIDs() {
        assertEquals(0, CustomerCollaboratorID.of("123").compareTo(CustomerCollaboratorID.of("123")));
    }

    @Test
    void compareToReturnsNonZeroForDifferentIDs() {
        assertNotEquals(0, CustomerCollaboratorID.of("123").compareTo(CustomerCollaboratorID.of("456")));
    }
}