package aisafe.app.backoffice.console.presentation.aircraftmodel;

import aisafe.aircraftmodel.application.AddEngineModelToAircraftModelController;
import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.enginemodel.domain.EngineModel;
import aisafe.enginemodel.domain.EngineType;
import aisafe.persistence.PersistenceContext;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("squid:S106")
public class AddEngineModelToAircraftModelUI extends AbstractUI {

    private final AddEngineModelToAircraftModelController controller =
            new AddEngineModelToAircraftModelController(
                    PersistenceContext.repositories().aircraftModels(),
                    PersistenceContext.repositories().engineModels()
            );

    @Override
    protected boolean doShow() {

        // 1. Show available aircraft models
        final List<AircraftModel> aircraftModelList = new ArrayList<>();
        controller.getAircraftModels().forEach(aircraftModelList::add);

        if (aircraftModelList.isEmpty()) {
            System.out.println("No aircraft models registered in the system.");
            return false;
        }

        System.out.println("Available Aircraft Models:");
        for (int i = 0; i < aircraftModelList.size(); i++) {
            final var model = aircraftModelList.get(i);
            System.out.printf("  %d. %s — Certified engines: %s%n",
                    i + 1, model, model.certifiedEngineModels());
        }

        int modelIndex;
        do {
            modelIndex = Console.readInteger("Select aircraft model (number):") - 1;
            if (modelIndex < 0 || modelIndex >= aircraftModelList.size()) {
                System.out.println("Invalid selection. Please choose a number between 1 and "
                        + aircraftModelList.size() + ".");
            }
        } while (modelIndex < 0 || modelIndex >= aircraftModelList.size());

        final AircraftModel selectedModel = aircraftModelList.get(modelIndex);

        // 2. Determine compatible engine type from existing certified engines
        final EngineType compatibleType = selectedModel.certifiedEngineModels()
                .iterator().next().type();

        // 3. Filter engine models — only show compatible and not yet certified
        final List<EngineModel> allEngineModels = new ArrayList<>();
        controller.getEngineModels().forEach(allEngineModels::add);

        final List<EngineModel> availableEngineModels = allEngineModels.stream()
                .filter(e -> e.type().equals(compatibleType))
                .filter(e -> !selectedModel.hasCertifiedEngine(e))
                .toList();

        if (availableEngineModels.isEmpty()) {
            System.out.println("No compatible engine models available to add.");
            System.out.printf("(Compatible type: %s — all models of this type are already certified)%n",
                    compatibleType);
            return false;
        }

        System.out.printf("Available Engine Models (type: %s):%n", compatibleType);
        for (int i = 0; i < availableEngineModels.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, availableEngineModels.get(i));
        }

        int engineIndex;
        do {
            engineIndex = Console.readInteger("Select engine model to add (number):") - 1;
            if (engineIndex < 0 || engineIndex >= availableEngineModels.size()) {
                System.out.println("Invalid selection. Please choose a number between 1 and "
                        + availableEngineModels.size() + ".");
            }
        } while (engineIndex < 0 || engineIndex >= availableEngineModels.size());

        final String engineModelID = availableEngineModels.get(engineIndex).identity().toString();

        // 4. Confirm and add
        System.out.printf("%nSummary:%n");
        System.out.printf("  Aircraft Model: %s%n", selectedModel);
        System.out.printf("  Engine Model:   %s%n", availableEngineModels.get(engineIndex));

        final String confirm = Console.readLine("Confirm operation? (y/n):");
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Operation cancelled.");
            return false;
        }

        try {
            controller.addEngineModel(selectedModel.identity().toString(), engineModelID);
            System.out.println("Engine model added successfully.");
            return true;
        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        } catch (final RuntimeException e) {
            System.out.println("Unexpected error:");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String headline() {
        return "Add Engine Model to Aircraft Model";
    }
}