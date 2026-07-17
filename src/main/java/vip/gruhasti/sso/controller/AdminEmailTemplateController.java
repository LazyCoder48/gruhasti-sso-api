package vip.gruhasti.sso.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import vip.gruhasti.sso.dto.UpdateEmailTemplateRequest;
import vip.gruhasti.sso.model.EmailTemplate;
import vip.gruhasti.sso.repository.EmailTemplateRepository;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/email-templates")
@RequiredArgsConstructor
public class AdminEmailTemplateController {

    private static final Set<String> ALLOWED_HEADER_IMAGE_TYPES =
            Set.of("image/png", "image/jpeg", "image/gif", "image/webp");
    private static final long MAX_HEADER_IMAGE_BYTES = 2L * 1024 * 1024;

    private final EmailTemplateRepository emailTemplateRepository;

    @GetMapping
    public List<EmailTemplate> list() {
        return emailTemplateRepository.findAll();
    }

    @GetMapping("/{type}")
    public EmailTemplate getByType(@PathVariable EmailTemplate.Type type) {
        return find(type);
    }

    @PutMapping("/{type}")
    public EmailTemplate update(@PathVariable EmailTemplate.Type type, @Valid @RequestBody UpdateEmailTemplateRequest req) {
        EmailTemplate template = find(type);
        template.setSubject(req.getSubject());
        template.setHeading(req.getHeading());
        template.setBodyText(req.getBodyText());
        template.setButtonLabel(req.getButtonLabel());
        template.setButtonUrl(req.getButtonUrl());
        return emailTemplateRepository.save(template);
    }

    @PostMapping("/{type}/header-image")
    public EmailTemplate uploadHeaderImage(@PathVariable EmailTemplate.Type type, @RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file was uploaded");
        }
        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_HEADER_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unsupported image type. Allowed types: PNG, JPEG, GIF, WEBP");
        }
        if (file.getSize() > MAX_HEADER_IMAGE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image must be 2MB or smaller");
        }

        EmailTemplate template = find(type);
        try {
            template.setHeaderImage(file.getBytes());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read the uploaded file");
        }
        template.setHeaderImageContentType(contentType);
        return emailTemplateRepository.save(template);
    }

    @DeleteMapping("/{type}/header-image")
    public EmailTemplate removeHeaderImage(@PathVariable EmailTemplate.Type type) {
        EmailTemplate template = find(type);
        template.setHeaderImage(null);
        template.setHeaderImageContentType(null);
        return emailTemplateRepository.save(template);
    }

    private EmailTemplate find(EmailTemplate.Type type) {
        return emailTemplateRepository.findByType(type)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email template not found"));
    }
}
