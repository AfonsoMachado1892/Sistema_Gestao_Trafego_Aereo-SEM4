package aisafe.airtransportcompany.application;

import aisafe.airtransportcompany.domain.AirTransportCompany;
import aisafe.airtransportcompany.domain.AirTransportCompanyCode;
import aisafe.airtransportcompany.domain.AirTransportCompanyRepository;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RegisterAirTransportCompanyController} (US060).
 */
class RegisterAirTransportCompanyControllerTest {

    static class AuthorizationServiceStub extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) { }
    }

    static class AirTransportCompanyRepositoryStub implements AirTransportCompanyRepository {
        final Map<String, AirTransportCompany> store = new LinkedHashMap<>();

        @Override
        public Optional<AirTransportCompany> findByIataCode(final AirTransportCompanyCode code) {
            return Optional.ofNullable(store.get(code.toString()));
        }

        @Override
        public Optional<AirTransportCompany> findByIcaoCode(final String icaoCode) {
            return store.values().stream()
                    .filter(c -> c.icaoCode().equalsIgnoreCase(icaoCode))
                    .findFirst();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S extends AirTransportCompany> S save(final S entity) {
            store.put(entity.iataCode().toString(), entity);
            return entity;
        }

        @Override
        public Iterable<AirTransportCompany> findAll() {
            return Collections.unmodifiableCollection(store.values());
        }

        @Override
        public Optional<AirTransportCompany> findById(final AirTransportCompanyCode id) {
            return Optional.ofNullable(store.get(id.toString()));
        }

        @Override
        public void delete(final AirTransportCompany entity) {
            store.remove(entity.iataCode().toString());
        }

        @Override
        public void deleteById(final AirTransportCompanyCode id) {
            store.remove(id.toString());
        }

        @Override
        public long count() { return store.size(); }
    }

    private AirTransportCompanyRepositoryStub repo;
    private RegisterAirTransportCompanyController controller;

    @BeforeEach
    void setUp() {
        repo = new AirTransportCompanyRepositoryStub();
        controller = new RegisterAirTransportCompanyController(
                new AuthorizationServiceStub(), repo);
    }

    @Test
    void ensureCompanyIsRegisteredSuccessfully() {
        final AirTransportCompany result =
                controller.registerAirTransportCompany("TP", "TAP", "TAP Air Portugal");
        assertNotNull(result);
        assertEquals("TP", result.iataCode().toString());
        assertEquals("TAP", result.icaoCode());
        assertEquals("TAP Air Portugal", result.name());
    }

    @Test
    void ensureIataCodeIsNormalizedToUpperCase() {
        final AirTransportCompany result =
                controller.registerAirTransportCompany("tp", "tap", "TAP Air Portugal");
        assertEquals("TP", result.iataCode().toString());
        assertEquals("TAP", result.icaoCode());
    }

    @Test
    void ensureTwoLetterIcaoCodeIsAccepted() {
        assertDoesNotThrow(() ->
                controller.registerAirTransportCompany("TP", "TP", "TAP Air Portugal"));
    }

    @Test
    void ensureThreeLetterIcaoCodeIsAccepted() {
        assertDoesNotThrow(() ->
                controller.registerAirTransportCompany("TP", "TAP", "TAP Air Portugal"));
    }

    @Test
    void ensureCompanyIsSavedInRepository() {
        controller.registerAirTransportCompany("FR", "RYR", "Ryanair");
        assertTrue(repo.store.containsKey("FR"));
    }

    @Test
    void ensureDuplicateIataCodeIsRejected() {
        controller.registerAirTransportCompany("TP", "TAP", "TAP Air Portugal");
        assertThrows(IllegalStateException.class,
                () -> controller.registerAirTransportCompany("TP", "XYZ", "Another Company"));
    }

    @Test
    void ensureDuplicateIcaoCodeIsRejected() {
        controller.registerAirTransportCompany("TP", "TAP", "TAP Air Portugal");
        assertThrows(IllegalStateException.class,
                () -> controller.registerAirTransportCompany("LH", "TAP", "Another Company"));
    }

    @Test
    void ensureDifferentCompaniesCanBeRegistered() {
        controller.registerAirTransportCompany("TP", "TAP", "TAP Air Portugal");
        controller.registerAirTransportCompany("FR", "RYR", "Ryanair");
        controller.registerAirTransportCompany("LH", "DLH", "Lufthansa");
        assertEquals(3, repo.store.size());
    }

    @Test
    void ensureInvalidIataCodeIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.registerAirTransportCompany("TAP", "TAP", "TAP Air Portugal"));
    }

    @Test
    void ensureEmptyNameIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> controller.registerAirTransportCompany("TP", "TAP", ""));
    }
}