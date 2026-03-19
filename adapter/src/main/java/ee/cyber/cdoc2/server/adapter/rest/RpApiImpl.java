package ee.cyber.cdoc2.server.adapter.rest;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.adapter.generated.api.Cdoc2RpApiDelegate;
import ee.cyber.cdoc2.server.adapter.generated.model.NonceResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.WellKnownResponse;
import ee.cyber.cdoc2.server.app.usecase.GetSessionNonce;

@Component
@RequiredArgsConstructor
public class RpApiImpl implements Cdoc2RpApiDelegate {
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
}
