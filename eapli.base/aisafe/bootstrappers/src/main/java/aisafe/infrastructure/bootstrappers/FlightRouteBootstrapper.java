package aisafe.infrastructure.bootstrappers;

import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.flightroute.domain.FlightRouteService;
import aisafe.flightroute.domain.RegularSchedule;
import aisafe.flightroute.domain.ScheduleEntry;
import aisafe.persistence.PersistenceContext;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class FlightRouteBootstrapper implements Action {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(FlightRouteBootstrapper.class);

    private final FlightRouteService service = new FlightRouteService(
            PersistenceContext.repositories().flightRoutes(),
            PersistenceContext.repositories().airports(),
            PersistenceContext.repositories().flightPlans()
    );

    @Override
    public boolean execute() {
        /*
         * IMPORTANT:
         * These routes assume the referenced airports exist in AirportBootstrapper.
         * If an airport is missing, the route is skipped and logged.
         */

        // ─────────────────────────────────────────────────────────────────────
        // Portugal domestic / islands
        // ─────────────────────────────────────────────────────────────────────

        registerRegular(
                "TP101",
                "LIS",
                "OPO",
                "TP",
                schedule(
                        entry(DayOfWeek.MONDAY, 9, 0),
                        entry(DayOfWeek.WEDNESDAY, 9, 0),
                        entry(DayOfWeek.FRIDAY, 9, 0)
                )
        );

        registerRegular(
                "TP102",
                "OPO",
                "LIS",
                "TP",
                schedule(
                        entry(DayOfWeek.MONDAY, 17, 30),
                        entry(DayOfWeek.WEDNESDAY, 17, 30),
                        entry(DayOfWeek.FRIDAY, 17, 30)
                )
        );

        registerRegular(
                "TP123",
                "OPO",
                "LIS",
                "TP",
                schedule(
                        entry(DayOfWeek.WEDNESDAY, 10, 30)
                )
        );

        registerRegular(
                "TP202",
                "OPO",
                "FNC",
                "TP",
                schedule(
                        entry(DayOfWeek.TUESDAY, 14, 30),
                        entry(DayOfWeek.THURSDAY, 14, 30)
                )
        );

        registerRegular(
                "TP203",
                "FNC",
                "OPO",
                "TP",
                schedule(
                        entry(DayOfWeek.WEDNESDAY, 8, 15),
                        entry(DayOfWeek.SATURDAY, 8, 15)
                )
        );

        registerRegular(
                "TP301",
                "LIS",
                "FNC",
                "TP",
                schedule(
                        entry(DayOfWeek.MONDAY, 7, 0),
                        entry(DayOfWeek.THURSDAY, 7, 0),
                        entry(DayOfWeek.SUNDAY, 7, 0)
                )
        );

        registerRegular(
                "TP302",
                "FNC",
                "LIS",
                "TP",
                schedule(
                        entry(DayOfWeek.MONDAY, 19, 45),
                        entry(DayOfWeek.THURSDAY, 19, 45),
                        entry(DayOfWeek.SUNDAY, 19, 45)
                )
        );

        // ─────────────────────────────────────────────────────────────────────
        // Iberia / nearby Europe
        // ─────────────────────────────────────────────────────────────────────

        registerRegular(
                "TP401",
                "LIS",
                "MAD",
                "TP",
                schedule(
                        entry(DayOfWeek.MONDAY, 6, 45),
                        entry(DayOfWeek.WEDNESDAY, 6, 45),
                        entry(DayOfWeek.FRIDAY, 6, 45)
                )
        );

        registerRegular(
                "TP402",
                "MAD",
                "LIS",
                "TP",
                schedule(
                        entry(DayOfWeek.MONDAY, 20, 10),
                        entry(DayOfWeek.WEDNESDAY, 20, 10),
                        entry(DayOfWeek.FRIDAY, 20, 10)
                )
        );

        registerRegular(
                "TP405",
                "OPO",
                "MAD",
                "TP",
                schedule(
                        entry(DayOfWeek.TUESDAY, 11, 20),
                        entry(DayOfWeek.THURSDAY, 11, 20)
                )
        );

        registerRegular(
                "TP406",
                "MAD",
                "OPO",
                "TP",
                schedule(
                        entry(DayOfWeek.TUESDAY, 18, 50),
                        entry(DayOfWeek.THURSDAY, 18, 50)
                )
        );

        registerRegular(
                "TP411",
                "LIS",
                "BCN",
                "TP",
                schedule(
                        entry(DayOfWeek.TUESDAY, 8, 40),
                        entry(DayOfWeek.THURSDAY, 8, 40),
                        entry(DayOfWeek.SATURDAY, 8, 40)
                )
        );

        registerRegular(
                "TP412",
                "BCN",
                "LIS",
                "TP",
                schedule(
                        entry(DayOfWeek.TUESDAY, 21, 5),
                        entry(DayOfWeek.THURSDAY, 21, 5),
                        entry(DayOfWeek.SATURDAY, 21, 5)
                )
        );

        registerRegular(
                "TP421",
                "LIS",
                "LPA",
                "TP",
                schedule(
                        entry(DayOfWeek.WEDNESDAY, 12, 0),
                        entry(DayOfWeek.SUNDAY, 12, 0)
                )
        );

        registerRegular(
                "TP422",
                "LPA",
                "LIS",
                "TP",
                schedule(
                        entry(DayOfWeek.WEDNESDAY, 18, 40),
                        entry(DayOfWeek.SUNDAY, 18, 40)
                )
        );

        // ─────────────────────────────────────────────────────────────────────
        // France / Netherlands / Germany / Italy
        // ─────────────────────────────────────────────────────────────────────

        registerRegular(
                "TP501",
                "LIS",
                "ORY",
                "TP",
                schedule(
                        entry(DayOfWeek.MONDAY, 10, 10),
                        entry(DayOfWeek.WEDNESDAY, 10, 10),
                        entry(DayOfWeek.FRIDAY, 10, 10)
                )
        );

        registerRegular(
                "TP502",
                "ORY",
                "LIS",
                "TP",
                schedule(
                        entry(DayOfWeek.MONDAY, 17, 20),
                        entry(DayOfWeek.WEDNESDAY, 17, 20),
                        entry(DayOfWeek.FRIDAY, 17, 20)
                )
        );

        registerRegular(
                "TP503",
                "LIS",
                "CDG",
                "TP",
                schedule(
                        entry(DayOfWeek.TUESDAY, 9, 35),
                        entry(DayOfWeek.THURSDAY, 9, 35),
                        entry(DayOfWeek.SUNDAY, 9, 35)
                )
        );

        registerRegular(
                "TP504",
                "CDG",
                "LIS",
                "TP",
                schedule(
                        entry(DayOfWeek.TUESDAY, 19, 0),
                        entry(DayOfWeek.THURSDAY, 19, 0),
                        entry(DayOfWeek.SUNDAY, 19, 0)
                )
        );

        registerRegular(
                "TP511",
                "OPO",
                "AMS",
                "TP",
                schedule(
                        entry(DayOfWeek.MONDAY, 13, 15),
                        entry(DayOfWeek.FRIDAY, 13, 15)
                )
        );

        registerRegular(
                "TP512",
                "AMS",
                "OPO",
                "TP",
                schedule(
                        entry(DayOfWeek.MONDAY, 20, 30),
                        entry(DayOfWeek.FRIDAY, 20, 30)
                )
        );

        registerRegular(
                "TP521",
                "LIS",
                "FRA",
                "TP",
                schedule(
                        entry(DayOfWeek.MONDAY, 7, 25),
                        entry(DayOfWeek.WEDNESDAY, 7, 25),
                        entry(DayOfWeek.FRIDAY, 7, 25)
                )
        );

        registerRegular(
                "TP522",
                "FRA",
                "LIS",
                "TP",
                schedule(
                        entry(DayOfWeek.MONDAY, 18, 10),
                        entry(DayOfWeek.WEDNESDAY, 18, 10),
                        entry(DayOfWeek.FRIDAY, 18, 10)
                )
        );

        registerRegular(
                "TP523",
                "OPO",
                "MUC",
                "TP",
                schedule(
                        entry(DayOfWeek.TUESDAY, 7, 55),
                        entry(DayOfWeek.THURSDAY, 7, 55)
                )
        );

        registerRegular(
                "TP524",
                "MUC",
                "OPO",
                "TP",
                schedule(
                        entry(DayOfWeek.TUESDAY, 17, 35),
                        entry(DayOfWeek.THURSDAY, 17, 35)
                )
        );

        registerRegular(
                "TP531",
                "LIS",
                "FCO",
                "TP",
                schedule(
                        entry(DayOfWeek.TUESDAY, 11, 0),
                        entry(DayOfWeek.THURSDAY, 11, 0),
                        entry(DayOfWeek.SATURDAY, 11, 0)
                )
        );

        registerRegular(
                "TP532",
                "FCO",
                "LIS",
                "TP",
                schedule(
                        entry(DayOfWeek.TUESDAY, 19, 55),
                        entry(DayOfWeek.THURSDAY, 19, 55),
                        entry(DayOfWeek.SATURDAY, 19, 55)
                )
        );

        registerRegular(
                "TP533",
                "OPO",
                "MXP",
                "TP",
                schedule(
                        entry(DayOfWeek.WEDNESDAY, 9, 10),
                        entry(DayOfWeek.SUNDAY, 9, 10)
                )
        );

        registerRegular(
                "TP534",
                "MXP",
                "OPO",
                "TP",
                schedule(
                        entry(DayOfWeek.WEDNESDAY, 18, 25),
                        entry(DayOfWeek.SUNDAY, 18, 25)
                )
        );

        // ─────────────────────────────────────────────────────────────────────
        // UK / Transatlantic / South America / Africa
        // ─────────────────────────────────────────────────────────────────────

        registerRegular(
                "TP601",
                "LIS",
                "LHR",
                "TP",
                schedule(
                        entry(DayOfWeek.MONDAY, 8, 0),
                        entry(DayOfWeek.WEDNESDAY, 8, 0),
                        entry(DayOfWeek.FRIDAY, 8, 0),
                        entry(DayOfWeek.SUNDAY, 8, 0)
                )
        );

        registerRegular(
                "TP602",
                "LHR",
                "LIS",
                "TP",
                schedule(
                        entry(DayOfWeek.MONDAY, 21, 30),
                        entry(DayOfWeek.WEDNESDAY, 21, 30),
                        entry(DayOfWeek.FRIDAY, 21, 30),
                        entry(DayOfWeek.SUNDAY, 21, 30)
                )
        );

        registerRegular(
                "TP701",
                "LIS",
                "JFK",
                "TP",
                schedule(
                        entry(DayOfWeek.MONDAY, 11, 0),
                        entry(DayOfWeek.THURSDAY, 11, 0),
                        entry(DayOfWeek.SATURDAY, 11, 0)
                )
        );

        registerRegular(
                "TP702",
                "JFK",
                "LIS",
                "TP",
                schedule(
                        entry(DayOfWeek.MONDAY, 23, 30),
                        entry(DayOfWeek.THURSDAY, 23, 30),
                        entry(DayOfWeek.SATURDAY, 23, 30)
                )
        );

        registerRegular(
                "TP703",
                "OPO",
                "EWR",
                "TP",
                schedule(
                        entry(DayOfWeek.TUESDAY, 12, 15),
                        entry(DayOfWeek.FRIDAY, 12, 15)
                )
        );

        registerRegular(
                "TP704",
                "EWR",
                "OPO",
                "TP",
                schedule(
                        entry(DayOfWeek.TUESDAY, 23, 45),
                        entry(DayOfWeek.FRIDAY, 23, 45)
                )
        );

        registerRegular(
                "TP801",
                "LIS",
                "GRU",
                "TP",
                schedule(
                        entry(DayOfWeek.WEDNESDAY, 10, 30),
                        entry(DayOfWeek.SATURDAY, 10, 30)
                )
        );

        registerRegular(
                "TP802",
                "GRU",
                "LIS",
                "TP",
                schedule(
                        entry(DayOfWeek.WEDNESDAY, 22, 10),
                        entry(DayOfWeek.SATURDAY, 22, 10)
                )
        );

        registerRegular(
                "TP803",
                "LIS",
                "GIG",
                "TP",
                schedule(
                        entry(DayOfWeek.TUESDAY, 10, 0),
                        entry(DayOfWeek.FRIDAY, 10, 0)
                )
        );

        registerRegular(
                "TP804",
                "GIG",
                "LIS",
                "TP",
                schedule(
                        entry(DayOfWeek.TUESDAY, 22, 40),
                        entry(DayOfWeek.FRIDAY, 22, 40)
                )
        );

        registerRegular(
                "TP901",
                "LIS",
                "SID",
                "TP",
                schedule(
                        entry(DayOfWeek.MONDAY, 13, 0),
                        entry(DayOfWeek.THURSDAY, 13, 0)
                )
        );

        registerRegular(
                "TP902",
                "SID",
                "LIS",
                "TP",
                schedule(
                        entry(DayOfWeek.MONDAY, 20, 40),
                        entry(DayOfWeek.THURSDAY, 20, 40)
                )
        );

        return true;
    }

    private RegularSchedule schedule(final ScheduleEntry... entries) {
        return RegularSchedule.of(List.of(entries));
    }

    private ScheduleEntry entry(final DayOfWeek dayOfWeek,
                                final int hour,
                                final int minute) {
        return ScheduleEntry.of(dayOfWeek, LocalTime.of(hour, minute));
    }

    private void registerRegular(final String routeCode,
                                 final String departureIata,
                                 final String arrivalIata,
                                 final String companyIata,
                                 final RegularSchedule schedule) {
        try {
            service.createRegularFlightRoute(
                    routeCode,
                    departureIata,
                    arrivalIata,
                    AirTransportCompanyCode.of(companyIata),
                    schedule
            );

            LOGGER.debug("»»» Regular FlightRoute registered: {}", routeCode);

        } catch (final Exception e) {
            LOGGER.warn("FlightRoute skipped [{}]: {}", routeCode, e.getMessage());
        }
    }
}