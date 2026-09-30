package ee.cyber.cdoc2.server.adapter.conf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import org.springframework.context.annotation.Configuration;

import com.nimbusds.jose.jwk.JWK;

import ee.cyber.cdoc2.server.adapter.rest.AuthServerClient;
import ee.cyber.cdoc2.server.app.conf.AuthServerJwkConf;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AuthServerJwkConfImpl implements AuthServerJwkConf {
    private volatile List<JWK> publicKeys = List.of();
    private final AuthServerClient authServerClient;

    @Override
    public List<JWK> getPublicKeys() {
        List<JWK> keys = this.publicKeys;
        if (keys.isEmpty()) {
            synchronized (this) {
                keys = this.publicKeys;
                if (keys.isEmpty()) {
                    try {
                        keys = List.copyOf(authServerClient.getAuthServerWellKnown());
                        this.publicKeys = keys;
                    } catch (ParseException e) {
                        log.error("Failed to parse Auth server well-known JWK set", e);
                        throw new IllegalStateException("Invalid JWK set from auth server", e);
                    }
                }
            }
        }
        return Collections.unmodifiableList(keys);
    }
}
