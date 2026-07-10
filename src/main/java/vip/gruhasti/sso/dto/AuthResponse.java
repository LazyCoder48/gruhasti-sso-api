package vip.gruhasti.sso.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import vip.gruhasti.sso.model.User;

import java.util.Set;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String mobile;
    private String flatNumber;
    private Set<User.Role> roles;
}
