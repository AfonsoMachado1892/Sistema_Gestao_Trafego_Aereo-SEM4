package aisafe.infrastructure.bootstrappers;

import aisafe.aircraft.application.AircraftService;
import aisafe.aircraft.domain.AircraftRepository;
import aisafe.aircraft.domain.CabinConfiguration;
import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.aircraftmodel.domain.AircraftModelID;
import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.persistence.PersistenceContext;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:S106")
public class AircraftBootstrapper implements Action {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AircraftBootstrapper.class);

    private final AircraftRepository aircraftRepository =
            PersistenceContext.repositories().aircrafts();

    private final AircraftService service =
            new AircraftService(aircraftRepository);

    @Override
    public boolean execute() {
        /*
         * IMPORTANT:
         * These aircraft models must exist in AircraftModelBootstrapper.
         * Company TP must exist in AirTransportCompanyBootstrapper.
         */

        // ─────────────────────────────────────────────────────────────────────
        // TAP-style narrow-body fleet
        // ─────────────────────────────────────────────────────────────────────

        register(
                "CS-TAA",
                "A319",
                "TP",
                CabinConfiguration.of(120, 24, 0),
                "PT",
                "Portugal",
                6
        );

        register(
                "CS-TAB",
                "777-200ER",
                "TP",
                CabinConfiguration.of(180, 40, 12),
                "PT",
                "Portugal",
                10
        );

        register(
                "CS-TAC",
                "777-200ER",
                "TP",
                CabinConfiguration.of(180, 36, 8),
                "PT",
                "Portugal",
                10
        );

        register(
                "CS-TAD",
                "A320",
                "TP",
                CabinConfiguration.of(156, 24, 0),
                "PT",
                "Portugal",
                6
        );

        register(
                "CS-TAE",
                "A320",
                "TP",
                CabinConfiguration.of(162, 18, 0),
                "PT",
                "Portugal",
                6
        );

        register(
                "CS-TAF",
                "A320neo",
                "TP",
                CabinConfiguration.of(168, 18, 0),
                "PT",
                "Portugal",
                6
        );

        register(
                "CS-TAG",
                "A320neo",
                "TP",
                CabinConfiguration.of(174, 12, 0),
                "PT",
                "Portugal",
                6
        );

        register(
                "CS-TAH",
                "A321",
                "TP",
                CabinConfiguration.of(190, 24, 0),
                "PT",
                "Portugal",
                7
        );

        register(
                "CS-TAI",
                "A321neo",
                "TP",
                CabinConfiguration.of(210, 16, 0),
                "PT",
                "Portugal",
                7
        );

        register(
                "CS-TAJ",
                "A321neo",
                "TP",
                CabinConfiguration.of(220, 12, 0),
                "PT",
                "Portugal",
                7
        );

        // ─────────────────────────────────────────────────────────────────────
        // Medium / long-haul fleet
        // ─────────────────────────────────────────────────────────────────────

        register(
                "CS-TBA",
                "A330-200",
                "TP",
                CabinConfiguration.of(220, 24, 0),
                "PT",
                "Portugal",
                9
        );

        register(
                "CS-TBB",
                "A330-300",
                "TP",
                CabinConfiguration.of(250, 27, 0),
                "PT",
                "Portugal",
                9
        );

        register(
                "CS-TBC",
                "A330-900neo",
                "TP",
                CabinConfiguration.of(260, 34, 0),
                "PT",
                "Portugal",
                10
        );

        register(
                "CS-TBD",
                "A330-900neo",
                "TP",
                CabinConfiguration.of(265, 34, 0),
                "PT",
                "Portugal",
                10
        );

        register(
                "CS-TBE",
                "A350-900",
                "TP",
                CabinConfiguration.of(265, 38, 0),
                "PT",
                "Portugal",
                10
        );

        register(
                "CS-TBF",
                "A350-1000",
                "TP",
                CabinConfiguration.of(315, 44, 0),
                "PT",
                "Portugal",
                11
        );

        register(
                "CS-TBG",
                "787-8",
                "TP",
                CabinConfiguration.of(214, 28, 0),
                "PT",
                "Portugal",
                9
        );

        register(
                "CS-TBH",
                "787-9",
                "TP",
                CabinConfiguration.of(255, 30, 0),
                "PT",
                "Portugal",
                10
        );

        register(
                "CS-TBI",
                "777-300ER",
                "TP",
                CabinConfiguration.of(330, 42, 8),
                "PT",
                "Portugal",
                12
        );

        register(
                "CS-TBJ",
                "A380",
                "TP",
                CabinConfiguration.of(430, 76, 14),
                "PT",
                "Portugal",
                18
        );

        // ─────────────────────────────────────────────────────────────────────
        // Partner / mixed fleet examples under TP for testing
        // ─────────────────────────────────────────────────────────────────────

        register(
                "CS-TCA",
                "737-800",
                "TP",
                CabinConfiguration.of(174, 12, 0),
                "PT",
                "Portugal",
                6
        );

        register(
                "CS-TCB",
                "737-MAX8",
                "TP",
                CabinConfiguration.of(174, 16, 0),
                "PT",
                "Portugal",
                6
        );

        register(
                "CS-TCC",
                "767-300ER",
                "TP",
                CabinConfiguration.of(232, 24, 0),
                "PT",
                "Portugal",
                9
        );

        // ─────────────────────────────────────────────────────────────────────
        // Cargo / freight fleet
        // ─────────────────────────────────────────────────────────────────────

        register(
                "CS-TFA",
                "777F",
                "TP",
                CabinConfiguration.of(2, 0, 0),
                "PT",
                "Portugal",
                4
        );

        register(
                "CS-TFB",
                "A330-200F",
                "TP",
                CabinConfiguration.of(2, 0, 0),
                "PT",
                "Portugal",
                4
        );

        register(
                "CS-TFC",
                "747-8F",
                "TP",
                CabinConfiguration.of(4, 0, 0),
                "PT",
                "Portugal",
                6
        );

        return true;
    }

    private void register(final String registration,
                          final String aircraftModelID,
                          final String companyIata,
                          final CabinConfiguration cabinConfiguration,
                          final String countryIsoCode,
                          final String countryName,
                          final int numCrewElements) {
        try {
            final AircraftModel aircraftModel =
                    PersistenceContext.repositories()
                            .aircraftModels()
                            .findByModelID(AircraftModelID.of(aircraftModelID))
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Aircraft model not found: " + aircraftModelID));

            final AirTransportCompany airTransportCompany =
                    PersistenceContext.repositories()
                            .airTransportCompanies()
                            .findByIataCode(AirTransportCompanyCode.of(companyIata))
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Air transport company not found: " + companyIata));

            service.addAircraft(
                    registration,
                    aircraftModel,
                    airTransportCompany,
                    cabinConfiguration,
                    Country.of(IsoCode.of(countryIsoCode), countryName),
                    numCrewElements
            );

            LOGGER.debug("»»» Aircraft registered: {} ({})", registration, companyIata);

        } catch (final Exception e) {
            LOGGER.warn("Aircraft skipped [{}]: {}", registration, e.getMessage());
        }
    }
}