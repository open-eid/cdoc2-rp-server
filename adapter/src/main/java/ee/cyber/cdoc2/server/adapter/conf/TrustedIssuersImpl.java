package ee.cyber.cdoc2.server.adapter.conf;

import ee.cyber.cdoc2.server.app.conf.TrustedIssuers;
import lombok.RequiredArgsConstructor;

import java.security.KeyStore;

import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class TrustedIssuersImpl implements TrustedIssuers {
    private static final String TRUSTED_ISSUERS_BUNDLE_NAME = "trusted-issuers";

    private final SslBundles sslBundles;

    @Override
    public KeyStore getTrustStore() {
        return sslBundles.getBundle(TRUSTED_ISSUERS_BUNDLE_NAME).getStores().getTrustStore();
    }
}
