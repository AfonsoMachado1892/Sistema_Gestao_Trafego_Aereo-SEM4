package aisafe.flightplan.domain;

import aisafe.aircraft.domain.Aircraft;
import aisafe.aircraft.domain.AircraftRegistration;
import aisafe.aircraft.domain.AircraftRepository;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.flightplan.application.FlightPlanDTO;
import aisafe.flightroute.application.FlightRouteDTO;
import aisafe.flightroute.domain.FlightRoute;
import aisafe.flightroute.domain.FlightRouteRepository;
import aisafe.flightroute.domain.RouteCode;
import aisafe.pilot.domain.Pilot;
import eapli.framework.validations.Preconditions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlightPlanService {

    private final FlightPlanRepository flightPlanRepository;
    private final FlightRouteRepository flightRouteRepository;
    private final AircraftRepository aircraftRepository;

    public FlightPlanService(final FlightPlanRepository flightPlanRepository,
                             final FlightRouteRepository flightRouteRepository,
                             final AircraftRepository aircraftRepository) {
        Preconditions.noneNull(flightPlanRepository, "Flight plan repository must be provided.");
        Preconditions.noneNull(flightRouteRepository, "Flight route repository must be provided.");
        Preconditions.noneNull(aircraftRepository, "Aircraft repository must be provided.");

        this.flightPlanRepository = flightPlanRepository;
        this.flightRouteRepository = flightRouteRepository;
        this.aircraftRepository = aircraftRepository;
    }

    public Iterable<FlightRouteDTO> listAvailableRoutesFor(final Pilot pilot) {
        Preconditions.ensure(pilot.isActive(), "Pilot must be active.");

        final List<FlightRouteDTO> dtos = new ArrayList<>();

        for (final FlightRoute route :
                flightRouteRepository.findByCompany(pilot.currentCompanyIataCode())) {

            if (route.isActive()) {
                dtos.add(toDTO(route));
            }
        }

        return dtos;
    }

    public Iterable<Aircraft> listAvailableAircraftFor(final Pilot pilot) {
        Preconditions.ensure(pilot.isActive(), "Pilot must be active.");

        final List<Aircraft> aircrafts = new ArrayList<>();

        for (final Aircraft aircraft : aircraftRepository.findAll()) {
            if (aircraft.isOperational()
                    && aircraft.belongsTo(pilot.currentCompanyIataCode())) {
                aircrafts.add(aircraft);
            }
        }

        return aircrafts;
    }

    public boolean routeAllowsDepartureAtFor(final Pilot pilot,
                                             final String routeCodeValue,
                                             final LocalDateTime plannedDepartureDateTime) {

        Preconditions.ensure(pilot.isActive(), "Pilot must be active.");

        final RouteCode routeCode = RouteCode.of(routeCodeValue);

        final FlightRoute route = flightRouteRepository.findByRouteCode(routeCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Flight route not found: " + routeCodeValue));

        ensurePilotCanUseRoute(pilot, route.airTransportCompanyIataCode());

        return route.isActiveOn(plannedDepartureDateTime.toLocalDate())
                && route.allowsDepartureAt(plannedDepartureDateTime);
    }

    public FlightPlanDTO createDraftFlightPlan(final Pilot pilot,
                                               final String routeCodeValue,
                                               final String aircraftRegistrationValue,
                                               final LocalDateTime plannedDepartureDateTime,
                                               final LocalDateTime plannedArrivalDateTime,
                                               final double fuelAmount,
                                               final FuelUnit fuelUnit,
                                               final String descriptionText) {

        Preconditions.ensure(pilot.isActive(), "Pilot must be active.");

        final RouteCode routeCode = RouteCode.of(routeCodeValue);

        final FlightRoute route = flightRouteRepository.findByRouteCode(routeCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Flight route not found: " + routeCodeValue));

        ensureRouteCanBeUsed(route, plannedDepartureDateTime);

        final AirTransportCompanyCode routeCompanyCode = route.airTransportCompanyIataCode();

        ensurePilotCanUseRoute(pilot, routeCompanyCode);

        final AircraftRegistration aircraftRegistration =
                AircraftRegistration.of(aircraftRegistrationValue);

        final Aircraft aircraft = aircraftRepository.findByRegistration(aircraftRegistration)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aircraft not found: " + aircraftRegistrationValue));

        ensureAircraftCanBeUsed(aircraft, routeCompanyCode);

        final FuelQuantity fuelQuantity = FuelQuantity.of(fuelAmount, fuelUnit);
        final FlightPlanDescription description = FlightPlanDescription.of(descriptionText);

        final FlightPlan flightPlan = FlightPlan.draft(
                FlightPlanID.newID(),
                route,
                aircraft,
                pilot,
                plannedDepartureDateTime,
                plannedArrivalDateTime,
                fuelQuantity,
                description
        );

        return FlightPlanDTO.from(flightPlanRepository.save(flightPlan));
    }

    public Iterable<FlightPlanDTO> listFlightPlansFor(final Pilot pilot) {
        Preconditions.noneNull(pilot, "Pilot must not be null.");
        Preconditions.ensure(pilot.isActive(), "Pilot must be active.");

        final List<FlightPlanDTO> dtos = new ArrayList<>();

        for (final FlightPlan flightPlan : flightPlanRepository.findByPilotID(pilot.identity())) {
            dtos.add(FlightPlanDTO.from(flightPlan));
        }

        return dtos;
    }

    private void ensureRouteCanBeUsed(final FlightRoute route,
                                      final LocalDateTime plannedDepartureDateTime) {
        Preconditions.ensure(route.isActiveOn(plannedDepartureDateTime.toLocalDate()),
                "Flight route must be active on the planned departure date.");

        Preconditions.ensure(
                route.allowsDepartureAt(plannedDepartureDateTime),
                "The planned departure date/time is not allowed by the route schedule."
        );
    }

    private void ensurePilotCanUseRoute(final Pilot pilot,
                                        final AirTransportCompanyCode routeCompanyCode) {
        Preconditions.ensure(
                pilot.belongsTo(routeCompanyCode),
                "Pilot must belong to the route's company."
        );
    }

    private void ensureAircraftCanBeUsed(final Aircraft aircraft,
                                         final AirTransportCompanyCode routeCompanyCode) {
        Preconditions.ensure(
                aircraft.isOperational(),
                "Aircraft must be operational."
        );

        Preconditions.ensure(
                aircraft.belongsTo(routeCompanyCode),
                "Aircraft must belong to the route's company."
        );
    }

    private FlightRouteDTO toDTO(final FlightRoute route) {
        return new FlightRouteDTO(
                route.routeCode().toString(),
                route.effectiveStatus().name(),
                route.departureAirportIataCode().toString(),
                route.arrivalAirportIataCode().toString(),
                route.airTransportCompanyIataCode().toString(),
                route.flightType().name(),
                route.deactivationDate() == null ? "-" : route.deactivationDate().toString(),
                route.schedule().toString()
        );
    }
}