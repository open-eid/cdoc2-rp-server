package ee.cyber.cdoc2.server.app.conf;


import java.util.List;

import com.nimbusds.jose.jwk.JWK;

public interface AuthServerJwkConf {
    List<JWK> getPublicKeys();
}
