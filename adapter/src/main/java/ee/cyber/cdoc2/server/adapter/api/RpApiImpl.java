package ee.cyber.cdoc2.server.adapter.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.auth.exception.VerificationException;
import ee.cyber.cdoc2.server.adapter.clients.mobileid.MiDClient;
import ee.cyber.cdoc2.server.adapter.clients.mobileid.MiDSessionStatusMapper;
import ee.cyber.cdoc2.server.adapter.clients.smartid.SessionStatusMapper;
import ee.cyber.cdoc2.server.adapter.clients.smartid.SiDClient;
import ee.cyber.cdoc2.server.adapter.generated.api.Cdoc2RpApiDelegate;
import ee.cyber.cdoc2.server.adapter.generated.model.MidAuthenticateRequest;
import ee.cyber.cdoc2.server.adapter.generated.model.MidSessionStatusResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.NonceResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.SessionIDResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.SessionStatusResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.SidAuthenticateRequest;
import ee.cyber.cdoc2.server.adapter.generated.model.WellKnownResponse;
import ee.cyber.cdoc2.server.app.usecase.CounterSign;
import ee.cyber.cdoc2.server.app.usecase.GetSessionNonce;
import ee.cyber.cdoc2.server.app.usecase.ValidateSessionToken;

import static ee.cyber.cdoc2.server.adapter.clients.mobileid.MidValidationUtil.validatePhoneNumberAndNationalIdentityNumber;


@Slf4j
@Component
@RequiredArgsConstructor
public class RpApiImpl implements Cdoc2RpApiDelegate {

    private final SiDClient siDClient;
    private final MiDClient miDClient;
    private final ValidateSessionToken validateSessionToken;
    private final CounterSign counterSign;

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
            validationResponse.identifier(),
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

    @Override
    public ResponseEntity<SessionIDResponse> midAuthenticate(
        String sessionToken,
        String signingCertificate,
        MidAuthenticateRequest midAuthenticateRequest
    ) {
        validatePhoneNumberAndNationalIdentityNumber(
            midAuthenticateRequest.getPhoneNumber(),
            midAuthenticateRequest.getNationalIdentityNumber()
        );

        try {
            validateSessionToken.execute(new ValidateSessionToken.Request(
                sessionToken,
                signingCertificate,
                midAuthenticateRequest.getNationalIdentityNumber()
            ));
        } catch (VerificationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED.value()).build();
        }
        var sessionId = miDClient.authenticate(
            midAuthenticateRequest.getNationalIdentityNumber(),
            midAuthenticateRequest
        );

        return ResponseEntity.ok(new SessionIDResponse(sessionId));
    }
    
    @Override
    public ResponseEntity<MidSessionStatusResponse> midSession(
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
        var midResponse = miDClient.sessionStatus(sessionID);

        MidSessionStatusResponse response = MiDSessionStatusMapper.map(midResponse);

        if (response.getSignature() != null) {
            return counterSignedResponse(response);
        }

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<MidSessionStatusResponse> counterSignedResponse(
        MidSessionStatusResponse response
    ) {
        Objects.requireNonNull(response.getSignature());

        CounterSign.Response counterSignResponse =
            counterSign.execute(new CounterSign.Request(
                response.getSignature().getValue()
            ));

        return ResponseEntity.status(HttpStatus.OK)
            .headers(createCounterSignatureHeaders(counterSignResponse))
            .body(response);
    }

    private HttpHeaders createCounterSignatureHeaders(CounterSign.Response counterSignResponse) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-rp-signed-hash", counterSignResponse.signedHash());
        headers.add("x-rp-name", counterSignResponse.rpName());
        headers.add("Signature-Input", counterSignResponse.signatureInput());
        headers.add("Signature", counterSignResponse.signature());

        return headers;
    }
}
