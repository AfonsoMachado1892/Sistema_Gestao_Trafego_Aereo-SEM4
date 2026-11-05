package aisafe.costumerCollaborator.domain;

import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.infrastructure.authz.domain.model.Role;

public enum CustomerType {

    AIR_TRANSPORT_COMPANY {
        @Override
        public Role role() {
            return AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR;
        }
    },
    AIR_CONTROL_AREA {
        @Override
        public Role role() {
            return AiSafeRoles.FLIGHT_CONTROL_OPERATOR;
        }
    };

    public abstract Role role();
}