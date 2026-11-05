package aisafe.pilot.application;

import aisafe.aircraftmodel.domain.AircraftModel;
import aisafe.aircraftmodel.domain.AircraftModelID;
import aisafe.aircraftmodel.domain.AircraftModelRepository;
import aisafe.aircraftmodel.domain.AircraftType;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.costumerCollaborator.domain.CustomerCollaborator;
import aisafe.costumerCollaborator.domain.CustomerCollaboratorID;
import aisafe.costumerCollaborator.domain.CustomerReference;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.enginemodel.domain.EngineModel;
import aisafe.enginemodel.domain.EngineModelID;
import aisafe.enginemodel.domain.EngineType;
import aisafe.enginemodel.domain.FuelType;
import aisafe.maker.domain.Maker;
import aisafe.maker.domain.MakerID;
import aisafe.maker.domain.MakerType;
import aisafe.pilot.domain.*;
import aisafe.sharedkernel.Country;
import aisafe.sharedkernel.IsoCode;
import aisafe.usermanagement.domain.UserBuilderHelper;
import aisafe.usermanagement.domain.legacyFiles.Email;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ListPilotRosterController} (US076).
 * Uses the AAA convention (Arrange, Act, Assert).
 *
 * <p>The roster lists ALL pilots of the company (active and inactive).
 * Tests use the package-private {@code listPilotRoster(companyCode)} to
 * bypass authentication, mirroring DeactivateFlightRouteControllerTest.
 */
class ListPilotRosterControllerTest {

    // ── Stubs ────────────────────────────────────────────────────────────────

