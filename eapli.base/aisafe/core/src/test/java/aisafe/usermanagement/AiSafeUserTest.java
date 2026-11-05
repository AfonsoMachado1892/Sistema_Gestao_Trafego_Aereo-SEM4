package aisafe.usermanagement;

import aisafe.usermanagement.domain.AiSafeUser;
import aisafe.usermanagement.domain.PhoneNumber;
import eapli.framework.infrastructure.authz.domain.model.Username;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class AiSafeUserTest {

    @Test
    void identityReturnsUsername() {
        final Username username = Username.valueOf("user@test.com");
        final PhoneNumber phoneNumber = phoneNumber();

        final AiSafeUser user = createAiSafeUser(username, phoneNumber);

        assertEquals(username, user.identity());
    }

    @Test
    void phoneNumberReturnsCorrectValue() {
        final PhoneNumber phoneNumber = phoneNumber();

        final AiSafeUser user = createAiSafeUser(
                Username.valueOf("user@test.com"),
                phoneNumber
        );

        assertEquals(phoneNumber, user.phoneNumber());
    }

    @Test
    void changePhoneNumberSuccessfully() {
        final AiSafeUser user = createAiSafeUser(
                Username.valueOf("user@test.com"),
                phoneNumber()
        );

        final PhoneNumber newPhoneNumber = PhoneNumber.of(Integer.parseInt("+351"), "913456789");

        user.changePhoneNumber(newPhoneNumber);

        assertEquals(newPhoneNumber, user.phoneNumber());
    }

    @Test
    void changePhoneNumberWithNullThrows() {
        final AiSafeUser user = createAiSafeUser(
                Username.valueOf("user@test.com"),
                phoneNumber()
        );

        assertThrows(IllegalArgumentException.class, () -> user.changePhoneNumber(null));
    }

    @Test
    void sameAsReturnsTrueForSameObject() {
        final AiSafeUser user = createAiSafeUser(
                Username.valueOf("user@test.com"),
                phoneNumber()
        );

        assertTrue(user.sameAs(user));
    }

    @Test
    void sameAsReturnsTrueForSameUsernameAndPhoneNumber() {
        final Username username = Username.valueOf("user@test.com");
        final PhoneNumber phoneNumber = phoneNumber();

        final AiSafeUser first = createAiSafeUser(username, phoneNumber);
        final AiSafeUser second = createAiSafeUser(username, phoneNumber);

        assertTrue(first.sameAs(second));
    }

    @Test
    void sameAsReturnsFalseForDifferentUsername() {
        final PhoneNumber phoneNumber = phoneNumber();

        final AiSafeUser first = createAiSafeUser(
                Username.valueOf("user1@test.com"),
                phoneNumber
        );

        final AiSafeUser second = createAiSafeUser(
                Username.valueOf("user2@test.com"),
                phoneNumber
        );

        assertFalse(first.sameAs(second));
    }

    @Test
    void sameAsReturnsFalseForDifferentPhoneNumber() {
        final Username username = Username.valueOf("user@test.com");

        final AiSafeUser first = createAiSafeUser(
                username,
                PhoneNumber.of(Integer.parseInt("+351"), "912345678")
        );

        final AiSafeUser second = createAiSafeUser(
                username,
                PhoneNumber.of(Integer.parseInt("+351"), "913456789")
        );

        assertFalse(first.sameAs(second));
    }

    @Test
    void sameAsReturnsFalseForDifferentObjectType() {
        final AiSafeUser user = createAiSafeUser(
                Username.valueOf("user@test.com"),
                phoneNumber()
        );

        assertFalse(user.sameAs("not an ai safe user"));
    }

    @Test
    void sameAsReturnsFalseForNull() {
        final AiSafeUser user = createAiSafeUser(
                Username.valueOf("user@test.com"),
                phoneNumber()
        );

        assertFalse(user.sameAs(null));
    }

    @Test
    void equalsReturnsTrueForSameIdentity() {
        final Username username = Username.valueOf("user@test.com");

        final AiSafeUser first = createAiSafeUser(
                username,
                PhoneNumber.of(Integer.parseInt("+351"), "912345678")
        );

        final AiSafeUser second = createAiSafeUser(
                username,
                PhoneNumber.of(Integer.parseInt("+351"), "913456789")
        );

        assertEquals(first, second);
    }

    @Test
    void equalsReturnsFalseForDifferentIdentity() {
        final AiSafeUser first = createAiSafeUser(
                Username.valueOf("user1@test.com"),
                phoneNumber()
        );

        final AiSafeUser second = createAiSafeUser(
                Username.valueOf("user2@test.com"),
                phoneNumber()
        );

        assertNotEquals(first, second);
    }

    @Test
    void hashCodeIsConsistent() {
        final AiSafeUser user = createAiSafeUser(
                Username.valueOf("user@test.com"),
                phoneNumber()
        );

        assertEquals(user.hashCode(), user.hashCode());
    }

    private PhoneNumber phoneNumber() {
        return PhoneNumber.of(Integer.parseInt("+351"), "912345678");
    }

    private AiSafeUser createAiSafeUser(final Username username, final PhoneNumber phoneNumber) {
        final AiSafeUser user = createEmptyAiSafeUser();

        setField(user, "username", username);
        setField(user, "phoneNumber", phoneNumber);

        return user;
    }

    private AiSafeUser createEmptyAiSafeUser() {
        try {
            final var constructor = AiSafeUser.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (final Exception e) {
            throw new RuntimeException("Could not create AiSafeUser for test.", e);
        }
    }

    private void setField(final Object target, final String fieldName, final Object value) {
        try {
            final Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (final Exception e) {
            throw new RuntimeException("Could not set field " + fieldName + " for test.", e);
        }
    }
}