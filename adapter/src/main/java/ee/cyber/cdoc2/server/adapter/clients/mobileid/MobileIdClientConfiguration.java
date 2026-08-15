package ee.cyber.cdoc2.server.adapter.clients.mobileid;

import ee.sk.mid.MidClient;
import lombok.RequiredArgsConstructor;

import java.security.KeyStore;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ee.cyber.cdoc2.server.app.conf.RelyingPartyConf;

@Configuration
@RequiredArgsConstructor
public class MobileIdClientConfiguration {
    private static final String SSL_BUNDLE_NAME = "mid-server";
    private final SslBundles sslBundles;

    @ConfigurationProperties(prefix = "app.mobileid.client")
    public record AppProperties(
        String hostUrl,
        @DefaultValue("1") int statusPollTimeoutSeconds
    ) {
    }

    @Bean
    public MidClient mobileIdClient(AppProperties props, RelyingPartyConf relyingPartyConf) {
        KeyStore trustStore = sslBundles.getBundle(SSL_BUNDLE_NAME).getStores().getTrustStore();

        return MidClient.newBuilder()
            .withHostUrl(props.hostUrl())
            .withRelyingPartyUUID(relyingPartyConf.getMidUuid().toString())
            .withRelyingPartyName(relyingPartyConf.getMidName())
            .withTrustStore(trustStore)
            .build();
    }
}
