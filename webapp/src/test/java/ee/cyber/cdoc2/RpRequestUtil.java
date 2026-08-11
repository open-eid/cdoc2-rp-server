package ee.cyber.cdoc2;

import ee.sk.mid.rest.dao.MidSessionSignature;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.smartid.rest.dao.SessionCertificate;
import ee.sk.smartid.rest.dao.SessionMaskGenAlgorithm;
import ee.sk.smartid.rest.dao.SessionMaskGenAlgorithmParameters;
import ee.sk.smartid.rest.dao.SessionResult;
import ee.sk.smartid.rest.dao.SessionSignature;
import ee.sk.smartid.rest.dao.SessionSignatureAlgorithmParameters;
import ee.sk.smartid.rest.dao.SessionStatus;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ee.cyber.cdoc2.server.adapter.generated.model.AuthCertificateLevel;
import ee.cyber.cdoc2.server.adapter.generated.model.AuthSignatureProtocol;
import ee.cyber.cdoc2.server.adapter.generated.model.AuthSignatureProtocolParameters;
import ee.cyber.cdoc2.server.adapter.generated.model.HashAlgorithm;
import ee.cyber.cdoc2.server.adapter.generated.model.MidAuthenticateRequest;
import ee.cyber.cdoc2.server.adapter.generated.model.MidDisplayTextFormat;
import ee.cyber.cdoc2.server.adapter.generated.model.MidLanguage;
import ee.cyber.cdoc2.server.adapter.generated.model.SidAuthenticateRequest;
import ee.cyber.cdoc2.server.adapter.generated.model.SignatureAlgorithm;
import ee.cyber.cdoc2.server.adapter.generated.model.SignatureAlgorithmParametersInRequest;
import ee.cyber.cdoc2.server.adapter.generated.model.VerificationCodeType;

import static ee.cyber.cdoc2.server.adapter.generated.model.MidHashType.SHA512;

public final class RpRequestUtil {

    private RpRequestUtil() {
    }

    public static final String EE_SEMANTICS_IDENTIFIER_OK = "PNOEE-40504040001";
    public static final String MID_IDENTIFIER_OK = "51307149560";
    public static final String MID_PHONE_NUMBER = "+37200000000";
    public static final String MID_DISPLAY_TEXT = "Authenticate to decrypt CDOC2 document";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String MID_SAMPLE_SIGNATURE_VALUE =
        "IO6qDBcUtpIpcQSuTVp49TJ3jbJc+WA0z+26JSFgfW2x29y2I1dSMeHfUewAv4k55YxT1mYKw9DW9Efagp6tWg==";

    private static final String exampleSignature =
        "Vak2Q0NiFnh6+lW+YaJuB8yMYM7k3I5QfsUxS3Y1Ddm3qy6HvebLl0/t17dq289/+4mGx45qnHVNj1CzqF88lY"
            + "FpwAQXFc5XiHtYbvnENgjwLSfrQ9mrSt0phemAK29GzAexilPPy5+PdeCdO+TEuMIhi/qkKhL+lk5d1flm"
            + "rWZQ4B0H7kXTqRjJvCAUc8bsdM7SLV5jZuEIhOqTCsyDY3FZmyJi4ms8SsOmYCy/Do20CIiQbv75eunKfT"
            + "KciqVnOA+WYN5OXLJVqhgaHJ+hYiYA+QXOvIIYKVIUdB1rDRnKwwvZk1lZ3oKW1r7XGkovbmwRz+NpmHTy"
            + "MUubXXQHYah3T4h7vf0C14MBpupWRB47s6rsdPZzH2No7AoDGjMGAMEWg5rdB45fYXbjRV2T69e4c47Vpu"
            + "bG1DXTsoXowe/yzXdIjUYfZDwozzW6+IXTdAKL4LP6wmCU+PxP/GfYQ1k1w3fXjh4XeO9QmipRDEle2l0z"
            + "6nYJaGuIYCb7pDh6ZoJllyKSmhqWG0PMLod34Lo1MMP4WGvLe86/fiTFPbh/VR981MIjz5xIRaSxFZQmNz"
            + "I5IvzORskgsHGALb7Nb51q3jPKE2D1UOrQN6N6DMbGFMFR/7vRgkvUbeC0jAWevVrssEK77d5OmgXqk7se"
            + "vYDwP1WnmBwr2ov/Tt+AM58BTgdHsnWRw6hL1mS9+DzCaCuDb/+vspiZnVjP9S9ckmCLP8yrwzL8Myj5lS"
            + "62Rta727GulSUet21euQl0E2CuDpSoiKuO3NkYWqskfCowb0GiiX0oZe98mFiBFo1Za5Tb207fPOQyGAwY"
            + "29k0/XaYxwMPsU7/hwI5Ba8iek1A09Et3HB0q0hXeKyWl425ZrMdVoOMnVyquBNo/FhAOvoSGUyiIUSDd"
            + "pSwgpiJAH5cVX3W6lOy93mNtPBMk0qxtItYD3b9N7Lut6SCLhL/IGoNmrouOUX3xM8KD8qgnXmUXF0996"
            + "YK9WxFfZ8HScFVno8kRZdDY4QinqcwZy+FNXBG";
    private static final int DEFAULT_SALT_LENGTH = 64;
    private static final int RP_CHALLENGE_LENGTH = 64;

