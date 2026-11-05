package server.atcc;

import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRepository;
import aisafe.aircraft.domain.CabinConfiguration;
import aisafe.aircraft.application.AddAircraftController;
import aisafe.aircraft.application.DecommissionAircraftController;
import aisafe.aircraft.application.ListCompanyFleetController;
import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.aircraftmodel.domain.AircraftModelID;
import aisafe.aircraftmodel.domain.AircraftModelRepository;
import aisafe.airport.domain.AirportRepository;
import aisafe.airtransportcompany.domain.AirTransportCompanyRepository;
import aisafe.authz.AuthenticationCredentialHandler;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.flight.domain.FlightRepository;
import aisafe.flightplan.domain.FlightPlanRepository;
import aisafe.flightroute.application.*;
import aisafe.flightroute.domain.*;
import aisafe.persistence.PersistenceContext;
import aisafe.pilot.application.*;
import aisafe.pilot.domain.Certification;
import aisafe.pilot.domain.FlightHours;
import aisafe.pilot.domain.PilotRepository;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import aisafe.usermanagement.domain.AiSafeRoles;
import aisafe.usermanagement.domain.AiSafeUserRepository;
import server.logging.RemoteAccessLogger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * TCP request handler for the Air Transport Company Collaborator remote
 * access service (US78).
 *
 * Mirrors the structure of the Pilot handler (US86), exposing the ATCC
 * use cases: fleet management (US070-072), flight routes (US073-074)
 * and pilot management (US075-077).
 */
public class AirTransportCompanyCollaboratorClientHandler implements Runnable {

    private static final String SERVICE = "US78";
    private static final String LOGGING_SERVER_HOST = "localhost";
    private static final int LOGGING_SERVER_PORT = 9001;

    private final Socket socket;
    private DataInputStream sIn;
    private DataOutputStream sOut;
    private String username;

    private final String clientIp;
    private final int clientPort;
    private final RemoteAccessLogger remoteAccessLogger;

    private final AuthenticationCredentialHandler authHandler;

    // Fleet (US070-072)
    private final AddAircraftController addAircraftController;
    private final DecommissionAircraftController decommissionAircraftController;
    private final ListCompanyFleetController listCompanyFleetController;
    private final AircraftModelRepository aircraftModelRepository;

    // Flight routes (US073-074)
    private final CreateFlightRouteController createFlightRouteController;
    private final DeactivateFlightRouteController deactivateFlightRouteController;

    // Pilots (US075-077)
    private final AddPilotController addPilotController;
    private final ListPilotRosterController listPilotRosterController;
    private final RemovePilotController removePilotController;

