package aisafe.persistence.impl.inmemory;

import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.flightroute.domain.FlightRoute;
import aisafe.flightroute.domain.FlightRouteRepository;
import aisafe.flightroute.domain.RouteCode;
import eapli.framework.infrastructure.repositories.impl.inmemory.InMemoryDomainRepository;

import java.util.Optional;
import java.util.stream.StreamSupport;

public class InMemoryFlightRouteRepository
        extends InMemoryDomainRepository<FlightRoute, RouteCode>
        implements FlightRouteRepository {

    @Override
    public Optional<FlightRoute> findByRouteCode(final RouteCode routeCode) {
        return matchOne(r -> r.routeCode().equals(routeCode));
    }

    @Override
    public Iterable<FlightRoute> findByCompany(final AirTransportCompanyCode companyIataCode) {
        return match(r -> r.airTransportCompanyIataCode().equals(companyIataCode));
    }

    @Override
    public Iterable<FlightRoute> findActiveByCompany(final AirTransportCompanyCode companyIataCode) {
        return match(r -> r.airTransportCompanyIataCode().equals(companyIataCode) && r.isActive());
    }
}
