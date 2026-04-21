package ee.cyber.cdoc2.server.adapter.conf;

import conf.AuthServerJwkConf;
import lombok.RequiredArgsConstructor;

import java.text.ParseException;
import java.util.List;

import org.springframework.context.annotation.Configuration;

import com.nimbusds.jose.jwk.JWK;

import ee.cyber.cdoc2.server.adapter.rest.AuthServerClient;

@Configuration
@RequiredArgsConstructor
public class AuthServerJwkConfImpl implements AuthServerJwkConf {
    private List<JWK> publicKeys;
    private final AuthServerClient authServerClient;

    @Override
    public List<JWK> getPublicKeys() {
        if (this.publicKeys == null) {
            try {
                this.publicKeys = authServerClient.getAuthServerWellKnown();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        return this.publicKeys;
    }
}
