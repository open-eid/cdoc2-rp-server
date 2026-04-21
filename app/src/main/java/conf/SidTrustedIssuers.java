package conf;

import java.security.KeyStore;

public interface SidTrustedIssuers {
    KeyStore getTrustStore();
}
