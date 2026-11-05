package aisafe.persistence.impl.jpa;

import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.flightroute.domain.FlightRoute;
import aisafe.flightroute.domain.FlightRouteRepository;
import aisafe.flightroute.domain.RouteCode;

import java.util.Map;
import java.util.Optional;

public class JpaFlightRouteRepository
        extends AiSafeJpaRepositoryBase<FlightRoute, RouteCode, RouteCode>
        implements FlightRouteRepository {

    public JpaFlightRouteRepository(final String persistenceUnitName,
                                    final Map extendedProperties) {
        super(persistenceUnitName, extendedProperties, "routeCode");
    }

    @Override
    public Optional<FlightRoute> findByRouteCode(final RouteCode routeCode) {
        return entityManager()
                .createQuery(
                        "SELECT r FROM FlightRoute r WHERE r.routeCode = :routeCode",
                        FlightRoute.class)
                .setParameter("routeCode", routeCode)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public Iterable<FlightRoute> findByCompany(final AirTransportCompanyCode companyIataCode) {
        return entityManager()
                .createQuery(
                        "SELECT r FROM FlightRoute r WHERE r.airTransportCompanyIataCode = :code",
                        FlightRoute.class)
                .setParameter("code", companyIataCode)
                .getResultList();
    }

    @Override
    public Iterable<FlightRoute> findActiveByCompany(final AirTransportCompanyCode companyIataCode) {
        return entityManager()
                .createQuery(
                        "SELECT r FROM FlightRoute r "
                                + "WHERE r.airTransportCompanyIataCode = :code "
                                + "AND r.routeStatus = 'ACTIVE'",
                        FlightRoute.class)
                .setParameter("code", companyIataCode)
                .getResultList();
    }
}
