package dsl.parser;

import dsl.listener.FlightPlanStructureListener;
import dsl.model.ImportedFlightPlan;
import dsl.repository.ImportedFlightPlanMemoryRepository;
import dsl.validation.FlightPlanSemanticValidator;
import dsl.validation.SemanticError;
import dsl.visitor.FlightPlanBuilderVisitor;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class FlightPlanImportService {

    private final FlightPlanParserService parserService;
    private final FlightPlanSemanticValidator semanticValidator;
    private final ImportedFlightPlanMemoryRepository repository;

    public FlightPlanImportService() {
        this.parserService = new FlightPlanParserService();
        this.semanticValidator = new FlightPlanSemanticValidator();
        this.repository = ImportedFlightPlanMemoryRepository.instance();
    }

    public ImportResult importFromFile(String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            return ImportResult.failure(
                    List.of("FILE_ERROR at line 0, column 0: File path cannot be empty.")
            );
        }

        if (!filePath.toLowerCase().endsWith(".flight")) {
            return ImportResult.failure(
                    List.of("FILE_ERROR at line 0, column 0: Invalid file format. Only .flight files are supported.")
            );
        }

        return importFromFile(Path.of(filePath));
    }

    public ImportResult importFromFile(Path filePath) throws IOException {
        FlightPlanParserService.ParseResult parseResult = parserService.parseFile(filePath);

        if (!parseResult.isSuccess()) {
            return ImportResult.failure(parseResult.errors());
        }

        FlightPlanStructureListener listener = new FlightPlanStructureListener();
        ParseTreeWalker.DEFAULT.walk(listener, parseResult.tree());

        FlightPlanBuilderVisitor visitor = new FlightPlanBuilderVisitor();
        ImportedFlightPlan flightPlan = visitor.visit(parseResult.tree());

        List<SemanticError> semanticErrors = semanticValidator.validate(flightPlan);

        if (!semanticErrors.isEmpty()) {
            List<String> errors = semanticErrors.stream()
                    .map(SemanticError::toString)
                    .toList();

            return ImportResult.failure(errors);
        }

        repository.save(flightPlan);

        return ImportResult.success(
                flightPlan,
                listener.flightCount(),
                listener.legCount(),
                listener.segmentCount()
        );
    }

    public List<ImportedFlightPlan> listImportedFlightPlans() {
        return repository.findAll();
    }

    public static class ImportResult {

        private final boolean success;
        private final ImportedFlightPlan flightPlan;
        private final List<String> errors;
        private final int flightCount;
        private final int legCount;
        private final int segmentCount;

        private ImportResult(
                boolean success,
                ImportedFlightPlan flightPlan,
                List<String> errors,
                int flightCount,
                int legCount,
                int segmentCount) {

            this.success = success;
            this.flightPlan = flightPlan;
            this.errors = errors;
            this.flightCount = flightCount;
            this.legCount = legCount;
            this.segmentCount = segmentCount;
        }

        public static ImportResult success(
                ImportedFlightPlan flightPlan,
                int flightCount,
                int legCount,
                int segmentCount) {

            return new ImportResult(
                    true,
                    flightPlan,
                    List.of(),
                    flightCount,
                    legCount,
                    segmentCount
            );
        }

        public static ImportResult failure(List<String> errors) {
            return new ImportResult(
                    false,
                    null,
                    List.copyOf(errors),
                    0,
                    0,
                    0
            );
        }

        public boolean isSuccess() {
            return success;
        }

        public ImportedFlightPlan flightPlan() {
            return flightPlan;
        }

        public List<String> errors() {
            return errors;
        }

        public int flightCount() {
            return flightCount;
        }

        public int legCount() {
            return legCount;
        }

        public int segmentCount() {
            return segmentCount;
        }
    }
}