package aisafe.app.backoffice.console.presentation.enginemodel;

import aisafe.enginemodel.application.RegisterEngineModelController;
import aisafe.enginemodel.domain.EngineType;
import aisafe.enginemodel.domain.FuelType;
import aisafe.maker.domain.Maker;
import aisafe.persistence.PersistenceContext;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("squid:S106")
public class RegisterEngineModelUI extends AbstractUI {

    private final RegisterEngineModelController controller = new RegisterEngineModelController(
            PersistenceContext.repositories().makers(),
            PersistenceContext.repositories().engineModels()
    );

    @Override
    protected boolean doShow() {

        // 1. Show available engine makers
        final var makers = controller.getEngineMakers();
        final List<Maker> makerList = new ArrayList<>();
        makers.forEach(makerList::add);

        if (makerList.isEmpty()) {
            System.out.println("No engine makers registered in the system. Please register a maker first.");
            return false;
        }

        System.out.println("Available Engine Makers:");
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

        // 2. Gather engine model data
        final String engineModelID = Console.readNonEmptyLine("Engine Model ID:", "ID cannot be empty");
        final String name = Console.readNonEmptyLine("Engine Model Name:", "Name cannot be empty");

        // 3. Select engine type
        System.out.println("Engine Types:");
        final EngineType[] engineTypes = EngineType.values();
        for (int i = 0; i < engineTypes.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, engineTypes[i]);
        }

        int typeIndex;
        do {
            typeIndex = Console.readInteger("Select engine type (number):") - 1;
            if (typeIndex < 0 || typeIndex >= engineTypes.length) {
                System.out.println("Invalid selection. Please choose a number between 1 and " + engineTypes.length + ".");
            }
        } while (typeIndex < 0 || typeIndex >= engineTypes.length);
        final String type = engineTypes[typeIndex].name();

        // 4. Gather numeric data with validation
        double power;
        do {
            power = Console.readDouble("Power (kN):");
            if (power <= 0) {
                System.out.println("Power must be a positive value.");
            }
        } while (power <= 0);

        double tsfc;
        do {
            tsfc = Console.readDouble("TSFC (N/N/s):");
            if (tsfc <= 0) {
                System.out.println("TSFC must be a positive value.");
            }
        } while (tsfc <= 0);

        // 5. Select fuel type
        System.out.println("Fuel Types:");
        final FuelType[] fuelTypes = FuelType.values();
        for (int i = 0; i < fuelTypes.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, fuelTypes[i]);
        }

        int fuelIndex;
        do {
            fuelIndex = Console.readInteger("Select fuel type (number):") - 1;
            if (fuelIndex < 0 || fuelIndex >= fuelTypes.length) {
                System.out.println("Invalid selection. Please choose a number between 1 and " + fuelTypes.length + ".");
            }
        } while (fuelIndex < 0 || fuelIndex >= fuelTypes.length);
        final String fuel = fuelTypes[fuelIndex].name();

        // 6. Confirm and register
        System.out.printf("%nSummary:%n");
        System.out.printf("  ID:     %s%n", engineModelID);
        System.out.printf("  Name:   %s%n", name);
        System.out.printf("  Type:   %s%n", type);
        System.out.printf("  Power:  %.2f kN%n", power);
        System.out.printf("  TSFC:   %.4f N/N/s%n", tsfc);
        System.out.printf("  Fuel:   %s%n", fuel);
        System.out.printf("  Maker:  %s%n", makerList.get(makerIndex).name());

        final String confirm = Console.readLine("Confirm registration? (y/n):");
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Registration cancelled.");
            return false;
        }

        try {
            controller.registerEngineModel(engineModelID, name, type, power, fuel, tsfc, makerID);
            System.out.println("Engine model registered successfully.");
            return true;
        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String headline() {
        return "Register Engine Model";
    }
}