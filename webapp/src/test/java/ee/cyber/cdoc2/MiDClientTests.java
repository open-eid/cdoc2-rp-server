package ee.cyber.cdoc2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ee.cyber.cdoc2.server.adapter.clients.mobileid.MiDClient;
import ee.cyber.cdoc2.server.adapter.generated.model.MidAuthenticateRequest;
import ee.cyber.cdoc2.server.adapter.generated.model.MidLanguage;

import static ee.cyber.cdoc2.RpRequestUtil.createRpChallengeBytes;
import static ee.cyber.cdoc2.server.adapter.generated.model.MidHashType.SHA512;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MiDClientTests {

    private static final String PHONE_OK = "+37268000769";
    private static final String IDENTITY_OK = "60001017869";

    private static final String PHONE_USER_CANCELLED = "+37201100266";
    private static final String IDENTITY_USER_CANCELLED = "60001019950";

    private static final String DISPLAY_TEXT = "Authenticate to decrypt CDOC2 document";

    @Autowired
    private MiDClient miDClient;

    @Test
    void mobileIdAuthenticationSuccessfulTest() {
        var request = createMidAuthenticateRequest(PHONE_OK, IDENTITY_OK);

        var sessionId = miDClient.authenticate(
            IDENTITY_OK,
            request
        );

        assertNotNull(sessionId);

        var sessionStatus = miDClient.sessionStatus(sessionId);

        assertNotNull(sessionStatus);
        assertEquals("COMPLETE", sessionStatus.getState());
        assertEquals("OK", sessionStatus.getResult());
    }

    @Test
    void mobileIdAuthenticationUserCancelledTest() {
        var request = createMidAuthenticateRequest(PHONE_USER_CANCELLED, IDENTITY_USER_CANCELLED);

        var sessionId = miDClient.authenticate(
            IDENTITY_USER_CANCELLED,
            request
        );

        assertNotNull(sessionId);

        var sessionStatus = miDClient.sessionStatus(sessionId);

        assertNotNull(sessionStatus);
        assertEquals("COMPLETE", sessionStatus.getState());
        assertEquals("USER_CANCELLED", sessionStatus.getResult());
    }

    private static MidAuthenticateRequest createMidAuthenticateRequest(
        String phoneNumber,
        String nationalIdentityNumber
    ) {
        var request = new MidAuthenticateRequest();

        request.setPhoneNumber(phoneNumber);
        request.setNationalIdentityNumber(nationalIdentityNumber);
        request.setHash(createRpChallengeBytes());
        request.setHashType(SHA512);
        request.setLanguage(MidLanguage.ENG);
        request.setDisplayText(DISPLAY_TEXT);

        return request;
    }
}
