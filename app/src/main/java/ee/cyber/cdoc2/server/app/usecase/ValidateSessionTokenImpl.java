package ee.cyber.cdoc2.server.app.usecase;

import ee.cyber.cdoc2.server.app.conf.AuthCertificateConf;
import ee.cyber.cdoc2.server.app.conf.AuthServerJwkConf;
import ee.cyber.cdoc2.server.app.conf.SidTrustedIssuers;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Component;

import com.nimbusds.jose.jwk.JWK;

import ee.cyber.cdoc2.auth.SessionTokenVerifier;
import ee.cyber.cdoc2.auth.exception.VerificationException;

@Component
@RequiredArgsConstructor
public class ValidateSessionTokenImpl implements ValidateSessionToken {
    private final AuthServerJwkConf authServerJwkConf;
    private final SidTrustedIssuers sidTrustedIssuers;
    private final AuthCertificateConf authCertificateConf;
    private final FindSessionNonce findSessionNonce;
    private final Clock clock;

    @Override
    public Response execute(Request request) throws VerificationException {
        List<JWK> keys = authServerJwkConf.getPublicKeys();

        SessionTokenVerifier sessionTokenVerifier = new SessionTokenVerifier(
            sidTrustedIssuers.getTrustStore(),
            authCertificateConf.isRevocationChecksEnabled(),
            clock
        );

        SessionTokenVerifier.Response response = sessionTokenVerifier.getVerifiedSessionNonce(
            request.sessionToken(),
            request.signingCertificate(),
            keys
        );

        String uriString = response.sessionNonceUri().toString();
        String sessionNonce = uriString.substring(uriString.lastIndexOf('/') + 1);

        if (!findSessionNonce.isPresent(sessionNonce)) {
            throw new VerificationException("Could not find session nonce");
        }

        return new Response(
            response.identifier().getSemanticsIdentifier()
        );
    }
}
