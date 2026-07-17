package vip.gruhasti.sso.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vip.gruhasti.sso.dto.AuthResponse;
import vip.gruhasti.sso.dto.LoginRequest;
import vip.gruhasti.sso.dto.ProfileUpdateRequest;
import vip.gruhasti.sso.dto.RegisterRequest;
import vip.gruhasti.sso.exception.AuthException;
import vip.gruhasti.sso.model.EmailTemplate;
import vip.gruhasti.sso.model.KnownDevice;
import vip.gruhasti.sso.model.PasswordResetToken;
import vip.gruhasti.sso.model.User;
import vip.gruhasti.sso.repository.EmailTemplateRepository;
import vip.gruhasti.sso.repository.KnownDeviceRepository;
import vip.gruhasti.sso.repository.PasswordResetTokenRepository;
import vip.gruhasti.sso.repository.UserRepository;
import vip.gruhasti.sso.security.DeviceFingerprint;
import vip.gruhasti.sso.security.JwtUtil;
import vip.gruhasti.sso.security.TokenHasher;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final DateTimeFormatter SIGNED_IN_AT_FORMAT =
            DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a 'UTC'").withZone(ZoneOffset.UTC);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final GraphMailService graphMailService;
    private final KnownDeviceRepository knownDeviceRepository;
    private final DeviceFingerprint deviceFingerprint;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenHasher tokenHasher;
    private final EmailTemplateRepository emailTemplateRepository;

    @Value("${gruhasti.sso-ui-origin}")
    private String ssoUiOrigin;

    @Value("${gruhasti.public-base-url}")
    private String publicBaseUrl;

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
        sendTemplatedEmail(user.getEmail(), EmailTemplate.Type.REGISTRATION_WELCOME,
                Map.of("firstName", user.getFirstName()));
        return authResponse(user);
    }

    public AuthResponse login(LoginRequest req, String userAgent) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new AuthException("Invalid email or password"));
        if (!user.isEnabled()) {
            throw new AuthException("Account is disabled");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new AuthException("Invalid email or password");
        }
        checkDevice(user, userAgent);
        return authResponse(user);
    }

    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String rawToken = tokenHasher.generateToken();
            var resetToken = new PasswordResetToken();
            resetToken.setUserId(user.getId());
            resetToken.setTokenHash(tokenHasher.hash(rawToken));
            resetToken.setCreatedAt(Instant.now());
            resetToken.setExpiresAt(Instant.now().plusSeconds(1800));
            passwordResetTokenRepository.save(resetToken);

            String resetUrl = ssoUiOrigin + "/reset-password?token=" + rawToken;
            sendTemplatedEmail(user.getEmail(), EmailTemplate.Type.PASSWORD_RESET,
                    Map.of("resetUrl", resetUrl));
        });
        // Always returns normally regardless of whether the email exists — anti-enumeration.
    }

    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHasher.hash(rawToken))
                .orElseThrow(() -> new AuthException("Invalid or expired reset link"));
        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new AuthException("Invalid or expired reset link");
        }
        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new AuthException("Invalid or expired reset link"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    public AuthResponse me(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));
        return authResponse(user);
    }

    public AuthResponse updateProfile(String userId, ProfileUpdateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));

        List<String> changes = new ArrayList<>();
        if (!Objects.equals(user.getFirstName(), req.getFirstName())) {
            changes.add("First name changed to " + req.getFirstName());
        }
        if (!Objects.equals(user.getLastName(), req.getLastName())) {
            changes.add("Last name changed to " + req.getLastName());
        }
        if (!Objects.equals(user.getMobile(), req.getMobile())) {
            changes.add("Mobile number updated");
        }
        if (!Objects.equals(user.getFlatNumber(), req.getFlatNumber())) {
            changes.add("Address updated");
        }

        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setMobile(req.getMobile());
        user.setFlatNumber(req.getFlatNumber());
        userRepository.save(user);

        if (!changes.isEmpty()) {
            sendTemplatedEmail(user.getEmail(), EmailTemplate.Type.PROFILE_UPDATED,
                    Map.of("firstName", user.getFirstName(), "changes", changes));
        }

        return authResponse(user);
    }

    private void checkDevice(User user, String userAgent) {
        DeviceFingerprint.Device device = deviceFingerprint.describe(userAgent);
        String hash = deviceFingerprint.hash(device);
        var existing = knownDeviceRepository.findByUserIdAndDeviceHash(user.getId(), hash);

        if (existing.isPresent()) {
            KnownDevice known = existing.get();
            known.setLastSeenAt(Instant.now());
            knownDeviceRepository.save(known);
            return;
        }

        var known = new KnownDevice();
        known.setUserId(user.getId());
        known.setDeviceHash(hash);
        known.setFirstSeenAt(Instant.now());
        known.setLastSeenAt(Instant.now());
        knownDeviceRepository.save(known);

        sendTemplatedEmail(user.getEmail(), EmailTemplate.Type.NEW_DEVICE_LOGIN,
                Map.of(
                        "firstName", user.getFirstName(),
                        "browserFamily", device.browserFamily(),
                        "osFamily", device.osFamily(),
                        "signedInAt", SIGNED_IN_AT_FORMAT.format(Instant.now())
                ));
    }

    /**
     * Loads the admin-editable copy for {@code type} and merges it with the event's own
     * dynamic vars before rendering — this is what makes an admin's edits via the Email
     * Templates admin page actually change what gets sent, rather than editing a Mongo
     * document the render path never reads from.
     */
    private void sendTemplatedEmail(String toEmail, EmailTemplate.Type type, Map<String, Object> dynamicVars) {
        emailTemplateRepository.findByType(type).ifPresentOrElse(template -> {
            Map<String, Object> vars = new HashMap<>(dynamicVars);
            vars.put("heading", template.getHeading());
            vars.put("bodyText", template.getBodyText());
            vars.put("buttonLabel", template.getButtonLabel());
            vars.put("buttonUrl", template.getButtonUrl());
            if (template.isHeaderImagePresent()) {
                vars.put("headerImageUrl", publicBaseUrl + "/api/email-templates/" + type.name() + "/header-image");
            }
            graphMailService.sendTemplatedMail(toEmail, templateFileName(type), vars, template.getSubject());
        }, () -> log.warn("No EmailTemplate found for type {}, skipping send to {}", type, toEmail));
    }

    private String templateFileName(EmailTemplate.Type type) {
        return switch (type) {
            case REGISTRATION_WELCOME -> "registration-welcome";
            case NEW_DEVICE_LOGIN -> "new-device-login";
            case PASSWORD_RESET -> "password-reset";
            case PROFILE_UPDATED -> "profile-updated";
        };
    }

    private AuthResponse authResponse(User user) {
        String token = jwtUtil.generate(user.getId(), user.getEmail(), user.getFirstName(), user.getRoles());
        return new AuthResponse(token, user.getId(), user.getEmail(),
                user.getFirstName(), user.getLastName(), user.getMobile(), user.getFlatNumber(), user.getRoles());
    }
}
