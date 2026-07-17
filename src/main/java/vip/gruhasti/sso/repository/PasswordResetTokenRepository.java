package vip.gruhasti.sso.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vip.gruhasti.sso.model.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
}
