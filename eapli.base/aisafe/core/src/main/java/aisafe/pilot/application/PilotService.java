package aisafe.pilot.application;

import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.aircraftmodel.domain.AircraftModelRepository;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.flightplan.domain.FlightPlan;
import aisafe.flightplan.domain.FlightPlanRepository;
import aisafe.flightplan.domain.FlightPlanStatus;
import aisafe.pilot.domain.*;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.validations.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PilotService {

    private final PilotRepository pilotRepository;
    private final AircraftModelRepository aircraftModelRepository;

    public PilotService(final PilotRepository pilotRepository,
                        final AircraftModelRepository aircraftModelRepository) {
        Preconditions.noneNull(pilotRepository, "Pilot repository must be provided.");
        Preconditions.noneNull(aircraftModelRepository, "Aircraft model repository must be provided.");
        this.pilotRepository = pilotRepository;
        this.aircraftModelRepository = aircraftModelRepository;
    }

    // ── US075 — Add a pilot ────────────────────────────────────────────────────

    public Pilot addPilot(final SystemUser systemUser,
                          final AirTransportCompanyCode companyIataCode,
                          final Set<Certification> certifications) {
        ensureDistinctSystemUser(systemUser);
        final Pilot pilot = new PilotFactory()
                .withSystemUser(systemUser)
                .withCompany(companyIataCode)
                .withCertifications(certifications)
                .build();
        return pilotRepository.save(pilot);
    }

    public Iterable<AircraftModelDTO> listAircraftModels() {
        final List<AircraftModelDTO> dtos = new ArrayList<>();
        for (final AircraftModel model : aircraftModelRepository.findAll()) {
            dtos.add(new AircraftModelDTO(
                    model.identity().toString(),
                    model.identity().toString(),
                    model.maker().name(),
                    model.type().name()
            ));
        }
        return dtos;
    }

    public AircraftModel findAircraftModel(final String modelID) {
        return aircraftModelRepository
                .findByModelID(aisafe.aircraftmodel.domain.AircraftModelID.of(modelID))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aircraft model not found: " + modelID));
    }

    // ── US076 — List pilot roster ─────────────────────────────────────────────

    /**
     * Returns DTOs of all pilots (active and inactive) of the given company.
     * The roster shows every pilot, with the status field distinguishing
     * ACTIVE from INACTIVE.
     *
     * @param companyCode the IATA code of the air transport company
     * @return list of {@link PilotDTO} for all pilots of the company
     */
    public List<PilotDTO> listPilotsByCompany(final AirTransportCompanyCode companyCode) {
        Preconditions.noneNull(companyCode, "Company code must be provided.");
        return toDTOs(pilotRepository.findByCompany(companyCode));
    }

    /**
     * Returns DTOs of the active pilots of the given company.
     * Used for selection in the removal flow — only active pilots can be removed.
     *
     * @param companyCode the IATA code of the air transport company
     * @return list of {@link PilotDTO} for active pilots
     */
    public List<PilotDTO> listActivePilotsByCompany(final AirTransportCompanyCode companyCode) {
        Preconditions.noneNull(companyCode, "Company code must be provided.");
        return toDTOs(pilotRepository.findActiveByCompany(companyCode));
    }

    // ── US077 — Remove a pilot ────────────────────────────────────────────────

    /**
     * Deactivates a pilot after verifying:
     * <ul>
     *   <li>The pilot exists.</li>
     *   <li>The pilot belongs to the given company.</li>
     *   <li>The pilot has no flight plans assigned in an active state
     *       (DRAFT, TESTED or VALIDATED).</li>
     * </ul>
     *
     * <p>The pilot is set to INACTIVE; it is not deleted, and the associated
     * SystemUser (login) is intentionally left untouched.
     *
     * @param pilotID              the ID of the pilot to deactivate
     * @param companyCode          the company the requesting user belongs to
     * @param flightPlanRepository used to check for assigned flight plans
     * @return DTO of the deactivated pilot
     * @throws IllegalArgumentException if the pilot is not found, does not belong
     *                                  to the company, or has assigned flight plans
     */
    public PilotDTO removePilot(final PilotID pilotID,
                                final AirTransportCompanyCode companyCode,
                                final FlightPlanRepository flightPlanRepository) {
        Preconditions.noneNull(pilotID, "Pilot ID must be provided.");
        Preconditions.noneNull(companyCode, "Company code must be provided.");
        Preconditions.noneNull(flightPlanRepository, "FlightPlan repository must be provided.");

        final Pilot pilot = pilotRepository.findByPilotID(pilotID)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Pilot not found: " + pilotID));

        Preconditions.ensure(pilot.belongsTo(companyCode),
                "Pilot does not belong to company " + companyCode + ".");

        ensureNoAssignedFlightPlans(pilot, flightPlanRepository);

        pilot.deactivate();
        pilotRepository.save(pilot);

        return toDTO(pilot);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void ensureDistinctSystemUser(final SystemUser systemUser) {
        pilotRepository.findBySystemUser(systemUser).ifPresent(p -> {
            throw new IllegalStateException("This user is already registered as a pilot.");
        });
    }

    /**
     * A flight plan blocks pilot removal if it is in an active workflow state:
     * DRAFT, TESTED or VALIDATED. REJECTED and CANCELLED plans no longer count.
     */
    private void ensureNoAssignedFlightPlans(final Pilot pilot,
                                             final FlightPlanRepository flightPlanRepository) {
        for (final FlightPlan fp : flightPlanRepository.findByPilotID(pilot.identity())) {
            final FlightPlanStatus status = fp.status();
            if (status == FlightPlanStatus.DRAFT
                    || status == FlightPlanStatus.TESTED
                    || status == FlightPlanStatus.VALIDATED) {
                throw new IllegalArgumentException(
                        "Cannot remove a pilot with assigned flight plans "
                                + "(DRAFT, TESTED or VALIDATED).");
            }
        }
    }

    private List<PilotDTO> toDTOs(final Iterable<Pilot> pilots) {
        final List<PilotDTO> dtos = new ArrayList<>();
        for (final Pilot pilot : pilots) {
            dtos.add(toDTO(pilot));
        }
        return dtos;
    }

    private PilotDTO toDTO(final Pilot pilot) {
        return new PilotDTO(
                pilot.identity().number(),
                pilot.systemUser().name().toString(),
                pilot.currentCompanyIataCode().toString(),
                pilot.status().name(),
                pilot.certifications().size()
        );
    }
}
