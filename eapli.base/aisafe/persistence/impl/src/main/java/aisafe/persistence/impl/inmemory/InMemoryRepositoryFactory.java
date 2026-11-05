package aisafe.persistence.impl.inmemory;

import aisafe.aircontrolarea.domain.AirControlAreaRepository;
import aisafe.airport.domain.AirportRepository;
import aisafe.aircraftmodel.domain.AircraftModelRepository;
import aisafe.enginemodel.domain.EngineModelRepository;
import aisafe.flight.domain.FlightRepository;
import aisafe.flightplan.domain.FlightPlanRepository;
import aisafe.flightroute.domain.FlightRouteRepository;
import aisafe.infrastructure.persistence.RepositoryFactory;
import aisafe.pilot.domain.PilotRepository;
import aisafe.maker.domain.MakerRepository;
import aisafe.usermanagement.domain.AiSafeUserRepository;
import aisafe.weather.domain.WeatherDataRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.infrastructure.authz.repositories.impl.inmemory.InMemoryUserRepository;
import aisafe.airtransportcompany.domain.AirTransportCompanyRepository;

public class InMemoryRepositoryFactory implements RepositoryFactory {

    @Override
    public UserRepository users(final TransactionalContext tx) {
        return new InMemoryUserRepository();
    }

    @Override
    public UserRepository users() {
        return users(null);
    }

    @Override
    public TransactionalContext newTransactionalContext() {
        return null;
    }

    @Override
    public MakerRepository makers() {
        return new InMemoryMakerRepository();
    }

    @Override
    public EngineModelRepository engineModels() {
        return new InMemoryEngineModelRepository();
    }

    @Override
    public AiSafeUserRepository aiSafeUsers() {
        return new InMemoryAiSafeUserRepository();

    }
    public AircraftModelRepository aircraftModels() {
        return new InMemoryAircraftModelRepository();
    }

    @Override
    public AirControlAreaRepository airControlAreas() {
        return new InMemoryAirControlAreaRepository();
    }

    @Override
    public AirportRepository airports() {
        return new InMemoryAirportRepository();
    }

    @Override
    public AirTransportCompanyRepository airTransportCompanies() {
        return new InMemoryAirTransportCompanyRepository();
    }

    @Override
    public InMemoryCustomerCollaboratorRepository customerCollaborators() {
        return new InMemoryCustomerCollaboratorRepository();
    }

    @Override
    public InMemoryAircraftRepository aircrafts(){
        return new InMemoryAircraftRepository();
    }

    @Override
    public WeatherDataRepository weatherData() {
        return new InMemoryWeatherDataRepository();
    }

    @Override
    public FlightRepository flights() {
        return new InMemoryFlightRepository();
    }

    @Override
    public FlightRouteRepository flightRoutes() {
        return new InMemoryFlightRouteRepository();
    }

    @Override
    public FlightPlanRepository flightPlans() { return new InMemoryFlightPlanRepository(); }

    @Override
    public PilotRepository pilots() {
        return new InMemoryPilotRepository();
    }
}
