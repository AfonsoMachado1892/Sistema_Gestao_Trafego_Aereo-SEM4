package server;

import aisafe.aircontrolarea.domain.AirControlAreaRepository;
import aisafe.authz.AuthenticationCredentialHandler;
import aisafe.persistence.PersistenceContext;
import aisafe.usermanagement.domain.AiSafeRoles;
import aisafe.weather.application.*;
import aisafe.weather.domain.WeatherData;
import aisafe.weather.domain.WeatherDataRepository;
import server.logging.RemoteAccessLogger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {

    private static final String SERVICE = "US44";
    private static final String LOGGING_SERVER_HOST = "localhost";
    private static final int LOGGING_SERVER_PORT = 9001;

    private final Socket socket;
    private DataInputStream sIn;
    private DataOutputStream sOut;
    private String username;

    private final String clientIp;
    private final int clientPort;
    private final RemoteAccessLogger remoteAccessLogger;

    private final RegisterWeatherDataController registerController;
    private final ListWeatherDataController listController;
    private final ImportBulkWeatherDataController importController;
    private final AuthenticationCredentialHandler authHandler;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.clientIp = socket.getInetAddress().getHostAddress();
        this.clientPort = socket.getPort();
        this.remoteAccessLogger = new RemoteAccessLogger(LOGGING_SERVER_HOST, LOGGING_SERVER_PORT);

        WeatherDataRepository weatherDataRepository = PersistenceContext.repositories().weatherData();
        AirControlAreaRepository airControlAreaRepository = PersistenceContext.repositories().airControlAreas();

        this.registerController = new RegisterWeatherDataController(
                airControlAreaRepository, weatherDataRepository);

        this.listController = new ListWeatherDataController(
                eapli.framework.infrastructure.authz.application.AuthzRegistry.authorizationService(),
                weatherDataRepository);

        this.importController = new ImportBulkWeatherDataController(weatherDataRepository);

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
                    case "REGISTER_WEATHER":
                        handleRegisterWeather();
                        break;
                    case "IMPORT_WEATHER":
                        handleImportWeather();
                        break;
                    case "CONSULT_WEATHER":
                        handleConsultWeather();
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

        boolean ok = authHandler.authenticated(user, pass, AiSafeRoles.WEATHER_PERSON);

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

    private void handleRegisterWeather() throws IOException {
        try {
            String areaCode  = sIn.readUTF();
            String day       = sIn.readUTF();
            String startTime = sIn.readUTF();
            String endTime   = sIn.readUTF();
            double latStart  = sIn.readDouble();
            double lonStart  = sIn.readDouble();
            double latEnd    = sIn.readDouble();
            double lonEnd    = sIn.readDouble();
            int minAltitude  = sIn.readInt();
            int maxAltitude  = sIn.readInt();
            double windSpeed = sIn.readDouble();
            double windDir   = sIn.readDouble();

            registerController.registerWeatherData(
                    areaCode,
                    LocalDate.parse(day),
                    LocalTime.parse(startTime),
                    LocalTime.parse(endTime),
                    latStart, lonStart,
                    latEnd, lonEnd,
                    minAltitude, maxAltitude,
                    windSpeed, windDir
            );

            sOut.writeUTF("OK: Weather data registered");

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    private void handleImportWeather() throws IOException {
        try {
            int count = sIn.readInt();
            List<WeatherDataDTO> dtos = new ArrayList<>();

            for (int i = 0; i < count; i++) {
                String areaCode  = sIn.readUTF();
                String day       = sIn.readUTF();
                String startTime = sIn.readUTF();
                String endTime   = sIn.readUTF();
                double latStart  = sIn.readDouble();
                double lonStart  = sIn.readDouble();
                double latEnd    = sIn.readDouble();
                double lonEnd    = sIn.readDouble();
                int minAlt       = sIn.readInt();
                int maxAlt       = sIn.readInt();
                double windSpeed = sIn.readDouble();
                double windDir   = sIn.readDouble();

                dtos.add(new WeatherDataDTO(
                        areaCode,
                        LocalDate.parse(day),
                        LocalTime.parse(startTime),
                        LocalTime.parse(endTime),
                        latStart, lonStart,
                        latEnd, lonEnd,
                        minAlt, maxAlt,
                        windSpeed, windDir
                ));
            }

            int imported = importController.confirmImport(dtos);
            sOut.writeUTF("OK: " + imported + " records imported");

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    /**
     * Returns a delimited response so the client can paginate/format
     * results instead of relying on WeatherData#toString():
     *
     *   OK
     *   <count>
     *   id|areaCode|day|startTime|endTime|bboxStartLat|bboxStartLon|bboxEndLat|bboxEndLon|altMin|altMax|windSpeed|windDir
     *   ...
     */
    private void handleConsultWeather() throws IOException {
        try {
            String areaCode = sIn.readUTF();
            String day      = sIn.readUTF();

            Iterable<WeatherData> results = listController.filteredWeatherData(
                    areaCode,
                    LocalDate.parse(day)
            );

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
}