package ee.cyber.cdoc2.server.app.usecase;

import java.util.UUID;

import org.jspecify.annotations.NullMarked;


@NullMarked
public interface StartSidAuthentication {
    UUID execute();
}
