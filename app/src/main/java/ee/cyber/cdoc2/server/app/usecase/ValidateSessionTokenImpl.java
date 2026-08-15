package ee.cyber.cdoc2.server.app.usecase;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.nimbusds.jose.jwk.JWK;

import ee.cyber.cdoc2.auth.EtsiIdentifier;
import ee.cyber.cdoc2.auth.SessionTokenVerifier;
import ee.cyber.cdoc2.auth.TokenVerificationResponse;
import ee.cyber.cdoc2.auth.exception.VerificationException;
import ee.cyber.cdoc2.server.app.conf.AuthCertificateConf;
import ee.cyber.cdoc2.server.app.conf.AuthServerJwkConf;
import ee.cyber.cdoc2.server.app.conf.TrustedIssuers;

@Component
@RequiredArgsConstructor
public class ValidateSessionTokenImpl implements ValidateSessionToken {
    private final AuthServerJwkConf authServerJwkConf;
    private final TrustedIssuers trustedIssuers;
    private final AuthCertificateConf authCertificateConf;
    private final FindSessionNonce findSessionNonce;
    private final Clock clock;

    private static final Pattern ID_CODE = Pattern.compile("\\d{11}");

    @Override
    public Response execute(Request request) throws VerificationException {
        List<JWK> keys = authServerJwkConf.getPublicKeys();

        SessionTokenVerifier sessionTokenVerifier = new SessionTokenVerifier(
            trustedIssuers.getTrustStore(),
            authCertificateConf.isRevocationChecksEnabled(),
            clock
        );

        TokenVerificationResponse verificationResponse = sessionTokenVerifier.verify(
            request.sessionToken(),
            request.signingCertificate(),
            keys
        );

        validateIdentifier(
            request.identifier(),
            verificationResponse.identifier()
        );

        String uriString = verificationResponse.nonceUri().toString();
        String sessionNonce = uriString.substring(uriString.lastIndexOf('/') + 1);

        if (!findSessionNonce.isPresent(sessionNonce)) {
            throw new VerificationException("Could not find session nonce");
        }

        return new Response(
            verificationResponse.identifier().getSemanticsIdentifier()
        );
    }

    private void validateIdentifier(
        @Nullable String requestIdentifier,
        EtsiIdentifier sessionTokenIdentifier
    ) throws VerificationException {

        var inIdCodeFormat =
            requestIdentifier != null && ID_CODE.matcher(requestIdentifier.trim()).matches();

        var sessionTokenId = inIdCodeFormat
            ? sessionTokenIdentifier.getIdentifier()
            : sessionTokenIdentifier.getSemanticsIdentifier();

        if (requestIdentifier != null
            && !sessionTokenId
            .equals(requestIdentifier)) {
            throw new VerificationException(
                "Request semantics identifier does not match session token"
            );
        }
    }
}
