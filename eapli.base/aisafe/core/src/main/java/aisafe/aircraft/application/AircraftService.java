package aisafe.aircraft.application;

import aisafe.aircraft.domain.*;
import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.flight.domain.FlightRepository;
import aisafe.sharedkernel.Country;
import eapli.framework.validations.Preconditions;

public class AircraftService {

    private final AircraftRepository aircraftRepository;

    public AircraftService(final AircraftRepository aircraftRepository) {
        Preconditions.noneNull(aircraftRepository, "Aircraft repository must be provided.");
        this.aircraftRepository = aircraftRepository;
    }

    public Aircraft addAircraft(
            final String registration,
            final AircraftModel aircraftModel,
            final AirTransportCompany airTransportCompany,
            final CabinConfiguration cabinConfiguration,
            final Country country,
            final int numCrewElements
    ) {
        if (aircraftRepository.findByRegistration(AircraftRegistration.of(registration)).isPresent()) {
            throw new IllegalArgumentException("Aircraft registration already exists.");
        }

        if (cabinConfiguration.exceedsCapacity(aircraftModel.maxPassengerCapacity())) {
            throw new IllegalArgumentException(
                    "Cabin configuration exceeds aircraft model maximum capacity of "
                            + aircraftModel.maxPassengerCapacity() + " seats.");
        }

        final Aircraft aircraft = new AircraftFactory()
                .withRegistration(registration)
                .withAircraftModel(aircraftModel)
                .withAirTransportCompany(airTransportCompany)
                .withCabinConfiguration(cabinConfiguration)
                .withCountry(country)
                .withNumCrewElements(numCrewElements)
                .build();

        return aircraftRepository.save(aircraft);
    }

    public Iterable<Aircraft> listAircraftByCompany(final AirTransportCompany airTransportCompany) {
        Preconditions.noneNull(airTransportCompany, "Air transport company must be provided.");
        return aircraftRepository.findByAirTransportCompany(airTransportCompany);
    }

    // -------------------------------------------------------------------------
    // US071 - Decommission
    // -------------------------------------------------------------------------

    /**
     * Decommissions the aircraft identified by {@code registration}.
     *
     * Business rules enforced:
     * - Aircraft must belong to the given company.
     * - Aircraft must exist.
     * - Aircraft must have no pending flight plans (DRAFT or VALIDATED).
     *
     * @param registration       the registration code of the aircraft
     * @param airTransportCompany the company the collaborator belongs to
     * @param flightRepository   used to check for pending flights
     * @return the saved, decommissioned Aircraft
     */
    public Aircraft decommissionAircraft(
            final String registration,
            final AirTransportCompany airTransportCompany,
            final FlightRepository flightRepository
    ) {
        Preconditions.noneNull(registration, "Registration must be provided.");
        Preconditions.noneNull(airTransportCompany, "Air transport company must be provided.");
        Preconditions.noneNull(flightRepository, "Flight repository must be provided.");

        final AircraftRegistration reg = AircraftRegistration.of(registration);

        final Aircraft aircraft = aircraftRepository.findByRegistration(reg)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aircraft with registration " + registration + " not found."));

        if (!aircraft.airCompany().sameAs(airTransportCompany)) {
            throw new IllegalArgumentException(
                    "Aircraft " + registration + " does not belong to your company.");
        }

        if (flightRepository.existsPendingFlightsFor(reg)) {
            throw new IllegalStateException(
                    "Cannot decommission aircraft " + registration
                            + ": it has pending flight plans (DRAFT or VALIDATED).");
        }

        aircraft.decommission();

        return aircraftRepository.save(aircraft);
    }
}