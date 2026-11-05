package pilot;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class PilotClient {
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

            // Identify this connection as a Pilot (US86) session
            sOut.writeUTF("US86");
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
                    case "1":
                        listRoutes();
                        break;
                    case "2":
                        listAircraft();
                        break;
                    case "3":
                        createFlightPlan();
                        break;
                    case "4":
                        listMyFlightPlans();
                        break;
                    case "5":
                        createFlightPlanFromFile();
                        break;
                    case "6":
                        suggestWeatherSearch();
                        break;
                    case "7":
                        getWeatherData();
                        break;
                    case "8":
                        insertWeatherData();
                        break;
                    case "9":
                        listDraftFlightPlans();
                        break;
                    case "10":
                        testFlightPlan();
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
        System.out.println("\n=== Pilot Menu ===");
        System.out.println("1.  List available routes");
        System.out.println("2.  List available aircraft");
        System.out.println("3.  Create a flight plan (US080)");
        System.out.println("4.  List my flight plans");
        System.out.println("5.  Create a flight plan from a file (US081)");
        System.out.println("6.  Suggest weather search for a flight plan");
        System.out.println("7.  Get weather data by area and date");
        System.out.println("8.  Insert weather data into a flight plan (US082)");
        System.out.println("9.  List my draft flight plans");
        System.out.println("10. Test/validate a flight plan (US085)");
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
        // lines[0] = "OK", lines[1] = count, lines[2..] = rows (last may be empty)
        int count = Integer.parseInt(lines[1].trim());

        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rows.add(lines[2 + i].split("\\|"));
        }
        return rows;
    }

    // -----------------------------------------------------------
    // US080 - Create a flight plan
    // -----------------------------------------------------------

    private static void listRoutes() throws IOException {
        sOut.writeUTF("LIST_ROUTES");

        List<String[]> rows = readDelimitedList();
        if (rows == null) return;

        if (rows.isEmpty()) {
            System.out.println();
            System.out.println("No flight routes found.");
            return;
        }

        System.out.printf("%nTotal: %d flight route(s)%n", rows.size());

        paginate(rows, PilotClient::printRoutesPage, PilotClient::showRouteDetails);
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
        // routeCode|departureIata|arrivalIata|type|status|deactivationDate|schedule
        System.out.println();
        System.out.println("Flight Route Details");
        System.out.println("====================");
        System.out.println("Number            : " + selectedNumber);
        System.out.println("Code              : " + r[0]);
        System.out.printf("Route             : %s -> %s%n", r[1], r[2]);
        System.out.println("Type              : " + r[3]);
        System.out.println("Status            : " + r[4]);
        System.out.println("Deactivation date : " + r[5]);
        System.out.println("Schedule          : " + (r.length > 6 ? r[6].replace("/", "|") : ""));
        System.out.println();
        System.out.print("Press ENTER to continue...");
        scanner.nextLine();
    }

    private static void listAircraft() throws IOException {
        sOut.writeUTF("LIST_AIRCRAFT");

        List<String[]> rows = readDelimitedList();
        if (rows == null) return;

        if (rows.isEmpty()) {
            System.out.println();
            System.out.println("No aircraft available.");
            return;
        }

        System.out.printf("%nTotal: %d aircraft%n", rows.size());

        paginate(rows, PilotClient::printAircraftPage, PilotClient::showAircraftDetails);
    }

    private static void printAircraftPage(List<String[]> rows, int page, int totalPages) {
        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, rows.size());

        System.out.printf("%nPage %d of %d%n", page + 1, totalPages);
        System.out.println("Available Aircraft");
        System.out.println("-------------------");
        System.out.printf("%-4s %-12s %-12s %-10s %-6s %-12s%n",
                "#", "Registration", "Model", "Company", "Crew", "Status");
        System.out.println("-".repeat(60));

        for (int i = from; i < to; i++) {
            // registration|model|company|crew|status
            String[] a = rows.get(i);
            System.out.printf("%-4d %-12s %-12s %-10s %-6s %-12s%n",
                    i + 1, a[0], a[1], a[2], a[3], a[4]);
        }
    }

    private static void showAircraftDetails(String[] a, int selectedNumber) {
        // registration|model|company|crew|status
        System.out.println();
        System.out.println("Aircraft Details");
        System.out.println("================");
        System.out.println("Number       : " + selectedNumber);
        System.out.println("Registration : " + a[0]);
        System.out.println("Model        : " + a[1]);
        System.out.println("Company      : " + a[2]);
        System.out.println("Crew         : " + a[3]);
        System.out.println("Status       : " + a[4]);
        System.out.println();
        System.out.print("Press ENTER to continue...");
        scanner.nextLine();
    }

    private static final int SUMMARY_BOX_WIDTH = 60;

    private static void createFlightPlan() throws IOException {
        System.out.println();
        System.out.println("Create Flight Plan");
        System.out.println("==================");
        System.out.println("This operation creates a new flight plan in DRAFT status.");
        System.out.println("You can type 0 at any step to cancel.");

        String[] route = chooseRouteForCreation();
        if (route == null) return;

        String[] aircraft = chooseAircraftForCreation();
        if (aircraft == null) return;

        LocalDateTime departure = readDateTimeOrCancel("Planned departure");
        if (departure == null) return;

        LocalDateTime arrival = readArrivalAfter(departure);
        if (arrival == null) return;

        Double fuelAmount = readFuelAmount();
        if (fuelAmount == null) return;

        System.out.print("Fuel unit (e.g. KG): ");
        String fuelUnit = scanner.nextLine().trim().toUpperCase();
        if (fuelUnit.equals("0")) {
            System.out.println("Flight plan creation cancelled.");
            return;
        }

        System.out.print("Description: ");
        String description = scanner.nextLine();

        showCreationSummary(route, aircraft, departure, arrival, fuelAmount, fuelUnit, description);

        System.out.print("Confirm creation? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("y")) {
            System.out.println("Flight plan creation cancelled.");
            return;
        }

        // routeCode is route[0]; aircraft registration is aircraft[0]
        sOut.writeUTF("CREATE_FLIGHT_PLAN");
        sOut.writeUTF(route[0]);
        sOut.writeUTF(aircraft[0]);
        sOut.writeUTF(departure.toString());
        sOut.writeUTF(arrival.toString());
        sOut.writeDouble(fuelAmount);
        sOut.writeUTF(fuelUnit);
        sOut.writeUTF(description);

        System.out.println("Server: " + sIn.readUTF());
    }

    /**
     * Fetches the available routes and lets the user pick one by number.
     * Returns the chosen row (routeCode|departureIata|arrivalIata|type|status|deactivationDate|schedule),
     * or {@code null} if the user cancelled or no routes are available.
     */
    private static String[] chooseRouteForCreation() throws IOException {
        sOut.writeUTF("LIST_ROUTES");
        List<String[]> rows = readDelimitedList();
        if (rows == null) return null;

        if (rows.isEmpty()) {
            System.out.println();
            System.out.println("No flight routes available.");
            return null;
        }

        System.out.println();
        System.out.println("Available Flight Routes");
        System.out.println("------------------------------------------------------------------------------");
        System.out.printf("%-4s %-8s %-6s %-6s %-8s %s%n",
                "#", "Route", "From", "To", "Type", "Schedule");
        System.out.println("------------------------------------------------------------------------------");

        for (int i = 0; i < rows.size(); i++) {
            String[] r = rows.get(i);
            System.out.printf("%-4d %-8s %-6s %-6s %-8s %s%n",
                    i + 1, r[0], r[1], r[2], r[3], r.length > 6 ? r[6].replace("/", " ") : "");
        }

        System.out.println("------------------------------------------------------------------------------");
        System.out.println("  0. Cancel");

        while (true) {
            System.out.print("Choose route: ");
            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                System.out.println("Flight plan creation cancelled.");
                return null;
            }

            try {
                int option = Integer.parseInt(input);
                if (option >= 1 && option <= rows.size()) {
                    String[] selected = rows.get(option - 1);
                    System.out.println();
                    System.out.println("Selected route: " + selected[0]
                            + " (" + selected[1] + " -> " + selected[2] + ")");
                    return selected;
                }
                System.out.printf("  Invalid option. Choose a number between 1 and %d.%n", rows.size());
            } catch (NumberFormatException e) {
                System.out.println("  Invalid option. Please insert a valid number.");
            }
        }
    }

    /**
     * Fetches the available aircraft and lets the user pick one by number.
     * Returns the chosen row (registration|model|company|crew|status),
     * or {@code null} if the user cancelled or no aircraft are available.
     */
    private static String[] chooseAircraftForCreation() throws IOException {
        sOut.writeUTF("LIST_AIRCRAFT");
        List<String[]> rows = readDelimitedList();
        if (rows == null) return null;

        if (rows.isEmpty()) {
            System.out.println();
            System.out.println("No aircraft available.");
            return null;
        }

        System.out.println();
        System.out.println("Available Aircraft");
        System.out.println("------------------------------------------------------------");
        System.out.printf("%-4s %-12s %-12s %-10s %-6s %-12s%n",
                "#", "Registration", "Model", "Company", "Crew", "Status");
        System.out.println("------------------------------------------------------------");

        for (int i = 0; i < rows.size(); i++) {
            String[] a = rows.get(i);
            System.out.printf("%-4d %-12s %-12s %-10s %-6s %-12s%n",
                    i + 1, a[0], a[1], a[2], a[3], a[4]);
        }

        System.out.println("------------------------------------------------------------");
        System.out.println("  0. Cancel");

        while (true) {
            System.out.print("Choose aircraft: ");
            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                System.out.println("Flight plan creation cancelled.");
                return null;
            }

            try {
                int option = Integer.parseInt(input);
                if (option >= 1 && option <= rows.size()) {
                    String[] selected = rows.get(option - 1);
                    System.out.println();
                    System.out.println("Selected aircraft: " + selected[0] + " (" + selected[1] + ")");
                    return selected;
                }
                System.out.printf("  Invalid option. Choose a number between 1 and %d.%n", rows.size());
            } catch (NumberFormatException e) {
                System.out.println("  Invalid option. Please insert a valid number.");
            }
        }
    }

    /**
     * Reads a date and time (yyyy-MM-dd then HH:mm), or returns null if
     * the user types 0 to cancel at either step.
     */
    private static LocalDateTime readDateTimeOrCancel(String label) {
        while (true) {
            System.out.printf("%s date (yyyy-MM-dd, 0 to cancel): ", label);
            String dateInput = scanner.nextLine().trim();
            if (dateInput.equals("0")) {
                System.out.println("Flight plan creation cancelled.");
                return null;
            }

            LocalDate date;
            try {
                date = LocalDate.parse(dateInput);
            } catch (DateTimeParseException e) {
                System.out.println("  Invalid date format. Use yyyy-MM-dd.");
                continue;
            }

            System.out.printf("%s time (HH:mm, 0 to cancel): ", label);
            String timeInput = scanner.nextLine().trim();
            if (timeInput.equals("0")) {
                System.out.println("Flight plan creation cancelled.");
                return null;
            }

            LocalTime time;
            try {
                time = LocalTime.parse(timeInput);
            } catch (DateTimeParseException e) {
                System.out.println("  Invalid time format. Use HH:mm.");
                continue;
            }

            return LocalDateTime.of(date, time);
        }
    }

    /**
     * Reads an arrival date/time and ensures it's after the given departure.
     * Returns null if the user cancels.
     */
    private static LocalDateTime readArrivalAfter(LocalDateTime departure) {
        while (true) {
            LocalDateTime arrival = readDateTimeOrCancel("Planned arrival");
            if (arrival == null) return null;

            if (!arrival.isAfter(departure)) {
                System.out.println("  Arrival must be after departure. Please try again.");
                continue;
            }

            return arrival;
        }
    }

    /**
     * Reads a non-negative fuel amount, or returns null if the user cancels (0).
     */
    private static Double readFuelAmount() {
        while (true) {
            System.out.print("Fuel amount (0 to cancel): ");
            String input = scanner.nextLine().trim();

            try {
                double value = Double.parseDouble(input);
                if (value == 0) {
                    System.out.println("Flight plan creation cancelled.");
                    return null;
                }
                if (value < 0) {
                    System.out.println("  Fuel amount must be positive.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("  Invalid number.");
            }
        }
    }

    private static void showCreationSummary(String[] route, String[] aircraft,
                                            LocalDateTime departure, LocalDateTime arrival,
                                            double fuelAmount, String fuelUnit, String description) {
        System.out.println();
        printBoxHeader("Flight Plan Summary");
        printBoxRow("Route", route[0] + " (" + route[1] + " -> " + route[2] + ")");
        printBoxRow("Aircraft", aircraft[0] + " (" + aircraft[1] + ")");
        printBoxRow("Departure", departure.toString());
        printBoxRow("Arrival", arrival.toString());
        printBoxRow("Fuel", fuelAmount + " " + fuelUnit);
        printBoxRow("Description", description.isBlank() ? "-" : description);
        printBoxFooter();
    }

    private static void printBoxHeader(String title) {
        String normalizedTitle = " " + title + " ";
        int left = Math.max(0, (SUMMARY_BOX_WIDTH - 2 - normalizedTitle.length()) / 2);
        int right = SUMMARY_BOX_WIDTH - 2 - normalizedTitle.length() - left;
        System.out.println("+" + "-".repeat(left) + normalizedTitle + "-".repeat(right) + "+");
    }

    private static void printBoxFooter() {
        System.out.println("+" + "-".repeat(SUMMARY_BOX_WIDTH - 2) + "+");
    }

    private static void printBoxRow(String label, String value) {
        int contentWidth = SUMMARY_BOX_WIDTH - 4;
        String content = String.format("%-12s: %s", label, value);
        if (content.length() > contentWidth) {
            content = content.substring(0, contentWidth - 3) + "...";
        }
        System.out.printf("| %-" + contentWidth + "s |%n", content);
    }

    private static void listMyFlightPlans() throws IOException {
        sOut.writeUTF("LIST_MY_FLIGHT_PLANS");
        showFlightPlanList("No created flight plans found.");
    }

    // -----------------------------------------------------------
    // US081 - Create a flight plan from a file (Core Flight DSL)
    // -----------------------------------------------------------

    private static void createFlightPlanFromFile() throws IOException {
        String path = chooseFlightPlanFile();
        if (path == null) return;

        String content;
        try {
            content = new String(Files.readAllBytes(Path.of(path)));
        } catch (IOException e) {
            System.out.println("Could not read local file: " + e.getMessage());
            return;
        }

        sOut.writeUTF("CREATE_FLIGHT_PLAN_FROM_FILE");
        sOut.writeUTF(content);

        String response = sIn.readUTF();

        if (!response.startsWith("OK")) {
            System.out.println("Server: " + response);
            return;
        }

        String[] parts = response.split("\\|");
        // OK|id|route|aircraft|status|departure|arrival|fuel|segmentCount
        System.out.println();
        System.out.println("Flight Plan Imported Successfully");
        System.out.println("==================================");
        System.out.println("Plan ID     : " + parts[1]);
        System.out.println("Route       : " + parts[2]);
        System.out.println("Aircraft    : " + parts[3]);
        System.out.println("Status      : " + parts[4]);
        System.out.println("Departure   : " + parts[5]);
        System.out.println("Arrival     : " + parts[6]);
        System.out.println("Fuel        : " + parts[7]);
        System.out.println("Segments    : " + parts[8]);
    }

    /**
     * Lets the user pick a .flight file from the local lprog/example
     * directory, or insert a custom path. Mirrors
     * ImportFlightPlanFromFileUI's file chooser.
     *
     * @return the chosen file path, or {@code null} if cancelled.
     */
    private static String chooseFlightPlanFile() throws IOException {
        List<Path> files = findExampleFlightFiles();

        if (files.isEmpty()) {
            System.out.println("No .flight files were found in: "
                    + examplesDirectory().toAbsolutePath());

            System.out.print("Insert flight plan file path (.flight) or 0 to return: ");
            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return null;
            }
            return input;
        }

        System.out.println();
        System.out.println("Available example flight plan files:");
        System.out.println();

        for (int i = 0; i < files.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, files.get(i).getFileName());
        }

        System.out.println("C. Insert custom path");
        System.out.println("0. Return");
        System.out.println();

        while (true) {
            System.out.print("Please choose an option: ");
            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return null;
            }

            if (input.equalsIgnoreCase("c")) {
                System.out.print("Insert flight plan file path (.flight): ");
                String customPath = scanner.nextLine().trim();

                if (customPath.equals("0")) {
                    return null;
                }
                return customPath;
            }

            try {
                int option = Integer.parseInt(input);

                if (option >= 1 && option <= files.size()) {
                    return files.get(option - 1).toString();
                }

                System.out.printf("Invalid option. Choose a number between 1 and %d, or C.%n", files.size());
            } catch (NumberFormatException e) {
                System.out.println("Invalid option. Please choose a number, or C.");
            }
        }
    }

    private static List<Path> findExampleFlightFiles() throws IOException {
        Path examplesDirectory = examplesDirectory();

        if (!Files.exists(examplesDirectory) || !Files.isDirectory(examplesDirectory)) {
            return List.of();
        }

        try (var stream = Files.list(examplesDirectory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".flight"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();
        }
    }

    private static Path examplesDirectory() {
        Path fromRepositoryRoot = Path.of("lprog", "example");

        if (Files.isDirectory(fromRepositoryRoot)) {
            return fromRepositoryRoot;
        }

        Path fromAisafeRoot = Path.of("..", "..", "lprog", "example");

        if (Files.isDirectory(fromAisafeRoot)) {
            return fromAisafeRoot;
        }

        return fromRepositoryRoot;
    }

    // -----------------------------------------------------------
    // US082 - Insert weather data in a flight
    // -----------------------------------------------------------

    private static void suggestWeatherSearch() throws IOException {
        sOut.writeUTF("LIST_MY_FLIGHT_PLANS");
        List<String[]> rows = readDelimitedList();
        if (rows == null) return;

        if (rows.isEmpty()) {
            System.out.println();
            System.out.println("No flight plans found.");
            return;
        }

        String[] selected = chooseFlightPlanByNumber(rows, "get a weather search suggestion for");
        if (selected == null) return;

        sOut.writeUTF("SUGGEST_WEATHER_SEARCH");
        sOut.writeUTF(selected[0]);

        System.out.println("Server: " + sIn.readUTF());
    }

    private static void getWeatherData() throws IOException {
        System.out.print("Air control area code: ");
        String areaCode = scanner.nextLine();
        System.out.print("Date (yyyy-MM-dd): ");
        String date = scanner.nextLine();

        sOut.writeUTF("GET_WEATHER_DATA");
        sOut.writeUTF(areaCode);
        sOut.writeUTF(date);

        List<String[]> rows = readDelimitedList();
        if (rows == null) return;

        if (rows.isEmpty()) {
            System.out.println();
            System.out.println("No weather data found.");
            return;
        }

        System.out.printf("%nTotal: %d weather record(s)%n", rows.size());

        paginate(rows, PilotClient::printWeatherPage, PilotClient::showWeatherDetails);
    }

    private static void printWeatherPage(List<String[]> rows, int page, int totalPages) {
        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, rows.size());

        System.out.printf("%nPage %d of %d%n", page + 1, totalPages);
        System.out.println("Weather Data");
        System.out.println("------------");
        System.out.printf("%-4s %-38s %-8s %-12s %-8s %-8s%n",
                "#", "ID", "Area", "Day", "Start", "End");
        System.out.println("-".repeat(85));

        for (int i = from; i < to; i++) {
            // id|areaCode|day|startTime|endTime|bboxStartLat|bboxStartLon|bboxEndLat|bboxEndLon|altMin|altMax|windSpeed|windDir
            String[] w = rows.get(i);
            System.out.printf("%-4d %-38s %-8s %-12s %-8s %-8s%n",
                    i + 1, w[0], w[1], w[2], w[3], w[4]);
        }
    }

    private static void showWeatherDetails(String[] w, int selectedNumber) {
        System.out.println();
        System.out.println("Weather Data Details");
        System.out.println("--------------------");
        System.out.println("Number       : " + selectedNumber);
        System.out.println("ID           : " + w[0]);
        System.out.println("Area Code    : " + w[1]);
        System.out.println("Day          : " + w[2]);
        System.out.println("Start Time   : " + w[3]);
        System.out.println("End Time     : " + w[4]);
        System.out.printf ("BBox Start   : %s, %s%n", w[5], w[6]);
        System.out.printf ("BBox End     : %s, %s%n", w[7], w[8]);
        System.out.println("Alt Min (ft) : " + w[9]);
        System.out.println("Alt Max (ft) : " + w[10]);
        System.out.println("Wind Speed   : " + w[11] + " knots");
        System.out.println("Wind Dir     : " + w[12] + " deg");
        System.out.println();
        System.out.print("Press ENTER to continue...");
        scanner.nextLine();
    }

    private static void insertWeatherData() throws IOException {
        sOut.writeUTF("LIST_MY_FLIGHT_PLANS");
        List<String[]> planRows = readDelimitedList();
        if (planRows == null) return;

        if (planRows.isEmpty()) {
            System.out.println();
            System.out.println("No flight plans found.");
            return;
        }

        String[] selectedPlan = chooseFlightPlanByNumber(planRows, "insert weather data into");
        if (selectedPlan == null) return;

        System.out.print("Air control area code: ");
        String areaCode = scanner.nextLine();
        System.out.print("Date (yyyy-MM-dd): ");
        String date = scanner.nextLine();

        sOut.writeUTF("GET_WEATHER_DATA");
        sOut.writeUTF(areaCode);
        sOut.writeUTF(date);

        List<String[]> weatherRows = readDelimitedList();
        if (weatherRows == null) return;

        if (weatherRows.isEmpty()) {
            System.out.println();
            System.out.println("No weather data found for that area/date.");
            return;
        }

        String[] selectedWeather = chooseWeatherDataByNumber(weatherRows);
        if (selectedWeather == null) return;

        sOut.writeUTF("INSERT_WEATHER_DATA");
        sOut.writeUTF(selectedPlan[0]);
        sOut.writeUTF(selectedWeather[0]);

        System.out.println("Server: " + sIn.readUTF());
    }

    /**
     * Prints a numbered list of weather data records and lets the user pick one.
     * Returns the chosen row (id|areaCode|day|...), or {@code null} if cancelled.
     */
    private static String[] chooseWeatherDataByNumber(List<String[]> rows) {
        System.out.println();
        System.out.println("Weather Data");
        System.out.println("------------");
        System.out.printf("%-4s %-38s %-8s %-12s %-8s %-8s%n",
                "#", "ID", "Area", "Day", "Start", "End");
        System.out.println("-".repeat(85));

        for (int i = 0; i < rows.size(); i++) {
            String[] w = rows.get(i);
            System.out.printf("%-4d %-38s %-8s %-12s %-8s %-8s%n",
                    i + 1, w[0], w[1], w[2], w[3], w[4]);
        }

        System.out.println();
        System.out.println("  0. Cancel");

        while (true) {
            System.out.print("Choose weather data: ");
            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return null;
            }

            try {
                int option = Integer.parseInt(input);
                if (option >= 1 && option <= rows.size()) {
                    return rows.get(option - 1);
                }
                System.out.printf("  Invalid option. Choose a number between 1 and %d.%n", rows.size());
            } catch (NumberFormatException e) {
                System.out.println("  Invalid option. Please insert a valid number.");
            }
        }
    }

    // -----------------------------------------------------------
    // US085 - Test/validate flight plan
    // -----------------------------------------------------------

    private static void listDraftFlightPlans() throws IOException {
        sOut.writeUTF("LIST_DRAFT_FLIGHT_PLANS");
        showFlightPlanList("No draft flight plans found.");
    }

    private static void testFlightPlan() throws IOException {
        sOut.writeUTF("LIST_DRAFT_FLIGHT_PLANS");
        List<String[]> rows = readDelimitedList();
        if (rows == null) return;

        if (rows.isEmpty()) {
            System.out.println();
            System.out.println("No draft flight plans found.");
            return;
        }

        String[] selected = chooseFlightPlanByNumber(rows, "test/validate");
        if (selected == null) return;

        sOut.writeUTF("TEST_FLIGHT_PLAN");
        sOut.writeUTF(selected[0]);

        String response = sIn.readUTF();

        if (!response.startsWith("OK")) {
            System.out.println("Server: " + response);
            return;
        }

        String[] lines = response.split("\n", -1);
        // lines[0] = "OK", lines[1] = fields, lines[2..] = messages (may be empty)
        String[] f = lines[1].split("\\|");
        // flightPlanID|routeCode|aircraft|pilotID|finalStatus|passed|testedAt|summary|
        // totalDistanceKm|loadedFuelKg|estimatedFuelRequiredKg|estimatedFuelRemainingKg|
        // weatherUsed|weatherWindSpeed|weatherWindDirection|weatherFuelFactor

        boolean passed = Boolean.parseBoolean(f[5]);

        System.out.println();
        printBoxHeader("Flight Plan Test Result");
        printBoxRow("Flight Plan", shortId(f[0]));
        printBoxRow("Route", f[1]);
        printBoxRow("Aircraft", f[2]);
        printBoxRow("Status", f[4]);
        printBoxRow("Result", passed ? "PASSED" : "FAILED");
        if (!f[6].isBlank()) {
            printBoxRow("Tested at", f[6]);
        }
        if (!f[7].isBlank()) {
            printBoxRow("Summary", f[7]);
        }
        printBoxRow("Distance", f[8] + " km");
        printBoxRow("Loaded fuel", f[9] + " kg");
        printBoxRow("Fuel required", f[10] + " kg");
        printBoxRow("Fuel remaining", f[11] + " kg");

        boolean weatherUsed = Boolean.parseBoolean(f[12]);
        if (weatherUsed) {
            printBoxRow("Wind speed", f[13] + " m/s");
            printBoxRow("Wind direction", f[14] + " deg");
            printBoxRow("Weather fuel factor", f[15]);
        } else {
            printBoxRow("Weather data", "Not used");
        }
        printBoxFooter();

        if (lines.length > 2) {
            System.out.println();
            System.out.println("Messages:");
            for (int i = 2; i < lines.length; i++) {
                if (!lines[i].isBlank()) {
                    System.out.println("  - " + lines[i]);
                }
            }
        }
    }

    /**
     * Prints a numbered list of flight plans and lets the user pick one.
     * Returns the chosen row (id|route|aircraft|status|departure|arrival|fuel|...),
     * or {@code null} if cancelled.
     */
    private static String[] chooseFlightPlanByNumber(List<String[]> rows, String actionLabel) {
        System.out.println();
        System.out.println("Flight Plans");
        System.out.println("------------");
        System.out.printf("%-4s %-12s %-8s %-9s %-10s %-16s %-16s %-12s%n",
                "#", "Plan ID", "Route", "Aircraft", "Status", "Departure", "Arrival", "Fuel");
        System.out.println("-".repeat(105));

        for (int i = 0; i < rows.size(); i++) {
            String[] p = rows.get(i);
            System.out.printf("%-4d %-12s %-8s %-9s %-10s %-16s %-16s %-12s%n",
                    i + 1, shortId(p[0]), p[1], p[2], p[3], p[4], p[5], p[6]);
        }

        System.out.println();
        System.out.println("  0. Cancel");

        while (true) {
            System.out.printf("Choose a flight plan to %s: ", actionLabel);
            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return null;
            }

            try {
                int option = Integer.parseInt(input);
                if (option >= 1 && option <= rows.size()) {
                    return rows.get(option - 1);
                }
                System.out.printf("  Invalid option. Choose a number between 1 and %d.%n", rows.size());
            } catch (NumberFormatException e) {
                System.out.println("  Invalid option. Please insert a valid number.");
            }
        }
    }

    // -----------------------------------------------------------
    // Shared flight plan listing (US080's "my plans" and US085's "drafts")
    // -----------------------------------------------------------

    private static void showFlightPlanList(String emptyMessage) throws IOException {
        List<String[]> rows = readDelimitedList();
        if (rows == null) return;

        if (rows.isEmpty()) {
            System.out.println();
            System.out.println(emptyMessage);
            return;
        }

        System.out.printf("%nTotal: %d flight plan(s)%n", rows.size());

        paginate(rows, PilotClient::printFlightPlansPage, PilotClient::showFlightPlanDetails);
    }

    private static void printFlightPlansPage(List<String[]> rows, int page, int totalPages) {
        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, rows.size());

        System.out.printf("%nPage %d of %d%n", page + 1, totalPages);
        System.out.println("Flight Plans");
        System.out.println("------------");
        System.out.printf("%-4s %-12s %-8s %-9s %-10s %-16s %-16s %-12s%n",
                "#", "Plan ID", "Route", "Aircraft", "Status", "Departure", "Arrival", "Fuel");
        System.out.println("-".repeat(105));

        for (int i = from; i < to; i++) {
            // id|route|aircraft|status|departure|arrival|fuel|createdAt|weatherDataId|pilotName
            String[] p = rows.get(i);
            System.out.printf("%-4d %-12s %-8s %-9s %-10s %-16s %-16s %-12s%n",
                    i + 1, shortId(p[0]), p[1], p[2], p[3], p[4], p[5], p[6]);
        }
    }

    private static void showFlightPlanDetails(String[] p, int selectedNumber) {
        // id|route|aircraft|status|departure|arrival|fuel|createdAt|weatherDataId|pilotName
        System.out.println();
        System.out.println("Flight Plan Details");
        System.out.println("===================");
        System.out.println("Number       : " + selectedNumber);
        System.out.println("Full ID      : " + p[0]);
        System.out.println("Route        : " + p[1]);
        System.out.println("Aircraft     : " + p[2]);
        System.out.println("Status       : " + p[3]);
        System.out.println("Departure    : " + p[4]);
        System.out.println("Arrival      : " + p[5]);
        System.out.println("Fuel         : " + p[6]);
        System.out.println("Created at   : " + p[7]);
        if (p.length > 9) {
            System.out.println("Pilot        : " + p[9]);
        }

        String weatherDataId = p.length > 8 ? p[8] : "";
        if (!weatherDataId.isBlank()) {
            System.out.println();
            System.out.println("Weather Data ID : " + weatherDataId);
        } else {
            System.out.println();
            System.out.println("Weather Data : Not assigned");
        }

        System.out.println();
        System.out.print("Press ENTER to continue...");
        scanner.nextLine();
    }

    // -----------------------------------------------------------
    // Generic pagination
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

    private static String shortId(String id) {
        if (id == null || id.length() <= 8) {
            return id;
        }
        return id.substring(0, 8) + "...";
    }

    // -----------------------------------------------------------

    private static void logout() throws IOException {
        sOut.writeUTF("LOGOUT");
        System.out.println("Server: " + sIn.readUTF());
        System.out.println("Goodbye!");
    }
}