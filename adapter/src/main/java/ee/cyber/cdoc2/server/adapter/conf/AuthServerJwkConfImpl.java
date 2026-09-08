package ee.cyber.cdoc2.server.adapter.conf;

import lombok.RequiredArgsConstructor;

import java.text.ParseException;
import java.util.List;

import org.springframework.context.annotation.Configuration;

import com.nimbusds.jose.jwk.JWK;

import ee.cyber.cdoc2.server.adapter.rest.AuthServerClient;
import ee.cyber.cdoc2.server.app.conf.AuthServerJwkConf;

@Configuration
@RequiredArgsConstructor
public class AuthServerJwkConfImpl implements AuthServerJwkConf {
    private List<JWK> publicKeys = List.of();
    private final AuthServerClient authServerClient;

    @Override
    public List<JWK> getPublicKeys() {
        if (this.publicKeys.isEmpty()) {
            try {
                this.publicKeys = authServerClient.getAuthServerWellKnown();
            } catch (ParseException e) {
                throw new IllegalStateException(e);
            }
        }

        return this.publicKeys;
    }
}
