package vip.gruhasti.sso.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vip.gruhasti.sso.dto.AuthResponse;
import vip.gruhasti.sso.dto.LoginRequest;
import vip.gruhasti.sso.dto.RegisterRequest;
import vip.gruhasti.sso.exception.AuthException;
import vip.gruhasti.sso.model.User;
import vip.gruhasti.sso.repository.UserRepository;
import vip.gruhasti.sso.security.JwtUtil;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new AuthException("Email already in use");
        }
        var user = new User();
        user.setEmail(req.getEmail());
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setMobile(req.getMobile());
        user.setFlatNumber(req.getFlatNumber());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRoles(req.getRoles() != null && !req.getRoles().isEmpty()
                ? req.getRoles() : Set.of(User.Role.CUSTOMER));
        userRepository.save(user);
        String token = jwtUtil.generate(user.getId(), user.getEmail(), user.getRoles());
        return new AuthResponse(token, user.getId(), user.getEmail(),
                user.getFirstName(), user.getLastName(), user.getMobile(), user.getFlatNumber(), user.getRoles());
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new AuthException("Invalid email or password"));
        if (!user.isEnabled()) {
            throw new AuthException("Account is disabled");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new AuthException("Invalid email or password");
        }
        String token = jwtUtil.generate(user.getId(), user.getEmail(), user.getRoles());
        return new AuthResponse(token, user.getId(), user.getEmail(),
                user.getFirstName(), user.getLastName(), user.getMobile(), user.getFlatNumber(), user.getRoles());
    }
}
