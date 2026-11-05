package aisafe.app.backoffice.console.presentation.customercollaborator;

import aisafe.aircontrolarea.domain.AirControlArea;
import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.domain.CustomerType;
import aisafe.costumerCollaborator.service.ListCustomerCollaboratorsController;
import aisafe.persistence.PersistenceContext;
import eapli.framework.io.util.Console;
import eapli.framework.presentation.console.AbstractUI;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("squid:S106")
public class ListCustomerCollaboratorsUI extends AbstractUI {

    private final ListCustomerCollaboratorsController controller =
            new ListCustomerCollaboratorsController(
                    PersistenceContext.repositories().customerCollaborators(),
                    PersistenceContext.repositories().airTransportCompanies(),
                    PersistenceContext.repositories().airControlAreas()
            );

    @Override
    protected boolean doShow() {
        try {
            final CustomerType customerType = requestCustomerType();
            final String customerCode = requestCustomerCode(customerType);

            final List<CustomerCollaborator> collaborators = new ArrayList<>();
            controller.listCustomerCollaborators(customerType, customerCode)
                    .forEach(collaborators::add);

            System.out.printf("%nActive collaborators for %s - %s:%n", customerType, customerCode);

            if (collaborators.isEmpty()) {
                System.out.println("There are no active collaborators for the selected customer.");
                return false;
            }

            for (final CustomerCollaborator collaborator : collaborators) {
                System.out.println(collaborator);
            }

            return false;

        } catch (final IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        } catch (final Exception e) {
            System.out.println("Error: Operation could not be completed.");
            return false;
        }
    }

    private CustomerType requestCustomerType() {
        System.out.println("Customer type:");
        System.out.println("1 - Air Transport Company");
        System.out.println("2 - Air Control Area");

        final int option = Console.readInteger("Option:");

        if (option == 1) {
            return CustomerType.AIR_TRANSPORT_COMPANY;
        }

        if (option == 2) {
            return CustomerType.AIR_CONTROL_AREA;
        }

        throw new IllegalArgumentException("Invalid customer type option.");
    }

    private String requestCustomerCode(final CustomerType customerType) {
        if (customerType == CustomerType.AIR_TRANSPORT_COMPANY) {
            return requestAirTransportCompanyCode();
        }

        if (customerType == CustomerType.AIR_CONTROL_AREA) {
            return requestAirControlAreaCode();
        }

        throw new IllegalArgumentException("Unsupported customer type.");
    }

    private String requestAirTransportCompanyCode() {
        final List<AirTransportCompany> companies = new ArrayList<>();
        controller.listAirTransportCompanies().forEach(companies::add);

        if (companies.isEmpty()) {
            throw new IllegalStateException("There are no air transport companies registered.");
        }

        System.out.println("\nAvailable Air Transport Companies:");
        for (int i = 0; i < companies.size(); i++) {
            System.out.printf("%d - %s%n", i + 1, companies.get(i));
        }

        final int option = Console.readInteger("Select air transport company:");

        if (option < 1 || option > companies.size()) {
            throw new IllegalArgumentException("Invalid air transport company option.");
        }

        return companies.get(option - 1).identity().toString();
    }

    private String requestAirControlAreaCode() {
        final List<AirControlArea> areas = new ArrayList<>();
        controller.listAirControlAreas().forEach(areas::add);

        if (areas.isEmpty()) {
            throw new IllegalStateException("There are no air control areas registered.");
        }

        System.out.println("\nAvailable Air Control Areas:");
        for (int i = 0; i < areas.size(); i++) {
            System.out.printf("%d - %s%n", i + 1, areas.get(i));
        }

        final int option = Console.readInteger("Select air control area:");

        if (option < 1 || option > areas.size()) {
            throw new IllegalArgumentException("Invalid air control area option.");
        }

        return areas.get(option - 1).identity().toString();
    }

    @Override
    public String headline() {
        return "List Customer Collaborators";
    }
}