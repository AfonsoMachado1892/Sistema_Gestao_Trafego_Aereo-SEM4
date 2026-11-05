package aisafe.sharedkernel;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class IcaoCode implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    private String code;

    protected IcaoCode() {
        // for ORM
    }

    public IcaoCode(final String code) {
        Preconditions.nonEmpty(code, "ICAO code cannot be empty");
        Preconditions.ensure(code.length() >= 2 && code.length() <= 4,
                "ICAO code must be 2-4 characters");
        this.code = code.toUpperCase();
    }

    public static IcaoCode of(final String code) {
        return new IcaoCode(code);
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
        if (!(o instanceof IcaoCode)) return false;
        IcaoCode icaoCode = (IcaoCode) o;
        return Objects.equals(code, icaoCode.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}