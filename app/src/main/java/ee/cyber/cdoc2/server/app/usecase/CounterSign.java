package ee.cyber.cdoc2.server.app.usecase;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public interface CounterSign {

    Response execute(Request request);

    record Response(
        String signedHash,
        String rpName,
        String signatureInput,
        String signature
    ) {
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    record Request(byte[] signature) {
    }
}
