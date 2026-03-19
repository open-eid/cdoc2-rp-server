package ee.cyber.cdoc2.server.adapter.db;

import java.util.HashMap;
import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Repository;

import ee.cyber.cdoc2.server.app.usecase.StoreSessionNonce;

@NullMarked
@Repository
public class SessionNonceRepository implements StoreSessionNonce {
    private final HashMap<UUID, String> inMemoryDb = new HashMap<>();

    @Override
    public void execute(String nonce) {
        inMemoryDb.put(UUID.randomUUID(), nonce);
    }
}
