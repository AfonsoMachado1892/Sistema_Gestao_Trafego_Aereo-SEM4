package aisafe.weather.domain;

import aisafe.sharedkernel.Coordinates;
import eapli.framework.domain.model.ValueObject;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import java.io.Serial;
import java.util.Objects;

@Embeddable
public class BoundingBox implements ValueObject {

    @Serial
    private static final long serialVersionUID = 1L;

    @Embedded
    private Coordinates start;

    @Embedded
    private Coordinates end;

    public BoundingBox(final Coordinates start, final Coordinates end) {
        if (start == null) throw new IllegalArgumentException("Start coordinates are necessary");
        if (end == null) throw new IllegalArgumentException("End coordinates are necessary");
        this.start = start;
        this.end = end;
    }

    protected BoundingBox() {
        //for ORM
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoundingBox that)) return false;
        return Objects.equals(start, that.start) && Objects.equals(end, that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    public Coordinates end() {
        return end;
    }

    public Coordinates start() {
        return start;
    }

    @Override
    public String toString() {
        return start.toString() + end.toString();
    }

}
