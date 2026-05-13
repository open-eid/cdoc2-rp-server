package ee.cyber.cdoc2.server.adapter.db.jpa;

import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionNonceJpaRepository extends JpaRepository<SessionNonceEntity, Long> {
    Optional<SessionNonceEntity> findByNonce(byte[] nonce);

    boolean existsByNonce(byte[] nonce);

    @Modifying
    @Transactional
    @Query(
        value = "DELETE FROM session_nonce WHERE id IN "
            + "(SELECT id FROM session_nonce WHERE created_at < :createdAtCutoff LIMIT :deletionLimit)",
        nativeQuery = true
    )
    int deleteExpiredNonces(
        @Param("createdAtCutoff") Instant cutoff,
        @Param("deletionLimit") int deletionLimit
    );
}
