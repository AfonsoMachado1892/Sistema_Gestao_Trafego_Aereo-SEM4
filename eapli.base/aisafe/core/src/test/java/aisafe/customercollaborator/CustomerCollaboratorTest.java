package aisafe.customercollaborator;


import aisafe.aircontrolarea.domain.*;
import aisafe.airtransportcompany.domain.*;
import aisafe.costumerCollaborator.domain.*;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.costumerCollaborator.service.CustomerCollaboratorService;
import aisafe.sharedkernel.Coordinates;
import aisafe.usermanagement.domain.UserBuilderHelper;
import aisafe.usermanagement.domain.legacyFiles.Email;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import aisafe.usermanagement.domain.UserBuilderHelper;
import aisafe.usermanagement.domain.legacyFiles.Email;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CustomerCollaboratorTest {

    private SystemUser systemUser;

    @BeforeEach
    void setUp() {
        systemUser = UserBuilderHelper.builder()
                .with("joao.silva", "Password1", "João", "Silva", "joao.silva@tap.pt")
                .withRoles(Set.of())
                .build();
    }

    private CustomerCollaboratorID id() { return CustomerCollaboratorID.of("123"); }
    private Email email() { return Email.of("joao.silva@tap.pt"); }

    private CustomerCollaborator validCollaborator() {
        return new CustomerCollaborator(
                id(),
                "TP",
                CustomerType.AIR_TRANSPORT_COMPANY,
                email(),
                systemUser,
                "Operations Manager"
        );
    }

    @Test
    void validCollaboratorCreation() {
        assertDoesNotThrow(this::validCollaborator);
    }

    @Test
    void nullIdThrows() {
        assertThrows(Exception.class, () ->
                new CustomerCollaborator(null, "TP",
                        CustomerType.AIR_TRANSPORT_COMPANY,
                        email(), systemUser, "Operations Manager"));
    }

    @Test
    void emptyCustomerCodeThrows() {
        assertThrows(Exception.class, () ->
                new CustomerCollaborator(id(), "",
                        CustomerType.AIR_TRANSPORT_COMPANY,
                        email(), systemUser, "Operations Manager"));
    }

    @Test
    void nullCustomerTypeThrows() {
        assertThrows(Exception.class, () ->
                new CustomerCollaborator(id(), "TP",
                        null,
                        email(), systemUser, "Operations Manager"));
    }

    @Test
    void nullEmailThrows() {
        assertThrows(Exception.class, () ->
                new CustomerCollaborator(id(), "TP",
                        CustomerType.AIR_TRANSPORT_COMPANY,
                        null, systemUser, "Operations Manager"));
    }

    @Test
    void nullSystemUserThrows() {
        assertThrows(Exception.class, () ->
                new CustomerCollaborator(id(), "TP",
                        CustomerType.AIR_TRANSPORT_COMPANY,
                        email(), null, "Operations Manager"));
    }

    @Test
    void emptyPositionThrows() {
        assertThrows(Exception.class, () ->
                new CustomerCollaborator(id(), "TP",
                        CustomerType.AIR_TRANSPORT_COMPANY,
                        email(), systemUser, ""));
    }

    @Test
    void initialStatusIsActive() {
        assertTrue(validCollaborator().isActive());
    }

    @Test
    void disableChangesStatusToDisabled() {
        final CustomerCollaborator collaborator = validCollaborator();
        collaborator.disable();
        assertEquals(CustomerStatus.DISABLED, collaborator.status());
        assertFalse(collaborator.isActive());
    }

    @Test
    void changeEmailUpdatesEmail() {
        final CustomerCollaborator collaborator = validCollaborator();
        final Email newEmail = Email.of("novo@tap.pt");
        collaborator.changeEmail(newEmail);
        assertEquals(newEmail, collaborator.email());
    }

    @Test
    void changeEmailWithNullThrows() {
        assertThrows(Exception.class, () -> validCollaborator().changeEmail(null));
    }

    @Test
    void identityReturnsID() {
        assertEquals(id(), validCollaborator().identity());
    }

    @Test
    void customerTypeReturnsCorrectValue() {
        assertEquals(CustomerType.AIR_TRANSPORT_COMPANY, validCollaborator().customerType());
    }

    @Test
    void customerCodeReturnsCorrectValue() {
        assertEquals("TP", validCollaborator().customerCode());
    }

    @Test
    void emailReturnsCorrectValue() {
        assertEquals(email(), validCollaborator().email());
    }

    @Test
    void positionReturnsCorrectValue() {
        assertEquals("Operations Manager", validCollaborator().position());
    }

    @Test
    void systemUserReturnsCorrectValue() {
        assertEquals(systemUser, validCollaborator().systemUser());
    }

    @Test
    void sameAsReturnsTrueForSameCollaborator() {
        final CustomerCollaborator c = validCollaborator();
        assertTrue(c.sameAs(c));
    }

    @Test
    void sameAsReturnsFalseForDifferentCollaborator() {
        final CustomerCollaborator c1 = validCollaborator();
        final CustomerCollaborator c2 = new CustomerCollaborator(
                CustomerCollaboratorID.of("456"),
                "TP",
                CustomerType.AIR_TRANSPORT_COMPANY,
                Email.of("outro@tap.pt"),
                systemUser,
                "Fleet Manager"
        );
        assertFalse(c1.sameAs(c2));
    }

    @Test
    void sameAsReturnsFalseForNonCollaborator() {
        assertFalse(validCollaborator().sameAs("not a collaborator"));
    }

    @Test
    void toStringContainsRelevantInfo() {
        final String result = validCollaborator().toString();
        assertTrue(result.contains("Operations Manager"));
        assertTrue(result.contains("ACTIVE"));
    }

    @Test
    void ofFactoryMethodCreatesCollaborator() {
        assertDoesNotThrow(() -> CustomerCollaborator.of(
                "TP",
                CustomerType.AIR_TRANSPORT_COMPANY,
                email(),
                systemUser,
                "Operations Manager"
        ));
    }

    @Test
    void hashCodeIsConsistent() {
        final CustomerCollaborator c = validCollaborator();
        assertEquals(c.hashCode(), c.hashCode());
    }

    @Test
    void equalCollaboratorsAreEqual() {
        final CustomerCollaborator c1 = validCollaborator();
        final CustomerCollaborator c2 = validCollaborator();
        assertEquals(c1, c2);
    }

    @Test
    void sameAsReturnsTrueForIdenticalCollaborator() {
        final CustomerCollaborator c1 = new CustomerCollaborator(
                id(), "TP", CustomerType.AIR_TRANSPORT_COMPANY,
                email(), systemUser, "Operations Manager");

        final CustomerCollaborator c2 = new CustomerCollaborator(
                id(), "TP", CustomerType.AIR_TRANSPORT_COMPANY,
                email(), systemUser, "Operations Manager");

        assertTrue(c1.sameAs(c2));
    }

    @Test
    void sameAsReturnsFalseForDifferentEmail() {
        final CustomerCollaborator c1 = new CustomerCollaborator(
                id(), "TP", CustomerType.AIR_TRANSPORT_COMPANY,
                email(), systemUser, "Operations Manager");

        final CustomerCollaborator c2 = new CustomerCollaborator(
                id(), "TP", CustomerType.AIR_TRANSPORT_COMPANY,
                Email.of("outro@tap.pt"), systemUser, "Operations Manager");

        assertFalse(c1.sameAs(c2));
    }

    @Test
    void sameAsReturnsFalseForDifferentPosition() {
        final CustomerCollaborator c1 = new CustomerCollaborator(
                id(), "TP", CustomerType.AIR_TRANSPORT_COMPANY,
                email(), systemUser, "Operations Manager");

        final CustomerCollaborator c2 = new CustomerCollaborator(
                id(), "TP", CustomerType.AIR_TRANSPORT_COMPANY,
                email(), systemUser, "Fleet Manager");

        assertFalse(c1.sameAs(c2));
    }

    @Test
    void sameAsReturnsFalseForDifferentCustomerCode() {
        final CustomerCollaborator c1 = new CustomerCollaborator(
                id(), "TP", CustomerType.AIR_TRANSPORT_COMPANY,
                email(), systemUser, "Operations Manager");

        final CustomerCollaborator c2 = new CustomerCollaborator(
                id(), "RY", CustomerType.AIR_TRANSPORT_COMPANY,
                email(), systemUser, "Operations Manager");

        assertFalse(c1.sameAs(c2));
    }

    @Test
    void sameAsReturnsFalseForDifferentStatus() {
        final CustomerCollaborator c1 = new CustomerCollaborator(
                id(), "TP", CustomerType.AIR_TRANSPORT_COMPANY,
                email(), systemUser, "Operations Manager");

        final CustomerCollaborator c2 = new CustomerCollaborator(
                id(), "TP", CustomerType.AIR_TRANSPORT_COMPANY,
                email(), systemUser, "Operations Manager");

        c2.disable();

        assertFalse(c1.sameAs(c2));
    }

    @Test
    void customerCollaboratorIDGetterReturnsCorrectValue() {
        final CustomerCollaborator c = validCollaborator();
        assertEquals(id(), c.customerCollaboratorID());
    }
}
