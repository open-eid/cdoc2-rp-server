package ee.cyber.cdoc2.server.app.usecase;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;

class GetSessionNonceImplTest {

    @Test
    void httpSig() throws JOSEException {

        String pem = """
            -----BEGIN PUBLIC KEY-----
            MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAESIsDcu6c2CjOEIxZyh4ctZZA+zz4
            pFYv0duHPlNWinXnR0Lng+k5W0EcEbLNbVLoOBoG2z7LBjcqA5yVIhX3sw==
            -----END PUBLIC KEY-----
            
            """;

// Parse PEM and convert to JWK in one shot
        JWK jwk = ECKey.parseFromPEMEncodedObjects(pem);

// Get as JSON string
        String jwkJson = jwk.toJSONString();

// Or as a JWK Set
        JWKSet jwkSet = new JWKSet(jwk);
        String jwkSetJson = jwkSet.toString();

        Assertions.assertEquals("", "");
    }
}
