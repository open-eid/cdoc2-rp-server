package ee.cyber.cdoc2;

import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import ee.cyber.cdoc2.server.adapter.clients.smartid.SiDClient;
import ee.cyber.cdoc2.server.adapter.db.jpa.SessionNonceJpaRepository;
import ee.cyber.cdoc2.server.adapter.generated.model.SessionIDResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.SessionStatusResponse;

import static ee.cyber.cdoc2.RpRequestUtil.*;
import static ee.cyber.cdoc2.server.adapter.generated.model.SessionStatusResponse.StateEnum.COMPLETE;
import static ee.cyber.cdoc2.server.adapter.generated.model.SessionStatusResponseResult.EndResultEnum.OK;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class Cdoc2RpServerApplicationTest {
    @Autowired
    private SessionNonceJpaRepository sessionNonceJpaRepository;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int EXPECTED_SESSION_NONCE_BYTES = 16;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SiDClient sidClient;

    @Test
    void shouldGetSessionNonce() throws Exception {
        MockHttpServletResponse getSessionNonceResponse = mockMvc.perform(
                post(URI.create("/session_nonce"))
            ).andExpect(status().isOk())
            .andReturn().getResponse();

        GetSessionNonceResponseBody getSessionNonceResponseBody = OBJECT_MAPPER.readValue(
            getSessionNonceResponse.getContentAsString(),
            GetSessionNonceResponseBody.class
        );

        assertNotNull(getSessionNonceResponseBody.nonce);
        assertTrue(getSessionNonceResponseBody.nonce.length() >= EXPECTED_SESSION_NONCE_BYTES);

        byte[] decodedNonce = Base64.getUrlDecoder().decode(getSessionNonceResponseBody.nonce);

        assertEquals(EXPECTED_SESSION_NONCE_BYTES, decodedNonce.length);

        var nonceFromDb = sessionNonceJpaRepository.findBySessionNonce(decodedNonce);
        assertTrue(nonceFromDb.isPresent());
        assertArrayEquals(decodedNonce, nonceFromDb.get().getSessionNonce());
    }

    @Test
    void shouldGetWellKnown() throws Exception {
        MockHttpServletResponse getWellKnownResponse = mockMvc.perform(
                get(URI.create("/.well-known/jwks.jws"))
            ).andExpect(status().isOk())
            .andReturn().getResponse();

        GetWellKnownResponseBody getWellKnownResponseBody = OBJECT_MAPPER.readValue(
            getWellKnownResponse.getContentAsString(),
            GetWellKnownResponseBody.class
        );

        assertEquals(2, getWellKnownResponseBody.keys.size());

        assertTrue(
            getWellKnownResponseBody.keys.stream()
                .allMatch(key -> key.kid != null && key.kty != null)
        );
    }

    @Test
    void shouldStartSidAuthentication() throws Exception {
        // Given
        var request = createSidAuthenticateRequest(
            UUID.randomUUID(),
            "DigiDoc4"
        );
        var sessionId = UUID.randomUUID();

        when(sidClient.authenticate(EE_DOCUMENT_NUMBER_OK, request))
            .thenReturn(sessionId);

        // When
        MockHttpServletResponse response = mockMvc.perform(
                post(URI.create("/sid/authenticate"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(OBJECT_MAPPER.writeValueAsString(request))
                    .headers(createDummyHeaders())
            ).andExpect(status().isOk())
            .andReturn().getResponse();

        // Then
        SessionIDResponse sessionIDResponse = OBJECT_MAPPER.readValue(
            response.getContentAsString(),
            SessionIDResponse.class
        );

        assertNotNull(sessionIDResponse.getSessionID());
        assertEquals(sessionId, sessionIDResponse.getSessionID());
    }

    @Test
    void shouldGetSidSession() throws Exception {
        // Given
        var sessionId = UUID.randomUUID();
        var sessionResponse = createSessionStatusResponse();

        when(sidClient.sessionStatus(sessionId)).thenReturn(sessionResponse);

        // When
        MockHttpServletResponse response = mockMvc.perform(
                get(URI.create("/sid/session/" + sessionId))
                    .headers(createDummyHeaders())
            ).andExpect(status().isOk())
            .andReturn().getResponse();

        // Then
        SessionStatusResponse sessionStatusResponse = OBJECT_MAPPER.readValue(
            response.getContentAsString(),
            SessionStatusResponse.class
        );

        assertEquals(COMPLETE, sessionStatusResponse.getState());
        assertNotNull(sessionStatusResponse.getResult());
        assertEquals(OK, sessionStatusResponse.getResult().getEndResult());
    }

    private HttpHeaders createDummyHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-cdoc2-session-token", "dummy_session_token");
        headers.add("x-cdoc2-session-x5c", "dummy_cert");
        return headers;
    }

    private record GetSessionNonceResponseBody(String nonce) {
    }

    private record GetWellKnownResponseBody(
        List<WellKnownKeys> keys
    ) {
    }

    private record WellKnownKeys(
        String kid,
        String kty
    ) {
    }
}
