package ee.cyber.cdoc2.server.adapter.conf;

import conf.SidTrustedIssuers;
import lombok.RequiredArgsConstructor;

import java.security.KeyStore;

import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SidTrustedIssuersImpl implements SidTrustedIssuers {
    private static final String SID_TRUSTED_ISSUERS_BUNDLE_NAME = "sid-trusted-issuers";

    private final SslBundles sslBundles;

    @Override
    public KeyStore getTrustStore() {
        return sslBundles.getBundle(SID_TRUSTED_ISSUERS_BUNDLE_NAME).getStores().getTrustStore();
    }
}
