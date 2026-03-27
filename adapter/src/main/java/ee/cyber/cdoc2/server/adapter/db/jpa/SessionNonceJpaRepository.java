package ee.cyber.cdoc2.server.adapter.db.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionNonceJpaRepository extends JpaRepository<SessionNonceEntity, Long> {
    boolean existsBySessionNonce(byte[] sessionNonce);
}
