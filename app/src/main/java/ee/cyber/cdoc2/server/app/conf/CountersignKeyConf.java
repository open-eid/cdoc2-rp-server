package ee.cyber.cdoc2.server.app.conf;

import com.nimbusds.jose.jwk.ECKey;

public interface CountersignKeyConf {
    ECKey ecPrivateKey();

    String getKid();
}
