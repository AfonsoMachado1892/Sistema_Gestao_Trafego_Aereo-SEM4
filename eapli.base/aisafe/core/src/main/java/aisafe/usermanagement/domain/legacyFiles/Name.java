package aisafe.usermanagement.domain.legacyFiles;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class Name implements Comparable<Name>, ValueObject, Serializable{

    private String firstName;
    private String lastName;

    protected Name(){}

    public Name(String firstName, String lastName){
        Preconditions.nonEmpty(firstName, "First name must be filled.");
        Preconditions.nonEmpty(lastName, "Last name must be filled.");

        this.firstName = firstName.trim();
        this.lastName = lastName.trim();

    }

    public static Name of(String firstName, String lastName){

        return new Name(firstName, lastName);

    }

    public String firstName() { return firstName; }
    public String lastName() { return lastName; }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Name)) return false;
        Name that = (Name) o;
        return Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName);
    }

    @Override
    public int compareTo(Name o) {
        int first = this.firstName.compareTo(o.firstName);
        if (first != 0) return first;
        return this.lastName.compareTo(o.lastName);
    }
}
