package ee.cyber.cdoc2;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.UUID;

import ee.cyber.cdoc2.server.adapter.generated.model.SidAuthenticateRequest;
import ee.cyber.cdoc2.server.adapter.generated.model.SidAuthenticateRequestSignatureProtocolParameters;
import ee.cyber.cdoc2.server.adapter.generated.model.SidAuthenticateRequestSignatureProtocolParametersSignatureAlgorithmParameters;

import static ee.cyber.cdoc2.server.adapter.generated.model.SidAuthenticateRequestSignatureProtocolParameters.*;
import static ee.cyber.cdoc2.server.adapter.generated.model.SidAuthenticateRequestSignatureProtocolParametersSignatureAlgorithmParameters.*;

public class RpRequestUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static SidAuthenticateRequest createSidAuthenticateRequest() {
        var signatureAlgorithmParameters =
            new SidAuthenticateRequestSignatureProtocolParametersSignatureAlgorithmParameters(
                HashAlgorithmEnum.SHA_512
            );

        var signatureProtocolParameters =
            new SidAuthenticateRequestSignatureProtocolParameters()
                .rpChallenge(createRpChallengeBytes())
                .signatureAlgorithm(SignatureAlgorithmEnum.RSASSA_PSS)
                .signatureAlgorithmParameters(signatureAlgorithmParameters);

        String interactions = createSidInteractions();

        return new SidAuthenticateRequest()
            .relyingPartyUUID(UUID.randomUUID())
            .relyingPartyName("DigiDoc4")
            .certificateLevel(SidAuthenticateRequest.CertificateLevelEnum.QUALIFIED)
            .signatureProtocol(SidAuthenticateRequest.SignatureProtocolEnum.ACSP_V2)
            .signatureProtocolParameters(signatureProtocolParameters)
            .interactions(interactions.getBytes(StandardCharsets.UTF_8))
            .vcType(SidAuthenticateRequest.VcTypeEnum.NUMERIC4);
    }

    public static String createSidInteractions() {
        ArrayNode array = OBJECT_MAPPER.createArrayNode();

        ObjectNode node1 = OBJECT_MAPPER.createObjectNode();
        node1.put("type", "confirmationMessage");
        node1.put("displayText200", "Decrypting container file \"test.txt\"");
        array.add(node1);

        ObjectNode node2 = OBJECT_MAPPER.createObjectNode();
        node2.put("type", "displayTextAndPIN");
        node2.put("displayText60", "Decrypting container file \"test.txt\"");
        array.add(node2);

        return OBJECT_MAPPER.writeValueAsString(array);
    }

    public static byte[] createRpChallengeBytes() {
        byte[] rpChallengeBytes = new byte[64];
        new SecureRandom().nextBytes(rpChallengeBytes);
        return rpChallengeBytes;
    }
}
