package ee.cyber.cdoc2.server.adapter.rest;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.adapter.generated.api.Cdoc2RpApiDelegate;
import ee.cyber.cdoc2.server.adapter.generated.model.NonceResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.SessionIDResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.SessionStatusResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.SidAuthenticateRequest;
import ee.cyber.cdoc2.server.adapter.generated.model.WellKnownResponse;
import ee.cyber.cdoc2.server.app.usecase.GetSessionNonce;
import ee.cyber.cdoc2.server.app.usecase.StartSidAuthentication;

@Component
@RequiredArgsConstructor
public class RpApiImpl implements Cdoc2RpApiDelegate {
    private final GetSessionNonce getSessionNonce;
    private final StartSidAuthentication startSidAuthentication;
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
    public ResponseEntity<SessionIDResponse> sidAuthenticate(SidAuthenticateRequest sidAuthenticateRequest) {
        var sessionId = startSidAuthentication.execute(sidAuthenticateRequest);

        return ResponseEntity.ok(new SessionIDResponse(sessionId));
    }

    @Override
    public ResponseEntity<SessionStatusResponse> sidSession(UUID sessionID) {
        InputStream input = getClass()
            .getClassLoader()
            .getResourceAsStream("sid-session-status-sample.json");

        SessionStatusResponse response = OBJECT_MAPPER.readValue(input, SessionStatusResponse.class);

        return ResponseEntity.ok(response);
    }
}
