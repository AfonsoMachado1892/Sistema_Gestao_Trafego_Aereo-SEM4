package aisafe.usermanagement;

import aisafe.usermanagement.domain.legacyFiles.UserID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserIDTest {

    @Test
    void validUserIDIsCreated() {
        assertDoesNotThrow(() -> UserID.of("USER001"));
    }

    @Test
    void toStringReturnsNumber() {
        final UserID userID = UserID.of("USER001");

        assertEquals("USER001", userID.toString());
    }

    @Test
    void nullUserIDThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                UserID.of(null));
    }

    @Test
    void emptyUserIDThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                UserID.of(""));
    }

    @Test
    void blankUserIDThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                UserID.of("   "));
    }

    @Test
    void equalUserIDsAreEqual() {
        assertEquals(
                UserID.of("USER001"),
                UserID.of("USER001")
        );
    }

    @Test
    void sameObjectIsEqual() {
        final UserID userID = UserID.of("USER001");

        assertEquals(userID, userID);
    }

    @Test
    void differentUserIDsAreNotEqual() {
        assertNotEquals(
                UserID.of("USER001"),
                UserID.of("USER002")
        );
    }

    @Test
    void userIDIsNotEqualToDifferentObjectType() {
        assertNotEquals(UserID.of("USER001"), "USER001");
    }

    @Test
    void userIDIsNotEqualToNull() {
        assertNotEquals(null, UserID.of("USER001"));
    }

    @Test
    void equalUserIDsHaveSameHashCode() {
        final UserID first = UserID.of("USER001");
        final UserID second = UserID.of("USER001");

        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void hashCodeIsConsistent() {
        final UserID userID = UserID.of("USER001");

        assertEquals(userID.hashCode(), userID.hashCode());
    }

    @Test
    void compareToReturnsZeroForEqualUserIDs() {
        final UserID first = UserID.of("USER001");
        final UserID second = UserID.of("USER001");

        assertEquals(0, first.compareTo(second));
    }

    @Test
    void compareToReturnsNegativeForLowerUserID() {
        final UserID first = UserID.of("USER001");
        final UserID second = UserID.of("USER002");

        assertTrue(first.compareTo(second) < 0);
    }

    @Test
    void compareToReturnsPositiveForGreaterUserID() {
        final UserID first = UserID.of("USER002");
        final UserID second = UserID.of("USER001");

        assertTrue(first.compareTo(second) > 0);
    }
}