    public static SidAuthenticateRequest createSidAuthenticateRequest()
        throws JsonProcessingException {
        var signatureAlgorithmParameters =
            new SignatureAlgorithmParametersInRequest(
                HashAlgorithm.SHA_512
            );

        var signatureProtocolParameters =
            new AuthSignatureProtocolParameters()
                .rpChallenge(createRpChallengeBytes())
                .signatureAlgorithm(SignatureAlgorithm.RSASSA_PSS)
                .signatureAlgorithmParameters(signatureAlgorithmParameters);

        String interactions = createSidInteractions();
        String interactionsBase64 = Base64.getEncoder().encodeToString(
            interactions.getBytes(StandardCharsets.UTF_8)
        );

        return new SidAuthenticateRequest()
            .semanticsIdentifier(EE_SEMANTICS_IDENTIFIER_OK)
            .certificateLevel(AuthCertificateLevel.QUALIFIED)
            .signatureProtocol(AuthSignatureProtocol.ACSP_V2)
            .signatureProtocolParameters(signatureProtocolParameters)
            .interactions(interactionsBase64)
            .vcType(VerificationCodeType.NUMERIC4);
    }

    public static SessionStatus createSessionStatusResponse() {
        SessionSignature signature = getSessionSignature();

        SessionCertificate sessionCertificate = new SessionCertificate();
        sessionCertificate.setCertificateLevel("QUALIFIED");
        sessionCertificate.setValue("");

        SessionResult sessionResult = new SessionResult();
        sessionResult.setEndResult("OK");

        SessionStatus sessionStatus = new SessionStatus();

        sessionStatus.setState("COMPLETE");
        sessionStatus.setResult(sessionResult);
        sessionStatus.setSignatureProtocol("ACSP_V2");
        sessionStatus.setSignature(signature);
        sessionStatus.setCert(sessionCertificate);
        sessionStatus.setInteractionTypeUsed("confirmationMessage");

        return sessionStatus;
    }

    private static SessionSignature getSessionSignature() {
        SessionSignatureAlgorithmParameters signatureAlgorithmParameters = getSessionSignatureAlgorithmParameters();

        SessionSignature signature = new SessionSignature();
        signature.setValue(exampleSignature);
        signature.setServerRandom("+wVP2U/SMKVkVrggDjNTXFV/");
        signature.setUserChallenge("TLSjYRH2oYw8tW2bq0it0IUb7WIFkCLgF8NTc7-4Zq4");
        signature.setFlowType("NOTIFICATION");
        signature.setSignatureAlgorithm("rsassa-pss");
        signature.setSignatureAlgorithmParameters(signatureAlgorithmParameters);
        return signature;
    }

    private static SessionSignatureAlgorithmParameters getSessionSignatureAlgorithmParameters() {
        SessionMaskGenAlgorithmParameters sessionMaskGenAlgorithmParameters = new SessionMaskGenAlgorithmParameters();
        sessionMaskGenAlgorithmParameters.setHashAlgorithm("SHA-512");

        SessionMaskGenAlgorithm sessionMaskGenAlgorithm = new SessionMaskGenAlgorithm();
        sessionMaskGenAlgorithm.setAlgorithm("id-mgf1");
        sessionMaskGenAlgorithm.setParameters(sessionMaskGenAlgorithmParameters);

        SessionSignatureAlgorithmParameters signatureAlgorithmParameters = new SessionSignatureAlgorithmParameters();
        signatureAlgorithmParameters.setHashAlgorithm("SHA-512");
        signatureAlgorithmParameters.setMaskGenAlgorithm(sessionMaskGenAlgorithm);
        signatureAlgorithmParameters.setSaltLength(DEFAULT_SALT_LENGTH);
        signatureAlgorithmParameters.setTrailerField("0xbc");
        return signatureAlgorithmParameters;
    }

    private static String createSidInteractions() throws JsonProcessingException {
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
        byte[] rpChallengeBytes = new byte[RP_CHALLENGE_LENGTH];
        new SecureRandom().nextBytes(rpChallengeBytes);
        return rpChallengeBytes;
    }

    public static MidAuthenticateRequest createMidAuthenticateRequest() {
        return new MidAuthenticateRequest()
            .phoneNumber(MID_PHONE_NUMBER)
            .nationalIdentityNumber(MID_IDENTIFIER_OK)
            .hash(createRpChallengeBytes())
            .hashType(SHA512)
            .language(MidLanguage.ENG)
            .displayText(MID_DISPLAY_TEXT)
            .displayTextFormat(MidDisplayTextFormat.GSM_7);
    }

    public static MidSessionStatus createMidSessionStatus() {
        MidSessionSignature signature = new MidSessionSignature();
        signature.setValue(MID_SAMPLE_SIGNATURE_VALUE);

        MidSessionStatus status = new MidSessionStatus();
        status.setState("COMPLETE");
        status.setResult("OK");
        status.setSignature(signature);
        return status;
    }
}
