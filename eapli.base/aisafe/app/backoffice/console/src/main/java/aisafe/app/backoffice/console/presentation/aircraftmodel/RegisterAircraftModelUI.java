package aisafe.app.backoffice.console.presentation.aircraftmodel;

import aisafe.aircraftmodel.application.RegisterAircraftModelController;
import aisafe.aircraftmodel.domain.AircraftType;
import aisafe.enginemodel.domain.EngineModel;
import aisafe.enginemodel.domain.EngineType;
import aisafe.maker.domain.Maker;
import aisafe.persistence.PersistenceContext;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("squid:S106")
public class RegisterAircraftModelUI extends AbstractUI {

    private final RegisterAircraftModelController controller = new RegisterAircraftModelController(
            PersistenceContext.repositories().makers(),
            PersistenceContext.repositories().engineModels(),
            PersistenceContext.repositories().aircraftModels()
    );

    @Override
    protected boolean doShow() {

        // 1. Show available aircraft makers only
        final var makers = controller.getMakers();
        final List<Maker> makerList = new ArrayList<>();
        makers.forEach(makerList::add);

        if (makerList.isEmpty()) {
            System.out.println("No aircraft makers registered in the system. Please register a maker first.");
            return false;
        }

        System.out.println("Available Aircraft Makers:");
        for (int i = 0; i < makerList.size(); i++) {
            System.out.printf("  %d. %s%n", i + 1, makerList.get(i));
        }

        int makerIndex;
        do {
            makerIndex = Console.readInteger("Select maker (number):") - 1;
            if (makerIndex < 0 || makerIndex >= makerList.size()) {
                System.out.println("Invalid selection. Please choose a number between 1 and " + makerList.size() + ".");
            }
        } while (makerIndex < 0 || makerIndex >= makerList.size());
        final String makerID = makerList.get(makerIndex).identity().toString();

        // 2. Gather model data
        final String modelID = Console.readNonEmptyLine("Aircraft Model ID (e.g. A320, B737-Max-8):", "ID cannot be empty");

        // 3. Select aircraft type
        System.out.println("Aircraft Types:");
        final AircraftType[] aircraftTypes = AircraftType.values();
        for (int i = 0; i < aircraftTypes.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, aircraftTypes[i]);
        }

        int typeIndex;
        do {
            typeIndex = Console.readInteger("Select aircraft type (number):") - 1;
            if (typeIndex < 0 || typeIndex >= aircraftTypes.length) {
                System.out.println("Invalid selection. Please choose a number between 1 and " + aircraftTypes.length + ".");
            }
        } while (typeIndex < 0 || typeIndex >= aircraftTypes.length);
        final String type = aircraftTypes[typeIndex].name();

        // 4. Select certified engine models with type filtering
        final List<EngineModel> allEngineModels = new ArrayList<>();
        controller.getEngineModels().forEach(allEngineModels::add);

        if (allEngineModels.isEmpty()) {
            System.out.println("No engine models registered in the system. Please register an engine model first.");
            return false;
        }

        final Set<String> selectedEngineModelIDs = new HashSet<>();
        EngineType selectedEngineType = null;

        System.out.println("Select certified engine models (enter 0 to finish):");

        while (true) {
            // Filter available engines: if type already chosen, show only same type; exclude already selected
            final EngineType filterType = selectedEngineType;
            final List<EngineModel> availableEngines = allEngineModels.stream()
                    .filter(e -> filterType == null || e.type().equals(filterType))
                    .filter(e -> !selectedEngineModelIDs.contains(e.identity().toString()))
                    .toList();

            if (availableEngines.isEmpty()) {
                if (selectedEngineModelIDs.isEmpty()) {
                    System.out.println("No engine models available.");
                    return false;
                }
                System.out.println("No more compatible engine models available.");
                break;
            }

            if (selectedEngineType != null) {
                System.out.printf("Available Engine Models (type: %s):%n", selectedEngineType);
            } else {
                System.out.println("Available Engine Models:");
            }
            for (int i = 0; i < availableEngines.size(); i++) {
                System.out.printf("  %d. %s%n", i + 1, availableEngines.get(i));
            }

            final int engineIndex = Console.readInteger("Add engine model (number) or 0 to finish:") - 1;

            if (engineIndex == -1) {
                if (selectedEngineModelIDs.isEmpty()) {
                    System.out.println("At least one engine model must be selected.");
                } else {
                    break;
                }
            } else if (engineIndex < 0 || engineIndex >= availableEngines.size()) {
                System.out.println("Invalid selection. Please choose a number between 1 and "
                        + availableEngines.size() + ".");
            } else {
                final EngineModel chosen = availableEngines.get(engineIndex);
                selectedEngineModelIDs.add(chosen.identity().toString());
                selectedEngineType = chosen.type();
                System.out.println("Added: " + chosen);
            }
        }

