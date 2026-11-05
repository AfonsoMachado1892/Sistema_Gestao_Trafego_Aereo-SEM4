    package aisafe.sharedkernel;

    import eapli.framework.domain.model.ValueObject;
    import eapli.framework.validations.Preconditions;

    import jakarta.persistence.Embeddable;
    import jakarta.persistence.Embedded;
    import java.io.Serializable;
    import java.util.Objects;

    @Embeddable
    public class Country implements ValueObject, Serializable {

        private static final long serialVersionUID = 1L;

        @Embedded
        private IsoCode isoCode;

        private String name;

        protected Country() {
            // for ORM
        }

        public Country(final IsoCode isoCode, final String name) {
            Preconditions.noneNull(isoCode, "ISO code cannot be null");
            Preconditions.nonEmpty(name, "Country name cannot be empty");
            this.isoCode = isoCode;
            this.name = name;
        }

        public static Country of(final IsoCode isoCode, final String name){

            return new Country(isoCode, name);
        }

        public IsoCode isoCode() {
            return isoCode;
        }

        public String name() {
            return name;
        }

        @Override
        public String toString() {
            return name + " (" + isoCode + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Country)) return false;
            Country country = (Country) o;
            return Objects.equals(isoCode, country.isoCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(isoCode);
        }
    }