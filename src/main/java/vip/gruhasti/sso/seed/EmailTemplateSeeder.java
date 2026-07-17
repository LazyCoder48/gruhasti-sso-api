package vip.gruhasti.sso.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import vip.gruhasti.sso.model.EmailTemplate;
import vip.gruhasti.sso.repository.EmailTemplateRepository;

import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailTemplateSeeder implements CommandLineRunner {

    private final EmailTemplateRepository emailTemplateRepository;

    @Override
    public void run(String... args) {
        seedIfMissing(EmailTemplate.Type.REGISTRATION_WELCOME, () -> {
            var t = new EmailTemplate();
            t.setType(EmailTemplate.Type.REGISTRATION_WELCOME);
            t.setSubject("Welcome to Gruhasti!");
            t.setHeading("Welcome to Gruhasti!");
            t.setBodyText(
                    "Your Gruhasti.vip account is ready. Sign in once to shop Pen2Paper, Shuddha, and "
                            + "everything else on Gruhasti — all your orders, one account.");
            t.setButtonLabel("Start shopping");
            t.setButtonUrl("https://gruhasti.vip");
            t.setEnabled(true);
            return t;
        });

        seedIfMissing(EmailTemplate.Type.NEW_DEVICE_LOGIN, () -> {
            var t = new EmailTemplate();
            t.setType(EmailTemplate.Type.NEW_DEVICE_LOGIN);
            t.setSubject("New sign-in to your Gruhasti account");
            t.setHeading("New sign-in to your account");
            t.setBodyText("We noticed a sign-in from a browser or device we haven't seen before:");
            t.setButtonLabel("Not you? Reset your password");
            t.setButtonUrl("https://gruhasti.vip/sso-ui/login");
            t.setEnabled(true);
            return t;
        });

        seedIfMissing(EmailTemplate.Type.PASSWORD_RESET, () -> {
            var t = new EmailTemplate();
            t.setType(EmailTemplate.Type.PASSWORD_RESET);
            t.setSubject("Reset your Gruhasti password");
            t.setHeading("Reset your password");
            t.setBodyText(
                    "We received a request to reset your Gruhasti.vip password. This link expires in "
                            + "30 minutes. If you didn't request this, you can safely ignore this email.");
            t.setButtonLabel("Reset password");
            t.setButtonUrl("(unused — this email always links to the one-time reset link, not this URL)");
            t.setEnabled(true);
            return t;
        });

        seedIfMissing(EmailTemplate.Type.PROFILE_UPDATED, () -> {
            var t = new EmailTemplate();
            t.setType(EmailTemplate.Type.PROFILE_UPDATED);
            t.setSubject("Your Gruhasti profile was updated");
            t.setHeading("Your profile was updated");
            t.setBodyText("The following details on your Gruhasti.vip account were just changed:");
            t.setButtonLabel("Go to my profile");
            t.setButtonUrl("https://gruhasti.vip/sso-ui/dashboard");
            t.setEnabled(true);
            return t;
        });
    }

    private void seedIfMissing(EmailTemplate.Type type, Supplier<EmailTemplate> factory) {
        if (emailTemplateRepository.findByType(type).isPresent()) {
            return;
        }
        try {
            emailTemplateRepository.save(factory.get());
            log.info("Seeded default email template: {}", type);
        } catch (DuplicateKeyException e) {
            // Another instance seeded it concurrently — fine, nothing to do.
        }
    }
}
