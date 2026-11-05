package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WeatherPersonClient {
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

            sOut.writeUTF("US44");
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
                        registerWeather();
                        break;
                    case "2":
                        importWeather();
                        break;
                    case "3":
                        consultWeather();
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
        System.out.println("\n=== Weather Person Menu ===");
        System.out.println("1. Register weather data ");
        System.out.println("2. Import bulk weather data ");
        System.out.println("3. Consult weather data ");
        System.out.println("0. Logout");
        System.out.print("Option: ");
    }

    private static void registerWeather() throws IOException {
        // US041
        System.out.print("Air control area code: ");
        String area = scanner.nextLine();
        System.out.print("Date (yyyy-MM-dd): ");
        String day = scanner.nextLine();
        System.out.print("Start time (HH:mm): ");
        String startTime = scanner.nextLine();
        System.out.print("End time (HH:mm): ");
        String endTime = scanner.nextLine();
        System.out.print("Lat start: ");
        double latStart = Double.parseDouble(scanner.nextLine());
        System.out.print("Lon start: ");
        double lonStart = Double.parseDouble(scanner.nextLine());
        System.out.print("Lat end: ");
        double latEnd = Double.parseDouble(scanner.nextLine());
        System.out.print("Lon end: ");
        double lonEnd = Double.parseDouble(scanner.nextLine());
        System.out.print("Min altitude: ");
        int minAlt = Integer.parseInt(scanner.nextLine());
        System.out.print("Max altitude: ");
        int maxAlt = Integer.parseInt(scanner.nextLine());
        System.out.print("Wind speed: ");
        double windSpeed = Double.parseDouble(scanner.nextLine());
        System.out.print("Wind direction: ");
        double windDir = Double.parseDouble(scanner.nextLine());

        sOut.writeUTF("REGISTER_WEATHER");
        sOut.writeUTF(area);
        sOut.writeUTF(day);
        sOut.writeUTF(startTime);
        sOut.writeUTF(endTime);
        sOut.writeDouble(latStart);
        sOut.writeDouble(lonStart);
        sOut.writeDouble(latEnd);
        sOut.writeDouble(lonEnd);
        sOut.writeInt(minAlt);
        sOut.writeInt(maxAlt);
        sOut.writeDouble(windSpeed);
        sOut.writeDouble(windDir);

        System.out.println("Server: " + sIn.readUTF());
    }

    private static void importWeather() throws IOException {
        // US042
        System.out.print("How many records to import? ");
        int count = Integer.parseInt(scanner.nextLine().trim());

        sOut.writeUTF("IMPORT_WEATHER");
        sOut.writeInt(count);

        for (int i = 0; i < count; i++) {
            System.out.println("\n--- Record " + (i + 1) + " ---");
            System.out.print("Air control area code: ");
            String area = scanner.nextLine();
            System.out.print("Date (yyyy-MM-dd): ");
            String day = scanner.nextLine();
            System.out.print("Start time (HH:mm): ");
            String startTime = scanner.nextLine();
            System.out.print("End time (HH:mm): ");
            String endTime = scanner.nextLine();
            System.out.print("Lat start: ");
            double latStart = Double.parseDouble(scanner.nextLine());
            System.out.print("Lon start: ");
            double lonStart = Double.parseDouble(scanner.nextLine());
            System.out.print("Lat end: ");
            double latEnd = Double.parseDouble(scanner.nextLine());
            System.out.print("Lon end: ");
            double lonEnd = Double.parseDouble(scanner.nextLine());
            System.out.print("Min altitude: ");
            int minAlt = Integer.parseInt(scanner.nextLine());
            System.out.print("Max altitude: ");
            int maxAlt = Integer.parseInt(scanner.nextLine());
            System.out.print("Wind speed: ");
            double windSpeed = Double.parseDouble(scanner.nextLine());
            System.out.print("Wind direction: ");
            double windDir = Double.parseDouble(scanner.nextLine());

            sOut.writeUTF(area);
            sOut.writeUTF(day);
            sOut.writeUTF(startTime);
            sOut.writeUTF(endTime);
            sOut.writeDouble(latStart);
            sOut.writeDouble(lonStart);
            sOut.writeDouble(latEnd);
            sOut.writeDouble(lonEnd);
            sOut.writeInt(minAlt);
            sOut.writeInt(maxAlt);
            sOut.writeDouble(windSpeed);
            sOut.writeDouble(windDir);
        }

        System.out.println("Server: " + sIn.readUTF());
    }

    private static void consultWeather() throws IOException {
        // US043
        System.out.print("Air control area code: ");
        String area = scanner.nextLine();
        System.out.print("Date (yyyy-MM-dd): ");
        String day = scanner.nextLine();

        sOut.writeUTF("CONSULT_WEATHER");
        sOut.writeUTF(area);
        sOut.writeUTF(day);

        List<String[]> rows = readDelimitedList();
        if (rows == null) return;

        if (rows.isEmpty()) {
            System.out.println();
            System.out.println("No weather data found.");
            return;
        }

        System.out.printf("%nTotal: %d weather record(s)%n", rows.size());

        paginate(rows, WeatherPersonClient::printWeatherPage, WeatherPersonClient::showWeatherDetails);
    }


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

    private static void logout() throws IOException {
        sOut.writeUTF("LOGOUT");
        System.out.println("Server: " + sIn.readUTF());
        System.out.println("Goodbye!");
    }
}