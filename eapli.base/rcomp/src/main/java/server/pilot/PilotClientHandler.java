package server.pilot;

import aisafe.aircraft.domain.AircraftRepository;
import aisafe.aircraft.domain.Aircraft;
import aisafe.airport.domain.AirportRepository;
import aisafe.authz.AuthenticationCredentialHandler;
import aisafe.flightplan.application.CreateFlightPlanFromFileController;
import aisafe.flightplan.application.FlightPlanDTO;
import aisafe.flightplan.application.FlightPlanTestBoard;
import aisafe.flightplan.application.InsertWeatherDataInFlightController;
import aisafe.flightplan.application.TestFlightPlanController;
import aisafe.flightplan.domain.FlightPlan;
import aisafe.flightplan.domain.FlightPlanID;
import aisafe.flightplan.domain.FlightPlanRepository;
import aisafe.flightplan.domain.FuelUnit;
import aisafe.flightplan.application.CreateFlightPlanController;
import aisafe.flightroute.application.FlightRouteDTO;
import aisafe.flightroute.domain.FlightRouteRepository;
import aisafe.pilot.domain.PilotRepository;
import aisafe.persistence.PersistenceContext;
import aisafe.usermanagement.domain.AiSafeRoles;
import aisafe.weather.domain.WeatherData;
import aisafe.weather.domain.WeatherDataID;
import aisafe.weather.domain.WeatherDataRepository;
import server.logging.RemoteAccessLogger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * TCP request handler for the Pilot remote access service (US86).
 *
 * Mirrors the structure of the Weather Person handler (US44), but exposes
 * the Pilot-related use cases: US080, US081, US082 and US085.
 */
public class PilotClientHandler implements Runnable {

    private static final String SERVICE = "US86";
    private static final String LOGGING_SERVER_HOST = "localhost";
    private static final int LOGGING_SERVER_PORT = 9001;
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");


    private final Socket socket;
    private DataInputStream sIn;
    private DataOutputStream sOut;
    private String username;

    private final String clientIp;
    private final int clientPort;
    private final RemoteAccessLogger remoteAccessLogger;

    private final AuthenticationCredentialHandler authHandler;

    private final CreateFlightPlanController createFlightPlanController;
    private final CreateFlightPlanFromFileController createFromFileController;
    private final InsertWeatherDataInFlightController insertWeatherController;
    private final TestFlightPlanController testFlightPlanController;

    public PilotClientHandler(Socket socket) {
        this.socket = socket;
        this.clientIp = socket.getInetAddress().getHostAddress();
        this.clientPort = socket.getPort();
        this.remoteAccessLogger = new RemoteAccessLogger(LOGGING_SERVER_HOST, LOGGING_SERVER_PORT);

        FlightPlanRepository flightPlanRepository = PersistenceContext.repositories().flightPlans();
        FlightRouteRepository flightRouteRepository = PersistenceContext.repositories().flightRoutes();
        AircraftRepository aircraftRepository = PersistenceContext.repositories().aircrafts();
        AirportRepository airportRepository = PersistenceContext.repositories().airports();
        WeatherDataRepository weatherDataRepository = PersistenceContext.repositories().weatherData();
        PilotRepository pilotRepository = PersistenceContext.repositories().pilots();

        this.createFlightPlanController = new CreateFlightPlanController(
                flightPlanRepository, flightRouteRepository, aircraftRepository, pilotRepository);

        this.createFromFileController = new CreateFlightPlanFromFileController(
                flightPlanRepository, flightRouteRepository, aircraftRepository,
                airportRepository, weatherDataRepository, pilotRepository);

        this.insertWeatherController = new InsertWeatherDataInFlightController(
                flightPlanRepository, weatherDataRepository, pilotRepository,
                flightRouteRepository, airportRepository);

        this.testFlightPlanController = new TestFlightPlanController(
                flightPlanRepository, flightRouteRepository, aircraftRepository,
                airportRepository, weatherDataRepository, pilotRepository);

        this.authHandler = new AuthenticationCredentialHandler();
    }

