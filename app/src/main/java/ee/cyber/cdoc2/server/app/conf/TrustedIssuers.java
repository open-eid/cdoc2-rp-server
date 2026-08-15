package ee.cyber.cdoc2.server.app.conf;

import java.security.KeyStore;

public interface TrustedIssuers {
    KeyStore getTrustStore();
}
