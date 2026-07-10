package vip.gruhasti.sso.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vip.gruhasti.sso.model.User;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
