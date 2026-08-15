package ee.cyber.cdoc2;

import ee.sk.smartid.rest.dao.SessionStatus;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ee.cyber.cdoc2.server.adapter.clients.smartid.SiDClient;

import static ee.cyber.cdoc2.util.RpRequestUtil.EE_SEMANTICS_IDENTIFIER_OK;
import static ee.cyber.cdoc2.util.RpRequestUtil.createSidAuthenticateRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class SiDClientTests {
    private static final TimeUnit SESSION_POLL_SLEEP_TIMEUNIT = TimeUnit.SECONDS;
    private static final long SESSION_POLL_SLEEP_QUANTITY = 1L;
    private static final int MAX_POLL_COUNT = 10;

    private static final String EE_SEMANTICS_IDENTIFIER_USER_REFUSED = "PNOEE-30403039917";

    @Autowired
    private SiDClient siDClient;

    @Tag("net")
    @Test
    void smartIdAuthenticationSuccessfulTest() throws Exception {
        var rpRequest = createSidAuthenticateRequest();

        var sessionId = siDClient.authenticate(EE_SEMANTICS_IDENTIFIER_OK, rpRequest);

        assertNotNull(sessionId);

        var sessionStatusResponse = pollForFinalSessionStatus(sessionId);

        assertNotNull(sessionStatusResponse);
        assertEquals("COMPLETE", sessionStatusResponse.getState());
        assertEquals("OK", sessionStatusResponse.getResult().getEndResult());
    }

    @Tag("net")
    @Test
    void smartIdAuthenticationUserRefusedTest() throws Exception {
        var rpRequest = createSidAuthenticateRequest();

        var sessionId = siDClient.authenticate(EE_SEMANTICS_IDENTIFIER_USER_REFUSED, rpRequest);

        assertNotNull(sessionId);

        var sessionStatusResponse = pollForFinalSessionStatus(sessionId);

        assertNotNull(sessionStatusResponse);
        assertEquals("COMPLETE", sessionStatusResponse.getState());
        assertEquals("USER_REFUSED", sessionStatusResponse.getResult().getEndResult());
    }

    private SessionStatus pollForFinalSessionStatus(
        UUID sessionId
    ) throws InterruptedException {
        int pollCount = 0;
        SessionStatus sessionStatus = null;
        while (sessionStatus == null || "RUNNING".equalsIgnoreCase(sessionStatus.getState())) {
            if (pollCount == MAX_POLL_COUNT) {
                break;
            }

            sessionStatus = siDClient.sessionStatus(sessionId);
            if (sessionStatus != null && "COMPLETE".equalsIgnoreCase(sessionStatus.getState())) {
                break;
            }

            SESSION_POLL_SLEEP_TIMEUNIT.sleep(SESSION_POLL_SLEEP_QUANTITY);

            pollCount++;
        }

        return sessionStatus;
    }
}
