package ee.cyber.cdoc2.server.adapter.clients.smartid;

import ee.cyber.cdoc2.server.adapter.generated.model.*;
import ee.cyber.cdoc2.server.adapter.generated.model.SessionStatusResponse.*;
import ee.cyber.cdoc2.server.adapter.generated.model.SessionStatusResponseResult.*;
import ee.sk.smartid.rest.dao.*;

import java.util.Arrays;
import java.util.Base64;

public final class SessionStatusMapper {

    private SessionStatusMapper() {
    }

    public static SessionStatusResponse map(SessionStatus source) {
        SessionStatusResponse target = new SessionStatusResponse(
            StateEnum.fromValue(source.getState())
        );

        if (source.getResult() != null) {
            target.setResult(mapResult(source.getResult()));
        }

        if (source.getSignatureProtocol() != null) {
            target.setSignatureProtocol(
                SignatureProtocolEnum.fromValue(source.getSignatureProtocol())
            );
        }

        if (source.getSignature() != null) {
            target.setSignature(mapSignature(source.getSignature()));
        }

        if (source.getCert() != null) {
            target.setCert(mapCert(source.getCert()));
        }

        if (source.getInteractionTypeUsed() != null) {
            target.setInteractionTypeUsed(
                InteractionTypeUsedEnum.fromValue(source.getInteractionTypeUsed())
            );
        }

        target.setDeviceIpAddress(source.getDeviceIpAddress());

        if (source.getIgnoredProperties() != null) {
            target.setIgnoredProperties(Arrays.asList(source.getIgnoredProperties()));
        }

        return target;
    }

    private static SessionStatusResponseResult mapResult(SessionResult source) {
        SessionStatusResponseResult target = new SessionStatusResponseResult(
            EndResultEnum.fromValue(source.getEndResult())
        );

        target.setDocumentNumber(source.getDocumentNumber());

        if (source.getDetails() != null) {
            target.setDetails(mapResultDetails(source.getDetails()));
        }

        return target;
    }

    private static SessionStatusResponseResultDetails mapResultDetails(SessionResultDetails source) {
        SessionStatusResponseResultDetails target = new SessionStatusResponseResultDetails();
        target.setInteraction(source.getInteraction());
        return target;
    }

    private static AcspV2Signature mapSignature(SessionSignature source) {
        byte[] value = source.getValue() != null
            ? Base64.getDecoder().decode(source.getValue())
            : null;
        byte[] serverRandom = source.getServerRandom() != null
            ? Base64.getDecoder().decode(source.getServerRandom())
            : null;

        AcspV2Signature target = new AcspV2Signature(
            value,
            serverRandom,
            source.getUserChallenge(),
            FlowType.fromValue(source.getFlowType()),
            SignatureAlgorithm.fromValue(source.getSignatureAlgorithm())
        );

        if (source.getSignatureAlgorithmParameters() != null) {
            target.setSignatureAlgorithmParameters(
                mapSignatureAlgorithmParameters(source.getSignatureAlgorithmParameters())
            );
        }

        return target;
    }

    private static SignatureAlgorithmParametersInResponse mapSignatureAlgorithmParameters(
        SessionSignatureAlgorithmParameters source
    ) {
        SignatureAlgorithmParametersInResponse target =
            new SignatureAlgorithmParametersInResponse();

        if (source.getHashAlgorithm() != null) {
            target.setHashAlgorithm(
                HashAlgorithm.fromValue(source.getHashAlgorithm())
            );
        }

        if (source.getMaskGenAlgorithm() != null) {
            target.setMaskGenAlgorithm(
                mapMaskGenAlgorithm(source.getMaskGenAlgorithm())
            );
        }

        target.setSaltLength(source.getSaltLength());

        if (source.getTrailerField() != null) {
            target.setTrailerField(
                SignatureAlgorithmParametersInResponse.TrailerFieldEnum.fromValue(
                    source.getTrailerField()
                )
            );
        }

        return target;
    }

    private static SignatureAlgorithmParametersInResponseMaskGenAlgorithm mapMaskGenAlgorithm(
        SessionMaskGenAlgorithm source
    ) {
        SignatureAlgorithmParametersInResponseMaskGenAlgorithm target =
            new SignatureAlgorithmParametersInResponseMaskGenAlgorithm();

        if (source.getAlgorithm() != null) {
            target.setAlgorithm(
                SignatureAlgorithmParametersInResponseMaskGenAlgorithm.AlgorithmEnum.fromValue(
                    source.getAlgorithm()
                )
            );
        }

        if (source.getParameters() != null) {
            target.setParameters(
                mapMaskGenAlgorithmParameters(source.getParameters())
            );
        }

        return target;
    }

    private static SignatureAlgorithmParametersInResponseMaskGenAlgorithmParameters
    mapMaskGenAlgorithmParameters(SessionMaskGenAlgorithmParameters source) {
        var target = new SignatureAlgorithmParametersInResponseMaskGenAlgorithmParameters();

        target.setHashAlgorithm(
            MGF1HashAlgorithm.fromValue(source.getHashAlgorithm())
        );

        return target;
    }

    private static SessionStatusResponseCert mapCert(SessionCertificate source) {
        byte[] value = source.getValue() != null
            ? Base64.getDecoder().decode(source.getValue())
            : null;

        return new SessionStatusResponseCert(
            value,
            source.getCertificateLevel()
        );
    }
}
