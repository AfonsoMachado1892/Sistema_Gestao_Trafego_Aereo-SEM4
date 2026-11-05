package aisafe.costumerCollaborator.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CustomerCollaboratorID implements ValueObject, Serializable, Comparable<CustomerCollaboratorID> {


    private static final long serialVersionUID = 1L;

    private String code;

    protected CustomerCollaboratorID(){

        //

    }

    public CustomerCollaboratorID(String code){

        Preconditions.nonEmpty(code, "Please insert a valid code.");

        this.code = code;

    }

    public static CustomerCollaboratorID of(String code){

        return new CustomerCollaboratorID(code);

    }

    public static CustomerCollaboratorID newID() {
        return new CustomerCollaboratorID(UUID.randomUUID().toString());
    }


    public String code(){

        return code;

    }

    @Override
    public int compareTo(final CustomerCollaboratorID other) {
        return this.code.compareTo(other.code);
    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CustomerCollaboratorID)) {
            return false;
        }

        final CustomerCollaboratorID that = (CustomerCollaboratorID) other;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }


}
