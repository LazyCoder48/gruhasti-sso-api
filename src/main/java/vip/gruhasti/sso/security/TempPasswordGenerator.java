package vip.gruhasti.sso.security;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates a random, human-typeable temporary password for a freshly registered account.
 * This is NOT a token (see TokenHasher) — it's a real password an admin-uninvolved end user
 * reads out of an email and types into a login form, so it deliberately excludes visually
 * ambiguous characters (0/O, 1/l/I) and stays short enough to type without transcription errors.
 */
@Component
public class TempPasswordGenerator {

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String ALL = UPPER + LOWER + DIGITS;
    private static final int LENGTH = 12;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder sb = new StringBuilder(LENGTH);
        // Guarantee at least one of each character class, then fill the rest randomly.
        sb.append(UPPER.charAt(random.nextInt(UPPER.length())));
        sb.append(LOWER.charAt(random.nextInt(LOWER.length())));
        sb.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        for (int i = 3; i < LENGTH; i++) {
            sb.append(ALL.charAt(random.nextInt(ALL.length())));
        }
        // Shuffle so the guaranteed characters aren't always in fixed positions.
        for (int i = sb.length() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = sb.charAt(i);
            sb.setCharAt(i, sb.charAt(j));
            sb.setCharAt(j, tmp);
        }
        return sb.toString();
    }
}
