package ee.cyber.cdoc2.server.adapter.db;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import ee.cyber.cdoc2.server.adapter.db.jpa.SessionNonceEntity;
import ee.cyber.cdoc2.server.adapter.db.jpa.SessionNonceJpaRepository;
import ee.cyber.cdoc2.server.app.usecase.StoreSessionNonce;

@NullMarked
@Repository
@RequiredArgsConstructor
public class SessionNonceRepository implements StoreSessionNonce {
    private final SessionNonceJpaRepository sessionNonceJpaRepository;

    @Override
    @Transactional
    public void execute(byte[] nonce) {
        var sessionNonce = new SessionNonceEntity();
        sessionNonce.setSessionNonce(nonce);

        sessionNonceJpaRepository.save(sessionNonce);
    }
}