    static class AuthorizationServiceStub extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) { }
    }

    static class PilotRepositoryStub implements PilotRepository {
        final Map<PilotID, Pilot> store = new LinkedHashMap<>();
        void add(final Pilot pilot) { store.put(pilot.identity(), pilot); }

        @Override public Optional<Pilot> findBySystemUser(final SystemUser u) {
            return store.values().stream().filter(p -> p.systemUser().equals(u)).findFirst();
        }
        @Override public Iterable<Pilot> findByCompany(final AirTransportCompanyCode c) {
            return store.values().stream().filter(p -> p.currentCompanyIataCode().equals(c)).toList();
        }
        @Override public Iterable<Pilot> findActiveByCompany(final AirTransportCompanyCode c) {
            return store.values().stream()
                    .filter(p -> p.currentCompanyIataCode().equals(c) && p.isActive()).toList();
        }
        @Override public Optional<Pilot> findByPilotID(final PilotID id) {
            return Optional.ofNullable(store.get(id));
        }
        @Override @SuppressWarnings("unchecked")
        public <S extends Pilot> S save(final S entity) {
            store.put(entity.identity(), entity); return entity;
        }
        @Override public Iterable<Pilot> findAll() { return Collections.unmodifiableCollection(store.values()); }
        @Override public Optional<Pilot> findById(final PilotID id) { return Optional.ofNullable(store.get(id)); }
        @Override public void delete(final Pilot entity) { store.remove(entity.identity()); }
        @Override public void deleteById(final PilotID id) { store.remove(id); }
        @Override public long count() { return store.size(); }
    }

    static class AircraftModelRepositoryStub implements AircraftModelRepository {
        @Override public Optional<AircraftModel> findByModelIDAndMaker(final AircraftModelID id, final MakerID m) { return Optional.empty(); }
        @Override public Optional<AircraftModel> findByModelID(final AircraftModelID id) { return Optional.empty(); }
        @Override public <S extends AircraftModel> S save(final S e) { return e; }
        @Override public Iterable<AircraftModel> findAll() { return Collections.emptyList(); }
        @Override public Optional<AircraftModel> findById(final AircraftModelID id) { return Optional.empty(); }
        @Override public void delete(final AircraftModel e) { }
        @Override public void deleteById(final AircraftModelID id) { }
        @Override public long count() { return 0; }
    }

    static class CustomerCollaboratorRepositoryStub implements CustomerCollaboratorRepository {
        @Override public Optional<CustomerCollaborator> findBySystemUser(final SystemUser u) { return Optional.empty(); }
        @Override public Optional<CustomerCollaborator> findByUsername(final Username u) { return Optional.empty(); }
        @Override public Optional<CustomerCollaborator> findByEmail(final Email e) { return Optional.empty(); }
        @Override public Iterable<CustomerCollaborator> findByCustomerReference(final CustomerReference r) { return Collections.emptyList(); }
        @Override public <S extends CustomerCollaborator> S save(final S entity) { return entity; }
        @Override public Iterable<CustomerCollaborator> findAll() { return Collections.emptyList(); }
        @Override public Optional<CustomerCollaborator> findById(final CustomerCollaboratorID id) { return Optional.empty(); }
        @Override public void delete(final CustomerCollaborator entity) { }
        @Override public void deleteById(final CustomerCollaboratorID id) { }
        @Override public Iterable<CustomerCollaborator> findAllActive() { return Collections.emptyList(); }
        @Override public long count() { return 0; }
    }

    // ── Setup ────────────────────────────────────────────────────────────────

    private PilotRepositoryStub pilotRepo;
    private ListPilotRosterController controller;

    private static final AirTransportCompanyCode TP = AirTransportCompanyCode.of("TP");
    private static final AirTransportCompanyCode FR = AirTransportCompanyCode.of("FR");
    private static final LocalDate ISSUED      = LocalDate.of(2023, 1, 1);
    private static final LocalDate VALID_UNTIL = LocalDate.of(2028, 1, 1);

    private AircraftModel a320;

    private AircraftModel buildAircraftModel(final String modelID) {
        final Maker engineMaker = new Maker(MakerID.of("CFM"), "CFM International",
                new Country(IsoCode.of("FR"), "France"), Set.of(MakerType.ENGINE));
        final EngineModel engine = new EngineModel(
                EngineModelID.of("CFM56-5B"), "CFM56-5B",
                EngineType.TURBOFAN, 120000.0, FuelType.JET_A1, 0.36, engineMaker);
        final Maker airbus = new Maker(MakerID.of("AIRBUS"), "Airbus",
                new Country(IsoCode.of("FR"), "France"), Set.of(MakerType.AIRCRAFT));
        return new AircraftModel(AircraftModelID.of(modelID), airbus, AircraftType.PASSENGER,
                Set.of(engine), 42000, 78000, 70000, 26000, 12000, 833, 122.6, 0.025, 1.5, 6300, 180, 2);
    }

    private Pilot buildPilot(final SystemUser user, final AirTransportCompanyCode company) {
        return new PilotFactory()
                .withSystemUser(user)
                .withCompany(company)
                .withCertifications(Set.of(
                        Certification.of(a320, FlightHours.of(500), ISSUED, VALID_UNTIL)))
                .build();
    }

    private SystemUser user(final String username, final String email) {
        return UserBuilderHelper.builder()
                .with(username, "Password1", "First", "Last", email)
                .withRoles(Set.of()).build();
    }

    @BeforeEach
    void setUp() {
        pilotRepo = new PilotRepositoryStub();
        controller = new ListPilotRosterController(
                new AuthorizationServiceStub(),
                pilotRepo,
                new AircraftModelRepositoryStub(),
                new CustomerCollaboratorRepositoryStub()
        );
        a320 = buildAircraftModel("A320");
    }

    // ── listPilotRoster(companyCode) ──────────────────────────────────────────

    @Test
    void ensureRosterReturnsAllPilotsOfCompany() {
        // Arrange
        pilotRepo.add(buildPilot(user("p1", "p1@tp.pt"), TP));
        pilotRepo.add(buildPilot(user("p2", "p2@tp.pt"), TP));
        // Act
        final List<PilotDTO> result = controller.listPilotRoster(TP);
        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void ensureRosterIncludesInactivePilots() {
        // Arrange
        pilotRepo.add(buildPilot(user("p1", "p1@tp.pt"), TP));
        final Pilot inactive = buildPilot(user("p2", "p2@tp.pt"), TP);
        inactive.deactivate();
        pilotRepo.add(inactive);
        // Act
        final List<PilotDTO> result = controller.listPilotRoster(TP);
        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(d -> d.status.equals("INACTIVE")));
    }

    @Test
    void ensureRosterReturnsEmptyWhenNoPilots() {
        assertTrue(controller.listPilotRoster(TP).isEmpty());
    }

    @Test
    void ensureRosterDoesNotReturnPilotsFromOtherCompany() {
        // Arrange
        pilotRepo.add(buildPilot(user("p1", "p1@fr.pt"), FR));
        // Act + Assert
        assertTrue(controller.listPilotRoster(TP).isEmpty());
    }

    @Test
    void ensureRosterReturnsDTOWithCorrectFields() {
        // Arrange
        pilotRepo.add(buildPilot(user("p1", "p1@tp.pt"), TP));
        // Act
        final List<PilotDTO> result = controller.listPilotRoster(TP);
        // Assert
        assertEquals(1, result.size());
        final PilotDTO dto = result.get(0);
        assertEquals("TP", dto.companyIata);
        assertEquals("ACTIVE", dto.status);
        assertEquals(1, dto.certificationCount);
        assertNotNull(dto.pilotID);
        assertNotNull(dto.name);
    }
}
