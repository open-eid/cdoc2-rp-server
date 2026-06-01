package ee.cyber.cdoc2.server.adapter.clients.smartid;


import ee.sk.smartid.RpChallenge;
import ee.sk.smartid.SmartIdClient;
import ee.sk.smartid.common.notification.interactions.NotificationInteraction;
import ee.sk.smartid.exception.UserAccountException;
import ee.sk.smartid.exception.UserActionException;
import ee.sk.smartid.rest.SessionStatusPoller;
import ee.sk.smartid.rest.dao.SemanticsIdentifier;
import ee.sk.smartid.rest.dao.SessionStatus;
import ee.sk.smartid.signature.AuthenticationSignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import ee.cyber.cdoc2.server.adapter.conf.RelyingPartyConfImpl;
import ee.cyber.cdoc2.server.adapter.exception.ClientBadRequestException;
import ee.cyber.cdoc2.server.adapter.generated.model.SidAuthenticateRequest;

import static ee.cyber.cdoc2.server.adapter.clients.smartid.SmartIdUtilMethods.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SiDClient {
    static final String SID_CLIENT_ERROR_CODE = "SID_CLIENT_ERROR";

    private final SmartIdClient smartIdClient;
    private final RelyingPartyConfImpl relyingPartyConf;

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

        List<NotificationInteraction> interactions;
        try {
            interactions = decodeFromBase64(sidAuthenticateRequest.getInteractions());
        } catch (JsonProcessingException e) {
            log.error("Unable to decode interactions from SID authenticate request");
            throw new ClientBadRequestException(SID_CLIENT_ERROR_CODE, e.getMessage());
        }

        try {
            var authenticationSessionResponse = smartIdClient
                .createNotificationAuthentication()
                .withSemanticsIdentifier(new SemanticsIdentifier(semanticsIdentifier))
                .withRpChallenge(rpChallenge.toBase64EncodedValue())
                .withCertificateLevel(certificateLevel)
                .withSignatureAlgorithm(signatureAlgorithm)
                .withHashAlgorithm(hashAlgorithm)
                .withInteractions(interactions)
                .withRelyingPartyUUID(String.valueOf(relyingPartyConf.getSidUuid()))
                .withRelyingPartyName(relyingPartyConf.getSidName())
                .initAuthenticationSession();

            return UUID.fromString(authenticationSessionResponse.sessionID());
        } catch (UserAccountException | UserActionException e) {
            throw new ClientBadRequestException(SID_CLIENT_ERROR_CODE, e.getMessage());
        }
    }

    public SessionStatus sessionStatus(UUID sessionId) {
        SessionStatusPoller poller = smartIdClient.getSessionStatusPoller();

        try {
            return poller.getSessionStatus(String.valueOf(sessionId));
        } catch (UserAccountException | UserActionException e) {
            throw new ClientBadRequestException(SID_CLIENT_ERROR_CODE, e.getMessage());
        }
    }
}
