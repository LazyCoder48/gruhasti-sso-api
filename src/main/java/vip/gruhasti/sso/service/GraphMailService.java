package vip.gruhasti.sso.service;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.models.BodyType;
import com.microsoft.graph.models.EmailAddress;
import com.microsoft.graph.models.ItemBody;
import com.microsoft.graph.models.Message;
import com.microsoft.graph.models.Recipient;
import com.microsoft.graph.core.requests.GraphClientFactory;
import com.microsoft.graph.serviceclient.GraphServiceClient;
import com.microsoft.graph.users.item.sendmail.SendMailPostRequestBody;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Sends mail as the {@code azure.sender-email} mailbox via Microsoft Graph, authenticating
 * with an app-only client-credentials token (requires the Mail.Send application permission
 * with admin consent on the Azure AD app registration).
 */
@Slf4j
@Service
public class GraphMailService {

    private static final Duration TIMEOUT = Duration.ofSeconds(15);

    private final GraphServiceClient graphClient;
    private final TemplateEngine templateEngine;
    private final String senderEmail;

    public GraphMailService(
            @Value("${azure.client-id}") String clientId,
            @Value("${azure.client-secret}") String clientSecret,
            @Value("${azure.tenant-id}") String tenantId,
            @Value("${azure.sender-email}") String senderEmail,
            TemplateEngine templateEngine
    ) {
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .tenantId(tenantId)
                .build();
        OkHttpClient httpClient = GraphClientFactory.create(credential)
                .callTimeout(TIMEOUT)
                .connectTimeout(TIMEOUT)
                .readTimeout(TIMEOUT)
                .writeTimeout(TIMEOUT)
                .build();
        this.graphClient = new GraphServiceClient(httpClient);
        this.templateEngine = templateEngine;
        this.senderEmail = senderEmail;
    }

    /**
     * Best-effort send: failures are logged and swallowed so a Graph/mail outage never
     * breaks the caller's flow (e.g. registration).
     */
    public void sendMail(String toEmail, String subject, String htmlBody) {
        try {
            var body = new ItemBody();
            body.setContentType(BodyType.Html);
            body.setContent(htmlBody);

            var message = new Message();
            message.setSubject(subject);
            message.setBody(body);
            message.setToRecipients(List.of(toRecipient(toEmail)));

            var requestBody = new SendMailPostRequestBody();
            requestBody.setMessage(message);
            requestBody.setSaveToSentItems(true);

            graphClient.users().byUserId(senderEmail).sendMail().post(requestBody);
            log.info("Sent mail to {} via Graph API as {}", toEmail, senderEmail);
        } catch (Exception e) {
            log.warn("Failed to send mail to {} via Graph API: {}", toEmail, e.getMessage());
        }
    }

    /** Renders {@code email/<templateName>.html} with {@code vars} and sends the result. */
    public void sendTemplatedMail(String toEmail, String templateName, Map<String, Object> vars, String subject) {
        Context context = new Context();
        context.setVariables(vars);
        String html = templateEngine.process("email/" + templateName, context);
        sendMail(toEmail, subject, html);
    }

    private Recipient toRecipient(String email) {
        var address = new EmailAddress();
        address.setAddress(email);
        var recipient = new Recipient();
        recipient.setEmailAddress(address);
        return recipient;
    }
}
