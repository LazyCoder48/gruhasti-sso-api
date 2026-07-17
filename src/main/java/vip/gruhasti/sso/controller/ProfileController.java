package vip.gruhasti.sso.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.gruhasti.sso.dto.AuthResponse;
import vip.gruhasti.sso.dto.ProfileUpdateRequest;
import vip.gruhasti.sso.service.AuthService;

// Deliberately NOT under /api/auth/** — that prefix is permitAll() in SecurityConfig, and
// this endpoint must stay behind the authenticated() catch-all.
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final AuthService authService;

    @PutMapping
    public ResponseEntity<AuthResponse> update(Authentication authentication, @Valid @RequestBody ProfileUpdateRequest req) {
        return ResponseEntity.ok(authService.updateProfile(authentication.getName(), req));
    }
}
