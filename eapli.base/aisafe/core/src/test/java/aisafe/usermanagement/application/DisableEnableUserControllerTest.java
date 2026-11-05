package aisafe.usermanagement.application;

import aisafe.usermanagement.domain.AiSafeRoles;
import eapli.framework.infrastructure.authz.application.AuthorizationService;
import eapli.framework.infrastructure.authz.application.UserManagementService;
import eapli.framework.infrastructure.authz.domain.model.Role;
import eapli.framework.infrastructure.authz.domain.model.SystemUser;
import eapli.framework.infrastructure.authz.domain.model.Username;
import eapli.framework.infrastructure.authz.domain.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DisableEnableUserController} (US032).
 *
 * <p>Uses manual stubs (no Mockito) and reflection to build SystemUser objects,
 * following the same pattern used in AiSafeUserTest.</p>
 */
class DisableEnableUserControllerTest {

    // -----------------------------------------------------------------------
    // Stubs
    // -----------------------------------------------------------------------

    static class AuthorizationServiceStub extends AuthorizationService {
        @Override
        public void ensureAuthenticatedUserHasAnyOf(final Role... roles) {
            // no-op for tests
        }
    }

    static class UserRepositoryStub implements UserRepository {
        final Map<String, SystemUser> store = new LinkedHashMap<>();

        @Override
        public Iterable<SystemUser> findByActive(final boolean active) {
            final List<SystemUser> result = new ArrayList<>();
            for (final SystemUser u : store.values()) {
                if (u.isActive() == active) result.add(u);
            }
            return result;
        }

        @Override
        public Iterable<SystemUser> findAll() {
            return Collections.unmodifiableCollection(store.values());
        }

        @Override
        @SuppressWarnings("unchecked")
        public <S extends SystemUser> S save(final S entity) {
            store.put(entity.username().toString(), entity);
            return entity;
        }

        @Override
        public Optional<SystemUser> ofIdentity(final Username id) {
            return Optional.ofNullable(store.get(id.toString()));
        }

        @Override
        public void delete(final SystemUser entity) {
            store.remove(entity.username().toString());
        }

        @Override
        public void deleteOfIdentity(final Username id) {
            store.remove(id.toString());
        }

        @Override
        public long count() {
            return store.size();
        }
    }

    // -----------------------------------------------------------------------
    // Helpers — build SystemUser via reflection (same pattern as AiSafeUserTest)
    // -----------------------------------------------------------------------

    /**
     * Creates an active SystemUser by setting fields directly via reflection.
     * Avoids SystemUserBuilder which requires non-null password policy and encoder.
     */
    private SystemUser buildActiveUser(final String usernameStr) {
        try {
            // Use the protected/package no-arg constructor via reflection
            final var constructor = SystemUser.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            final SystemUser user = constructor.newInstance();

            setField(user, "username", Username.valueOf(usernameStr));
            // SystemUser.active is typically a boolean field
            setField(user, "active", true);

            userRepo.store.put(usernameStr, user);
            return user;
        } catch (final Exception e) {
            throw new RuntimeException("Could not create SystemUser for test: " + e.getMessage(), e);
        }
    }

    private void setField(final Object target, final String fieldName, final Object value) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                final Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (final NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (final Exception e) {
                throw new RuntimeException("Could not set field " + fieldName, e);
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }

    // -----------------------------------------------------------------------
    // Fixtures
    // -----------------------------------------------------------------------

    private UserRepositoryStub userRepo;
    private UserManagementService userManagementService;
    private DisableEnableUserController controller;

    @BeforeEach
    void setUp() {
        userRepo = new UserRepositoryStub();
        userManagementService = new UserManagementService(userRepo, null, null);
        controller = new DisableEnableUserController(
                new AuthorizationServiceStub(), userManagementService);
    }

    // -----------------------------------------------------------------------
    // allUsers
    // -----------------------------------------------------------------------

    @Test
    void ensureAllUsersReturnsEmptyWhenNoUsersExist() {
        final List<SystemUser> result = new ArrayList<>();
        controller.allUsers().forEach(result::add);
        assertTrue(result.isEmpty());
    }

    @Test
    void ensureAllUsersAreListed() {
        // Arrange
        buildActiveUser("alice");
        buildActiveUser("bob");

        // Act
        final List<SystemUser> result = new ArrayList<>();
        controller.allUsers().forEach(result::add);

        // Assert
        assertEquals(2, result.size());
    }

    // -----------------------------------------------------------------------
    // disableUser
    // -----------------------------------------------------------------------

    @Test
    void ensureUserIsDisabledSuccessfully() {
        // Arrange
        final SystemUser user = buildActiveUser("charlie");
        assertTrue(user.isActive());

        // Act
        final SystemUser disabled = controller.disableUser(user);

        // Assert
        assertFalse(disabled.isActive());
    }

    @Test
    void ensureDisabledUserIsNoLongerActive() {
        // Arrange
        final SystemUser user = buildActiveUser("frank");

        // Act
        controller.disableUser(user);

        // Assert
        assertFalse(user.isActive());
    }

    // -----------------------------------------------------------------------
    // enableUser
    // -----------------------------------------------------------------------

    @Test
    void ensureDisabledUserCanBeEnabledAgain() {
        // Arrange
        final SystemUser user = buildActiveUser("diana");
        controller.disableUser(user);
        assertFalse(user.isActive());

        // Act
        final SystemUser enabled = controller.enableUser(user);

        // Assert
        assertTrue(enabled.isActive());
    }

    // -----------------------------------------------------------------------
    // round-trip
    // -----------------------------------------------------------------------

    @Test
    void ensureDisableAndEnableAreIdempotentInRepository() {
        // Arrange
        final SystemUser user = buildActiveUser("eve");

        // Act
        controller.disableUser(user);
        controller.enableUser(user);

        // Assert
        final Optional<SystemUser> found = userRepo.ofIdentity(user.username());
        assertTrue(found.isPresent());
        assertTrue(found.get().isActive());
    }
}