        // 5. Gather numeric data with validation
        double emptyWeight;
        do {
            emptyWeight = Console.readDouble("Empty weight (kg):");
            if (emptyWeight <= 0) System.out.println("Empty weight must be positive.");
        } while (emptyWeight <= 0);

        double mtow;
        do {
            mtow = Console.readDouble("MTOW (kg):");
            if (mtow <= emptyWeight) System.out.println("MTOW must be greater than empty weight (" + emptyWeight + " kg).");
        } while (mtow <= emptyWeight);

        double mzfw;
        do {
            mzfw = Console.readDouble("MZFW (kg):");
            if (mzfw <= 0) System.out.println("MZFW must be positive.");
        } while (mzfw <= 0);

        double maxFuelCapacity;
        do {
            maxFuelCapacity = Console.readDouble("Max fuel capacity (L):");
            if (maxFuelCapacity <= 0) System.out.println("Max fuel capacity must be positive.");
        } while (maxFuelCapacity <= 0);

        double serviceCeiling;
        do {
            serviceCeiling = Console.readDouble("Service ceiling (m):");
            if (serviceCeiling <= 0) System.out.println("Service ceiling must be positive.");
        } while (serviceCeiling <= 0);

        double cruiseSpeed;
        do {
            cruiseSpeed = Console.readDouble("Cruise speed (m/s):");
            if (cruiseSpeed <= 0) System.out.println("Cruise speed must be positive.");
        } while (cruiseSpeed <= 0);

        double wingArea;
        do {
            wingArea = Console.readDouble("Wing area (m²):");
            if (wingArea <= 0) System.out.println("Wing area must be positive.");
        } while (wingArea <= 0);

        double dragCoefficient;
        do {
            dragCoefficient = Console.readDouble("Drag coefficient (Cd):");
            if (dragCoefficient <= 0) System.out.println("Drag coefficient must be positive.");
        } while (dragCoefficient <= 0);

        double liftCoefficient;
        do {
            liftCoefficient = Console.readDouble("Lift coefficient (Cl):");
            if (liftCoefficient <= 0) System.out.println("Lift coefficient must be positive.");
        } while (liftCoefficient <= 0);

        double maximumRange;
        do {
            maximumRange = Console.readDouble("Maximum range (km):");
            if (maximumRange <= 0) System.out.println("Maximum range must be positive.");
        } while (maximumRange <= 0);

        int maxPassengerCapacity;
        do {
            maxPassengerCapacity = Console.readInteger("Max passenger capacity:");
            if (maxPassengerCapacity < 0) System.out.println("Max passenger capacity must be non-negative.");
        } while (maxPassengerCapacity < 0);

        int engineNumber;
        do {
            engineNumber = Console.readInteger("Number of engines:");
            if (engineNumber <= 0) System.out.println("Number of engines must be positive.");
        } while (engineNumber <= 0);

        // 6. Confirm and register
        System.out.printf("%nSummary:%n");
        System.out.printf("  Model ID:          %s%n", modelID);
        System.out.printf("  Maker:             %s%n", makerList.get(makerIndex).name());
        System.out.printf("  Type:              %s%n", type);
        System.out.printf("  Engine Models:     %s%n", selectedEngineModelIDs);
        System.out.printf("  Engine Type:       %s%n", selectedEngineType);
        System.out.printf("  Empty Weight:      %.2f kg%n", emptyWeight);
        System.out.printf("  MTOW:              %.2f kg%n", mtow);
        System.out.printf("  MZFW:              %.2f kg%n", mzfw);
        System.out.printf("  Max Fuel Capacity: %.2f L%n", maxFuelCapacity);
        System.out.printf("  Service Ceiling:   %.2f m%n", serviceCeiling);
        System.out.printf("  Cruise Speed:      %.2f m/s%n", cruiseSpeed);
        System.out.printf("  Wing Area:         %.2f m²%n", wingArea);
        System.out.printf("  Drag Coefficient:  %.4f%n", dragCoefficient);
        System.out.printf("  Lift Coefficient:  %.4f%n", liftCoefficient);
        System.out.printf("  Maximum Range:     %.2f km%n", maximumRange);
        System.out.printf("  Max Passengers:    %d%n", maxPassengerCapacity);
        System.out.printf("  Engine Number:     %d%n", engineNumber);

        final String confirm = Console.readLine("Confirm registration? (y/n):");
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Registration cancelled.");
            return false;
        }

        try {
            controller.registerAircraftModel(modelID, makerID, type, selectedEngineModelIDs,
                    emptyWeight, mtow, mzfw, maxFuelCapacity, serviceCeiling, cruiseSpeed,
                    wingArea, dragCoefficient, liftCoefficient, maximumRange,
                    maxPassengerCapacity, engineNumber);
            System.out.println("Aircraft model registered successfully.");
            return true;
        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Register Aircraft Model";
    }
}