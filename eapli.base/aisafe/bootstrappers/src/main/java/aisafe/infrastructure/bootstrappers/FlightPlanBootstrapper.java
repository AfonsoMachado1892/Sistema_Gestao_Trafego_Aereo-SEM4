package aisafe.infrastructure.bootstrappers;

import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.flightplan.application.FlightPlanDTO;
import aisafe.flightplan.domain.*;
import aisafe.flightroute.domain.RouteCode;
import aisafe.pilot.domain.Pilot;
import aisafe.persistence.PersistenceContext;
import eapli.framework.actions.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@SuppressWarnings("squid:S106")
public class FlightPlanBootstrapper implements Action {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(FlightPlanBootstrapper.class);

    private final FlightPlanRepository flightPlanRepository =
            PersistenceContext.repositories().flightPlans();

    private final FlightPlanService service =
            new FlightPlanService(
                    PersistenceContext.repositories().flightPlans(),
                    PersistenceContext.repositories().flightRoutes(),
                    PersistenceContext.repositories().aircrafts()
            );

    @Override
    public boolean execute() {
        /*
         * IMPORTANT:
         * These aircraft registrations must exist in your AircraftBootstrapper.
         * Replace them if your project uses different registrations.
         *
         * These departure date/times match the current FlightRouteBootstrapper:
         * TP101 -> Monday/Wednesday/Friday at 09:00
         * TP202 -> Tuesday/Thursday at 14:30
         */

        register(
                "TP101",
                "CS-TAB",
                "TP",
                LocalDateTime.of(2026, 6, 15, 9, 0),   // Monday 09:00
                LocalDateTime.of(2026, 6, 15, 11, 0),
                12000,
                FuelUnit.KG,
                """
                FLIGHT PLAN TP101

                ROUTE LIS OPO
                AIRCRAFT CS-TAB
                PLANNED DEPARTURE 2026-06-15T09:00
                PLANNED ARRIVAL 2026-06-15T11:00
                FUEL 12000 KG

                RAW DESCRIPTION CREATED BY BOOTSTRAP.
                DSL VALIDATION IS NOT PERFORMED IN US080.
                """
        );

        register(
                "TP202",
                "CS-TAC",
                "TP",
                LocalDateTime.of(2026, 6, 16, 14, 30), // Tuesday 14:30
                LocalDateTime.of(2026, 6, 16, 15, 30),
                8000,
                FuelUnit.KG,
                """
                FLIGHT PLAN TP202

                ROUTE OPO FAO
                AIRCRAFT CS-TAC
                PLANNED DEPARTURE 2026-06-16T14:30
                PLANNED ARRIVAL 2026-06-16T15:30
                FUEL 8000 KG

                RAW DESCRIPTION CREATED BY BOOTSTRAP.
                DSL VALIDATION IS NOT PERFORMED IN US080.
                """
        );

        // CANCELLED
        registerWithStatus(
                "TP202",
                "CS-TAC",
                "TP",
                LocalDateTime.of(2026, 6, 23, 14, 30),  // terça-feira
                LocalDateTime.of(2026, 6, 23, 15, 30),
                8000, FuelUnit.KG, "CANCELLED bootstrap", FlightPlanStatus.CANCELLED
        );

// REJECTED
        registerWithStatus(
                "TP202",
                "CS-TAC",
                "TP",
                LocalDateTime.of(2026, 6, 30, 14, 30),
                LocalDateTime.of(2026, 6, 30, 15, 30),
                8000, FuelUnit.KG, "REJECTED bootstrap", FlightPlanStatus.REJECTED
        );

// TESTED
        registerWithStatus(
                "TP202",
                "CS-TAC",
                "TP",
                LocalDateTime.of(2026, 7, 14, 14, 30),
                LocalDateTime.of(2026, 7, 14, 15, 30),
                8000, FuelUnit.KG, "TESTED bootstrap", FlightPlanStatus.TESTED
        );

        return true;
    }