    public AirTransportCompanyCollaboratorClientHandler(Socket socket) {
        this.socket = socket;
        this.clientIp = socket.getInetAddress().getHostAddress();
        this.clientPort = socket.getPort();
        this.remoteAccessLogger = new RemoteAccessLogger(LOGGING_SERVER_HOST, LOGGING_SERVER_PORT);

        AircraftRepository aircraftRepository = PersistenceContext.repositories().aircrafts();
        this.aircraftModelRepository = PersistenceContext.repositories().aircraftModels();
        AirTransportCompanyRepository airTransportCompanyRepository =
                PersistenceContext.repositories().airTransportCompanies();
        CustomerCollaboratorRepository customerCollaboratorRepository =
                PersistenceContext.repositories().customerCollaborators();
        FlightRepository flightRepository = PersistenceContext.repositories().flights();
        FlightPlanRepository flightPlanRepository = PersistenceContext.repositories().flightPlans();
        FlightRouteRepository flightRouteRepository = PersistenceContext.repositories().flightRoutes();
        AirportRepository airportRepository = PersistenceContext.repositories().airports();
        PilotRepository pilotRepository = PersistenceContext.repositories().pilots();
        AiSafeUserRepository aiSafeUserRepository = PersistenceContext.repositories().aiSafeUsers();

        this.addAircraftController = new AddAircraftController(
                aircraftRepository, aircraftModelRepository,
                airTransportCompanyRepository, customerCollaboratorRepository);

        this.decommissionAircraftController = new DecommissionAircraftController(
                aircraftRepository, airTransportCompanyRepository,
                customerCollaboratorRepository, flightRepository);

        this.listCompanyFleetController = new ListCompanyFleetController(
                aircraftRepository, airTransportCompanyRepository,
                customerCollaboratorRepository);

        this.createFlightRouteController = new CreateFlightRouteController(
                flightRouteRepository, airportRepository,
                flightPlanRepository, customerCollaboratorRepository);

        this.deactivateFlightRouteController = new DeactivateFlightRouteController(
                flightRouteRepository, airportRepository,
                flightPlanRepository, customerCollaboratorRepository);

        this.addPilotController = new AddPilotController(
                pilotRepository, aircraftModelRepository,
                customerCollaboratorRepository, aiSafeUserRepository);

        this.listPilotRosterController = new ListPilotRosterController(
                pilotRepository, aircraftModelRepository, customerCollaboratorRepository);

        this.removePilotController = new RemovePilotController(
                pilotRepository, aircraftModelRepository,
                flightPlanRepository, customerCollaboratorRepository);

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
                    // Fleet (US070-072)
                    case "LIST_AIRCRAFT_MODELS":
                        handleListAircraftModels();
                        break;
                    case "ADD_AIRCRAFT":
                        handleAddAircraft();
                        break;
                    case "DECOMMISSION_AIRCRAFT":
                        handleDecommissionAircraft();
                        break;
                    case "LIST_COMPANY_FLEET":
                        handleListCompanyFleet();
                        break;

                    // Flight routes (US073-074)
                    case "LIST_AIRPORTS":
                        handleListAirports();
                        break;
                    case "CREATE_REGULAR_ROUTE":
                        handleCreateRegularRoute();
                        break;
                    case "CREATE_CHARTER_ROUTE":
                        handleCreateCharterRoute();
                        break;
                    case "LIST_ACTIVE_ROUTES":
                        handleListActiveRoutes();
                        break;
                    case "LIST_COMPANY_ROUTES":
                        handleListCompanyRoutes();
                        break;
                    case "DEACTIVATE_ROUTE":
                        handleDeactivateRoute();
                        break;

                    // Pilots (US075-077)
                    case "LIST_PILOT_AIRCRAFT_MODELS":
                        handleListPilotAircraftModels();
                        break;
                    case "ADD_PILOT":
                        handleAddPilot();
                        break;
                    case "LIST_PILOT_ROSTER":
                        handleListPilotRoster();
                        break;
                    case "LIST_ACTIVE_PILOTS":
                        handleListActivePilots();
                        break;
                    case "REMOVE_PILOT":
                        handleRemovePilot();
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

        boolean ok = authHandler.authenticated(user, pass, AiSafeRoles.AIR_TRANSPORT_COMPANY_COLLABORATOR);

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
    // US070 - Add aircraft to fleet
    // ---------------------------------------------------------------

    private void handleListAircraftModels() throws IOException {
        try {
            List<AircraftModel> models = addAircraftController.listAircraftModels();

            int count = 0;
            StringBuilder rows = new StringBuilder();
            for (AircraftModel model : models) {
                count++;
                rows.append(String.join("|",
                        model.identity().toString(),
                        model.type().toString(),
                        model.maker().name().toString(),
                        String.valueOf(model.maxPassengerCapacity())
                )).append("\n");
            }

            sOut.writeUTF("OK\n" + count + "\n" + rows);

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    private void handleAddAircraft() throws IOException {
        try {
            String registration = sIn.readUTF();
            String modelID = sIn.readUTF();
            int economySeats = sIn.readInt();
            int businessSeats = sIn.readInt();
            int firstSeats = sIn.readInt();
            String countryIso = sIn.readUTF();
            String countryName = sIn.readUTF();
            int numCrewElements = sIn.readInt();

            AircraftModel model = aircraftModelRepository.findByModelID(AircraftModelID.of(modelID))
                    .orElseThrow(() -> new IllegalArgumentException("Aircraft model not found: " + modelID));

            CabinConfiguration cabinConfiguration = CabinConfiguration.of(economySeats, businessSeats, firstSeats);
            Country country = Country.of(IsoCode.of(countryIso), countryName);

            Aircraft aircraft = addAircraftController.addAircraft(
                    registration, model, cabinConfiguration, country, numCrewElements);

            sOut.writeUTF("OK: Aircraft " + aircraft.identity() + " added to fleet");

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // US071 - Decommission an aircraft
    // ---------------------------------------------------------------

    private void handleDecommissionAircraft() throws IOException {
        try {
            String registration = sIn.readUTF();

            Aircraft aircraft = decommissionAircraftController.decommissionAircraft(registration);

            sOut.writeUTF("OK: Aircraft " + aircraft.identity() + " decommissioned");

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // US072 - List company fleet
    // ---------------------------------------------------------------

    private void handleListCompanyFleet() throws IOException {
        try {
            List<Aircraft> fleet = listCompanyFleetController.listCompanyFleet();

            int count = 0;
            StringBuilder rows = new StringBuilder();
            for (Aircraft aircraft : fleet) {
                count++;
                rows.append(String.join("|",
                        aircraft.identity().toString(),
                        aircraft.aircraftModel().identity().toString(),
                        aircraft.aircraftModel().maker().name().toString(),
                        String.valueOf(aircraft.cabinConfiguration().totalSeats()),
                        aircraft.status().toString(),
                        aircraft.country().toString()
                )).append("\n");
            }

            sOut.writeUTF("OK\n" + count + "\n" + rows);

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // US073 - Create a flight route
    // ---------------------------------------------------------------

    private void handleListAirports() throws IOException {
        try {
            Iterable<AirportDTO> airports = createFlightRouteController.listAirports();

            int count = 0;
            StringBuilder rows = new StringBuilder();
            for (AirportDTO airport : airports) {
                count++;
                rows.append(String.join("|",
                        airport.iataCode,
                        airport.icaoCode,
                        airport.name,
                        airport.city
                )).append("\n");
            }

            sOut.writeUTF("OK\n" + count + "\n" + rows);

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    private void handleCreateRegularRoute() throws IOException {
        try {
            String routeCode = sIn.readUTF();
            String departureIata = sIn.readUTF();
            String arrivalIata = sIn.readUTF();

            int entryCount = sIn.readInt();
            List<ScheduleEntry> entries = new ArrayList<>();
            for (int i = 0; i < entryCount; i++) {
                String dayOfWeek = sIn.readUTF();
                String departureTime = sIn.readUTF();
                entries.add(ScheduleEntry.of(
                        java.time.DayOfWeek.valueOf(dayOfWeek.toUpperCase()),
                        java.time.LocalTime.parse(departureTime)
                ));
            }

            RegularSchedule schedule = RegularSchedule.of(entries);

            FlightRouteDTO route = createFlightRouteController.createRegularFlightRoute(
                    routeCode, departureIata, arrivalIata, schedule);

            sOut.writeUTF("OK: Flight route " + route.routeCode + " created");

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    private void handleCreateCharterRoute() throws IOException {
        try {
            String routeCode = sIn.readUTF();
            String departureIata = sIn.readUTF();
            String arrivalIata = sIn.readUTF();
            String departureDateTime = sIn.readUTF();
            String arrivalDateTime = sIn.readUTF();

            CharterSchedule schedule = CharterSchedule.of(
                    java.time.LocalDateTime.parse(departureDateTime),
                    java.time.LocalDateTime.parse(arrivalDateTime)
            );

            FlightRouteDTO route = createFlightRouteController.createCharterFlightRoute(
                    routeCode, departureIata, arrivalIata, schedule);

            sOut.writeUTF("OK: Flight route " + route.routeCode + " created");

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // US074 - Deactivate (delete) a flight route
    // ---------------------------------------------------------------

    private void handleListActiveRoutes() throws IOException {
        try {
            Iterable<FlightRouteDTO> routes = deactivateFlightRouteController.listActiveRoutes();
            sOut.writeUTF(formatRouteRows(routes));
        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    private void handleListCompanyRoutes() throws IOException {
        try {
            Iterable<FlightRouteDTO> routes = deactivateFlightRouteController.listCompanyRoutes();
            sOut.writeUTF(formatRouteRows(routes));
        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    private void handleDeactivateRoute() throws IOException {
        try {
            String routeCode = sIn.readUTF();
            String deactivationDate = sIn.readUTF();

            FlightRouteDTO route = deactivateFlightRouteController.deactivateRoute(
                    routeCode, LocalDate.parse(deactivationDate));

            sOut.writeUTF("OK: Flight route " + route.routeCode + " deactivated from " + deactivationDate);

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    private String formatRouteRows(Iterable<FlightRouteDTO> routes) {
        int count = 0;
        StringBuilder rows = new StringBuilder();

        for (FlightRouteDTO route : routes) {
            count++;
            rows.append(String.join("|",
                    route.routeCode,
                    route.departureAirportIata,
                    route.arrivalAirportIata,
                    route.flightType,
                    route.routeStatus,
                    route.deactivationDate == null ? "" : route.deactivationDate,
                    route.schedule == null ? "" : route.schedule.replace("|", "/")
            )).append("\n");
        }

        return "OK\n" + count + "\n" + rows;
    }

    // ---------------------------------------------------------------
    // US075 - Add a pilot
    // ---------------------------------------------------------------

    private void handleListPilotAircraftModels() throws IOException {
        try {
            Iterable<AircraftModelDTO> models = addPilotController.listAircraftModels();

            int count = 0;
            StringBuilder rows = new StringBuilder();
            for (AircraftModelDTO model : models) {
                count++;
                rows.append(String.join("|",
                        model.modelID,
                        model.name,
                        model.makerName,
                        model.type
                )).append("\n");
            }

            sOut.writeUTF("OK\n" + count + "\n" + rows);

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    private void handleAddPilot() throws IOException {
        try {
            String pilotUsername = sIn.readUTF();
            String password = sIn.readUTF();
            String firstName = sIn.readUTF();
            String lastName = sIn.readUTF();
            String email = sIn.readUTF();
            int countryCode = sIn.readInt();
            String phoneNumber = sIn.readUTF();
            String companyIataCode = addPilotController.currentCompanyIataCode();

            int certCount = sIn.readInt();
            List<String> modelIDs = new ArrayList<>();
            List<Integer> hoursList = new ArrayList<>();
            List<LocalDate> issuedDates = new ArrayList<>();
            List<LocalDate> validDates = new ArrayList<>();

            for (int i = 0; i < certCount; i++) {
                modelIDs.add(sIn.readUTF());
                hoursList.add(sIn.readInt());
                issuedDates.add(LocalDate.parse(sIn.readUTF()));
                validDates.add(LocalDate.parse(sIn.readUTF()));
            }

            PilotDTO pilot = addPilotController.addPilot(
                    pilotUsername, password, firstName, lastName, email,
                    countryCode, phoneNumber, companyIataCode,
                    modelIDs, hoursList, issuedDates, validDates);

            sOut.writeUTF("OK: Pilot " + pilot.pilotID + " (" + pilot.name + ") added");

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // US076 - List pilot roster
    // ---------------------------------------------------------------

    private void handleListPilotRoster() throws IOException {
        try {
            List<PilotDTO> roster = listPilotRosterController.listPilotRoster();
            sOut.writeUTF(formatPilotRows(roster));
        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------
    // US077 - Remove a pilot
    // ---------------------------------------------------------------

    private void handleListActivePilots() throws IOException {
        try {
            List<PilotDTO> pilots = removePilotController.getActivePilots();
            sOut.writeUTF(formatPilotRows(pilots));
        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    private void handleRemovePilot() throws IOException {
        try {
            String pilotID = sIn.readUTF();

            PilotDTO pilot = removePilotController.removePilot(pilotID);

            sOut.writeUTF("OK: Pilot " + pilot.pilotID + " (" + pilot.name + ") deactivated");

        } catch (Exception e) {
            sOut.writeUTF("ERROR: " + e.getMessage());
        }
    }

    private String formatPilotRows(Iterable<PilotDTO> pilots) {
        int count = 0;
        StringBuilder rows = new StringBuilder();

        for (PilotDTO pilot : pilots) {
            count++;
            rows.append(String.join("|",
                    pilot.pilotID,
                    pilot.name,
                    pilot.companyIata,
                    pilot.status,
                    String.valueOf(pilot.certificationCount)
            )).append("\n");
        }

        return "OK\n" + count + "\n" + rows;
    }
}