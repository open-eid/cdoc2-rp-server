package ee.cyber.cdoc2.server.app.usecase;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.adapter.generated.model.SidAuthenticateRequest;

@NullMarked
@RequiredArgsConstructor
@Component
public class StartSidAuthenticationImpl implements StartSidAuthentication {

    @Override
    public UUID execute(SidAuthenticateRequest sidAuthenticateRequest) {
        return UUID.randomUUID();
    }
}
