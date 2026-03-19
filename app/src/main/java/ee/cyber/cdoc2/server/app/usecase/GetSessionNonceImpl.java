package ee.cyber.cdoc2.server.app.usecase;


import lombok.RequiredArgsConstructor;

import java.security.SecureRandom;
import java.util.Base64;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

@NullMarked
@RequiredArgsConstructor
@Component
public class GetSessionNonceImpl implements GetSessionNonce {
    private static final int SESSION_NONCE_BYTES = 16;
    private final StoreSessionNonce storeSessionNonce;

    @Override
    public String execute() {
        String sessionNonce = generateSessionNonce();

        storeSessionNonce.execute(sessionNonce);

        return sessionNonce;
    }

    private static String generateSessionNonce() {
        return Base64.getEncoder().encodeToString(generateDummyNonce());
    }

    private static byte[] generateDummyNonce() {
        byte[] nonce = new byte[SESSION_NONCE_BYTES];
        SecureRandom random = new SecureRandom();
        random.nextBytes(nonce);
        return nonce;
    }
}
