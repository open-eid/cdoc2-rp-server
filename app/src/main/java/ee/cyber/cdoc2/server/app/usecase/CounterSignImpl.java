package ee.cyber.cdoc2.server.app.usecase;

import lombok.RequiredArgsConstructor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.authlete.hms.ComponentIdentifier;
import com.authlete.hms.ComponentValueProvider;
import com.authlete.hms.SignatureBase;
import com.authlete.hms.SignatureBaseBuilder;
import com.authlete.hms.SignatureContext;
import com.authlete.hms.SignatureMetadata;
import com.authlete.hms.SignatureMetadataParameters;
import com.authlete.hms.impl.JoseHttpSigner;

import ee.cyber.cdoc2.server.app.conf.JwtKeysConf;
import ee.cyber.cdoc2.server.app.conf.RelyingPartyConf;

@Component
@RequiredArgsConstructor
public class CounterSignImpl implements CounterSign {
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final String SIGNATURE_LABEL = "rp-sig";

    private final RelyingPartyConf relyingPartyConf;
    private final JwtKeysConf jwtKeysConf;

    @Override
    public Response execute(Request request) {
        String hashToSign = Base64.getEncoder().encodeToString(
            createHashToSign(request.signature())
        );

        String rpName = relyingPartyConf.getMidName();

        ComponentValueProvider ctx = new ComponentValueProvider()
            .setHeaders(Map.of(
                "x-rp-signed-hash", List.of(hashToSign),
                "x-rp-name", List.of(rpName)
            ));

        SignatureMetadataParameters params = new SignatureMetadataParameters()
            .setCreated(Instant.now())
            .setKeyid(jwtKeysConf.getKid());

        SignatureMetadata metadata = new SignatureMetadata(
            List.of(
                new ComponentIdentifier("x-rp-signed-hash"),
                new ComponentIdentifier("x-rp-name")
            ),
            params
        );

        byte[] signatureBytes = signSignatureMetadata(metadata, ctx);

        String signatureInput = SIGNATURE_LABEL + "=" + metadata.serialize();
        String signatureValue = SIGNATURE_LABEL + "=:" + Base64.getEncoder()
            .encodeToString(signatureBytes) + ":";

        return new Response(
            hashToSign,
            rpName,
            signatureInput,
            signatureValue
        );
    }

    private byte[] createHashToSign(byte[] dataToSign) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            return digest.digest(dataToSign);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] signSignatureMetadata(
        SignatureMetadata metadata,
        SignatureContext context
    ) {
        try {
            SignatureBase base = new SignatureBaseBuilder(context).build(metadata);
            return base.sign(new JoseHttpSigner(
                jwtKeysConf.ecPrivateKey()
            ));
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }
    }
}
