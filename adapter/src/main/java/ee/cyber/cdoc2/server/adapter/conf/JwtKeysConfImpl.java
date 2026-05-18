package ee.cyber.cdoc2.server.adapter.conf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;

import ee.cyber.cdoc2.server.adapter.resource.ResourceLoaderWrapper;
import ee.cyber.cdoc2.server.app.conf.JwtKeysConf;

@Configuration
public class JwtKeysConfImpl implements JwtKeysConf {
    private final ResourceLoaderWrapper resourceLoader;
    private final ECKey ecPrivateKey;
    private final String ecKeyKid;

    @ConfigurationProperties(prefix = "app.well-known")
    public record AppProperties(
        String ecPrivateKeyName,
        String ecKeyKid
    ) {
    }

    public JwtKeysConfImpl(
        AppProperties props,
        ResourceLoaderWrapper resourceLoader
    ) throws JOSEException,
        IOException {
        this.resourceLoader = resourceLoader;
        String ecPrivatePem = readFile(props.ecPrivateKeyName());
        ECKey ecKeyWithoutAlg = JWK.parseFromPEMEncodedObjects(ecPrivatePem).toECKey();

        this.ecPrivateKey = new ECKey.Builder(ecKeyWithoutAlg)
            .algorithm(JWSAlgorithm.ES256)
            .build();
        this.ecKeyKid = props.ecKeyKid();
    }

    @Override
    public ECKey ecPrivateKey() {
        return this.ecPrivateKey;
    }


    @Override
    public String getEcKeyKid() {
        return ecKeyKid;
    }

    private String readFile(String name) throws IOException {
        try (InputStream is = resourceLoader.loadResource(name).getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
