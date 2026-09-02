package vip.gruhasti.sso.dto;

import vip.gruhasti.sso.model.User;

import java.time.Instant;
import java.util.Set;

public record AdminUserDto(
        String id,
        String email,
        String firstName,
        String lastName,
        String mobile,
        String flatNumber,
        Set<User.Role> roles,
        boolean enabled,
        boolean mustChangePassword,
        Instant createdAt
) {
    public static AdminUserDto from(User user) {
        return new AdminUserDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getMobile(),
                user.getFlatNumber(),
                user.getRoles(),
                user.isEnabled(),
                user.isMustChangePassword(),
                user.getCreatedAt()
        );
    }
}
