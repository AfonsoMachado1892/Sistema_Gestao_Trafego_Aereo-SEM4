package atcc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Console client for the Air Transport Company Collaborator remote
 * access service (US78).
 *
 * Mirrors the structure of {@code PilotClient} (US86): connects, sends
 * the "US78" service tag, authenticates, then presents a menu covering
 * fleet management (US070-072), flight routes (US073-074) and pilot
 * management (US075-077).
 */
public class ATCCClient {
    private static final int PORT = 9999;
    private static final int PAGE_SIZE = 8;

    private static Socket socket;
    private static DataInputStream sIn;
    private static DataOutputStream sOut;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        System.out.print("Server IP: ");
        String serverIp = scanner.nextLine();

        try {
            socket = new Socket(serverIp, PORT);
            sIn = new DataInputStream(socket.getInputStream());
            sOut = new DataOutputStream(socket.getOutputStream());
            System.out.println("Connected to server.");

            // Identify this connection as an ATCC (US78) session
            sOut.writeUTF("US78");
            String serviceAck = sIn.readUTF();
            if (!serviceAck.startsWith("OK")) {
                System.out.println("Server rejected service tag. Exiting.");
                socket.close();
                return;
            }

            if (!authenticate()) {
                System.out.println("Authentication failed. Exiting.");
                socket.close();
                return;
            }

            boolean running = true;
            while (running) {
                printMenu();
                String option = scanner.nextLine().trim();

                switch (option) {
                    // Fleet (US070-072)
                    case "1":
                        addAircraft();
                        break;
                    case "2":
                        decommissionAircraft();
                        break;
                    case "3":
                        listCompanyFleet();
                        break;

                    // Flight routes (US073-074)
                    case "4":
                        createRegularRoute();
                        break;
                    case "5":
                        createCharterRoute();
                        break;
                    case "6":
                        listActiveRoutes();
                        break;
                    case "7":
                        listCompanyRoutes();
                        break;
                    case "8":
                        deactivateRoute();
                        break;

                    // Pilots (US075-077)
                    case "9":
                        addPilot();
                        break;
                    case "10":
                        listPilotRoster();
                        break;
                    case "11":
                        removePilot();
                        break;

                    case "0":
                        logout();
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            }

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        } finally {
            if (socket != null) socket.close();
        }
    }

    private static boolean authenticate() throws IOException {
        System.out.print("Username: ");
        String user = scanner.nextLine();
        System.out.print("Password: ");
        String pass = scanner.nextLine();

        sOut.writeUTF(user);
        sOut.writeUTF(pass);

        String response = sIn.readUTF();
        System.out.println("Server: " + response);
        return response.startsWith("OK");
    }

    private static void printMenu() {
        System.out.println("\n=== Air Transport Company Collaborator Menu ===");
        System.out.println("--- Fleet ---");
        System.out.println("1.  Add aircraft to fleet (US070)");
        System.out.println("2.  Decommission an aircraft (US071)");
        System.out.println("3.  List company fleet (US072)");
        System.out.println("--- Flight Routes ---");
        System.out.println("4.  Create a regular flight route (US073)");
        System.out.println("5.  Create a charter flight route (US073)");
        System.out.println("6.  List active routes");
        System.out.println("7.  List all company routes");
        System.out.println("8.  Deactivate a flight route (US074)");
        System.out.println("--- Pilots ---");
        System.out.println("9.  Add a pilot (US075)");
        System.out.println("10. List pilot roster (US076)");
        System.out.println("11. Remove a pilot (US077)");
        System.out.println("0.  Logout");
        System.out.print("Option: ");
    }

    // -----------------------------------------------------------
    // Generic delimited-response reading
    // -----------------------------------------------------------

