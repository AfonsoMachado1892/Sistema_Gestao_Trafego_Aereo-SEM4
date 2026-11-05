package aisafe.airport.application;

import aisafe.aircontrolarea.domain.AirControlArea;
import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.aircontrolarea.domain.AirControlAreaRepository;
import aisafe.airport.domain.*;
import aisafe.sharedkernel.Coordinates;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class RegisterAirportController {

    private final AuthorizationService authz = AuthzRegistry.authorizationService();
    private final AirportService airportService;
    private final AirControlAreaRepository airControlAreaRepository;
    private final AirportRepository airportRepository;

    public RegisterAirportController(
            final AirportRepository airportRepository,
            final AirControlAreaRepository airControlAreaRepository) {
        this.airportService = new AirportService(airportRepository, airControlAreaRepository);
        this.airControlAreaRepository = airControlAreaRepository;
        this.airportRepository = airportRepository;
    }

    public Airport registerAirport(
            final String iataCode,
            final String icaoCode,
            final String name,
            final String cityName,
            final String countryIsoCode,
            final String countryName,
            final double latitude,
            final double longitude,
            final double elevationMeters,
            final String airControlAreaCode) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);

        final Country country = Country.of(IsoCode.of(countryIsoCode), countryName);

        final AirControlArea airControlArea = airControlAreaRepository
                .findByCode(AirControlAreaCode.of(airControlAreaCode))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Air control area not found."));

        final Locality locality = Locality.of(cityName, country);

        return airportService.registerAirport(
                AirportIataCode.of(iataCode),
                AirportIcaoCode.of(icaoCode),
                locality,
                Coordinates.of(latitude, longitude),
                AirportName.of(name),
                Elevation.of(elevationMeters),
                airControlArea);
    }

    public Iterable<Airport> listAirports() {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);

        return airportService.listAirports();
    }

    public boolean iataCodeExists(final String iataCode) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);

        return airportRepository
                .findByIataCode(AirportIataCode.of(iataCode))
                .isPresent();
    }

    public boolean icaoCodeExists(final String icaoCode) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);

        return airportRepository
                .findByIcaoCode(AirportIcaoCode.of(icaoCode))
                .isPresent();
    }

    public Iterable<AirControlArea> listAreasContaining(
            final double latitude, final double longitude) {
        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);

        return airControlAreaRepository
                .findAreasContaining(Coordinates.of(latitude, longitude));
    }
}