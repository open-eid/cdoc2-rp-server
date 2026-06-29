package ee.cyber.cdoc2.server.adapter.clients.mobileid;

import ee.sk.mid.rest.dao.MidSessionSignature;
import ee.sk.mid.rest.dao.MidSessionStatus;

import ee.cyber.cdoc2.server.adapter.generated.model.MidSessionStatusResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.MidSignature;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Base64;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class MiDSessionStatusMapperTest {

    @Test
    void mapWithRunningStateSetsStateOnly() {
        MidSessionStatus source = new MidSessionStatus();
        source.setState("RUNNING");

        MidSessionStatusResponse result = MiDSessionStatusMapper.map(source);

        assertEquals(MidSessionStatusResponse.StateEnum.RUNNING, result.getState());
        assertNull(result.getResult());
        assertNull(result.getCert());
    }

    @Test
    void mapWithCompleteStateAndOkResultSetsStateAndResult() {
        MidSessionStatus source = new MidSessionStatus();
        source.setState("COMPLETE");
        source.setResult("OK");

        MidSessionStatusResponse result = MiDSessionStatusMapper.map(source);

        assertEquals(MidSessionStatusResponse.StateEnum.COMPLETE, result.getState());
        assertEquals(MidSessionStatusResponse.ResultEnum.OK, result.getResult());
        assertNull(result.getCert());
    }

    @Test
    void mapWithCompleteStateAndCertDecodesCertFromBase64() {
        byte[] certBytes = {1, 2, 3, 4, 5};
        String certBase64 = Base64.getEncoder().encodeToString(certBytes);

        MidSessionStatus source = new MidSessionStatus();
        source.setState("COMPLETE");
        source.setResult("OK");
        source.setCert(certBase64);

        MidSessionStatusResponse result = MiDSessionStatusMapper.map(source);

        assertArrayEquals(certBytes, result.getCert());
    }

    @Test
    void mapWithCompleteStateAndNullCertCertRemainsNull() {
        MidSessionStatus source = new MidSessionStatus();
        source.setState("COMPLETE");
        source.setResult("OK");

        MidSessionStatusResponse result = MiDSessionStatusMapper.map(source);

        assertNull(result.getCert());
    }

    @ParameterizedTest
    @MethodSource("resultCodes")
    void mapWithAllResultCodesMapsCorrectly(
        String resultCode,
        MidSessionStatusResponse.ResultEnum expected
    ) {
        MidSessionStatus source = new MidSessionStatus();
        source.setState("COMPLETE");
        source.setResult(resultCode);

        MidSessionStatusResponse result = MiDSessionStatusMapper.map(source);

        assertEquals(expected, result.getResult());
    }

    static Stream<Arguments> resultCodes() {
        return Stream.of(
            Arguments.of("OK", MidSessionStatusResponse.ResultEnum.OK),
            Arguments.of("TIMEOUT", MidSessionStatusResponse.ResultEnum.TIMEOUT),
            Arguments.of("NOT_MID_CLIENT", MidSessionStatusResponse.ResultEnum.NOT_MID_CLIENT),
            Arguments.of("USER_CANCELLED", MidSessionStatusResponse.ResultEnum.USER_CANCELLED),
            Arguments.of("SIGNATURE_HASH_MISMATCH", MidSessionStatusResponse.ResultEnum.SIGNATURE_HASH_MISMATCH),
            Arguments.of("PHONE_ABSENT", MidSessionStatusResponse.ResultEnum.PHONE_ABSENT),
            Arguments.of("DELIVERY_ERROR", MidSessionStatusResponse.ResultEnum.DELIVERY_ERROR),
            Arguments.of("SIM_ERROR", MidSessionStatusResponse.ResultEnum.SIM_ERROR)
        );
    }

    @Test
    void mapWithUnknownResultCodeThrowsRuntimeException() {
        MidSessionStatus source = new MidSessionStatus();
        source.setState("COMPLETE");
        source.setResult("UNKNOWN_CODE");

        assertThrows(RuntimeException.class, () -> MiDSessionStatusMapper.map(source));
    }

    @Test
    void mapWithCompleteStateAndOkResultAndNullSignatureSignatureRemainsNull() {
        MidSessionStatus source = new MidSessionStatus();
        source.setState("COMPLETE");
        source.setResult("OK");
        // signature explicitly left null

        MidSessionStatusResponse result = MiDSessionStatusMapper.map(source);

        assertNull(result.getSignature());
    }

    @Test
    void mapWithCompleteStateAndSignatureDecodesValueFromBase64AndKeepsAlgorithm() {
        byte[] signatureBytes = new byte[] {0x01, 0x02, 0x03, (byte) 0xff, (byte) 0x80};
        String signatureBase64 = Base64.getEncoder().encodeToString(signatureBytes);
        String algorithm = "SHA256WithECEncryption";

        MidSessionSignature sdkSignature = new MidSessionSignature();
        sdkSignature.setValue(signatureBase64);
        sdkSignature.setAlgorithm(algorithm);

        MidSessionStatus source = new MidSessionStatus();
        source.setState("COMPLETE");
        source.setResult("OK");
        source.setSignature(sdkSignature);

        MidSessionStatusResponse result = MiDSessionStatusMapper.map(source);

        MidSignature mapped = result.getSignature();
        assertNotNull(mapped);
        assertArrayEquals(signatureBytes, mapped.getValue());
        assertEquals(algorithm, mapped.getAlgorithm());
    }

    @ParameterizedTest
    @MethodSource("signatureAlgorithmAndValuePairs")
    void mapWithCompleteStateAndSignatureAcrossAlgorithms(
        String algorithm,
        byte[] rawSignatureBytes
    ) {
        String signatureBase64 = Base64.getEncoder().encodeToString(rawSignatureBytes);

        MidSessionSignature sdkSignature = new MidSessionSignature();
        sdkSignature.setValue(signatureBase64);
        sdkSignature.setAlgorithm(algorithm);

        MidSessionStatus source = new MidSessionStatus();
        source.setState("COMPLETE");
        source.setResult("OK");
        source.setSignature(sdkSignature);

        MidSessionStatusResponse result = MiDSessionStatusMapper.map(source);

        MidSignature mapped = result.getSignature();
        assertNotNull(mapped);
        assertEquals(algorithm, mapped.getAlgorithm());
        assertArrayEquals(rawSignatureBytes, mapped.getValue());
    }

    static Stream<Arguments> signatureAlgorithmAndValuePairs() {
        return Stream.of(
            Arguments.of("SHA256WithECEncryption", new byte[] {1, 2, 3}),
            Arguments.of("SHA384WithECEncryption", new byte[] {(byte) 0xab, (byte) 0xcd}),
            Arguments.of("SHA512WithECEncryption", new byte[] {(byte) 0xff}),
            // RSA legacy — even though MID uses ECDSA in practice, the mapper should not
            // assume an algorithm and simply forward whatever the SDK reports.
            Arguments.of("SHA256WithRSAEncryption", new byte[] {0x00, 0x10, 0x20, 0x30, 0x40})
        );
    }

    @Test
    void mapWithRunningStateDoesNotMapSignatureEvenIfPresent() {
        // The mapper guards COMPLETE-state with the cert/signature blocks; in RUNNING state
        // nothing else is set. This pins that contract.
        MidSessionSignature sdkSignature = new MidSessionSignature();
        sdkSignature.setValue(Base64.getEncoder().encodeToString(new byte[] {1, 2, 3}));
        sdkSignature.setAlgorithm("SHA256WithECEncryption");

        MidSessionStatus source = new MidSessionStatus();
        source.setState("RUNNING");
        source.setSignature(sdkSignature);

        MidSessionStatusResponse result = MiDSessionStatusMapper.map(source);

        assertEquals(MidSessionStatusResponse.StateEnum.RUNNING, result.getState());
        assertNull(result.getSignature());
    }
}
