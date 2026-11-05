package aisafe.app.backoffice.console.presentation.airtransportcompany;

import aisafe.airtransportcompany.application.RegisterAirTransportCompanyController;
import aisafe.persistence.PersistenceContext;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

@SuppressWarnings("squid:S106")
public class RegisterAirTransportCompanyUI extends AbstractUI {

    private final RegisterAirTransportCompanyController controller =
            new RegisterAirTransportCompanyController(
                    PersistenceContext.repositories().airTransportCompanies()
            );

    @Override
    protected boolean doShow() {
        final String iataCode = Console.readNonEmptyLine(
                "IATA Code (2 letters):",
                "IATA code cannot be empty"
        );

        final String icaoCode = Console.readNonEmptyLine(
                "ICAO Code (2-3 letters):",
                "ICAO code cannot be empty"
        );

        final String name = Console.readNonEmptyLine(
                "Company Name:",
                "Company name cannot be empty"
        );

        System.out.printf("%nSummary:%n");
        System.out.printf("  IATA Code: %s%n", iataCode.toUpperCase());
        System.out.printf("  ICAO Code: %s%n", icaoCode.toUpperCase());
        System.out.printf("  Name:      %s%n", name);

        final String confirm = Console.readLine("Confirm registration? (y/n):");
        if (!confirm.equalsIgnoreCase("y")) {
            System.out.println("Registration cancelled.");
            return false;
        }

        try {
            controller.registerAirTransportCompany(iataCode, icaoCode, name);
            System.out.println("Air transport company registered successfully.");
        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return false;
    }

    @Override
    public String headline() {
        return "Register Air Transport Company";
    }
}