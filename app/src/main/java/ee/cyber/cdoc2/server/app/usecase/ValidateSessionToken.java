package ee.cyber.cdoc2.server.app.usecase;

import ee.cyber.cdoc2.auth.exception.VerificationException;

public interface ValidateSessionToken {
    Response execute(Request request) throws VerificationException;

    record Request(String sessionToken, String signingCertificate) {
    }

    record Response(String semanticsIdentifier) {
    }
}
