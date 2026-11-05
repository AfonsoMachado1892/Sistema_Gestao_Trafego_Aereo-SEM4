package aisafe.infrastructure.bootstrappers;

import aisafe.aircontrolarea.domain.AirControlArea;
import aisafe.aircontrolarea.domain.AirControlAreaCode;
import aisafe.airport.application.AirportService;
import aisafe.airport.domain.*;
import aisafe.persistence.PersistenceContext;
import aisafe.sharedkernel.Coordinates;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("squid:S106")
public class AirportBootstrapper implements Action {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AirportBootstrapper.class);

    private final AirportRepository airportRepository =
            PersistenceContext.repositories().airports();

    private final AirportService service =
            new AirportService(
                    PersistenceContext.repositories().airports(),
                    PersistenceContext.repositories().airControlAreas()
            );

    @Override
    public boolean execute() {
        register("OPO", "LPPR", "Francisco de Sá Carneiro Airport", "Porto", "PT", "Portugal",
                41.2481003, -8.6813898, 69, "LPPC");

        register("LIS", "LPPT", "Humberto Delgado Airport", "Lisbon", "PT", "Portugal",
                38.7812996, -9.1359196, 114, "LPPC");

        register("PDL", "LPPD", "Joao Paulo II Airport", "Ponta Delgada", "PT", "Portugal",
                37.7411995, -25.6979008, 79, "LPPC");

        register("FNC", "LPMA", "Madeira Airport", "Funchal", "PT", "Portugal",
                32.6978989, -16.7744999, 59, "LPPC");

        register("MAD", "LEMD", "Adolfo Suárez Madrid Barajas International Airport", "Madrid", "ES", "Spain",
                40.4936, -3.56676, 610, "LECM");

        register("PMI", "LEPA", "Palma de Mallorca Airport", "Palma de Mallorca", "ES", "Spain",
                39.5517006, 2.7388101, 7, "LECM");

        register("LPA", "GCLP", "Gran Canaria Airport", "Las Palmas", "ES", "Spain",
                27.9319, -15.3865995, 23, "GCCC");

        register("CDG", "LFPG", "Charles de Gaulle International Airport", "Paris", "FR", "France",
                49.0127983, 2.55, 119, "LFFF");

        register("FRA", "EDDF", "Frankfurt am Main International Airport", "Frankfurt am Main", "DE", "Germany",
                50.0264015, 8.5431299, 111, "EDGG");

        register("BER", "EDDB", "Berlin Brandenburg Airport", "Berlin", "DE", "Germany",
                52.5166667, 13.4, 47, "EDGG");

        register("LHR", "EGLL", "London Heathrow Airport", "London", "GB", "United Kingdom",
                51.4706001, -0.461941, 25, "LFFF");

        register("GLA", "EGPF", "Glasgow International Airport", "Glasgow", "GB", "United Kingdom",
                55.8718987, -4.4330602, 8, "EGPX");

        register("SNN", "EINN", "Shannon Airport", "Limerick", "IE", "Ireland",
                52.7019997, -8.9248199, 14, "EGPX");

        register("KEF", "BIKF", "Keflavik International Airport", "Reykjavík", "IS", "Iceland",
                63.9850006, -22.6056004, 52, "EGPX");

        register("TRD", "ENVA", "Trondheim, Vaernes Airport", "Trondheim", "NO", "Norway",
                63.4578018, 10.9239998, 17, "ENOR");

        register("AMS", "EHAM", "Amsterdam Schiphol Airport", "Haarlemmermeer", "NL", "Netherlands",
                52.3086014, 4.7638898, -3, "EHAA");

        register("ZRH", "LSZH", "Zurich Airport", "Zurich", "CH", "Switzerland",
                47.4646988, 8.5491695, 432, "EDGG");

        register("MXP", "LIMC", "Malpensa International Airport", "Milano", "IT", "Italy",
                45.6306, 8.7281103, 234, "LIMM");

        register("FCO", "LIRF", "Leonardo Da Vinci (Fiumicino) International Airport", "Rome", "IT", "Italy",
                41.8044444, 12.2508333, 5, "LIMM");

        register("JFK", "KJFK", "John F Kennedy International Airport", "New York", "US", "United States of America",
                40.639801, -73.7789002, 4, "KZNY");

        register("ATL", "KATL", "Hartsfield Jackson Atlanta International Airport", "Atlanta", "US", "United States of America",
                33.6366997, -84.4281006, 313, "KZNY");

        register("IAD", "KIAD", "Washington Dulles International Airport", "Washington DC", "US", "United States of America",
                38.9445, -77.4558029, 95, "KZNY");

        register("ORD", "KORD", "Chicago O'Hare International Airport", "Chicago", "US", "United States of America",
                41.9785996, -87.9048004, 204, "KZKC");

        register("LAX", "KLAX", "Los Angeles International Airport", "Los Angeles", "US", "United States of America",
                33.9425011, -118.4079971, 38, "KZLA");

        register("DFW", "KDFW", "Dallas Fort Worth International Airport", "Dallas", "US", "United States of America",
                32.896801, -97.038002, 185, "KZKC");

        register("DEN", "KDEN", "Denver International Airport", "Denver", "US", "United States of America",
                39.8616982, -104.6729965, 1655, "KZKC");

        register("SFO", "KSFO", "San Francisco International Airport", "San Francisco", "US", "United States of America",
                37.6189995, -122.375, 3.96, "KZLA");

        register("LAS", "KLAS", "McCarran International Airport", "Las Vegas", "US", "United States of America",
                36.080101, -115.1520004, 665, "KZLA");

        register("MIA", "KMIA", "Miami International", "Miami", "US", "United States of America",
                25.7931995, -80.2906036, 3, "KZNY");

        register("EWR", "KEWR", "Newark Liberty International Airport", "Newark", "US", "United States of America",
                40.6925011, -74.1687012, 5, "KZNY");

        register("HNL", "PHNL", "Honolulu International Airport", "Honolulu", "US", "United States of America",
                21.3187008, -157.9219971, 4, "KZLA");

        register("BOS", "KBOS", "General Edward Lawrence Logan International Airport", "Boston", "US", "United States of America",
                42.3642998, -71.0052033, 6.1, "KZNY");

        register("MCI", "KMCI", "Kansas City International Airport", "Kansas City", "US", "United States of America",
                39.2975998, -94.7138977, 313, "KZKC");

        register("YYC", "CYYC", "Calgary International Airport", "Calgary", "CA", "Canada",
                51.1138992, -114.0199966, 1099, "KZKC");

        register("YUL", "CYUL", "Montréal–Pierre Elliott Trudeau International Airport", "Montreal", "CA", "Canada",
                45.4706001, -73.740799, 36, "KZNY");

        register("MEX", "MMMX", "Licenciado Benito Juarez International Airport", "Ciudad de Mexico", "MX", "Mexico",
                19.4363003, -99.0720978, 2238, "MMFR");

        register("BOG", "SKBO", "El Dorado International Airport", "Bogota", "CO", "Colombia",
                4.70159, -74.1469, 2548, "SKED");

        // Dataset uses CIG, but the Rio de Janeiro Antonio Carlos Jobim/Galeão airport is usually GIG/SBGL.
        register("CIG", "SBGL", "Rio de Janeiro – Antonio Carlos Jobim International Airport", "Rio de Janeiro", "BR", "Brazil",
                -22.8099995, -43.2505569, 9, "SBAZ");

        register("REC", "SBRF", "Guararapes - Gilberto Freyre International Airport", "Recife", "BR", "Brazil",
                -8.1264896, -34.9235992, 10, "SBRE");

        register("SVO", "UUEE", "Sheremetyevo International Airport", "Moscow", "RU", "Russia",
                55.972599, 37.4146004, 192, "UUWV");

        register("ALA", "UAAA", "Almaty International Airport", "Alamaty", "KZ", "Kazakhstan",
                43.3521004, 77.0404968, 681, "UUWV");

        register("JNB", "FAOR", "OR Tambo International Airport", "Johannesburg", "ZA", "South Africa",
                -26.1392002, 28.2460003, 1694, "FAJA");

        register("FIH", "FZAA", "Ndjili International Airport", "Kinshasa", "CD", "Democratic Republic of the Congo",
                -4.3857498, 15.4446001, 313, "FAJA");

        register("MPM", "FQMA", "Maputo International Airport", "Maputo", "MZ", "Mozambique",
                -25.9207993, 32.5726013, 44, "FAJA");

        register("HND", "RJTT", "Tokyo International Airport", "Tokyo", "JP", "Japan",
                35.5522995, 139.7799988, 6, "RJTG");

        register("HKG", "VHHH", "Hong Kong International Airport", "Hong Kong", "HK", "Hong Kong",
                22.308321, 113.919649, 9, "RJTG");

        register("PEK", "ZBAA", "Beijing Capital International Airport", "Beijing", "CN", "China",
                40.080101, 116.5849991, 35, "RJTG");

        register("CTU", "ZUUU", "Chengdu Shuangliu International Airport", "Chengdu", "CN", "China",
                30.5785007, 103.9469986, 495, "RJTG");

        register("LXA", "ZULS", "Lhasa Gonggar Airport", "Lhasa", "CN", "China",
                29.2978001, 90.9119034, 3570, "RJTG");

        register("SIN", "WSSS", "Singapore Changi International Airport", "Singapore", "SG", "Singapore",
                1.35019, 103.9940033, 7, "WSJC");

        register("SYD", "YSSY", "Sydney Kingsford Smith International Airport", "Sydney", "AU", "Australia",
                -33.9460983, 151.177002, 6, "YMMM");

        register("ASP", "YBAS", "Alice Springs Airport", "Alice Springs", "AU", "Australia",
                -23.8066998, 133.9019928, 545, "YMMM");

        return true;
    }

    private void register(
            final String iataCode,
            final String icaoCode,
            final String name,
            final String cityName,
            final String countryIsoCode,
            final String countryName,
            final double latitude,
            final double longitude,
            final double elevationMeters,
            final String airControlAreaCode
    ) {
        try {
            final AirportIataCode iata = AirportIataCode.of(iataCode);
            final AirportIcaoCode icao = AirportIcaoCode.of(icaoCode);

            if (airportRepository.findByIataCode(iata).isPresent()) {
                LOGGER.debug("Airport already exists with IATA: %s%n", iataCode);
                return;
            }

            if (airportRepository.findByIcaoCode(icao).isPresent()) {
                LOGGER.debug("Airport already exists with ICAO: %s%n", icaoCode);
                return;
            }

            final AirControlArea airControlArea =
                    PersistenceContext.repositories().airControlAreas()
                            .findByCode(AirControlAreaCode.of(airControlAreaCode))
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Air control area not found: " + airControlAreaCode
                            ));

            final Country country = Country.of(IsoCode.of(countryIsoCode), countryName);

            service.registerAirport(
                    iata,
                    icao,
                    Locality.of(cityName, country),
                    Coordinates.of(latitude, longitude),
                    AirportName.of(name),
                    Elevation.of(elevationMeters),
                    airControlArea
            );

            LOGGER.debug("Airport registered: {} - {}", iataCode, name);
        } catch (final IllegalArgumentException | IllegalStateException e) {
            LOGGER.debug("Airport already exists with IATA: {}", iataCode);
        }
    }
}