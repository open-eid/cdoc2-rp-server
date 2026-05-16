package ee.cyber.cdoc2.server.app.usecase;

public interface CounterSign {

    Response execute(Request request);

    record Response(
        String signedHash,
        String rpName,
        String signatureInput,
        String signature
    ) {
    }

    record Request(byte[] signature) {
    }
}