    private void register(final String routeCode,
                          final String aircraftRegistration,
                          final String companyIata,
                          final LocalDateTime plannedDepartureDateTime,
                          final LocalDateTime plannedArrivalDateTime,
                          final double fuelAmount,
                          final FuelUnit fuelUnit,
                          final String description) {
        try {
            if (alreadyExists(routeCode, aircraftRegistration, plannedDepartureDateTime)) {
                LOGGER.debug("»»» FlightPlan already exists for route {} / aircraft {} / departure {}",
                        routeCode, aircraftRegistration, plannedDepartureDateTime);
                return;
            }

            final Pilot pilot = findActivePilotForCompany(companyIata);

            final FlightPlanDTO flightPlan = service.createDraftFlightPlan(
                    pilot,
                    routeCode,
                    aircraftRegistration,
                    plannedDepartureDateTime,
                    plannedArrivalDateTime,
                    fuelAmount,
                    fuelUnit,
                    description
            );

            LOGGER.debug("»»» FlightPlan registered: {} for route {}",
                    flightPlan.flightPlanID(), routeCode);

        } catch (final Exception e) {
            LOGGER.warn("FlightPlan skipped for route [{}] and aircraft [{}]: {}",
                    routeCode, aircraftRegistration, e.getMessage());
        }
    }

    private boolean alreadyExists(final String routeCode,
                                  final String aircraftRegistration,
                                  final LocalDateTime plannedDepartureDateTime) {
        final RouteCode route = RouteCode.of(routeCode);

        for (final FlightPlan flightPlan : flightPlanRepository.findByRouteCode(route)) {
            final boolean sameAircraft =
                    flightPlan.aircraftRegistration().code()
                            .equalsIgnoreCase(aircraftRegistration);

            final boolean sameDeparture =
                    flightPlan.plannedDepartureDateTime()
                            .equals(plannedDepartureDateTime);

            if (sameAircraft && sameDeparture) {
                return true;
            }
        }

        return false;
    }

    private Pilot findActivePilotForCompany(final String companyIata) {
        final AirTransportCompanyCode companyCode =
                AirTransportCompanyCode.of(companyIata);

        final var pilots = PersistenceContext.repositories()
                .pilots()
                .findActiveByCompany(companyCode)
                .iterator();

        if (!pilots.hasNext()) {
            throw new IllegalStateException(
                    "No active pilot found for company: " + companyIata
            );
        }

        return pilots.next();
    }

    private void registerWithStatus(final String routeCode,
                                    final String aircraftRegistration,
                                    final String companyIata,
                                    final LocalDateTime plannedDepartureDateTime,
                                    final LocalDateTime plannedArrivalDateTime,
                                    final double fuelAmount,
                                    final FuelUnit fuelUnit,
                                    final String description,
                                    final FlightPlanStatus status) {
        try {
            if (alreadyExists(routeCode, aircraftRegistration, plannedDepartureDateTime)) {
                LOGGER.debug("»»» FlightPlan already exists for route {} / aircraft {} / departure {}",
                        routeCode, aircraftRegistration, plannedDepartureDateTime);
                return;
            }

            final Pilot pilot = findActivePilotForCompany(companyIata);

            final FlightPlanDTO created = service.createDraftFlightPlan(
                    pilot,
                    routeCode,
                    aircraftRegistration,
                    plannedDepartureDateTime,
                    plannedArrivalDateTime,
                    fuelAmount,
                    fuelUnit,
                    description
            );

            // The service only returns a DTO; fetch the actual aggregate
            // so we can apply the status transition and persist it.
            final FlightPlan flightPlan = flightPlanRepository.findById(
                            FlightPlanID.of(created.flightPlanID()))
                    .orElseThrow(() -> new IllegalStateException(
                            "Flight plan not found right after creation: " + created.flightPlanID()));

            if (status == FlightPlanStatus.TESTED) {
                flightPlan.markTested();
            } else if (status == FlightPlanStatus.CANCELLED) {
                flightPlan.cancel();
            } else if (status == FlightPlanStatus.REJECTED) {
                flightPlan.reject();
            }

            flightPlanRepository.save(flightPlan);

            LOGGER.debug("»»» FlightPlan registered with status {}: {} for route {}",
                    status, flightPlan.identity(), routeCode);

        } catch (final Exception e) {
            LOGGER.warn("FlightPlan skipped for route [{}] and aircraft [{}]: {}",
                    routeCode, aircraftRegistration, e.getMessage());
        }
    }
}