package vip.gruhasti.sso.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vip.gruhasti.sso.model.User;
import vip.gruhasti.sso.repository.UserRepository;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${gruhasti.admin.email}")
    private String adminEmail;

    @Value("${gruhasti.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }
        var admin = new User();
        admin.setEmail(adminEmail);
        admin.setFirstName("Gruhasti");
        admin.setLastName("Admin");
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRoles(Set.of(User.Role.ADMIN));
        userRepository.save(admin);
        log.info("Seeded default admin user: {}", adminEmail);
    }
}
