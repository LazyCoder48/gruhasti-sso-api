package vip.gruhasti.sso.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vip.gruhasti.sso.model.User;
import vip.gruhasti.sso.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

/**
 * Idempotently ensures a designated super-admin account exists with both {@code ADMIN} and
 * {@code SUPER_ADMIN} roles, whether that account already exists (e.g. as a self-registered
 * customer) or needs to be created outright.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SuperAdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${gruhasti.super-admin.email}")
    private String superAdminEmail;

    @Value("${gruhasti.super-admin.password}")
    private String superAdminPassword;

    @Override
    public void run(String... args) {
        userRepository.findByEmail(superAdminEmail).ifPresentOrElse(this::ensureSuperAdminRoles,
                this::createSuperAdmin);
    }

    private void ensureSuperAdminRoles(User user) {
        Set<User.Role> roles = new HashSet<>(user.getRoles() != null ? user.getRoles() : Set.of());
        boolean changed = roles.add(User.Role.ADMIN);
        changed |= roles.add(User.Role.SUPER_ADMIN);
        if (changed) {
            user.setRoles(roles);
            userRepository.save(user);
            log.info("Granted ADMIN/SUPER_ADMIN roles to existing user: {}", superAdminEmail);
        }
    }

    private void createSuperAdmin() {
        var admin = new User();
        admin.setEmail(superAdminEmail);
        String[] names = deriveName(superAdminEmail);
        admin.setFirstName(names[0]);
        admin.setLastName(names[1]);
        admin.setPasswordHash(passwordEncoder.encode(superAdminPassword));
        admin.setRoles(Set.of(User.Role.ADMIN, User.Role.SUPER_ADMIN));
        userRepository.save(admin);
        log.info("Seeded super admin user: {}", superAdminEmail);
    }

    private String[] deriveName(String email) {
        int at = email.indexOf('@');
        String localPart = at >= 0 ? email.substring(0, at) : email;
        String[] segments = localPart.split("\\.");
        if (segments.length >= 2) {
            return new String[] { capitalize(segments[0]), capitalize(segments[1]) };
        }
        return new String[] { "Gruhasti", "Super Admin" };
    }

    private String capitalize(String s) {
        if (s.isEmpty()) {
            return s;
        }
        return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
    }
}
