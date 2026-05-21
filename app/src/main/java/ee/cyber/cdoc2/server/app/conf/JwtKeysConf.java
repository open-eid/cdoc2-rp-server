package ee.cyber.cdoc2.server.app.conf;

import com.nimbusds.jose.jwk.ECKey;

public interface JwtKeysConf {
    ECKey ecPrivateKey();

    String getKid();
}
