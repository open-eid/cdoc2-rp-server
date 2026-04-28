package ee.cyber.cdoc2.server.app.usecase;

import jakarta.annotation.Nullable;

import ee.cyber.cdoc2.auth.exception.VerificationException;

public interface ValidateSessionToken {
    Response execute(Request request) throws VerificationException;

    record Request(
        String sessionToken,
        String signingCertificate,
        @Nullable String semanticsIdentifier
    ) {
    }

    record Response(String semanticsIdentifier) {
    }
}
