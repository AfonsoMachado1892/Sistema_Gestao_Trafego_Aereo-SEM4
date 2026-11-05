package aisafe.sharedkernel;

import eapli.framework.domain.model.ValueObject;
import eapli.framework.validations.Preconditions;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class IsoCode implements ValueObject, Serializable {

    private static final long serialVersionUID = 1L;

    private String code;

    protected IsoCode() {
        // for ORM
    }

    public IsoCode(final String code) {
        Preconditions.nonEmpty(code, "ISO code cannot be empty");

        final String normalizedCode = code.trim().toUpperCase();

        Preconditions.ensure(
                normalizedCode.matches("[A-Z]{2}"),
                "ISO code must follow ISO 3166-1 alpha-2 format"
        );

        this.code = normalizedCode;
    }

    public static IsoCode of(final String code) {
        return new IsoCode(code);
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
        if (!(o instanceof IsoCode)) return false;
        IsoCode isoCode = (IsoCode) o;
        return Objects.equals(code, isoCode.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}