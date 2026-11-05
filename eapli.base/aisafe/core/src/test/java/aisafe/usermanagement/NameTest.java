package aisafe.usermanagement;

import aisafe.usermanagement.domain.legacyFiles.Name;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NameTest {

    @Test
    void validNameIsCreated() {
        assertDoesNotThrow(() -> Name.of("Jose", "Cardoso"));
    }

    @Test
    void firstNameReturnsCorrectValue() {
        final Name name = Name.of("Jose", "Cardoso");

        assertEquals("Jose", name.firstName());
    }

    @Test
    void lastNameReturnsCorrectValue() {
        final Name name = Name.of("Jose", "Cardoso");

        assertEquals("Cardoso", name.lastName());
    }

    @Test
    void nameTrimsFirstAndLastName() {
        final Name name = Name.of("  Jose  ", "  Cardoso  ");

        assertEquals("Jose", name.firstName());
        assertEquals("Cardoso", name.lastName());
    }

    @Test
    void nullFirstNameThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                Name.of(null, "Cardoso"));
    }

    @Test
    void nullLastNameThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                Name.of("Jose", null));
    }

    @Test
    void emptyFirstNameThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                Name.of("", "Cardoso"));
    }

    @Test
    void emptyLastNameThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                Name.of("Jose", ""));
    }

    @Test
    void blankFirstNameThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                Name.of("   ", "Cardoso"));
    }

    @Test
    void blankLastNameThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                Name.of("Jose", "   "));
    }

    @Test
    void toStringReturnsFullName() {
        final Name name = Name.of("Jose", "Cardoso");

        assertEquals("Jose Cardoso", name.toString());
    }

    @Test
    void equalNamesAreEqual() {
        assertEquals(
                Name.of("Jose", "Cardoso"),
                Name.of("Jose", "Cardoso")
        );
    }

    @Test
    void sameObjectIsEqual() {
        final Name name = Name.of("Jose", "Cardoso");

        assertEquals(name, name);
    }

    @Test
    void differentFirstNameIsNotEqual() {
        assertNotEquals(
                Name.of("Jose", "Cardoso"),
                Name.of("Joao", "Cardoso")
        );
    }

    @Test
    void differentLastNameIsNotEqual() {
        assertNotEquals(
                Name.of("Jose", "Cardoso"),
                Name.of("Jose", "Silva")
        );
    }

    @Test
    void nameIsNotEqualToDifferentObjectType() {
        assertNotEquals(Name.of("Jose", "Cardoso"), "Jose Cardoso");
    }

    @Test
    void nameIsNotEqualToNull() {
        assertNotEquals(null, Name.of("Jose", "Cardoso"));
    }

    @Test
    void equalNamesHaveSameHashCode() {
        final Name first = Name.of("Jose", "Cardoso");
        final Name second = Name.of("Jose", "Cardoso");

        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void hashCodeIsConsistent() {
        final Name name = Name.of("Jose", "Cardoso");

        assertEquals(name.hashCode(), name.hashCode());
    }

    @Test
    void compareToReturnsZeroForEqualNames() {
        final Name first = Name.of("Jose", "Cardoso");
        final Name second = Name.of("Jose", "Cardoso");

        assertEquals(0, first.compareTo(second));
    }

    @Test
    void compareToReturnsNegativeWhenFirstNameIsLower() {
        final Name first = Name.of("Ana", "Cardoso");
        final Name second = Name.of("Jose", "Cardoso");

        assertTrue(first.compareTo(second) < 0);
    }

    @Test
    void compareToReturnsPositiveWhenFirstNameIsGreater() {
        final Name first = Name.of("Jose", "Cardoso");
        final Name second = Name.of("Ana", "Cardoso");

        assertTrue(first.compareTo(second) > 0);
    }

    @Test
    void compareToUsesLastNameWhenFirstNameIsEqual() {
        final Name first = Name.of("Jose", "Cardoso");
        final Name second = Name.of("Jose", "Silva");

        assertTrue(first.compareTo(second) < 0);
    }

    @Test
    void compareToReturnsPositiveWhenLastNameIsGreaterAndFirstNameEqual() {
        final Name first = Name.of("Jose", "Silva");
        final Name second = Name.of("Jose", "Cardoso");

        assertTrue(first.compareTo(second) > 0);
    }
}