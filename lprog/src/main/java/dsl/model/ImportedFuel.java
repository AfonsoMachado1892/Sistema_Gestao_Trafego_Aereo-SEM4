package dsl.model;

public class ImportedFuel {

    private final double quantity;
    private final String unit;
    private final int line;
    private final int column;

    public ImportedFuel(double quantity, String unit, int line, int column) {
        this.quantity = quantity;
        this.unit = unit;
        this.line = line;
        this.column = column;
    }

    public double quantity() {
        return quantity;
    }

    public String unit() {
        return unit;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }
}