package ee.cyber.cdoc2.server.adapter.db.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionNonceJpaRepository extends JpaRepository<SessionNonceEntity, Long> {
    Optional<SessionNonceEntity> findByNonce(byte[] nonce);
}
