package aisafe.usermanagement;

import aisafe.usermanagement.domain.AiSafeUserFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiSafeUserFactoryTest {

    @Test
    void withPhoneNumberReturnsSameFactory() {
        final AiSafeUserFactory factory = new AiSafeUserFactory();

        final AiSafeUserFactory result = factory.withPhoneNumber(351, "912345678");

        assertSame(factory, result);
    }

    @Test
    void withPhoneNumberWithInvalidCountryCodeThrows() {
        final AiSafeUserFactory factory = new AiSafeUserFactory();

        assertThrows(IllegalArgumentException.class, () ->
                factory.withPhoneNumber(0, "912345678"));
    }

    @Test
    void withPhoneNumberWithInvalidPhoneNumberThrows() {
        final AiSafeUserFactory factory = new AiSafeUserFactory();

        assertThrows(IllegalArgumentException.class, () ->
                factory.withPhoneNumber(351, "912ABC678"));
    }

    @Test
    void buildWithoutSystemUserThrows() {
        final AiSafeUserFactory factory = new AiSafeUserFactory()
                .withPhoneNumber(351, "912345678");

        assertThrows(IllegalArgumentException.class, factory::build);
    }

    @Test
    void buildWithoutPhoneNumberThrows() {
        final AiSafeUserFactory factory = new AiSafeUserFactory();

        assertThrows(IllegalArgumentException.class, factory::build);
    }

    @Test
    void buildWithoutSystemUserAndPhoneNumberThrows() {
        final AiSafeUserFactory factory = new AiSafeUserFactory();

        assertThrows(IllegalArgumentException.class, factory::build);
    }
}