    /**
     * Reads a delimited list response from the server:
     *   OK
     *   &lt;count&gt;
     *   &lt;row 1&gt;
     *   ...
     *   &lt;row count&gt;
     *
     * Returns the rows (each row still '|'-delimited), or {@code null} if
     * the server responded with an error (which is printed to the user).
     */
    private static List<String[]> readDelimitedList() throws IOException {
        String response = sIn.readUTF();

        if (!response.startsWith("OK")) {
            System.out.println("Server: " + response);
            return null;
        }

        String[] lines = response.split("\n", -1);
        int count = Integer.parseInt(lines[1].trim());

        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rows.add(lines[2 + i].split("\\|"));
        }
        return rows;
    }

    // -----------------------------------------------------------
    // Generic pagination (mirrors PilotClient)
    // -----------------------------------------------------------

    private interface PagePrinter {
        void print(List<String[]> rows, int page, int totalPages);
    }

    private interface DetailsShower {
        void show(String[] row, int selectedNumber);
    }

    private static void paginate(List<String[]> rows, PagePrinter pagePrinter, DetailsShower detailsShower) {
        int page = 0;
        int totalPages = (int) Math.ceil((double) rows.size() / PAGE_SIZE);

        while (true) {
            pagePrinter.print(rows, page, totalPages);

            System.out.println();
            System.out.println("Options:");
            System.out.println("  number - View details");
            if (page > 0) {
                System.out.println("  p      - Previous page");
            }
            if (page < totalPages - 1) {
                System.out.println("  n      - Next page");
            }
            System.out.println("  0      - Return");

            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("0")) {
                return;
            }

            if (input.equals("n") && page < totalPages - 1) {
                page++;
                continue;
            }

            if (input.equals("p") && page > 0) {
                page--;
                continue;
            }

            try {
                int selectedNumber = Integer.parseInt(input);
                int index = selectedNumber - 1;

                if (index >= 0 && index < rows.size()) {
                    detailsShower.show(rows.get(index), selectedNumber);
                } else {
                    System.out.printf("  Invalid option. Choose a number between 1 and %d.%n", rows.size());
                }
            } catch (NumberFormatException e) {
                System.out.println("  Invalid option.");
            }
        }
    }

    // =============================================================
    // US070 - Add aircraft to fleet
    // =============================================================

    private static void addAircraft() throws IOException {
        sOut.writeUTF("LIST_AIRCRAFT_MODELS");

        List<String[]> models = readDelimitedList();
        if (models == null) return;

        if (models.isEmpty()) {
            System.out.println("\nNo aircraft models available.");
            return;
        }

        System.out.println("\nAvailable Aircraft Models");
        System.out.println("--------------------------");
        System.out.printf("%-4s %-12s %-10s %-12s %-10s%n", "#", "Model ID", "Maker", "Type", "Capacity");
        System.out.println("-".repeat(55));
        for (int i = 0; i < models.size(); i++) {
            // modelID|type|makerName|maxPassengerCapacity
            String[] m = models.get(i);
            System.out.printf("%-4d %-12s %-10s %-12s %-10s%n", i + 1, m[0], m[2], m[1], m[3]);
        }

        System.out.print("\nChoose a model number (0 to cancel): ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid option.");
            return;
        }
        if (choice == 0) return;
        if (choice < 1 || choice > models.size()) {
            System.out.println("Invalid option.");
            return;
        }

        String modelID = models.get(choice - 1)[0];

        System.out.print("Aircraft registration: ");
        String registration = scanner.nextLine().trim();

        System.out.print("Economy seats: ");
        int economy = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Business seats: ");
        int business = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("First class seats: ");
        int first = Integer.parseInt(scanner.nextLine().trim());

        System.out.print("Country ISO code: ");
        String countryIso = scanner.nextLine().trim();
        System.out.print("Country name: ");
        String countryName = scanner.nextLine().trim();

        System.out.print("Number of crew elements: ");
        int crew = Integer.parseInt(scanner.nextLine().trim());

        sOut.writeUTF("ADD_AIRCRAFT");
        sOut.writeUTF(registration);
        sOut.writeUTF(modelID);
        sOut.writeInt(economy);
        sOut.writeInt(business);
        sOut.writeInt(first);
        sOut.writeUTF(countryIso);
        sOut.writeUTF(countryName);
        sOut.writeInt(crew);

        System.out.println("Server: " + sIn.readUTF());
    }

    // =============================================================
    // US071 - Decommission an aircraft
    // =============================================================

    private static void decommissionAircraft() throws IOException {
        System.out.print("Aircraft registration to decommission: ");
        String registration = scanner.nextLine().trim();

        sOut.writeUTF("DECOMMISSION_AIRCRAFT");
        sOut.writeUTF(registration);

        System.out.println("Server: " + sIn.readUTF());
    }

    // =============================================================
    // US072 - List company fleet
    // =============================================================

    private static void listCompanyFleet() throws IOException {
        sOut.writeUTF("LIST_COMPANY_FLEET");

        List<String[]> rows = readDelimitedList();
        if (rows == null) return;

        if (rows.isEmpty()) {
            System.out.println("\nNo aircraft in fleet.");
            return;
        }

        System.out.printf("%nTotal: %d aircraft%n", rows.size());

        paginate(rows, ATCCClient::printFleetPage, ATCCClient::showFleetDetails);
    }

    private static void printFleetPage(List<String[]> rows, int page, int totalPages) {
        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, rows.size());

        System.out.printf("%nPage %d of %d%n", page + 1, totalPages);
        System.out.println("Company Fleet");
        System.out.println("-------------");
        System.out.printf("%-4s %-12s %-10s %-8s %-8s %-12s %-15s%n",
                "#", "Registration", "Model", "Maker", "Seats", "Status", "Country");
        System.out.println("-".repeat(70));

        for (int i = from; i < to; i++) {
            // registration|modelID|makerName|totalSeats|operationalStatus|country
            String[] a = rows.get(i);
            System.out.printf("%-4d %-12s %-10s %-8s %-8s %-12s %-15s%n",
                    i + 1, a[0], a[1], a[2], a[3], a[4], a[5]);
        }
    }

    private static void showFleetDetails(String[] a, int selectedNumber) {
        System.out.println();
        System.out.println("Aircraft Details");
        System.out.println("=================");
        System.out.println("Number       : " + selectedNumber);
        System.out.println("Registration : " + a[0]);
        System.out.println("Model        : " + a[1]);
        System.out.println("Maker        : " + a[2]);
        System.out.println("Total seats  : " + a[3]);
        System.out.println("Status       : " + a[4]);
        System.out.println("Country      : " + a[5]);
        System.out.println();
        System.out.print("Press ENTER to continue...");
        scanner.nextLine();
    }

    // =============================================================
    // US073 - Create a flight route
    // =============================================================

    private static void createRegularRoute() throws IOException {
        Object[] basics = readRouteBasics();
        if (basics == null) return;
        String routeCode = (String) basics[0];
        String departureIata = (String) basics[1];
        String arrivalIata = (String) basics[2];

        System.out.println("\nDefine the weekly schedule (regular route).");
        List<String[]> entries = new ArrayList<>();
        boolean addMore = true;
        while (addMore) {
            System.out.print("Day of week (e.g. MONDAY): ");
            String day = scanner.nextLine().trim();
            try {
                DayOfWeek.valueOf(day.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid day of week. Try again.");
                continue;
            }

            System.out.print("Departure time (HH:mm): ");
            String time = scanner.nextLine().trim();
            try {
                LocalTime.parse(time);
            } catch (Exception e) {
                System.out.println("Invalid time format. Try again.");
                continue;
            }

            entries.add(new String[]{day.toUpperCase(), time});

            System.out.print("Add another schedule entry? (y/n): ");
            addMore = scanner.nextLine().trim().equalsIgnoreCase("y");
        }

        if (entries.isEmpty()) {
            System.out.println("At least one schedule entry is required. Cancelled.");
            return;
        }

        sOut.writeUTF("CREATE_REGULAR_ROUTE");
        sOut.writeUTF(routeCode);
        sOut.writeUTF(departureIata);
        sOut.writeUTF(arrivalIata);
        sOut.writeInt(entries.size());
        for (String[] entry : entries) {
            sOut.writeUTF(entry[0]);
            sOut.writeUTF(entry[1]);
        }

        System.out.println("Server: " + sIn.readUTF());
    }

    private static void createCharterRoute() throws IOException {
        Object[] basics = readRouteBasics();
        if (basics == null) return;
        String routeCode = (String) basics[0];
        String departureIata = (String) basics[1];
        String arrivalIata = (String) basics[2];

        System.out.print("Departure date/time (yyyy-MM-ddTHH:mm): ");
        String departureDateTime = scanner.nextLine().trim();
        System.out.print("Arrival date/time (yyyy-MM-ddTHH:mm): ");
        String arrivalDateTime = scanner.nextLine().trim();

        try {
            LocalDateTime.parse(departureDateTime);
            LocalDateTime.parse(arrivalDateTime);
        } catch (Exception e) {
            System.out.println("Invalid date/time format. Cancelled.");
            return;
        }

        sOut.writeUTF("CREATE_CHARTER_ROUTE");
        sOut.writeUTF(routeCode);
        sOut.writeUTF(departureIata);
        sOut.writeUTF(arrivalIata);
        sOut.writeUTF(departureDateTime);
        sOut.writeUTF(arrivalDateTime);

        System.out.println("Server: " + sIn.readUTF());
    }

    /**
     * Lists airports and prompts for route code, departure and arrival
     * IATA codes. Returns {@code [routeCode, departureIata, arrivalIata]}
     * or {@code null} if cancelled.
     */
    private static Object[] readRouteBasics() throws IOException {
        sOut.writeUTF("LIST_AIRPORTS");

        List<String[]> airports = readDelimitedList();
        if (airports == null) return null;

        if (airports.isEmpty()) {
            System.out.println("\nNo airports available.");
            return null;
        }

        System.out.println("\nAvailable Airports");
        System.out.println("-------------------");
        System.out.printf("%-4s %-6s %-6s %-25s %-15s%n", "#", "IATA", "ICAO", "Name", "City");
        System.out.println("-".repeat(60));
        for (int i = 0; i < airports.size(); i++) {
            // iataCode|icaoCode|name|city
            String[] a = airports.get(i);
            System.out.printf("%-4d %-6s %-6s %-25s %-15s%n", i + 1, a[0], a[1], a[2], a[3]);
        }

        System.out.print("\nRoute code (e.g. TP123): ");
        String routeCode = scanner.nextLine().trim();

        System.out.print("Departure airport IATA code: ");
        String departureIata = scanner.nextLine().trim().toUpperCase();

        System.out.print("Arrival airport IATA code: ");
        String arrivalIata = scanner.nextLine().trim().toUpperCase();

        return new Object[]{routeCode, departureIata, arrivalIata};
    }

    // =============================================================
    // US074 - Deactivate a flight route
    // =============================================================

    private static void listActiveRoutes() throws IOException {
        sOut.writeUTF("LIST_ACTIVE_ROUTES");
        showRouteList("No active routes found.");
    }

    private static void listCompanyRoutes() throws IOException {
        sOut.writeUTF("LIST_COMPANY_ROUTES");
        showRouteList("No routes found.");
    }

    private static void showRouteList(String emptyMessage) throws IOException {
        List<String[]> rows = readDelimitedList();
        if (rows == null) return;

        if (rows.isEmpty()) {
            System.out.println("\n" + emptyMessage);
            return;
        }

        System.out.printf("%nTotal: %d route(s)%n", rows.size());

        paginate(rows, ATCCClient::printRoutesPage, ATCCClient::showRouteDetails);
    }

    private static void printRoutesPage(List<String[]> rows, int page, int totalPages) {
        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, rows.size());

        System.out.printf("%nPage %d of %d%n", page + 1, totalPages);
        System.out.println("Flight Routes");
        System.out.println("-------------");
        System.out.printf("%-4s %-8s %-6s %-6s %-8s %-10s %-18s%n",
                "#", "Route", "From", "To", "Type", "Status", "Deactivation Date");
        System.out.println("-".repeat(75));

        for (int i = from; i < to; i++) {
            // routeCode|departureIata|arrivalIata|type|status|deactivationDate|schedule
            String[] r = rows.get(i);
            System.out.printf("%-4d %-8s %-6s %-6s %-8s %-10s %-18s%n",
                    i + 1, r[0], r[1], r[2], r[3], r[4], r[5]);
        }
    }

    private static void showRouteDetails(String[] r, int selectedNumber) {
        System.out.println();
        System.out.println("Flight Route Details");
        System.out.println("====================");
        System.out.println("Number            : " + selectedNumber);
        System.out.println("Route code        : " + r[0]);
        System.out.println("Departure airport : " + r[1]);
        System.out.println("Arrival airport   : " + r[2]);
        System.out.println("Type              : " + r[3]);
        System.out.println("Status            : " + r[4]);
        System.out.println("Deactivation date : " + (r[5].isBlank() ? "N/A" : r[5]));
        if (r.length > 6 && !r[6].isBlank()) {
            System.out.println("Schedule          : " + r[6]);
        }
        System.out.println();
        System.out.print("Press ENTER to continue...");
        scanner.nextLine();
    }

    private static void deactivateRoute() throws IOException {
        System.out.print("Route code to deactivate: ");
        String routeCode = scanner.nextLine().trim();

        System.out.print("Deactivation date (yyyy-MM-dd): ");
        String date = scanner.nextLine().trim();

        try {
            LocalDate.parse(date);
        } catch (Exception e) {
            System.out.println("Invalid date format. Cancelled.");
            return;
        }

        sOut.writeUTF("DEACTIVATE_ROUTE");
        sOut.writeUTF(routeCode);
        sOut.writeUTF(date);

        System.out.println("Server: " + sIn.readUTF());
    }

    // =============================================================
    // US075 - Add a pilot
    // =============================================================

    private static void addPilot() throws IOException {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("First name: ");
        String firstName = scanner.nextLine().trim();
        System.out.print("Last name: ");
        String lastName = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Phone country code (e.g. 351): ");
        int countryCode = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Phone number: ");
        String phoneNumber = scanner.nextLine().trim();

        // List available aircraft models for certification
        sOut.writeUTF("LIST_PILOT_AIRCRAFT_MODELS");
        List<String[]> models = readDelimitedList();
        if (models == null) return;

        List<String> modelIDs = new ArrayList<>();
        List<Integer> hoursList = new ArrayList<>();
        List<LocalDate> issuedDates = new ArrayList<>();
        List<LocalDate> validDates = new ArrayList<>();

        if (!models.isEmpty()) {
            System.out.println("\nAvailable Aircraft Models");
            System.out.println("--------------------------");
            System.out.printf("%-4s %-12s %-15s %-10s %-10s%n", "#", "Model ID", "Name", "Maker", "Type");
            System.out.println("-".repeat(60));
            for (int i = 0; i < models.size(); i++) {
                // modelID|name|makerName|type
                String[] m = models.get(i);
                System.out.printf("%-4d %-12s %-15s %-10s %-10s%n", i + 1, m[0], m[1], m[2], m[3]);
            }

            boolean addMore = true;
            while (addMore) {
                System.out.print("\nAdd a certification? (y/n): ");
                if (!scanner.nextLine().trim().equalsIgnoreCase("y")) break;

                System.out.print("Choose a model number: ");
                int choice;
                try {
                    choice = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid option.");
                    continue;
                }
                if (choice < 1 || choice > models.size()) {
                    System.out.println("Invalid option.");
                    continue;
                }

                System.out.print("Flight hours on this model: ");
                int hours = Integer.parseInt(scanner.nextLine().trim());
                System.out.print("Certification issued date (yyyy-MM-dd): ");
                LocalDate issued = LocalDate.parse(scanner.nextLine().trim());
                System.out.print("Certification valid until (yyyy-MM-dd): ");
                LocalDate valid = LocalDate.parse(scanner.nextLine().trim());

                modelIDs.add(models.get(choice - 1)[0]);
                hoursList.add(hours);
                issuedDates.add(issued);
                validDates.add(valid);

                System.out.print("Add another certification? (y/n): ");
                addMore = scanner.nextLine().trim().equalsIgnoreCase("y");
            }
        }

        sOut.writeUTF("ADD_PILOT");
        sOut.writeUTF(username);
        sOut.writeUTF(password);
        sOut.writeUTF(firstName);
        sOut.writeUTF(lastName);
        sOut.writeUTF(email);
        sOut.writeInt(countryCode);
        sOut.writeUTF(phoneNumber);

        sOut.writeInt(modelIDs.size());
        for (int i = 0; i < modelIDs.size(); i++) {
            sOut.writeUTF(modelIDs.get(i));
            sOut.writeInt(hoursList.get(i));
            sOut.writeUTF(issuedDates.get(i).toString());
            sOut.writeUTF(validDates.get(i).toString());
        }

        System.out.println("Server: " + sIn.readUTF());
    }

    // =============================================================
    // US076 - List pilot roster
    // =============================================================

    private static void listPilotRoster() throws IOException {
        sOut.writeUTF("LIST_PILOT_ROSTER");
        showPilotList("No pilots found in roster.");
    }

    private static void showPilotList(String emptyMessage) throws IOException {
        List<String[]> rows = readDelimitedList();
        if (rows == null) return;

        if (rows.isEmpty()) {
            System.out.println("\n" + emptyMessage);
            return;
        }

        System.out.printf("%nTotal: %d pilot(s)%n", rows.size());

        paginate(rows, ATCCClient::printPilotsPage, ATCCClient::showPilotDetails);
    }

    private static void printPilotsPage(List<String[]> rows, int page, int totalPages) {
        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, rows.size());

        System.out.printf("%nPage %d of %d%n", page + 1, totalPages);
        System.out.println("Pilot Roster");
        System.out.println("------------");
        System.out.printf("%-4s %-12s %-20s %-8s %-10s %-12s%n",
                "#", "Pilot ID", "Name", "Company", "Status", "Certifications");
        System.out.println("-".repeat(70));

        for (int i = from; i < to; i++) {
            // pilotID|name|companyIata|status|certificationCount
            String[] p = rows.get(i);
            System.out.printf("%-4d %-12s %-20s %-8s %-10s %-12s%n",
                    i + 1, shortId(p[0]), p[1], p[2], p[3], p[4]);
        }
    }

    private static void showPilotDetails(String[] p, int selectedNumber) {
        System.out.println();
        System.out.println("Pilot Details");
        System.out.println("==============");
        System.out.println("Number          : " + selectedNumber);
        System.out.println("Pilot ID        : " + p[0]);
        System.out.println("Name            : " + p[1]);
        System.out.println("Company         : " + p[2]);
        System.out.println("Status          : " + p[3]);
        System.out.println("Certifications  : " + p[4]);
        System.out.println();
        System.out.print("Press ENTER to continue...");
        scanner.nextLine();
    }

    // =============================================================
    // US077 - Remove a pilot
    // =============================================================

    private static void removePilot() throws IOException {
        sOut.writeUTF("LIST_ACTIVE_PILOTS");

        List<String[]> pilots = readDelimitedList();
        if (pilots == null) return;

        if (pilots.isEmpty()) {
            System.out.println("\nNo active pilots found.");
            return;
        }

        System.out.println("\nActive Pilots");
        System.out.println("-------------");
        System.out.printf("%-4s %-12s %-20s %-10s%n", "#", "Pilot ID", "Name", "Status");
        System.out.println("-".repeat(50));
        for (int i = 0; i < pilots.size(); i++) {
            // pilotID|name|companyIata|status|certificationCount
            String[] p = pilots.get(i);
            System.out.printf("%-4d %-12s %-20s %-10s%n", i + 1, shortId(p[0]), p[1], p[3]);
        }

        System.out.print("\nChoose a pilot number to remove (0 to cancel): ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid option.");
            return;
        }
        if (choice == 0) return;
        if (choice < 1 || choice > pilots.size()) {
            System.out.println("Invalid option.");
            return;
        }

        String pilotID = pilots.get(choice - 1)[0];

        sOut.writeUTF("REMOVE_PILOT");
        sOut.writeUTF(pilotID);

        System.out.println("Server: " + sIn.readUTF());
    }

    // -----------------------------------------------------------

    private static String shortId(String id) {
        if (id == null || id.length() <= 8) {
            return id;
        }
        return id.substring(0, 8) + "...";
    }

    private static void logout() throws IOException {
        sOut.writeUTF("LOGOUT");
        System.out.println("Server: " + sIn.readUTF());
        System.out.println("Goodbye!");
    }
}