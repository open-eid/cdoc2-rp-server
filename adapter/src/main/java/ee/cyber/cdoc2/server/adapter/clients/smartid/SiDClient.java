package ee.cyber.cdoc2.server.adapter.clients.smartid;


import ee.sk.smartid.RpChallenge;
import ee.sk.smartid.SmartIdClient;
import ee.sk.smartid.rest.SessionStatusPoller;
import ee.sk.smartid.rest.dao.SessionStatus;
import ee.sk.smartid.signature.AuthenticationSignatureAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.adapter.generated.model.SidAuthenticateRequest;

import static ee.cyber.cdoc2.server.adapter.clients.smartid.SmartIdUtilMethods.*;

@Component
public class SiDClient {

    private final SmartIdClient smartIdClient;

    public SiDClient(
        @Value("${smartid.client.hostUrl}") String hostUrl,
        @Value("${smartid.client.ssl.trust-store}") String trustStorePath,
        @Value("${smartid.client.ssl.trust-store-password}") String trustStorePassword
    ) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        // Read truststore containing Smart-ID service provider (SK) SSL certificates
        InputStream is = getClass()
            .getClassLoader()
            .getResourceAsStream(trustStorePath);
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(is, trustStorePassword.toCharArray());

        smartIdClient = new SmartIdClient();
        smartIdClient.setHostUrl(hostUrl);
        smartIdClient.setTrustStore(trustStore);
    }

    public UUID authenticate(
        String documentNumber,
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
        var rpUUID = sidAuthenticateRequest.getRelyingPartyUUID();
        var rpName = sidAuthenticateRequest.getRelyingPartyName();

        var authenticationSessionResponse = smartIdClient
            .createNotificationAuthentication()
            .withDocumentNumber(documentNumber)
            .withRpChallenge(rpChallenge.toBase64EncodedValue())
            .withCertificateLevel(certificateLevel)
            .withSignatureAlgorithm(signatureAlgorithm)
            .withHashAlgorithm(hashAlgorithm)
            .withInteractions(interactions)
            .withRelyingPartyUUID(String.valueOf(rpUUID))
            .withRelyingPartyName(rpName)
            .initAuthenticationSession();

        return UUID.fromString(authenticationSessionResponse.sessionID());
    }

    public SessionStatus sessionStatus(UUID sessionId) {
        SessionStatusPoller poller = smartIdClient.getSessionStatusPoller();

        return poller.getSessionStatus(String.valueOf(sessionId));
    }
}
