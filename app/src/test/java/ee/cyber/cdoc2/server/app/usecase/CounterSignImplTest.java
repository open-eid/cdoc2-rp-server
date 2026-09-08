package ee.cyber.cdoc2.server.app.usecase;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;

import ee.cyber.cdoc2.server.app.conf.CountersignKeyConf;
import ee.cyber.cdoc2.server.app.conf.RelyingPartyConf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CounterSignImplTest {

    private static final String RP_NAME = "DEMO";
    private static final String EC_KEY_KID = "rp-server-ec-key-test";

    @Mock
    private RelyingPartyConf relyingPartyConf;

    @Mock
    private CountersignKeyConf countersignKeyConf;

    @InjectMocks
    private CounterSignImpl counterSign;

    @BeforeEach
    void setUp() throws Exception {
        ECKey ecKey = generateTestEcKey();

        lenient().when(relyingPartyConf.getMidName()).thenReturn(RP_NAME);
        lenient().when(countersignKeyConf.getKid()).thenReturn(EC_KEY_KID);
        lenient().when(countersignKeyConf.ecPrivateKey()).thenReturn(ecKey);
    }

    @Test
    void returnsHashAndHeadersForValidSignature() throws Exception {
        byte[] midSignatureBytes = "mid-signature-bytes-here".getBytes(StandardCharsets.UTF_8);

        CounterSign.Response response = counterSign.execute(
            new CounterSign.Request(midSignatureBytes)
        );

        assertNotNull(response);
        // signedHash must be base64(SHA-256(input))
        byte[] expectedHash = MessageDigest.getInstance("SHA-256").digest(midSignatureBytes);
        assertEquals(Base64.getEncoder().encodeToString(expectedHash), response.signedHash());

        // rpName must come from RelyingPartyConf.midName
        assertEquals(RP_NAME, response.rpName());

        // signatureInput must reference the rp-sig label and the configured key id
        assertTrue(response.signatureInput().startsWith("rp-sig="),
            "Expected signature-input to start with rp-sig label, was: "
                + response.signatureInput());
        assertTrue(response.signatureInput().contains("keyid=\"" + EC_KEY_KID + "\""),
            "Expected signature-input to contain configured kid, was: "
                + response.signatureInput());
        assertTrue(response.signatureInput().contains("created="),
            "Expected signature-input to contain created timestamp, was: "
                + response.signatureInput());

        // signature value must be wrapped as rp-sig=:base64:
        assertTrue(response.signature().startsWith("rp-sig=:"),
            "Expected signature value to be wrapped as rp-sig label, was: "
                + response.signature());
        assertTrue(response.signature().endsWith(":"),
            "Expected signature value to be terminated with colon, was: "
                + response.signature());
    }

    @Test
    void throwsOnNullSignature() {
        assertThrows(NullPointerException.class,
            () -> counterSign.execute(new CounterSign.Request(null)));
    }

    @Test
    void propagatesFailureWhenPrivateKeyUnavailable() {
        // Simulate the EC private key bean failing to load — e.g. malformed PEM resource or
        // missing keystore.
        when(countersignKeyConf.ecPrivateKey())
            .thenThrow(new IllegalStateException("EC private key bean failed to initialize"));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> counterSign.execute(new CounterSign.Request(new byte[] {1, 2, 3})));

        assertEquals("EC private key bean failed to initialize", exception.getMessage());
    }

    private static ECKey generateTestEcKey() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair keyPair = generator.generateKeyPair();

        return new ECKey.Builder(Curve.P_256, (ECPublicKey) keyPair.getPublic())
            .privateKey((ECPrivateKey) keyPair.getPrivate())
            .algorithm(JWSAlgorithm.ES256)
            .keyID(UUID.randomUUID().toString())
            .build();
    }
}
