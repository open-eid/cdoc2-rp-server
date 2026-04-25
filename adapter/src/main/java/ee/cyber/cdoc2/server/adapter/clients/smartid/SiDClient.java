package ee.cyber.cdoc2.server.adapter.clients.smartid;


import ee.sk.smartid.RpChallenge;
import ee.sk.smartid.SmartIdClient;
import ee.sk.smartid.rest.SessionStatusPoller;
import ee.sk.smartid.rest.dao.SemanticsIdentifier;
import ee.sk.smartid.rest.dao.SessionStatus;
import ee.sk.smartid.signature.AuthenticationSignatureAlgorithm;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.adapter.conf.RelyingPartyConf;
import ee.cyber.cdoc2.server.adapter.generated.model.SidAuthenticateRequest;

import static ee.cyber.cdoc2.server.adapter.clients.smartid.SmartIdUtilMethods.*;

@Component
@RequiredArgsConstructor
public class SiDClient {
    private final SmartIdClient smartIdClient;
    private final RelyingPartyConf relyingPartyConf;

    public UUID authenticate(
        String semanticsIdentifier,
        SidAuthenticateRequest sidAuthenticateRequest
    ) {
        var signatureProtocolParams = sidAuthenticateRequest.getSignatureProtocolParameters();

        var rpChallenge = new RpChallenge(signatureProtocolParams.getRpChallenge());
        var certificateLevel = mapCertificateLevel(sidAuthenticateRequest.getCertificateLevel());
        var signatureAlgorithm = AuthenticationSignatureAlgorithm.fromString(
            signatureProtocolParams.getSignatureAlgorithm().getValue()
        );
        var hashAlgorithm = mapHashAlgorithm(
            signatureProtocolParams.getSignatureAlgorithmParameters().getHashAlgorithm().getValue()
        );
        var interactions = decodeFromBase64(sidAuthenticateRequest.getInteractions());

        var authenticationSessionResponse = smartIdClient
            .createNotificationAuthentication()
            .withSemanticsIdentifier(new SemanticsIdentifier(semanticsIdentifier))
            .withRpChallenge(rpChallenge.toBase64EncodedValue())
            .withCertificateLevel(certificateLevel)
            .withSignatureAlgorithm(signatureAlgorithm)
            .withHashAlgorithm(hashAlgorithm)
            .withInteractions(interactions)
            .withRelyingPartyUUID(String.valueOf(relyingPartyConf.getUuid()))
            .withRelyingPartyName(relyingPartyConf.getName())
            .initAuthenticationSession();

        return UUID.fromString(authenticationSessionResponse.sessionID());
    }

    public SessionStatus sessionStatus(UUID sessionId) {
        SessionStatusPoller poller = smartIdClient.getSessionStatusPoller();

        return poller.getSessionStatus(String.valueOf(sessionId));
    }
}
