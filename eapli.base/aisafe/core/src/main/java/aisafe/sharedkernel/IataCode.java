package aisafe.sharedkernel;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class IataCode implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    private String code;

    protected IataCode() {
        // for ORM
    }

    public IataCode(final String code) {
        Preconditions.nonEmpty(code, "IATA code cannot be empty");
        Preconditions.ensure(code.length() >= 2 && code.length() <= 3,
                "IATA code must be 2-3 characters");
        this.code = code.toUpperCase();
    }

    public static IataCode of(final String code) {
        return new IataCode(code);
    }

    public String code() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IataCode)) return false;
        IataCode iataCode = (IataCode) o;
        return Objects.equals(code, iataCode.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}