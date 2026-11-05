package aisafe.costumerCollaborator.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CustomerReference implements ValueObject, Comparable<CustomerReference>, Serializable {

    private static final long serialVersionUID = 1L;

    @Enumerated(EnumType.STRING)
    private CustomerType customerType;

    private String customerCode;


    protected CustomerReference(){

        //

    }

    public CustomerReference(CustomerType customerType, String customerCode){

        Preconditions.noneNull(customerType, "Insert a valid Customer Type.");
        Preconditions.nonEmpty(customerCode, "Insert a valid code.");

        this.customerType = customerType;
        this.customerCode = customerCode;

    }

    public static CustomerReference of(CustomerType type, String code){

        return new CustomerReference(type, code);

    }

    public CustomerType customerType() {
        return customerType;
    }

    public String customerCode() {
        return customerCode;
    }

    @Override
    public int compareTo(final CustomerReference other) {
        int cmp = this.customerType.compareTo(other.customerType);
        if (cmp != 0) {
            return cmp;
        }
        return this.customerCode.compareTo(other.customerCode);
    }

    @Override
    public String toString() {
        return customerType + " - " + customerCode;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CustomerReference)) {
            return false;
        }

        final CustomerReference that = (CustomerReference) other;

        return customerType == that.customerType
                && Objects.equals(customerCode, that.customerCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerType, customerCode);
    }

}
