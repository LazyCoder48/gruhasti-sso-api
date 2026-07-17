package vip.gruhasti.sso.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@Document(collection = "known_devices")
public class KnownDevice {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String deviceHash;
    private Instant firstSeenAt;
    private Instant lastSeenAt;
}
