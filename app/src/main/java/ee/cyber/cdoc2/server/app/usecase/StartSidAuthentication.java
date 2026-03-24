package ee.cyber.cdoc2.server.app.usecase;

import java.util.UUID;

import org.jspecify.annotations.NullMarked;

import ee.cyber.cdoc2.server.adapter.generated.model.SidAuthenticateRequest;

@NullMarked
public interface StartSidAuthentication {
    UUID execute(SidAuthenticateRequest sidAuthenticateRequest);
}
