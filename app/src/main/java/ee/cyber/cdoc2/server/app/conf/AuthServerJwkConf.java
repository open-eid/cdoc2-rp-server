package ee.cyber.cdoc2.server.app.conf;


import java.util.List;

import org.jspecify.annotations.Nullable;

import com.nimbusds.jose.jwk.JWK;

public interface AuthServerJwkConf {
    @Nullable List<JWK> getPublicKeys();
}
