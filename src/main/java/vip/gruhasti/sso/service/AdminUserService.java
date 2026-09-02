package vip.gruhasti.sso.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import vip.gruhasti.sso.dto.AdminUserDto;
import vip.gruhasti.sso.model.User;
import vip.gruhasti.sso.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    public List<AdminUserDto> listAll() {
        return userRepository.findAll().stream()
                .map(AdminUserDto::from)
                .toList();
    }

    public AdminUserDto setAdminRole(String targetUserId, boolean isAdmin, String callerUserId) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (target.getRoles() != null && target.getRoles().contains(User.Role.SUPER_ADMIN)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cannot change the admin role of a super admin.");
        }

        if (targetUserId.equals(callerUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You cannot change your own admin role.");
        }

        Set<User.Role> roles = new HashSet<>(target.getRoles() != null ? target.getRoles() : Set.of());
        if (isAdmin) {
            roles.add(User.Role.ADMIN);
        } else {
            roles.remove(User.Role.ADMIN);
        }
        target.setRoles(roles);
        userRepository.save(target);
        return AdminUserDto.from(target);
    }
}
