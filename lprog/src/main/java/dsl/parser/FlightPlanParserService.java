package dsl.parser;

import grammar.FlightPlanLexer;
import grammar.FlightPlanParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
public class FlightPlanParserService {

    public ParseResult parseFile(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        return parseText(content);
    }

    public ParseResult parseText(String content) {
        CharStream input = CharStreams.fromString(content);

        FlightPlanLexer lexer = new FlightPlanLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        FlightPlanParser parser = new FlightPlanParser(tokens);

        FlightPlanLexicalErrorListener lexicalErrorListener =
                new FlightPlanLexicalErrorListener();

        FlightPlanSyntaxErrorListener syntaxErrorListener =
                new FlightPlanSyntaxErrorListener();

        lexer.removeErrorListeners();
        parser.removeErrorListeners();

        lexer.addErrorListener(lexicalErrorListener);
        parser.addErrorListener(syntaxErrorListener);

        ParseTree tree = parser.flightPlan();

        if (lexicalErrorListener.hasErrors() || syntaxErrorListener.hasErrors()) {
            List<String> errors = new java.util.ArrayList<>();
            errors.addAll(lexicalErrorListener.errors());
            errors.addAll(syntaxErrorListener.errors());

            return ParseResult.failure(errors);
        }

        return ParseResult.success(tree);
    }

    public static class ParseResult {
        private final boolean success;
        private final ParseTree tree;
        private final List<String> errors;

        private ParseResult(boolean success, ParseTree tree, List<String> errors) {
            this.success = success;
            this.tree = tree;
            this.errors = errors;
        }

        public static ParseResult success(ParseTree tree) {
            return new ParseResult(true, tree, List.of());
        }

        public static ParseResult failure(List<String> errors) {
            return new ParseResult(false, null, List.copyOf(errors));
        }

        public boolean isSuccess() {
            return success;
        }

        public ParseTree tree() {
            return tree;
        }

        public List<String> errors() {
            return errors;
        }
    }
}