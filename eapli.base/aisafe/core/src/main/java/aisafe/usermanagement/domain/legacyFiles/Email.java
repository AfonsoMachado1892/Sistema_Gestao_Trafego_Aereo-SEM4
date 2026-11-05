package aisafe.usermanagement.domain.legacyFiles;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class Email implements Serializable, ValueObject, Comparable<Email> {

    private static final long serialVersionUID = 1L;

    private String email;

    protected Email(){}

    public Email(String email){
        Preconditions.nonEmpty(email, "Email cannot be empty");
        this.email = email;
    }

    public static Email of(String email){
        return new Email(email);
    }

    @Override
    public int compareTo(final Email other) {
        return this.email.compareTo(other.email);
    }

    @Override
    public String toString() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email)) return false;
        Email that = (Email) o;
        return Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}
