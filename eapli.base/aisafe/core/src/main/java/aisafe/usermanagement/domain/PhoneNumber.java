package aisafe.usermanagement.domain;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class PhoneNumber implements Serializable, ValueObject, Comparable<PhoneNumber> {

    private int countryCode;
    private String phoneNumber;

    private static final long serialVersionUID = 1L;

    protected PhoneNumber(){}

    public PhoneNumber(int countryCode, String phoneNumber){

        Preconditions.ensure(countryCode > 0 && countryCode <= 999, "Country Code must have, atleast, 1 digit.");
        Preconditions.nonEmpty(phoneNumber, "Phone Number must be filled.");
        Preconditions.ensure(phoneNumber.matches("\\d{1,15}"), "Invalid number.");

        this.countryCode = countryCode;
        this.phoneNumber = phoneNumber;

    }

    public static PhoneNumber of(int countryCode, String phoneNumber){

        return new PhoneNumber(countryCode, phoneNumber);

    }

    @Override
    public String toString() {
        return "+" + countryCode + " " + phoneNumber;
    }

    @Override
    public int compareTo(final PhoneNumber other) {
        int cmp = Integer.compare(this.countryCode, other.countryCode);
        if (cmp != 0) return cmp;
        return this.phoneNumber.compareTo(other.phoneNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhoneNumber)) return false;
        PhoneNumber that = (PhoneNumber) o;
        return countryCode == that.countryCode &&
                Objects.equals(phoneNumber, that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryCode, phoneNumber);
    }

}
