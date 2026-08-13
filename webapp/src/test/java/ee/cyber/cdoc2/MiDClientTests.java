package ee.cyber.cdoc2;

import ee.sk.mid.rest.dao.MidSessionStatus;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ee.cyber.cdoc2.server.adapter.clients.mobileid.MiDClient;
import ee.cyber.cdoc2.server.adapter.generated.model.MidAuthenticateRequest;
import ee.cyber.cdoc2.server.adapter.generated.model.MidLanguage;

import static ee.cyber.cdoc2.server.adapter.generated.model.MidHashType.SHA512;
import static ee.cyber.cdoc2.util.RpRequestUtil.createRpChallengeBytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class MiDClientTests {
    private static final TimeUnit SESSION_POLL_SLEEP_TIMEUNIT = TimeUnit.SECONDS;
    private static final long SESSION_POLL_SLEEP_QUANTITY = 1L;
    private static final int MAX_POLL_COUNT = 3;

    private static final String PHONE_OK = "+37268000769";
    private static final String IDENTITY_OK = "60001017869";

    private static final String PHONE_USER_CANCELLED = "+37201100266";
    private static final String IDENTITY_USER_CANCELLED = "60001019950";

    private static final String DISPLAY_TEXT = "Authenticate to decrypt CDOC2 document";

    @Autowired
    private MiDClient miDClient;

    @Tag("net")
    @Test
    void mobileIdAuthenticationSuccessfulTest() throws InterruptedException {
        var request = createMidAuthenticateRequest(PHONE_OK, IDENTITY_OK);

        var sessionId = miDClient.authenticate(
            IDENTITY_OK,
            request
        );

        assertNotNull(sessionId);

        var sessionStatus = pollForFinalSessionStatus(sessionId);

        assertNotNull(sessionStatus);
        assertEquals("COMPLETE", sessionStatus.getState());
        assertEquals("OK", sessionStatus.getResult());
    }

    @Tag("net")
    @Test
    void mobileIdAuthenticationUserCancelledTest() throws InterruptedException {
        var request = createMidAuthenticateRequest(PHONE_USER_CANCELLED, IDENTITY_USER_CANCELLED);

        var sessionId = miDClient.authenticate(
            IDENTITY_USER_CANCELLED,
            request
        );

        assertNotNull(sessionId);

        var sessionStatus = pollForFinalSessionStatus(sessionId);

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

    private MidSessionStatus pollForFinalSessionStatus(
        UUID sessionId
    ) throws InterruptedException {
        int pollCount = 0;
        MidSessionStatus sessionStatus = null;
        while (sessionStatus == null || "RUNNING".equalsIgnoreCase(sessionStatus.getState())) {
            if (pollCount == MAX_POLL_COUNT) {
                break;
            }

            sessionStatus = miDClient.sessionStatus(sessionId);
            if (sessionStatus != null && "COMPLETE".equalsIgnoreCase(sessionStatus.getState())) {
                break;
            }

            SESSION_POLL_SLEEP_TIMEUNIT.sleep(SESSION_POLL_SLEEP_QUANTITY);

            pollCount++;
        }

        return sessionStatus;
    }
}
