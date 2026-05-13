package ee.cyber.cdoc2;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import ee.cyber.cdoc2.server.adapter.db.SessionNonceCleanUpJob;
import ee.cyber.cdoc2.server.adapter.db.jpa.SessionNonceEntity;
import ee.cyber.cdoc2.server.adapter.db.jpa.SessionNonceJpaRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class SessionNonceCleanUpJobTest {

    @Autowired
    private SessionNonceJpaRepository sessionNonceJpaRepository;

    @Autowired
    private SessionNonceCleanUpJob sessionNonceCleanUpJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        sessionNonceJpaRepository.deleteAll();
    }

    @Test
    void shouldDeleteExpiredNonce() {
        var entity = saveNonce();
        backdateNonce(entity.getId(), 25);

        sessionNonceCleanUpJob.deleteExpiredSessionNonces();

        assertFalse(sessionNonceJpaRepository.findById(entity.getId()).isPresent());
    }

    @Test
    void shouldNotDeleteNonExpiredNonce() {
        var entity = saveNonce();

        sessionNonceCleanUpJob.deleteExpiredSessionNonces();

        assertTrue(sessionNonceJpaRepository.findById(entity.getId()).isPresent());
    }

    @Test
    void shouldDeleteOnlyExpiredNoncesWhenMixed() {
        var expiredNonce = saveNonce();
        backdateNonce(expiredNonce.getId(), 25);
        var recentNonce = saveNonce();

        sessionNonceCleanUpJob.deleteExpiredSessionNonces();

        assertFalse(sessionNonceJpaRepository.findById(expiredNonce.getId()).isPresent());
        assertTrue(sessionNonceJpaRepository.findById(recentNonce.getId()).isPresent());
    }

    @Test
    void shouldDeleteMultipleExpiredNonces() {
        for (int i = 0; i < 5; i++) {
            var entity = saveNonce();
            backdateNonce(entity.getId(), 25);
        }

        sessionNonceCleanUpJob.deleteExpiredSessionNonces();

        assertEquals(0, sessionNonceJpaRepository.count());
    }

    private SessionNonceEntity saveNonce() {
        byte[] nonce = new byte[16];
        ThreadLocalRandom.current().nextBytes(nonce);
        var entity = new SessionNonceEntity();
        entity.setNonce(nonce);
        return sessionNonceJpaRepository.save(entity);
    }

    private void backdateNonce(Long id, int hoursAgo) {
        jdbcTemplate.update(
            "UPDATE SESSION_NONCE SET CREATED_AT = ? WHERE ID = ?",
            Timestamp.from(Instant.now().minus(hoursAgo, ChronoUnit.HOURS)),
            id
        );
    }
}
