package vip.gruhasti.sso.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.gruhasti.sso.dto.AdminUserDto;
import vip.gruhasti.sso.dto.SetAdminRoleRequest;
import vip.gruhasti.sso.service.AdminUserService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public List<AdminUserDto> list() {
        return adminUserService.listAll();
    }

    @PutMapping("/{id}/admin-role")
    public AdminUserDto setAdminRole(Authentication authentication, @PathVariable String id,
            @Valid @RequestBody SetAdminRoleRequest req) {
        return adminUserService.setAdminRole(id, req.getIsAdmin(), authentication.getName());
    }
}
