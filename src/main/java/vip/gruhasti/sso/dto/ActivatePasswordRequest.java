package vip.gruhasti.sso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ActivatePasswordRequest {

    @NotBlank
    @Size(min = 8, max = 100)
    private String newPassword;
}
