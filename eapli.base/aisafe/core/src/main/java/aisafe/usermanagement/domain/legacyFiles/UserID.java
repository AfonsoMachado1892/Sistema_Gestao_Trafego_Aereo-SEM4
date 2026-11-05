package aisafe.usermanagement.domain.legacyFiles;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserID implements Serializable, ValueObject, Comparable<UserID> {

    private static final long serialVersionUID = 1L;

    private String number;

    protected UserID() {}

    public UserID(final String number) {
        Preconditions.nonEmpty(number, "UserID cannot be empty");
        this.number = number;
    }

    public static UserID of(final String number) {
        return new UserID(number);
    }

    @Override
    public int compareTo(final UserID other) {
        return this.number.compareTo(other.number);
    }

    @Override
    public String toString() {
        return number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserID)) return false;
        UserID that = (UserID) o;
        return Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}
