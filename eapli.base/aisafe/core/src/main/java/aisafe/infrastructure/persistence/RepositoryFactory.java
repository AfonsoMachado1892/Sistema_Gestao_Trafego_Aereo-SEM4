package aisafe.infrastructure.persistence;

import aisafe.aircontrolarea.domain.AirControlAreaRepository;
import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.aircraft.domain.AircraftRepository;
import aisafe.airport.domain.AirportRepository;
import aisafe.aircraftmodel.domain.AircraftModelRepository;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.enginemodel.domain.EngineModelRepository;
import aisafe.flight.domain.Flight;
import aisafe.flight.domain.FlightRepository;
import aisafe.flightplan.domain.FlightPlanRepository;
import aisafe.flightroute.domain.FlightRouteRepository;
import aisafe.maker.domain.MakerRepository;
import aisafe.pilot.domain.PilotRepository;
import aisafe.usermanagement.domain.AiSafeUserRepository;
import aisafe.weather.domain.WeatherData;
import aisafe.weather.domain.WeatherDataRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import aisafe.airtransportcompany.domain.AirTransportCompanyRepository;

public interface RepositoryFactory {

    /**
     * factory method to create a transactional context to use in the repositories
     *
     * @return
     */
    TransactionalContext newTransactionalContext();

    /**
     * @param autoTx the transactional context to enrol
     * @return
     */
    UserRepository users(TransactionalContext autoTx);

    /**
     * repository will be created in auto transaction mode
     *
     * @return
     */
    UserRepository users();

    /**
     * repository will be created in auto transaction mode
     *
     * @return the maker repository
     */
    MakerRepository makers();

    /**
     * repository will be created in auto transaction mode
     *
     * @return the engine model repository
     */
    EngineModelRepository engineModels();

    AiSafeUserRepository aiSafeUsers();

    /**
     * repository will be created in auto transaction mode
     *
     * @return the aircraft model repository
     */
    AircraftModelRepository aircraftModels();

    AirControlAreaRepository airControlAreas();

    AirportRepository airports();

    AirTransportCompanyRepository airTransportCompanies();

    CustomerCollaboratorRepository customerCollaborators();

    AircraftRepository aircrafts();

    WeatherDataRepository weatherData();

    FlightRepository flights();

    FlightRouteRepository flightRoutes();

    FlightPlanRepository flightPlans();

    PilotRepository pilots();
}