    public void run() {
        boolean authenticated = false;
        boolean loggedOut = false;

        try {
            sIn = new DataInputStream(socket.getInputStream());
            sOut = new DataOutputStream(socket.getOutputStream());

            authenticated = authenticate();
            if (!authenticated) {
                socket.close();
                return;
            }

            boolean running = true;
            while (running) {
                String command = sIn.readUTF();
                System.out.println("[" + username + "] Command: " + command);

                switch (command) {
                    case "LIST_ROUTES":
                        handleListRoutes();
                        break;
                    case "LIST_AIRCRAFT":
                        handleListAircraft();
                        break;
                    case "CREATE_FLIGHT_PLAN":
                        handleCreateFlightPlan();
                        break;
                    case "LIST_MY_FLIGHT_PLANS":
                        handleListMyFlightPlans();
                        break;
                    case "CREATE_FLIGHT_PLAN_FROM_FILE":
                        handleCreateFlightPlanFromFile();
                        break;
                    case "SUGGEST_WEATHER_SEARCH":
                        handleSuggestWeatherSearch();
                        break;
                    case "GET_WEATHER_DATA":
                        handleGetWeatherData();
                        break;
                    case "INSERT_WEATHER_DATA":
                        handleInsertWeatherData();
                        break;
                    case "LIST_DRAFT_FLIGHT_PLANS":
                        handleListDraftFlightPlans();
                        break;
                    case "TEST_FLIGHT_PLAN":
                        handleTestFlightPlan();
                        break;
                    case "LOGOUT":
                        sOut.writeUTF("OK: Goodbye");
                        running = false;
                        loggedOut = true;
                        remoteAccessLogger.logout(username, clientIp, clientPort, SERVICE);
                        break;
                    default:
                        sOut.writeUTF("ERROR: Unknown command");
                }
            }

        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            if (authenticated && !loggedOut) {
                remoteAccessLogger.disconnect(username, clientIp, clientPort, SERVICE);
            }
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }

    private boolean authenticate() throws IOException {
        String user = sIn.readUTF();
        String pass = sIn.readUTF();

        boolean ok = authHandler.authenticated(user, pass, AiSafeRoles.PILOT);

        if (ok) {
            this.username = user;
            sOut.writeUTF("OK: Authenticated");
            System.out.println("User authenticated: " + user);
            remoteAccessLogger.loginSuccess(username, clientIp, clientPort, SERVICE);
            return true;
        } else {
            sOut.writeUTF("ERROR: Invalid credentials or insufficient permissions");
            System.out.println("Authentication failed for: " + user);
            remoteAccessLogger.loginFailed(user, clientIp, clientPort, SERVICE);
            return false;
        }
    }

    // ---------------------------------------------------------------
    // US080 - Create a flight plan
    // ---------------------------------------------------------------

    private void handleListRoutes() throws IOException {
        try {
            Iterable<FlightRouteDTO> routes = createFlightPlanController.listAvailableRoutes();

            StringBuilder sb = new StringBuilder("OK\n");
            int count = 0;
            StringBuilder rows = new StringBuilder();
            for (FlightRouteDTO route : routes) {
                count++;
                rows.append(String.join("|",
                        nullSafe(route.routeCode),
                        nullSafe(route.departureAirportIata),
                        nullSafe(route.arrivalAirportIata),
                        nullSafe(route.flightType),
                        nullSafe(route.routeStatus),
                        nullSafe(route.deactivationDate),
                        nullSafe(route.schedule).replace("|", "/")
                )).append("\n");
            }

            sb.append(count).append("\n").append(rows);
            sOut.writeUTF(sb.toString());

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    private void handleListAircraft() throws IOException {
        try {
            Iterable<Aircraft> aircraft = createFlightPlanController.listAvailableAircrafts();

            int count = 0;
            StringBuilder rows = new StringBuilder();
            for (Aircraft a : aircraft) {
                count++;
                rows.append(String.join("|",
                        nullSafe(a.aircraftCode().code()),
                        nullSafe(a.aircraftModel().identity()),
                        nullSafe(a.airCompany().identity()),
                        String.valueOf(a.numCrewElements()),
                        nullSafe(a.status())
                )).append("\n");
            }

            sOut.writeUTF("OK\n" + count + "\n" + rows);

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    private void handleCreateFlightPlan() throws IOException {
        try {
            String routeCode = sIn.readUTF();
            String aircraftRegistration = sIn.readUTF();
            String plannedDeparture = sIn.readUTF();
            String plannedArrival = sIn.readUTF();
            double fuelAmount = sIn.readDouble();
            String fuelUnitName = sIn.readUTF();
            String description = sIn.readUTF();

            FlightPlanDTO flightPlan = createFlightPlanController.createFlightPlan(
                    routeCode,
                    aircraftRegistration,
                    LocalDateTime.parse(plannedDeparture),
                    LocalDateTime.parse(plannedArrival),
                    fuelAmount,
                    FuelUnit.valueOf(fuelUnitName),
                    description
            );

            sOut.writeUTF("OK: Flight plan created with ID " + flightPlan.flightPlanID());

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    private void handleListMyFlightPlans() throws IOException {
        try {
            Iterable<FlightPlanDTO> plans = createFlightPlanController.listMyCreatedFlightPlans();
            sOut.writeUTF(formatFlightPlanRows(plans));
        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // US081 - Create a flight plan from a file (Core Flight DSL)
    // ---------------------------------------------------------------

    /**
     * The client sends the DSL file content as text. The server writes it
     * to a temporary file and delegates to the use case controller, which
     * expects a file path (the DSL parsing/validation happens in LPROG's
     * implementation behind {@code createFromFile}).
     */
    private void handleCreateFlightPlanFromFile() throws IOException {
        File tempFile = null;
        try {
            String fileContent = sIn.readUTF();

            tempFile = File.createTempFile("flightplan-" + UUID.randomUUID(), ".flight");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(fileContent);
            }

            aisafe.flightplan.domain.CreateFlightPlanFromFileService.ImportFlightPlanResult result =
                    createFromFileController.createFromFile(tempFile.getAbsolutePath());

            if (!result.isSuccess()) {
                StringBuilder sb = new StringBuilder("ERROR: Flight plan import failed.\n");
                for (String error : result.errors()) {
                    sb.append(" - ").append(error).append("\n");
                }
                sOut.writeUTF(sb.toString());
                return;
            }

            FlightPlanDTO flightPlan = result.flightPlan();
            int segmentCount = result.segments() == null ? 0 : result.segments().size();

            String response = String.join("|",
                    "OK",
                    flightPlan.flightPlanID(),
                    flightPlan.routeCode(),
                    flightPlan.aircraftRegistration(),
                    nullSafe(flightPlan.status()),
                    formatDateTime(flightPlan.plannedDepartureDateTime()),
                    formatDateTime(flightPlan.plannedArrivalDateTime()),
                    flightPlan.fuelAmount() + " " + flightPlan.fuelUnit(),
                    String.valueOf(segmentCount)
            );

            sOut.writeUTF(response);

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

    // ---------------------------------------------------------------
    // US082 - Insert weather data in a flight
    // ---------------------------------------------------------------

    private void handleSuggestWeatherSearch() throws IOException {
        try {
            String flightPlanIDValue = sIn.readUTF();

            FlightPlan flightPlan = findFlightPlanOrThrow(flightPlanIDValue);

            InsertWeatherDataInFlightController.WeatherSearchSuggestion suggestion =
                    insertWeatherController.suggestWeatherSearchFor(flightPlan);

            String response = String.join("|",
                    suggestion.routeCode(),
                    suggestion.departureAirport(),
                    suggestion.arrivalAirport(),
                    suggestion.arrivalAirportName(),
                    suggestion.areaCode(),
                    suggestion.flightDate().toString()
            );

            sOut.writeUTF("OK: " + response);

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    private void handleGetWeatherData() throws IOException {
        try {
            String areaCode = sIn.readUTF();
            String date = sIn.readUTF();

            Iterable<WeatherData> results = insertWeatherController.getWeatherDataByAreaAndDate(
                    areaCode, LocalDate.parse(date));

            int count = 0;
            StringBuilder rows = new StringBuilder();
            for (WeatherData wd : results) {
                count++;
                rows.append(String.join("|",
                        wd.identity().toString(),
                        wd.airControlAreaCode().toString(),
                        wd.day().toString(),
                        wd.startTime().toString(),
                        wd.endTime().toString(),
                        String.valueOf(wd.boundingBox().start().latitude()),
                        String.valueOf(wd.boundingBox().start().longitude()),
                        String.valueOf(wd.boundingBox().end().latitude()),
                        String.valueOf(wd.boundingBox().end().longitude()),
                        String.valueOf(wd.minAltitude()),
                        String.valueOf(wd.maxAltitude()),
                        String.valueOf(wd.windSpeed()),
                        String.valueOf(wd.windDirection())
                )).append("\n");
            }

            sOut.writeUTF("OK\n" + count + "\n" + rows);

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    private void handleInsertWeatherData() throws IOException {
        try {
            String flightPlanIDValue = sIn.readUTF();
            String weatherDataIDValue = sIn.readUTF();

            insertWeatherController.insertWeatherData(
                    FlightPlanID.of(flightPlanIDValue.trim()),
                    WeatherDataID.of(weatherDataIDValue.trim())
            );

            sOut.writeUTF("OK: Weather data inserted into flight plan");

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // US085 - Test/validate flight plan
    // ---------------------------------------------------------------

    private void handleListDraftFlightPlans() throws IOException {
        try {
            Iterable<FlightPlanDTO> plans = testFlightPlanController.listDraftFlightPlans();
            sOut.writeUTF(formatFlightPlanRows(plans));
        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    private void handleTestFlightPlan() throws IOException {
        try {
            String flightPlanIDValue = sIn.readUTF();

            FlightPlanTestBoard board = testFlightPlanController.testAndValidate(flightPlanIDValue);

            StringBuilder sb = new StringBuilder("OK\n");
            sb.append(String.join("|",
                    board.flightPlanID(),
                    board.routeCode(),
                    board.aircraftRegistration(),
                    board.pilotID(),
                    nullSafe(board.finalStatus()),
                    String.valueOf(board.passed()),
                    board.testedAt() == null ? "" : formatDateTime(board.testedAt()),
                    nullSafe(board.summary()).replace("|", "/").replace("\n", " "),
                    String.valueOf(board.totalDistanceKm()),
                    String.valueOf(board.loadedFuelKg()),
                    String.valueOf(board.estimatedFuelRequiredKg()),
                    String.valueOf(board.estimatedFuelRemainingKg()),
                    String.valueOf(board.weatherUsed()),
                    String.valueOf(board.weatherWindSpeed()),
                    String.valueOf(board.weatherWindDirection()),
                    String.valueOf(board.weatherFuelFactor())
            )).append("\n");

            for (String message : board.messages()) {
                sb.append(message.replace("\n", " ")).append("\n");
            }

            sOut.writeUTF(sb.toString());

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private FlightPlan findFlightPlanOrThrow(String flightPlanIDValue) {
        FlightPlanID flightPlanID = FlightPlanID.of(flightPlanIDValue.trim());

        for (FlightPlan plan : insertWeatherController.getMyFlightPlans(
                insertWeatherController.getAuthenticatedPilot().identity())) {
            if (plan.identity().equals(flightPlanID)) {
                return plan;
            }
        }
        throw new IllegalArgumentException("Flight plan not found: " + flightPlanIDValue);
    }

    private String shortId(String id) {
        if (id == null || id.length() <= 8) {
            return id;
        }
        return id.substring(0, 8) + "...";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMAT);
    }

    private String nullSafe(Object value) {
        return value == null ? "" : value.toString();
    }

    /**
     * Formats an iterable of {@link FlightPlanDTO} as a delimited response:
     * "OK\n&lt;count&gt;\n" followed by one row per flight plan:
     * fullId|route|aircraft|status|departure|arrival|fuel|createdAt|weatherDataId
     *
     * Used by both LIST_MY_FLIGHT_PLANS and LIST_DRAFT_FLIGHT_PLANS so the
     * client can apply consistent pagination/details UI to both.
     */
    private String formatFlightPlanRows(Iterable<FlightPlanDTO> plans) {
        int count = 0;
        StringBuilder rows = new StringBuilder();

        for (FlightPlanDTO plan : plans) {
            count++;

            String pilotName;
            try {
                pilotName = createFlightPlanController.pilotDisplayNameFor(plan);
            } catch (Exception e) {
                pilotName = plan.pilotID();
            }

            rows.append(String.join("|",
                    plan.flightPlanID(),
                    nullSafe(plan.routeCode()),
                    plan.aircraftRegistration(),
                    nullSafe(plan.status()),
                    formatDateTime(plan.plannedDepartureDateTime()),
                    formatDateTime(plan.plannedArrivalDateTime()),
                    plan.fuelAmount() + " " + plan.fuelUnit(),
                    formatDateTime(plan.createdAt()),
                    plan.weatherDataID() == null ? "" : plan.weatherDataID(),
                    pilotName.replace("|", "/")
            )).append("\n");
        }

        return "OK\n" + count + "\n" + rows;
    }
}