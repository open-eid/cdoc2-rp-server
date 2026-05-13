package ee.cyber.cdoc2.server.adapter.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.auth.exception.VerificationException;
import ee.cyber.cdoc2.server.adapter.clients.smartid.SessionStatusMapper;
import ee.cyber.cdoc2.server.adapter.clients.smartid.SiDClient;
import ee.cyber.cdoc2.server.adapter.generated.api.Cdoc2RpApiDelegate;
import ee.cyber.cdoc2.server.adapter.generated.model.NonceResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.SessionIDResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.SessionStatusResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.SidAuthenticateRequest;
import ee.cyber.cdoc2.server.adapter.generated.model.WellKnownResponse;
import ee.cyber.cdoc2.server.app.usecase.GetSessionNonce;
import ee.cyber.cdoc2.server.app.usecase.ValidateSessionToken;

@Slf4j
@Component
@RequiredArgsConstructor
public class RpApiImpl implements Cdoc2RpApiDelegate {

    private final SiDClient siDClient;
    private final ValidateSessionToken validateSessionToken;

    private final GetSessionNonce getSessionNonce;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public ResponseEntity<NonceResponse> getSessionNonce() {
        String sessionNonce = getSessionNonce.execute();

        return ResponseEntity.ok(new NonceResponse(sessionNonce));
    }

    @Override
    public ResponseEntity<WellKnownResponse> getWellKnown() {
        InputStream input = getClass()
            .getClassLoader()
            .getResourceAsStream("well-known-sample.json");

        WellKnownResponse response = OBJECT_MAPPER.readValue(input, WellKnownResponse.class);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<SessionIDResponse> sidAuthenticate(
        String sessionToken,
        String signingCertificate,
        SidAuthenticateRequest sidAuthenticateRequest
    ) {
        ValidateSessionToken.Response validationResponse;
        try {
            validationResponse =
                validateSessionToken.execute(new ValidateSessionToken.Request(
                    sessionToken,
                    signingCertificate,
                    sidAuthenticateRequest.getSemanticsIdentifier()
                ));
        } catch (VerificationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).build();
        }

        var sessionId = siDClient.authenticate(
            validationResponse.semanticsIdentifier(),
            sidAuthenticateRequest
        );

        return ResponseEntity.ok(new SessionIDResponse(sessionId));
    }

    @Override
    public ResponseEntity<SessionStatusResponse> sidSession(
        UUID sessionID,
        String sessionToken,
        String signingCertificate
    ) {
        try {
            validateSessionToken.execute(new ValidateSessionToken.Request(
                sessionToken,
                signingCertificate,
                null
            ));
        } catch (VerificationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).build();
        }

        var sidResponse = siDClient.sessionStatus(sessionID);

        SessionStatusResponse response = SessionStatusMapper.map(sidResponse);

        return ResponseEntity.ok(response);
    }
}
