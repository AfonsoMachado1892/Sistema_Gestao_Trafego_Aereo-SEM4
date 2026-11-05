package aisafe.usermanagement.domain;

import eapli.framework.infrastructure.authz.domain.model.PasswordPolicy;
import eapli.framework.strings.util.StringPredicates;

public class AiSafePasswordPolicy implements PasswordPolicy {

    /*
     * (non-Javadoc)
     *
     * @see eapli.framework.infrastructure.authz.domain.model.PasswordPolicy#
     * meetsRequeriments(java.lang.String)
     */
    @Override
    public boolean isSatisfiedBy(final String rawPassword) {
        // sanity check


        // at least 6 characters long
        // at least one digit
        if (StringPredicates.isNullOrEmpty(rawPassword) || (rawPassword.length() < 6) || !StringPredicates.containsDigit(rawPassword)) {
            return false;
        }

        // at least one capital letter
        return StringPredicates.containsCapital(rawPassword);
    }

    /**
     * Check how strong a password is. just for demo purposes.
     *
     * <p>
     * look into
     * https://documentation.cpanel.net/display/CKB/How+to+Determine+Password+Strength
     * for example rules of password strength
     *
     * @param rawPassword the string to check
     *
     * @return how strong a password is
     */
    public PasswordStrength strength(final String rawPassword) {
        var passwordStrength = PasswordStrength.WEAK;
        if (rawPassword.length() >= 12
                || (rawPassword.length() >= 8 && StringPredicates.containsAny(rawPassword, "$#!%&?"))) {
            passwordStrength = PasswordStrength.EXCELENT;
        } else if (rawPassword.length() >= 8) {
            passwordStrength = PasswordStrength.GOOD;
        } else if (rawPassword.length() >= 6) {
            passwordStrength = PasswordStrength.WEAK;
        }
        return passwordStrength;
    }

    public enum PasswordStrength {
        WEAK, GOOD, EXCELENT,
    }
}
