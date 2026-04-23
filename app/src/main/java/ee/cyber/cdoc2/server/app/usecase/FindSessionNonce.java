package ee.cyber.cdoc2.server.app.usecase;

public interface FindSessionNonce {

    boolean isPresent(String sessionNonce);
}
