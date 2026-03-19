package ee.cyber.cdoc2;

import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class Cdoc2RpServerApplicationTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int EXPECTED_SESSION_NONCE_BYTES = 16;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldGetSessionNonce() throws Exception {
        MockHttpServletResponse getSessionNonceResponse = mockMvc.perform(
                get(URI.create("/session_nonce"))
            ).andExpect(status().isOk())
            .andReturn().getResponse();

        GetSessionNonceResponseBody getSessionNonceResponseBody = OBJECT_MAPPER.readValue(
            getSessionNonceResponse.getContentAsString(),
            GetSessionNonceResponseBody.class
        );

        assertNotNull(getSessionNonceResponseBody.nonce);
        assertTrue(getSessionNonceResponseBody.nonce.length() >= EXPECTED_SESSION_NONCE_BYTES);

        byte[] decodedNonce = Base64.getDecoder().decode(getSessionNonceResponseBody.nonce);

        assertEquals(EXPECTED_SESSION_NONCE_BYTES, decodedNonce.length);
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
