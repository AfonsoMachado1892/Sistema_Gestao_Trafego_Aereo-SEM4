package aisafe.usermanagement;

import aisafe.usermanagement.domain.AiSafePasswordPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiSafePasswordPolicyTest {

    private final AiSafePasswordPolicy policy = new AiSafePasswordPolicy();

    @Test
    void validPasswordIsAccepted() {
        assertTrue(policy.isSatisfiedBy("Abcde1"));
    }

    @Test
    void nullPasswordIsRejected() {
        assertFalse(policy.isSatisfiedBy(null));
    }

    @Test
    void emptyPasswordIsRejected() {
        assertFalse(policy.isSatisfiedBy(""));
    }

    @Test
    void passwordWithLessThanSixCharactersIsRejected() {
        assertFalse(policy.isSatisfiedBy("Abc1"));
    }

    @Test
    void passwordWithoutDigitIsRejected() {
        assertFalse(policy.isSatisfiedBy("Abcdef"));
    }

    @Test
    void passwordWithoutCapitalLetterIsRejected() {
        assertFalse(policy.isSatisfiedBy("abcde1"));
    }

    @Test
    void passwordWithSixCharactersDigitAndCapitalIsAccepted() {
        assertTrue(policy.isSatisfiedBy("Abcde1"));
    }

    @Test
    void passwordWithMoreThanSixCharactersDigitAndCapitalIsAccepted() {
        assertTrue(policy.isSatisfiedBy("Password1"));
    }

    @Test
    void strengthIsWeakForSixCharacterPassword() {
        assertEquals(
                AiSafePasswordPolicy.PasswordStrength.WEAK,
                policy.strength("Abcde1")
        );
    }

    @Test
    void strengthIsGoodForEightCharacterPasswordWithoutSpecialCharacter() {
        assertEquals(
                AiSafePasswordPolicy.PasswordStrength.GOOD,
                policy.strength("Abcdef12")
        );
    }

    @Test
    void strengthIsExcelentForEightCharacterPasswordWithSpecialCharacter() {
        assertEquals(
                AiSafePasswordPolicy.PasswordStrength.EXCELENT,
                policy.strength("Abcdef1$")
        );
    }

    @Test
    void strengthIsExcelentForTwelveCharacterPassword() {
        assertEquals(
                AiSafePasswordPolicy.PasswordStrength.EXCELENT,
                policy.strength("Abcdefghij12")
        );
    }

    @Test
    void strengthIsWeakForShortPassword() {
        assertEquals(
                AiSafePasswordPolicy.PasswordStrength.WEAK,
                policy.strength("abc")
        );
    }

    @Test
    void strengthIsExcellentForEightCharacterPasswordWithHash() {
        assertEquals(
                AiSafePasswordPolicy.PasswordStrength.EXCELENT,
                policy.strength("Abcdef1#")
        );
    }

    @Test
    void strengthIsExcellentForEightCharacterPasswordWithExclamation() {
        assertEquals(
                AiSafePasswordPolicy.PasswordStrength.EXCELENT,
                policy.strength("Abcdef1!")
        );
    }

    @Test
    void strengthIsExcellentForEightCharacterPasswordWithPercent() {
        assertEquals(
                AiSafePasswordPolicy.PasswordStrength.EXCELENT,
                policy.strength("Abcdef1%")
        );
    }

    @Test
    void strengthIsExcellentForEightCharacterPasswordWithAmpersand() {
        assertEquals(
                AiSafePasswordPolicy.PasswordStrength.EXCELENT,
                policy.strength("Abcdef1&")
        );
    }

    @Test
    void strengthIsExcellentForEightCharacterPasswordWithQuestionMark() {
        assertEquals(
                AiSafePasswordPolicy.PasswordStrength.EXCELENT,
                policy.strength("Abcdef1?")
        );
    }
}