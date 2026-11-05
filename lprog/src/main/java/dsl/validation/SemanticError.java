package dsl.validation;

public class SemanticError {

    private final String type;
    private final int line;
    private final int column;
    private final String message;

    public SemanticError(String type, int line, int column, String message) {
        this.type = type;
        this.line = line;
        this.column = column;
        this.message = message;
    }

    public String type() {
        return type;
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }

    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return String.format(
                "%s at line %d, column %d: %s",
                type,
                line,
                column,
                message
        );
    }
}