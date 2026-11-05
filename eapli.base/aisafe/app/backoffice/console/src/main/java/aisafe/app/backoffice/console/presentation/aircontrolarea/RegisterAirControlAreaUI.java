package aisafe.app.backoffice.console.presentation.aircontrolarea;

import aisafe.aircontrolarea.application.RegisterAirControlAreaController;
import aisafe.aircontrolarea.domain.AirControlArea;
import aisafe.aircontrolarea.domain.GeographicBoundary;
import aisafe.persistence.PersistenceContext;
import aisafe.sharedkernel.Coordinates;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.List;

@SuppressWarnings("squid:S106")
public class RegisterAirControlAreaUI extends AbstractUI {

    private final RegisterAirControlAreaController controller =
            new RegisterAirControlAreaController(
                    PersistenceContext.repositories().airControlAreas()
            );

    @Override
    protected boolean doShow() {

        // ── 1. Código ─────────────────────────────────────────────────────────
        String code;
        while (true) {
            code = Console.readNonEmptyLine(
                    "Air Control Area Code (0 to cancel):",
                    "Code cannot be empty"
            ).trim().toUpperCase();

            if (code.equals("0")) {
                System.out.println("Registration cancelled.");
                return false;
            }

            if (controller.codeAlreadyExists(code)) {
                System.out.printf(
                        "Code '%s' already exists. Please choose another.%n", code);
            } else {
                break;
            }
        }

        // ── 2. Nome ───────────────────────────────────────────────────────────
        String name;
        while (true) {
            name = Console.readNonEmptyLine(
                    "Air Control Area Name (0 to cancel):",
                    "Name cannot be empty"
            ).trim();

            if (name.equals("0")) {
                System.out.println("Registration cancelled.");
                return false;
            }

            if (controller.nameAlreadyExists(name)) {
                System.out.printf(
                        "Name '%s' already exists. Please choose another.%n", name);
            } else {
                break;
            }
        }

        // ── 3. Coordenadas ────────────────────────────────────────────────────
        double swLat, swLon, neLat, neLon;
        GeographicBoundary geographicBoundary;

        while (true) {
            System.out.println("\nSouthwest corner:");
            swLat = readDouble("Latitude (0 to cancel):");
            if (swLat == Double.MIN_VALUE) {
                System.out.println("Registration cancelled.");
                return false;
            }
            swLon = readDouble("Longitude (0 to cancel):");
            if (swLon == Double.MIN_VALUE) {
                System.out.println("Registration cancelled.");
                return false;
            }

            System.out.println("\nNortheast corner:");
            neLat = readDouble("Latitude (0 to cancel):");
            if (neLat == Double.MIN_VALUE) {
                System.out.println("Registration cancelled.");
                return false;
            }
            neLon = readDouble("Longitude (0 to cancel):");
            if (neLon == Double.MIN_VALUE) {
                System.out.println("Registration cancelled.");
                return false;
            }

            // Verificar sobreposição imediatamente
            try {
                geographicBoundary = GeographicBoundary.of(
                        Coordinates.of(swLat, swLon),
                        Coordinates.of(neLat, neLon));
            } catch (final IllegalArgumentException e) {
                System.out.println("Invalid coordinates: " + e.getMessage());
                continue;
            }

            final List<AirControlArea> overlapping =
                    controller.findOverlappingAreas(swLat, swLon, neLat, neLon);

            if (!overlapping.isEmpty()) {
                System.out.println(
                        "These coordinates overlap with the following areas:");
                overlapping.forEach(a ->
                        System.out.printf("  - %s (%s)%n", a.code(), a.name()));
                System.out.println("Please choose different coordinates.");
            } else {
                break;
            }
        }

        // ── 4. Resumo e confirmação ───────────────────────────────────────────
        System.out.printf("%nSummary:%n");
        System.out.printf("  Code            : %s%n", code);
        System.out.printf("  Name            : %s%n", name);
        System.out.printf("  Southwest corner: (%.4f, %.4f)%n", swLat, swLon);
        System.out.printf("  Northeast corner: (%.4f, %.4f)%n", neLat, neLon);

        final String confirm = Console.readLine("\nConfirm registration? (y/n):");
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Registration cancelled.");
            return false;
        }

        // ── 5. Registar ───────────────────────────────────────────────────────
        try {
            controller.registerAirControlArea(code, name, geographicBoundary);
            System.out.println("\nAir control area registered successfully.");
        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return false;
    }

    private double readDouble(final String prompt) {
        while (true) {
            final String input = Console.readLine(prompt).trim();
            if (input.equals("0")) return Double.MIN_VALUE;
            try {
                return Double.parseDouble(input.replace(",", "."));
            } catch (final NumberFormatException e) {
                System.out.println("  Please insert a valid numeric value.");
            }
        }
    }

    @Override
    public String headline() {
        return "Register Air Control Area";
    }
}