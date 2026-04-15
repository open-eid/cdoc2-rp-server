package ee.cyber.cdoc2.server.adapter.rest;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.adapter.clients.smartid.SessionStatusMapper;
import ee.cyber.cdoc2.server.adapter.clients.smartid.SiDClient;
import ee.cyber.cdoc2.server.adapter.generated.api.Cdoc2RpApiDelegate;
import ee.cyber.cdoc2.server.adapter.generated.model.NonceResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.SessionIDResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.SessionStatusResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.SidAuthenticateRequest;
import ee.cyber.cdoc2.server.adapter.generated.model.WellKnownResponse;
import ee.cyber.cdoc2.server.app.usecase.GetSessionNonce;

@Component
@RequiredArgsConstructor
public class RpApiImpl implements Cdoc2RpApiDelegate {

    private final SiDClient siDClient;

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
        // TODO: Once SD-JWT is added, validate the correct fields
        var sessionId = siDClient.authenticate(
            // TODO: The document number will come from the SD-JWT
            "PNOEE-40504040001-DEM0-Q",
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
        var sidResponse = siDClient.sessionStatus(sessionID);

        SessionStatusResponse response = SessionStatusMapper.map(sidResponse);

        return ResponseEntity.ok(response);
    }
}
