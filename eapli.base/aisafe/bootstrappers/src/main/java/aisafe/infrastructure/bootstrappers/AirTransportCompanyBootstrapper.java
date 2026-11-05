package aisafe.infrastructure.bootstrappers;

import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.airtransportcompany.domain.AirTransportCompanyRepository;
import aisafe.persistence.PersistenceContext;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AirTransportCompanyBootstrapper implements Action {

    private static final Logger LOGGER = LoggerFactory.getLogger(AirTransportCompanyBootstrapper.class);

    private final AirTransportCompanyRepository repository =
            PersistenceContext.repositories().airTransportCompanies();

    @Override
    public boolean execute() {
        register("TP", "TAP", "TAP Air Portugal");
        register("FR", "RYR", "Ryanair");
        register("LH", "DLH", "Lufthansa");

        return true;
    }

    private void register(final String iataCode,
                          final String icaoCode,
                          final String name) {
        try {
            final String normalizedIata = iataCode.trim().toUpperCase();
            final String normalizedIcao = icaoCode.trim().toUpperCase();

            final AirTransportCompanyCode code = AirTransportCompanyCode.of(normalizedIata);

            if (repository.findByIataCode(code).isPresent()) {
                LOGGER.debug("»»» AirTransportCompany already exists with IATA: {}", normalizedIata);
                return;
            }

            if (repository.findByIcaoCode(normalizedIcao).isPresent()) {
                LOGGER.debug("»»» AirTransportCompany already exists with ICAO: {}", normalizedIcao);
                return;
            }

            final AirTransportCompany company = new AirTransportCompany(
                    code,
                    normalizedIcao,
                    name
            );

            repository.save(company);

            LOGGER.debug("»»» AirTransportCompany: {} - {} - {}", normalizedIata, normalizedIcao, name);
        } catch (final Exception e) {
            LOGGER.warn("AirTransportCompany skipped: {} - {}", iataCode, e.getMessage());
        }
    }
}