package ee.cyber.cdoc2.server.app.usecase;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface StoreSessionNonce {

    void execute(byte[] nonce);
}
