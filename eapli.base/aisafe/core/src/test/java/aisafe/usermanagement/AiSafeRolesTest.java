package aisafe.usermanagement;

import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AiSafeRolesTest {

    @Test
    void adminRoleIsCorrect() {
        assertEquals(Role.valueOf("ADMIN"), AiSafeRoles.ADMIN);
    }

    @Test
    void backofficeOperatorRoleIsCorrect() {
        assertEquals(Role.valueOf("BACKOFFICE_OPERATOR"), AiSafeRoles.BACKOFFICE_OPERATOR);
    }

    @Test
    void weatherPersonRoleIsCorrect() {
        assertEquals(Role.valueOf("WEATHER_PERSON"), AiSafeRoles.WEATHER_PERSON);
    }

    @Test
    void airTransportCompanyCollaboratorRoleIsCorrect() {
        assertEquals(
                Role.valueOf("AIR_TRANSPORT_COMPANY_COLLABORATOR"),
                AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR
        );
    }

    @Test
    void pilotRoleIsCorrect() {
        assertEquals(Role.valueOf("PILOT"), AiSafeRoles.PILOT);
    }

    @Test
    void flightControlOperatorRoleIsCorrect() {
        assertEquals(Role.valueOf("FLIGHT_CONTROL_OPERATOR"), AiSafeRoles.FLIGHT_CONTROL_OPERATOR);
    }

    @Test
    void rolesAreNotNull() {
        assertNotNull(AiSafeRoles.ADMIN);
        assertNotNull(AiSafeRoles.BACKOFFICE_OPERATOR);
        assertNotNull(AiSafeRoles.WEATHER_PERSON);
        assertNotNull(AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);
        assertNotNull(AiSafeRoles.PILOT);
        assertNotNull(AiSafeRoles.FLIGHT_CONTROL_OPERATOR);
    }

    @Test
    void nonUserValuesReturnsAllAvailableRoles() {
        final Role[] roles = AiSafeRoles.nonUserValues();

        final List<Role> roleList = Arrays.asList(roles);

        assertEquals(6, roles.length);
        assertTrue(roleList.contains(AiSafeRoles.ADMIN));
        assertTrue(roleList.contains(AiSafeRoles.BACKOFFICE_OPERATOR));
        assertTrue(roleList.contains(AiSafeRoles.WEATHER_PERSON));
        assertTrue(roleList.contains(AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR));
        assertTrue(roleList.contains(AiSafeRoles.PILOT));
        assertTrue(roleList.contains(AiSafeRoles.FLIGHT_CONTROL_OPERATOR));
    }

    @Test
    void nonUserValuesReturnsRolesInExpectedOrder() {
        final Role[] roles = AiSafeRoles.nonUserValues();

        assertArrayEquals(
                new Role[] {
                        AiSafeRoles.ADMIN,
                        AiSafeRoles.BACKOFFICE_OPERATOR,
                        AiSafeRoles.WEATHER_PERSON,
                        AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR,
                        AiSafeRoles.PILOT,
                        AiSafeRoles.FLIGHT_CONTROL_OPERATOR
                },
                roles
        );
    }
}
