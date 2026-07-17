package vip.gruhasti.sso.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@Document(collection = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed(unique = true)
    private String tokenHash;

    // Backstop cleanup — Mongo's TTL monitor runs on its own ~60s cycle, so this is in
    // addition to (not instead of) the explicit expiresAt check in code.
    @Indexed(expireAfterSeconds = 1800)
    private Instant createdAt;

    private Instant expiresAt;
    private boolean used = false;
}
