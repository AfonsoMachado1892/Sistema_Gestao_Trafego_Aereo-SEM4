package aisafe.usermanagement.domain;

import eapli.framework.infrastructure.authz.domain.model.Role;

public final class AiSafeRoles {
    /**
     * BACKOFFICE_OPERATOR
     */
    public static final Role BACKOFFICE_OPERATOR = Role.valueOf("BACKOFFICE_OPERATOR");
    /**
     * PILOT
     */
    public static final Role PILOT = Role.valueOf("PILOT");
    /**
     * ADMIN
     */
    public static final Role ADMIN = Role.valueOf("ADMIN");
    /**
     * FLIGHT_CONTROL_OPERATOR
     */
    public static final Role FLIGHT_CONTROL_OPERATOR = Role.valueOf("FLIGHT_CONTROL_OPERATOR");
    /**
     * WEATHER_PERSON
     */
    public static final Role WEATHER_PERSON
            = Role.valueOf("WEATHER_PERSON");
    /**
     * AIR_TRANSPORT_COMPANY_COLLABORATOR
     */
    public static final Role AIR_TRANSPORT_COMPANY_COLLABORATOR
            = Role.valueOf("AIR_TRANSPORT_COMPANY_COLLABORATOR");

    /**
     * get available role types for adding new users
     *
     * @return
     */
    public static Role[] nonUserValues() {
        return new Role[] { ADMIN, BACKOFFICE_OPERATOR, WEATHER_PERSON, AIR_TRANSPORT_COMPANY_COLLABORATOR, PILOT, FLIGHT_CONTROL_OPERATOR };
    }
}


