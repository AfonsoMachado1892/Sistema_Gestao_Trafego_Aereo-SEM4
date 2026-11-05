package aisafe.usermanagement;

import aisafe.usermanagement.domain.PhoneNumber;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhoneNumberTest {

    @Test
    void validPhoneNumberIsCreated() {
        assertDoesNotThrow(() -> PhoneNumber.of(351, "912345678"));
    }

    @Test
    void toStringReturnsCountryCodeAndPhoneNumber() {
        final PhoneNumber phoneNumber = PhoneNumber.of(351, "912345678");

        assertEquals("+351 912345678", phoneNumber.toString());
    }

    @Test
    void countryCodeZeroThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                PhoneNumber.of(0, "912345678"));
    }

    @Test
    void negativeCountryCodeThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                PhoneNumber.of(-1, "912345678"));
    }

    @Test
    void countryCodeGreaterThan999Throws() {
        assertThrows(IllegalArgumentException.class, () ->
                PhoneNumber.of(1000, "912345678"));
    }

    @Test
    void nullPhoneNumberThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                PhoneNumber.of(351, null));
    }

    @Test
    void emptyPhoneNumberThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                PhoneNumber.of(351, ""));
    }

    @Test
    void blankPhoneNumberThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                PhoneNumber.of(351, "   "));
    }

    @Test
    void phoneNumberWithLettersThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                PhoneNumber.of(351, "912ABC678"));
    }

    @Test
    void phoneNumberWithSymbolsThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                PhoneNumber.of(351, "912-345-678"));
    }

    @Test
    void phoneNumberLongerThan15DigitsThrows() {
        assertThrows(IllegalArgumentException.class, () ->
                PhoneNumber.of(351, "1234567890123456"));
    }

    @Test
    void phoneNumberWithOneDigitIsValid() {
        assertDoesNotThrow(() -> PhoneNumber.of(351, "1"));
    }

    @Test
    void phoneNumberWithFifteenDigitsIsValid() {
        assertDoesNotThrow(() -> PhoneNumber.of(351, "123456789012345"));
    }

    @Test
    void equalPhoneNumbersAreEqual() {
        assertEquals(
                PhoneNumber.of(351, "912345678"),
                PhoneNumber.of(351, "912345678")
        );
    }

    @Test
    void sameObjectIsEqual() {
        final PhoneNumber phoneNumber = PhoneNumber.of(351, "912345678");

        assertEquals(phoneNumber, phoneNumber);
    }

    @Test
    void differentCountryCodeIsNotEqual() {
        assertNotEquals(
                PhoneNumber.of(351, "912345678"),
                PhoneNumber.of(44, "912345678")
        );
    }

    @Test
    void differentPhoneNumberIsNotEqual() {
        assertNotEquals(
                PhoneNumber.of(351, "912345678"),
                PhoneNumber.of(351, "913456789")
        );
    }

    @Test
    void phoneNumberIsNotEqualToDifferentObjectType() {
        assertNotEquals(PhoneNumber.of(351, "912345678"), "912345678");
    }

    @Test
    void phoneNumberIsNotEqualToNull() {
        assertNotEquals(null, PhoneNumber.of(351, "912345678"));
    }

    @Test
    void equalPhoneNumbersHaveSameHashCode() {
        final PhoneNumber first = PhoneNumber.of(351, "912345678");
        final PhoneNumber second = PhoneNumber.of(351, "912345678");

        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void hashCodeIsConsistent() {
        final PhoneNumber phoneNumber = PhoneNumber.of(351, "912345678");

        assertEquals(phoneNumber.hashCode(), phoneNumber.hashCode());
    }

    @Test
    void compareToReturnsZeroForEqualPhoneNumbers() {
        final PhoneNumber first = PhoneNumber.of(351, "912345678");
        final PhoneNumber second = PhoneNumber.of(351, "912345678");

        assertEquals(0, first.compareTo(second));
    }

    @Test
    void compareToReturnsNegativeWhenCountryCodeIsLower() {
        final PhoneNumber first = PhoneNumber.of(44, "912345678");
        final PhoneNumber second = PhoneNumber.of(351, "912345678");

        assertTrue(first.compareTo(second) < 0);
    }

    @Test
    void compareToReturnsPositiveWhenCountryCodeIsGreater() {
        final PhoneNumber first = PhoneNumber.of(351, "912345678");
        final PhoneNumber second = PhoneNumber.of(44, "912345678");

        assertTrue(first.compareTo(second) > 0);
    }

    @Test
    void compareToUsesPhoneNumberWhenCountryCodeIsEqual() {
        final PhoneNumber first = PhoneNumber.of(351, "912345678");
        final PhoneNumber second = PhoneNumber.of(351, "913456789");

        assertTrue(first.compareTo(second) < 0);
    }

    @Test
    void compareToReturnsPositiveWhenPhoneNumberIsGreaterAndCountryCodeEqual() {
        final PhoneNumber first = PhoneNumber.of(351, "913456789");
        final PhoneNumber second = PhoneNumber.of(351, "912345678");

        assertTrue(first.compareTo(second) > 0);
    }
}