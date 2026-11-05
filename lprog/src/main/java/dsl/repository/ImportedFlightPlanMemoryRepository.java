package dsl.repository;

import dsl.model.ImportedFlightPlan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ImportedFlightPlanMemoryRepository {

    private static final ImportedFlightPlanMemoryRepository INSTANCE =
            new ImportedFlightPlanMemoryRepository();

    private final List<ImportedFlightPlan> flightPlans = new ArrayList<>();

    private ImportedFlightPlanMemoryRepository() {
    }

    public static ImportedFlightPlanMemoryRepository instance() {
        return INSTANCE;
    }

    public void save(ImportedFlightPlan flightPlan) {
        if (findByFlightId(flightPlan.flightId()).isPresent()) {
            return;
        }

        flightPlans.add(flightPlan);
    }

    public List<ImportedFlightPlan> findAll() {
        return Collections.unmodifiableList(flightPlans);
    }

    public Optional<ImportedFlightPlan> findByFlightId(String flightId) {
        return flightPlans.stream()
                .filter(plan -> plan.flightId().equalsIgnoreCase(flightId))
                .findFirst();
    }
}