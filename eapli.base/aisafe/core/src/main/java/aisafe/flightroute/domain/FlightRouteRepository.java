package aisafe.flightroute.domain;

import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import eapli.framework.infrastructure.repositories.Repository;

import java.util.Optional;

public interface FlightRouteRepository
        extends Repository<FlightRoute, RouteCode> {

    Optional<FlightRoute> findByRouteCode(RouteCode routeCode);

    Iterable<FlightRoute> findByCompany(AirTransportCompanyCode companyIataCode);

    Iterable<FlightRoute> findActiveByCompany(AirTransportCompanyCode companyIataCode);
}
