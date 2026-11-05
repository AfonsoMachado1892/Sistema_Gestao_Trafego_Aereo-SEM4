package aisafe.app.backoffice.console.presentation.aircraftmodel;

import aisafe.aircraftmodel.application.RemoveEngineModelFromAircraftModelController;
import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.enginemodel.domain.EngineModel;
import aisafe.persistence.PersistenceContext;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("squid:S106")
public class RemoveEngineModelFromAircraftModelUI extends AbstractUI {

    private final RemoveEngineModelFromAircraftModelController controller =
            new RemoveEngineModelFromAircraftModelController(
                    PersistenceContext.repositories().aircraftModels(),
                    PersistenceContext.repositories().engineModels()
            );

    @Override
    protected boolean doShow() {

        // 1. Mostrar aircraft models disponíveis
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

        final AircraftModel selected = aircraftModelList.get(modelIndex);

        final List<EngineModel> certifiedEngines =
                new ArrayList<>(selected.certifiedEngineModels());

        if (certifiedEngines.size() <= 1) {
            System.out.println("Cannot remove: this aircraft model only has one certified engine.");
            return false;
        }

        System.out.println("Certified Engine Models:");
        for (int i = 0; i < certifiedEngines.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, certifiedEngines.get(i));
        }

        int engineIndex;
        do {
            engineIndex = Console.readInteger("Select engine model to remove (number):") - 1;
            if (engineIndex < 0 || engineIndex >= certifiedEngines.size()) {
                System.out.println("Invalid selection. Please choose a number between 1 and "
                        + certifiedEngines.size() + ".");
            }
        } while (engineIndex < 0 || engineIndex >= certifiedEngines.size());

        final String engineModelID = certifiedEngines.get(engineIndex).identity().toString();

        // 3. Confirmar e remover
        System.out.printf("%nSummary:%n");
        System.out.printf("  Aircraft Model: %s%n", selected);
        System.out.printf("  Engine Model:   %s%n", certifiedEngines.get(engineIndex));

        final String confirm = Console.readLine("Confirm operation? (y/n):");
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Operation cancelled.");
            return false;
        }

        try {
            controller.removeEngineModel(selected.identity().toString(), engineModelID);
            System.out.println("Engine model removed successfully.");
            return true;
        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Remove Engine Model from Aircraft Model";
    }
}