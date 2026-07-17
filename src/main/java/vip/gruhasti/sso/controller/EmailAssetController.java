package vip.gruhasti.sso.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import vip.gruhasti.sso.model.EmailTemplate;
import vip.gruhasti.sso.repository.EmailTemplateRepository;

import java.time.Duration;

/**
 * Serves email assets (e.g. the per-template header banner image) that must be reachable
 * WITHOUT authentication — recipients' mail clients (Gmail, Outlook, Apple Mail, etc.) load
 * {@code <img>} sources directly from the recipient's device and never send this app's auth
 * headers or cookies. Deliberately NOT under {@code /api/admin/**}.
 */
@RestController
@RequestMapping("/api/email-templates")
@RequiredArgsConstructor
public class EmailAssetController {

    private final EmailTemplateRepository emailTemplateRepository;

    @GetMapping("/{type}/header-image")
    public ResponseEntity<byte[]> headerImage(@PathVariable EmailTemplate.Type type) {
        EmailTemplate template = emailTemplateRepository.findByType(type)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email template not found"));

        if (!template.isHeaderImagePresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No header image set for this template");
        }

        MediaType mediaType = MediaType.parseMediaType(template.getHeaderImageContentType());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.maxAge(Duration.ofHours(24)).cachePublic())
                .body(template.getHeaderImage());
    }
}
