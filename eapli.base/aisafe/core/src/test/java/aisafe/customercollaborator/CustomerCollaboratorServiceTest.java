package aisafe.customercollaborator;

import aisafe.aircontrolarea.domain.*;
import aisafe.airtransportcompany.domain.*;
import aisafe.costumerCollaborator.domain.*;
import aisafe.costumerCollaborator.service.CustomerCollaboratorRepository;
import aisafe.costumerCollaborator.service.CustomerCollaboratorService;
import aisafe.sharedkernel.Coordinates;
import aisafe.usermanagement.domain.UserBuilderHelper;
import aisafe.usermanagement.domain.legacyFiles.Email;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CustomerCollaboratorServiceTest {

    // --- Stubs ---

    private static class CustomerCollaboratorRepositoryStub
            implements CustomerCollaboratorRepository {

        private final Map<CustomerCollaboratorID, CustomerCollaborator> collaborators = new HashMap<>();

        @Override
        public Optional<CustomerCollaborator> findByUsername(final Username username) {
            return collaborators.values().stream()
                    .filter(c -> c.systemUser().username().equals(username))
                    .findFirst();
        }

        @Override
        public Optional<CustomerCollaborator> findByEmail(final Email email) {
            return collaborators.values().stream()
                    .filter(c -> c.email().equals(email))
                    .findFirst();
        }

        @Override
        public Iterable<CustomerCollaborator> findByCustomerReference(final CustomerReference ref) {
            return collaborators.values().stream()
                    .filter(c -> c.customerReference().equals(ref))
                    .toList();
        }

        @Override
        public Optional<CustomerCollaborator> findBySystemUser(final SystemUser systemUser) {
            return collaborators.values().stream()
                    .filter(c -> c.systemUser().equals(systemUser))
                    .findFirst();
        }

        @Override
        public Iterable<CustomerCollaborator> findAllActive() {
            return collaborators.values().stream()
                    .filter(CustomerCollaborator::isActive)
                    .toList();
        }

        @Override
        public <S extends CustomerCollaborator> S save(final S entity) {
            collaborators.put(entity.identity(), entity);
            return entity;
        }

        @Override public Iterable<CustomerCollaborator> findAll() { return collaborators.values(); }
        @Override public Optional<CustomerCollaborator> findById(final CustomerCollaboratorID id2) { return Optional.ofNullable(collaborators.get(id2)); }
        @Override public void delete(final CustomerCollaborator entity) { collaborators.remove(entity.identity()); }
        @Override public void deleteById(final CustomerCollaboratorID id) { collaborators.remove(id); }
        @Override public long count() { return collaborators.size(); }
    }

    private static class AirTransportCompanyRepositoryStub
            implements AirTransportCompanyRepository {

        private final Map<AirTransportCompanyCode, AirTransportCompany> companies = new HashMap<>();

        void add(final AirTransportCompany company) {
            companies.put(company.identity(), company);
        }

        @Override
        public Optional<AirTransportCompany> findByIataCode(final AirTransportCompanyCode iataCode) {
            return Optional.ofNullable(companies.get(iataCode));
        }

        @Override
        public Optional<AirTransportCompany> findByIcaoCode(final String icaoCode) {
            return companies.values().stream()
                    .filter(c -> c.icaoCode().equalsIgnoreCase(icaoCode))
                    .findFirst();
        }

        @Override
        public <S extends AirTransportCompany> S save(final S entity) {
            companies.put(entity.identity(), entity);
            return entity;
        }

        @Override public Iterable<AirTransportCompany> findAll() { return companies.values(); }
        @Override public Optional<AirTransportCompany> findById(final AirTransportCompanyCode id) { return Optional.ofNullable(companies.get(id)); }
        @Override public void delete(final AirTransportCompany entity) { companies.remove(entity.identity()); }
        @Override public void deleteById(final AirTransportCompanyCode id) { companies.remove(id); }
        @Override public long count() { return companies.size(); }
    }

    private static class AirControlAreaRepositoryStub
            implements AirControlAreaRepository {

        private final Map<AirControlAreaCode, AirControlArea> areas = new HashMap<>();

        void add(final AirControlArea area) {
            areas.put(area.identity(), area);
        }

        @Override
        public Iterable<AirControlArea> findAreasContaining(final Coordinates coordinates) {
            return areas.values().stream()
                    .filter(a -> a.contains(coordinates))
                    .toList();
        }

        @Override
        public Optional<AirControlArea> findByCode(final AirControlAreaCode code) {
            return Optional.ofNullable(areas.get(code));
        }

        @Override
        public Optional<AirControlArea> findByName(final AirControlAreaName name) {
            return areas.values().stream()
                    .filter(a -> a.name().equals(name))
                    .findFirst();
        }

        @Override
        public <S extends AirControlArea> S save(final S entity) { areas.put(entity.identity(), entity); return entity; }
        @Override public Iterable<AirControlArea> findAll() { return areas.values(); }
        @Override public Optional<AirControlArea> findById(final AirControlAreaCode id) { return Optional.ofNullable(areas.get(id)); }
        @Override public void delete(final AirControlArea entity) { areas.remove(entity.identity()); }
        @Override public void deleteById(final AirControlAreaCode id) { areas.remove(id); }
        @Override public long count() { return areas.size(); }
    }

    // --- Setup ---

    private CustomerCollaboratorRepositoryStub collaboratorRepo;
    private AirTransportCompanyRepositoryStub atcRepo;
    private AirControlAreaRepositoryStub acaRepo;
    private CustomerCollaboratorService service;

    private SystemUser systemUser;
    private SystemUser systemUser2;

    private static final String ATC_CODE = "TP";
    private static final String ACA_CODE = "LPPC";

    @BeforeEach
    void setUp() {
        collaboratorRepo = new CustomerCollaboratorRepositoryStub();
        atcRepo = new AirTransportCompanyRepositoryStub();
        acaRepo = new AirControlAreaRepositoryStub();
        service = new CustomerCollaboratorService(collaboratorRepo, atcRepo, acaRepo);

        // SystemUser via UserBuilderHelper
        systemUser = UserBuilderHelper.builder()
                .with("joao.silva", "Password1", "João", "Silva", "joao.silva@tap.pt")
                .withRoles(Set.of())
                .build();

        systemUser2 = UserBuilderHelper.builder()
                .with("maria.santos", "Password1", "Maria", "Santos", "maria.santos@tap.pt")
                .withRoles(Set.of())
                .build();

        // Air Transport Company
        atcRepo.add(new AirTransportCompany(
                AirTransportCompanyCode.of(ATC_CODE),
                "TAP",
                "TAP Air Portugal"
        ));

        // Air Control Area
        acaRepo.add(new AirControlArea(
                AirControlAreaCode.of(ACA_CODE),
                GeographicBoundary.of(
                        Coordinates.of(36.0, -10.0),
                        Coordinates.of(42.0, -6.0)
                ),
                AirControlAreaName.of("Lisboa")
        ));
    }

    // --- Helpers ---

    private Email email() { return Email.of("joao.silva@tap.pt"); }
    private Email email2() { return Email.of("maria.santos@tap.pt"); }

    // --- Testes ---

    @Test
    void addCollaboratorToAirTransportCompanySuccessfully() {
        final CustomerCollaborator result = service.addCustomerCollaborator(
                systemUser,
                CustomerType.AIR_TRANSPORT_COMPANY,
                ATC_CODE,
                email(),
                "Operations Manager"
        );

        assertNotNull(result);
        assertEquals(CustomerType.AIR_TRANSPORT_COMPANY, result.customerType());
        assertEquals(ATC_CODE, result.customerCode());
        assertEquals(CustomerStatus.ACTIVE, result.status());
    }

    @Test
    void addCollaboratorToAirControlAreaSuccessfully() {
        final CustomerCollaborator result = service.addCustomerCollaborator(
                systemUser,
                CustomerType.AIR_CONTROL_AREA,
                ACA_CODE,
                email(),
                "Flight Control Operator"
        );

        assertNotNull(result);
        assertEquals(CustomerType.AIR_CONTROL_AREA, result.customerType());
        assertEquals(ACA_CODE, result.customerCode());
        assertEquals(CustomerStatus.ACTIVE, result.status());
    }

    @Test
    void addCollaboratorWithNonExistentAirTransportCompanyThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                service.addCustomerCollaborator(
                        systemUser,
                        CustomerType.AIR_TRANSPORT_COMPANY,
                        "XX",
                        email(),
                        "Operations Manager"
                ));
    }

    @Test
    void addCollaboratorWithNonExistentAirControlAreaThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                service.addCustomerCollaborator(
                        systemUser,
                        CustomerType.AIR_CONTROL_AREA,
                        "XXXX",
                        email(),
                        "Flight Control Operator"
                ));
    }

    @Test
    void addCollaboratorWithDuplicateSystemUserThrows() {
        service.addCustomerCollaborator(
                systemUser,
                CustomerType.AIR_TRANSPORT_COMPANY,
                ATC_CODE,
                email(),
                "Operations Manager"
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.addCustomerCollaborator(
                        systemUser,
                        CustomerType.AIR_TRANSPORT_COMPANY,
                        ATC_CODE,
                        email2(),
                        "Operations Manager"
                ));
    }

    @Test
    void addCollaboratorWithDuplicateEmailThrows() {
        service.addCustomerCollaborator(
                systemUser,
                CustomerType.AIR_TRANSPORT_COMPANY,
                ATC_CODE,
                email(),
                "Operations Manager"
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.addCustomerCollaborator(
                        systemUser2,
                        CustomerType.AIR_TRANSPORT_COMPANY,
                        ATC_CODE,
                        email(),
                        "Operations Manager"
                ));
    }

    @Test
    void addCollaboratorStatusIsActiveByDefault() {
        final CustomerCollaborator result = service.addCustomerCollaborator(
                systemUser,
                CustomerType.AIR_TRANSPORT_COMPANY,
                ATC_CODE,
                email(),
                "Operations Manager"
        );

        assertTrue(result.isActive());
    }

    @Test
    void listActiveCollaboratorsReturnsOnlyActive() {
        service.addCustomerCollaborator(
                systemUser,
                CustomerType.AIR_TRANSPORT_COMPANY,
                ATC_CODE,
                email(),
                "Operations Manager"
        );

        final Iterable<CustomerCollaborator> active = service.listActiveCustomerCollaborators(
                CustomerType.AIR_TRANSPORT_COMPANY,
                ATC_CODE
        );

        assertTrue(active.iterator().hasNext());
    }

    @Test
    void listActiveCollaboratorsExcludesDisabled() {
        final CustomerCollaborator collaborator = service.addCustomerCollaborator(
                systemUser,
                CustomerType.AIR_TRANSPORT_COMPANY,
                ATC_CODE,
                email(),
                "Operations Manager"
        );

        collaborator.disable();

        final Iterable<CustomerCollaborator> active = service.listActiveCustomerCollaborators(
                CustomerType.AIR_TRANSPORT_COMPANY,
                ATC_CODE
        );

        assertFalse(active.iterator().hasNext());
    }

    @Test
    void nullCollaboratorRepositoryThrows() {
        assertThrows(Exception.class, () ->
                new CustomerCollaboratorService(null, atcRepo, acaRepo));
    }

    @Test
    void nullAtcRepositoryThrows() {
        assertThrows(Exception.class, () ->
                new CustomerCollaboratorService(collaboratorRepo, null, acaRepo));
    }

    @Test
    void nullAcaRepositoryThrows() {
        assertThrows(Exception.class, () ->
                new CustomerCollaboratorService(collaboratorRepo, atcRepo, null));
    }
}
