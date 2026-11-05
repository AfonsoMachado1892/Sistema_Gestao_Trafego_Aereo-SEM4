package aisafe.persistence;

import aisafe.Application;
import aisafe.aircontrolarea.domain.AirControlAreaRepository;
import aisafe.aircraft.domain.AircraftRepository;
import aisafe.aircraftmodel.domain.AircraftModelRepository;
import aisafe.airport.domain.AirportRepository;
import aisafe.airtransportcompany.domain.AirTransportCompanyRepository;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.enginemodel.domain.EngineModelRepository;
import aisafe.flight.domain.FlightRepository;
import aisafe.flightplan.domain.FlightPlanRepository;
import aisafe.flightroute.domain.FlightRouteRepository;
import aisafe.infrastructure.persistence.RepositoryFactory;
import aisafe.maker.domain.MakerRepository;
import aisafe.persistence.impl.jpa.*;
import aisafe.pilot.domain.PilotRepository;
import aisafe.usermanagement.domain.AiSafeUserRepository;
import aisafe.weather.domain.WeatherDataRepository;
import eapli.framework.domain.repositories.TransactionalContext;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import eapli.framework.infrastructure.authz.repositories.impl.jpa.JpaAutoTxUserRepository;
import eapli.framework.infrastructure.repositories.impl.jpa.JpaAutoTxRepository;

import java.util.Map;

public class JpaRepositoryFactory implements RepositoryFactory {

    private final String persistenceUnit =
            Application.settings().getPersistenceUnitName();

    private final Map extendedProperties =
            Application.settings().getExtendedPersistenceProperties();

    @Override
    public TransactionalContext newTransactionalContext() {
        return JpaAutoTxRepository.buildTransactionalContext(
                persistenceUnit,
                extendedProperties
        );
    }

    @Override
    public UserRepository users(final TransactionalContext autoTx) {
        return new JpaAutoTxUserRepository(autoTx);
    }

    @Override
    public UserRepository users() {
        return new JpaAutoTxUserRepository(persistenceUnit, extendedProperties);
    }

    @Override
    public AirportRepository airports() {
        return new JpaAirportRepository(persistenceUnit, extendedProperties);
    }


    @Override
    public AirControlAreaRepository airControlAreas() {
        return new JpaAirControlAreaRepository(persistenceUnit, extendedProperties);
    }

    @Override
    public AirTransportCompanyRepository airTransportCompanies() {
        return new JpaAirTransportCompanyRepository(persistenceUnit, extendedProperties);
    }

    @Override
    public CustomerCollaboratorRepository customerCollaborators() {
        return new JpaCustomerCollaboratorRepository(persistenceUnit, extendedProperties);
    }

    @Override
    public AircraftRepository aircrafts() {
        return new JpaAircraftRepository(persistenceUnit, extendedProperties);
    }

    @Override
    public AircraftModelRepository aircraftModels() {
        return new JpaAircraftModelRepository(persistenceUnit, extendedProperties);
    }

    @Override
    public EngineModelRepository engineModels() {
        return new JpaEngineModelRepository(persistenceUnit, extendedProperties);
    }

    @Override
    public MakerRepository makers() {
        return new JpaMakerRepository(persistenceUnit, extendedProperties);
    }

    @Override
    public AiSafeUserRepository aiSafeUsers() {
        return new JpaAiSafeUserRepository(persistenceUnit, extendedProperties);
    }

    @Override
    public WeatherDataRepository weatherData() {
        return new JpaWeatherDataRepository(persistenceUnit, extendedProperties);
    }

    @Override
    public FlightRepository flights() {
        return new JpaFlightRepository(persistenceUnit, extendedProperties);
    }

    @Override
    public FlightRouteRepository flightRoutes() {
        return new JpaFlightRouteRepository(persistenceUnit, extendedProperties);
    }

    @Override
    public FlightPlanRepository flightPlans() {
        return new JpaFlightPlanRepository(persistenceUnit, extendedProperties);
    }

    @Override
    public PilotRepository pilots() {
        return new JpaPilotRepository(persistenceUnit, extendedProperties);
    }
}