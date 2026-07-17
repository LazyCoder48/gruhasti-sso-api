package vip.gruhasti.sso.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Generates high-entropy one-time tokens (password reset links) and their lookup hash.
 * SHA-256, not BCrypt: BCrypt salts per call, so hashing the same raw token twice yields two
 * different strings, which makes a "hash the incoming token, look up by hash" query impossible.
 * The token itself is already 256 bits of SecureRandom, so a deterministic hash carries none
 * of the rainbow-table risk that applies to low-entropy secrets like passwords.
 */
@Component
public class TokenHasher {

    private final SecureRandom random = new SecureRandom();

    public String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    public String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
