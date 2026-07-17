package vip.gruhasti.sso.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateEmailTemplateRequest {
    @NotBlank
    private String subject;
    @NotBlank
    private String heading;
    @NotBlank
    private String bodyText;
    @NotBlank
    private String buttonLabel;
    @NotBlank
    private String buttonUrl;
}
