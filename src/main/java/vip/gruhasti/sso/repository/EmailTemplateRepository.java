package vip.gruhasti.sso.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import vip.gruhasti.sso.model.EmailTemplate;

import java.util.Optional;

public interface EmailTemplateRepository extends MongoRepository<EmailTemplate, String> {
    Optional<EmailTemplate> findByType(EmailTemplate.Type type);
}
