package vip.gruhasti.sso.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.gruhasti.sso.model.User;
import vip.gruhasti.sso.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/internal/customers")
@RequiredArgsConstructor
public class InternalCustomerController {

    private final UserRepository userRepository;

    @GetMapping("/active-emails")
    public List<String> activeEmails() {
        return userRepository.findByRolesContainingAndEnabledTrue(User.Role.CUSTOMER)
                .stream()
                .map(User::getEmail)
                .toList();
    }
}
