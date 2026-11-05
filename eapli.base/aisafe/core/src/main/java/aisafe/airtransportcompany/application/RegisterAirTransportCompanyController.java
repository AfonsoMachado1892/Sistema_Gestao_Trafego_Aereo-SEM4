package aisafe.airtransportcompany.application;

import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.airtransportcompany.domain.AirTransportCompanyRepository;
import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.application.UseCaseController;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.AuthzRegistry;

@UseCaseController
public class RegisterAirTransportCompanyController {

    private final AuthorizationService authz;
    private final AirTransportCompanyRepository repository;

    /** Production constructor — uses the global registry. */
    public RegisterAirTransportCompanyController(
            final AirTransportCompanyRepository repository) {
        this(AuthzRegistry.authorizationService(), repository);
    }

    /** Package-private constructor for tests — allows injecting stubs. */
    RegisterAirTransportCompanyController(
            final AuthorizationService authz,
            final AirTransportCompanyRepository repository) {
        this.authz = authz;
        this.repository = repository;
    }

    public AirTransportCompany registerAirTransportCompany(
            final String iataCode,
            final String icaoCode,
            final String name) {

        authz.ensureAuthenticatedUserHasAnyOf(
                AiSafeRoles.BACKOFFICE_OPERATOR,
                AiSafeRoles.ADMIN);

        final String normalizedIata = iataCode.trim().toUpperCase();
        final String normalizedIcao = icaoCode.trim().toUpperCase();

        final AirTransportCompanyCode code = AirTransportCompanyCode.of(normalizedIata);

        repository.findByIataCode(code).ifPresent(c -> {
            throw new IllegalStateException(
                    "Air transport company with IATA code already exists: " + normalizedIata);
        });

        repository.findByIcaoCode(normalizedIcao).ifPresent(c -> {
            throw new IllegalStateException(
                    "Air transport company with ICAO code already exists: " + normalizedIcao);
        });

        final AirTransportCompany company = new AirTransportCompany(
                code,
                normalizedIcao,
                name
        );

        return repository.save(company);
    }
}