package ee.cyber.cdoc2.server.app.usecase;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;


@NullMarked
@RequiredArgsConstructor
@Component
public class StartSidAuthenticationImpl implements StartSidAuthentication {

    @Override
    public UUID execute() {
        return UUID.randomUUID();
    }
}
