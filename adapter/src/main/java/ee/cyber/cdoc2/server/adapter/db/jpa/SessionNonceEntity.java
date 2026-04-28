package ee.cyber.cdoc2.server.adapter.db.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Data
@Table(name = "SESSION_NONCE")
@SequenceGenerator(
    name = "SESSION_NONCE_PK_SEQUENCE",
    sequenceName = "SESSION_NONCE_PK_SEQUENCE",
    allocationSize = 1
)
public class SessionNonceEntity {
    @Id
    @GeneratedValue(generator = "SESSION_NONCE_PK_SEQUENCE")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "nonce", nullable = false)
    private byte[] nonce;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
