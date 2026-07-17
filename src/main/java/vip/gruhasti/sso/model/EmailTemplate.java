package vip.gruhasti.sso.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Admin-editable copy for a transactional email. The Thymeleaf templates under
 * {@code templates/email/} are fixed skeletons that slot these fields in at render time —
 * editing a document here changes what gets sent without touching any template or code.
 */
@Data
@NoArgsConstructor
@Document(collection = "email_templates")
public class EmailTemplate {

    public enum Type {
        REGISTRATION_WELCOME,
        NEW_DEVICE_LOGIN,
        PASSWORD_RESET,
        PROFILE_UPDATED
    }

    @Id
    private String id;

    @Indexed(unique = true)
    private Type type;

    private String subject;
    private String heading;
    private String bodyText;
    private String buttonLabel;
    private String buttonUrl;
    private boolean enabled = true;

    @JsonIgnore
    private byte[] headerImage;
    @JsonIgnore
    private String headerImageContentType;

    public boolean isHeaderImagePresent() {
        return headerImage != null && headerImage.length > 0;
    }
}
