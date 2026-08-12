package ee.cyber.cdoc2.server.adapter.clients.smartid;


import ee.sk.smartid.RpChallenge;
import ee.sk.smartid.SignatureProtocol;
import ee.sk.smartid.SmartIdClient;
import ee.sk.smartid.VerificationCodeType;
import ee.sk.smartid.exception.UserAccountException;
import ee.sk.smartid.exception.UserActionException;
import ee.sk.smartid.exception.permanent.SmartIdClientException;
import ee.sk.smartid.rest.SessionStatusPoller;
import ee.sk.smartid.rest.SmartIdConnector;
import ee.sk.smartid.rest.dao.AcspV2SignatureProtocolParameters;
import ee.sk.smartid.rest.dao.NotificationAuthenticationSessionRequest;
import ee.sk.smartid.rest.dao.SemanticsIdentifier;
import ee.sk.smartid.rest.dao.SessionStatus;
import ee.sk.smartid.rest.dao.SignatureAlgorithmParameters;
import ee.sk.smartid.signature.AuthenticationSignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.adapter.conf.RelyingPartyConfImpl;
import ee.cyber.cdoc2.server.adapter.exception.ClientBadRequestException;
import ee.cyber.cdoc2.server.adapter.generated.model.SidAuthenticateRequest;

import static ee.cyber.cdoc2.server.adapter.clients.smartid.SmartIdUtilMethods.mapCertificateLevel;
import static ee.cyber.cdoc2.server.adapter.clients.smartid.SmartIdUtilMethods.mapHashAlgorithm;

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

        SmartIdConnector connector = smartIdClient.getSmartIdConnector();

        try {
            var signatureProtocolParameters = new AcspV2SignatureProtocolParameters(
                rpChallenge.toBase64EncodedValue(),
                signatureAlgorithm.getAlgorithmName(),
                new SignatureAlgorithmParameters(hashAlgorithm.getAlgorithmName())
            );

            NotificationAuthenticationSessionRequest request =
                new NotificationAuthenticationSessionRequest(
                    String.valueOf(relyingPartyConf.getSidUuid()),
                    relyingPartyConf.getSidName(),
                    certificateLevel.name(),
                    SignatureProtocol.ACSP_V2.name(),
                    signatureProtocolParameters,
                    sidAuthenticateRequest.getInteractions(),
                    null,
                    null,
                    VerificationCodeType.NUMERIC4.getValue()
                );

            var authenticationSessionResponse =
                connector.initNotificationAuthentication(
                    request,
                    new SemanticsIdentifier(semanticsIdentifier)
                );

            return UUID.fromString(authenticationSessionResponse.sessionID());
        } catch (UserAccountException | UserActionException | SmartIdClientException e) {
            throw new ClientBadRequestException(SID_CLIENT_ERROR_CODE, e.getMessage());
        }
    }

    public SessionStatus sessionStatus(UUID sessionId) {
        SessionStatusPoller poller = smartIdClient.getSessionStatusPoller();

        try {
            return poller.getSessionStatus(String.valueOf(sessionId));
        } catch (UserAccountException | UserActionException | SmartIdClientException e) {
            throw new ClientBadRequestException(SID_CLIENT_ERROR_CODE, e.getMessage());
        }
    }
}
