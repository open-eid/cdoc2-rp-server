package ee.cyber.cdoc2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ee.cyber.cdoc2.server.adapter.clients.smartid.SiDClient;


import static ee.cyber.cdoc2.RpRequestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SiDClientTests {

    private static final String EE_SEMANTICS_IDENTIFIER_USER_REFUSED = "PNOEE-30403039917";

    @Autowired
    private SiDClient siDClient;

    @Test
    void smartIdAuthenticationSuccessfulTest() {
        var rpRequest = createSidAuthenticateRequest();

        var sessionId = siDClient.authenticate(EE_SEMANTICS_IDENTIFIER_OK, rpRequest);

        assertNotNull(sessionId);
        System.out.println(sessionId);

        var sessionStatusResponse = siDClient.sessionStatus(sessionId);

        assertNotNull(sessionStatusResponse);
        assertEquals("COMPLETE", sessionStatusResponse.getState());
        assertEquals("OK", sessionStatusResponse.getResult().getEndResult());
    }

    @Test
    void smartIdAuthenticationUserRefusedTest() {
        var rpRequest = createSidAuthenticateRequest();

        var sessionId = siDClient.authenticate(EE_SEMANTICS_IDENTIFIER_USER_REFUSED, rpRequest);

        assertNotNull(sessionId);

        var sessionStatusResponse = siDClient.sessionStatus(sessionId);

        assertNotNull(sessionStatusResponse);
        assertEquals("COMPLETE", sessionStatusResponse.getState());
        assertEquals("USER_REFUSED", sessionStatusResponse.getResult().getEndResult());
    }
}
