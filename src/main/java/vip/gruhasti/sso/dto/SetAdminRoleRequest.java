package vip.gruhasti.sso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetAdminRoleRequest {

    @NotNull
    private Boolean isAdmin;
}
