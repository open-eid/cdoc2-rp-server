package ee.cyber.cdoc2;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import ee.cyber.cdoc2.server.adapter.clients.mobileid.MiDClient;
import ee.cyber.cdoc2.server.adapter.clients.smartid.SiDClient;
import ee.cyber.cdoc2.server.adapter.db.jpa.SessionNonceEntity;
import ee.cyber.cdoc2.server.adapter.db.jpa.SessionNonceJpaRepository;
import ee.cyber.cdoc2.server.adapter.generated.model.MidSessionStatusResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.SessionIDResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.SessionStatusResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static ee.cyber.cdoc2.RpRequestUtil.*;
import static ee.cyber.cdoc2.server.adapter.generated.model.SessionStatusResponse.StateEnum.COMPLETE;
import static ee.cyber.cdoc2.server.adapter.generated.model.SessionStatusResponseResult.EndResultEnum.OK;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class Cdoc2RpServerApplicationTest {
    @SuppressWarnings("checkstyle:LineLength")
    private static final String SID_SESSION_TOKEN_WITH_FILTERED_DISCLOSURES_BASE64URL =
        "eyJraWQiOiJlYy1rZXktMjAyNiIsInR5cCI6InZuZC5jZG9jMi5zZXNzaW9uLXRva2VuLnYyK3NkLWp3dCIsImFsZyI6IkVTMjU2In0.eyJycENoYWxsZW5nZSI6InJrZ2s0cTE2eTYxbFJoRVJOcVZVdzBpdEhXZ3MzbWZMS3Y5cEQ2Z2xCdDl0ZDJRbmVhd1lLM0ZGOHFkMXBSakdDbnlyZ3NWTmprSXJ3T3NuZXkzOXl3PT0iLCJzdWIiOiJldHNpL1BOT0VFLTQwNTA0MDQwMDAxIiwic2lnbmF0dXJlIjp7InZhbHVlIjoiSlhtVmgwWlZqdFRUZHFFaHlET1NCejNMLyt4UHZPTGF1WXVmbmUydS8wRkVuZ0loQ0g4WEl6ZW5zazhsa3BLZlNxYVBjSzZReDF5bVZpdGM1YUNWY1N6bjRzUVV3SW5OODBXVEd1UTZtNTNESGdXWnFnS3NabHErSDcwamxxMXZSUE0vS3UwVEJIK01GRkhOeWp5SWZWN0MwOVMyK2pCbm1kYWEzM0FaNCtnOS9FNWVnL1p6QktHQWNnQmFyU01lYVpOdXNXQWNrdlJBQ3Y1WXlidUVYK0JSM3NYMXA0U09XNWdMT2lPOUwzaWtzVXNzVFp2K3MyVU9kbFVZTFpTVGVrWWIrYXFQRkdzODdjY1FQUmZuRlRWV1EwZ0pvVENWVG1lWWdXazA5MjZMOVREaUlQZlJ5cWxBbkNQNWRPZGlxRmNLSXFOZ3d4Z0RyZmlITmJ2R3hhQ3hGeVdJd2R2ekdmNEZPa3VMaVdndXVPQVo3YlgxVzIwQnNlZjFHTnRTQUdBVlRMbmJsMUNiOC8rT2dwRU52WXM2UUtxdXdiRlZHTDhzRDJEc1czNXZodllXdkJJN0tVK1NxODBnSDhURkFvdzNiN3QzOU80UEJmbVMzOUJrRTgxMW9mK2MwSFpNMXhJd3NTajZVVkR3bkNLOHYwZ1BKOGZMMmo5NDIrVUVVRUpQUEJaNmo1TWcrRmxBb1ZFR2pHakp1cDF3NVdCTFVBYTl1blRiNWp5UGtFUW84clVrS1Y0ZmQ0b21XTEpobGJXcmNYWlh5ZndrRGhJR211Uzh4Z2w2NSs0anMyVTI4THRDQzJYSjhlNWJhWm9rNWQ0Q2VVbFJvUjErbmIvZTg0cmthOUtPOUV6MGZHVmRlSmc0MFZhYTBWb2xBeDkxYVVmUS9mV0tpRXMxbHBOR0EyMG55cytJSTU3QWZqRDdkdGJxTzZaNzBXbEpUenMrREE0SHJEcXFQQ0ZRbVRmc2VhWFFOOVBxK3RnRFZqdFc1TlVRMTg4UlhSZ2pvalp4ZitCT2piVDJ6b0xxMS96VmdETkZTaW1kSElLQitJYUlwaDB5LzZHWWFGb0p2eERsRm9YSzhwUnU4My8vdGNnZmFuN1gzUWZKMnF1WTN3T2VDUHN5dmM1TklNdHRJdnhHRFlTdWI3QW9Ta3Z4eGFsdHg3Vy9FWEJWVmNDaWROZE1YM1ErZ2VNQlMzeS9NbEE3M0pqMXQxb1N6UmZxdFFpY2tYd0w3bmYrNXB5RzY2eUVFb1ZaZ1dVdE0zMUptSE9LVUF6UGVFOSthd2xRUjd3NXJSeitFemJ1MitLdlY2N1V4NFdXVG5JY0pOUG4rRkhOKzE1V1ZlTVhuRlhxVHZOSiIsInNlcnZlclJhbmRvbSI6InNWOXdsS3RaZTV0cjBnTjlpZXRQU0ovVCIsInVzZXJDaGFsbGVuZ2UiOiJmeWtaTHJmU2tsMW9uMXBITlBrZEFZUS1pekd0N1ZGeWN6eUNDM2x4cmlrIiwic2lnbmF0dXJlQWxnb3JpdGhtIjoicnNhc3NhLXBzcyIsImZsb3dUeXBlIjoiTm90aWZpY2F0aW9uIiwic2lnbmF0dXJlQWxnb3JpdGhtUGFyYW1ldGVycyI6eyJoYXNoQWxnb3JpdGhtIjoiU0hBLTI1NiIsIm1hc2tHZW5BbGdvcml0aG0iOnsiYWxnb3JpdGhtIjoiaWQtbWdmMSIsInBhcmFtZXRlcnMiOnsiaGFzaEFsZ29yaXRobSI6IlNIQS0yNTYifX0sInNhbHRMZW5ndGgiOjMyLCJ0cmFpbGVyRmllbGQiOiIweGJjIn19LCJpc3MiOiJodHRwczovL2Nkb2MyLWF1dGgtc2VydmVyLmVlIiwic2NoZW1lTmFtZSI6InNtYXJ0LWlkLWRlbW8iLCJzaWduYXR1cmVQcm90b2NvbCI6IlJTQVNTQS1QU1MrQUNTUF9WMiIsIl9zZCI6WyJuTEpHdS05X3lKMmlEOGhrRXU5Ym5yc0EzUHJ5Y3UwVVE1WXQ5UENTNV8wIl0sImludGVyYWN0aW9uc0RpZ2VzdCI6Im9sSk43T1hVdmZ5MWJVUE51NzEyWDNBN01PbTFCWGlXdGxBbXYrdWJJejA9IiwiX3NkX2FsZyI6InNoYS0yNTYiLCJleHAiOjE3NzY4NzI1MjksImlhdCI6MTc3Njc4NjEyOSwiaW50ZXJhY3Rpb25UeXBlVXNlZCI6ImNvbmZpcm1hdGlvbk1lc3NhZ2VBbmRWZXJpZmljYXRpb25Db2RlQ2hvaWNlIiwicnBOYW1lIjoiREVNTyJ9.5ORVwgy0tMpX5tdwZmhnnQK_H4ngB-duofWj2OYCrJU5kL5dUvJRSeiC5QLbuzH-8gk08b5asqIW9lWNEErAuw~WyJONGFScHVxNTVRZzh6LTVxS3dlRURBIiwiYXVkIixbeyIuLi4iOiIxM19rVmNGcXF3M0tycllRaUpnUEJ4Qm1zOG1rY0puMmtnNWZBRGc4aUlFIn0seyIuLi4iOiJkaG9VbVZod0c2TEJIbkwyMHJxbDZFTkVFdjlfdHBPOFo4aUVFbmVESmhjIn1dXQ~WyJMb2dqR24xc21ZNmxpWllEZnh4OGhnIiwiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3Nlc3Npb25fbm9uY2VfMi9uclZjU0VjSHVXdDJTS2Zqa01tNlJRIl0~";
    @SuppressWarnings("checkstyle:LineLength")
    private static final String SID_SIGNING_CERTIFICATE_BASE64URL =
        "MIIGpzCCBi6gAwIBAgIQGcJUbe6JHI6jJyV-42vjnTAKBggqhkjOPQQDAzBxMSwwKgYDVQQDDCNURVNUIG9mIFNLIElEIFNvbHV0aW9ucyBFSUQtUSAyMDI0RTEXMBUGA1UEYQwOTlRSRUUtMTA3NDcwMTMxGzAZBgNVBAoMElNLIElEIFNvbHV0aW9ucyBBUzELMAkGA1UEBhMCRUUwHhcNMjYwMTA2MTQyNTAxWhcNMjkwMTA1MTQyNTAwWjBXMQswCQYDVQQGEwJFRTEQMA4GA1UEAwwHVEVTVCxPSzENMAsGA1UEBAwEVEVTVDELMAkGA1UEKgwCT0sxGjAYBgNVBAUTEVBOT0VFLTQwNTA0MDQwMDAxMIIDIjANBgkqhkiG9w0BAQEFAAOCAw8AMIIDCgKCAwEAkI98VzyaeSueyaUQYIXMMf-1VY10Gw-b8Q13Rb9N62ROZY97wMIB__f8_PuOIoqkAPM6Tn_t4lp1R_rHrbuqs0hl2dgLlOcR5wmWmp7YfKPDvRndVLl_doIHruxY8O60rFGskSnqt4coHN4xGcmCyPkJoB8Rfm8-Y9poVKAreS0Ta32p5OSME0HjSs7-ahB2erWfb2GulFw1vyeH42d3XDpCCfd6CByvSsi4oByUqs5G-kjSrGUglflgWXK3MxBYto0swgsbD1nrW5doU_cMCfRoFURun4XguX8dTt9VeyqeJitxRfub2Hj18RbsKuoFNHQNOxAxRK4oTVCtUrYbVqBHDmoOm8r3CsSuqjuZ2njQybiUhBofpTVMCZ6lB6VgoLphmEwSEOQXIumpmpb2qJZqbZaBoyyWb4f5AQjw3Q5lwPSao5215hIgSuuENRezpP9rTzIwyOMbnV2nMSMInAuaXIXskB2NdpMsROsvOqBC0h5azTj9naCS-5EW-9eI7GGK03Du5JoKD5wYajJxfcxFwBAl8Ko71OvhGFtYiu-hqzz-CyG6NswB87KvzDYUCQ-0qOfgRBNCgYnbjnuYVJb3CGLp_cP5GmKtUC3wHX1WnPGyK4bD19Rcy-FhG6mD_ZrAPcmZ3s4FLLErpRJ3ui-fiMPLQl2bpCKTWoaEZoPg6Grnhr3bE2ZiKWmqdVwf30bG3-GnvTBTuF0T1lzt6NeBlB23SJsffCmzSFSNcFJHHYI1FYdZu2p0gL6KAabEmnE8GrTrCn93DFNBtoKu9vG30QrRzyh-itPvtn9w-9t-nDkhaVHmNCjWD1xcMeXsyK8ek0rbz5aVe_RPvCifhIpgjqNsDHh9q1QT9KIFsd6RD2XPMlekL9c6YiVY9H7uRyIQWqJwtrvNvBKj4ZT9745zTfkhCJTPvnLy-4iKeINVZ2f98BblsGAEHKGol8YA-3SRkPh9BVnVhSdI3lxCDEbmHuk21GIPE9689efSvbcDEHpqeYoxo3tXjl_hqfzPAgMBAAGjggH1MIIB8TAJBgNVHRMEAjAAMB8GA1UdIwQYMBaAFLAkFxmI42b4zShYZXtNFNiSZk9rMHAGCCsGAQUFBwEBBGQwYjAzBggrBgEFBQcwAoYnaHR0cDovL2Muc2suZWUvVEVTVF9FSUQtUV8yMDI0RS5kZXIuY3J0MCsGCCsGAQUFBzABhh9odHRwOi8vYWlhLmRlbW8uc2suZWUvZWlkcTIwMjRlMDAGA1UdEQQpMCekJTAjMSEwHwYDVQQDDBhQTk9FRS00MDUwNDA0MDAwMS1ERU0wLVEweAYDVR0gBHEwbzBjBgkrBgEEAc4fEQIwVjBUBggrBgEFBQcCARZIaHR0cHM6Ly93d3cuc2tpZHNvbHV0aW9ucy5ldS9yZXNvdXJjZXMvY2VydGlmaWNhdGlvbi1wcmFjdGljZS1zdGF0ZW1lbnQvMAgGBgQAj3oBAjAoBgNVHQkEITAfMB0GCCsGAQUFBwkBMREYDzE5MDUwNDA0MTIwMDAwWjAWBgNVHSUEDzANBgsrBgEEAYPmYgUHADA0BgNVHR8ELTArMCmgJ6AlhiNodHRwOi8vYy5zay5lZS90ZXN0X2VpZC1xXzIwMjRlLmNybDAdBgNVHQ4EFgQUX9YaVGlPdUOO2J6rzNc4sljBQBAwDgYDVR0PAQH_BAQDAgeAMAoGCCqGSM49BAMDA2cAMGQCMHhYJCeKceJv_m0xcFRssS4WVFnnCryDiuSEpjDZu0irJ_XurXXIFDr-9hhl2x7GMwIwbiD5GALRtwzUaEh-SV9jigT9Oc336f6QYf8YaSA0-Un8eRQPa9wTK0cSQrM_CUIu";
    @SuppressWarnings("checkstyle:LineLength")
    private static final String MID_SESSION_TOKEN_WITH_FILTERED_DISCLOSURES_BASE64URL = "eyJraWQiOiJlYy1rZXktMjAyNiIsInR5cCI6InZuZC5jZG9jMi5zZXNzaW9uLXRva2VuLnYyK3NkLWp3dCIsImFsZyI6IkVTMjU2In0.eyJpc3MiOiJodHRwczovL2Nkb2MyLWF1dGgtc2VydmVyLmVlIiwiX3NkIjpbImxYbWZSTkEzbWpDRUlDalYwWXRjaXlhV1NUeUZYSm1LUENwWnZ0RF9tQ1EiXSwic3ViIjoiZXRzaS9QTk9FRS01MTMwNzE0OTU2MCIsImV4cCI6MTc3Njk0NzQwMCwiaWF0IjoxNzc2ODYxMDAwLCJfc2RfYWxnIjoic2hhLTI1NiJ9.DBzttUGDo6e-UWOxNPcn2J1vvKXP5f0HS72Ze65SrHueb8zDwlPAkVPfBD9ATL5Cv_a4rLXwpI8CPCD8_ZXWhw~WyJaUlZxSnFOaGMxWU45NkFVLTlKV2JBIiwiYXVkIixbeyIuLi4iOiJLcHBNbWFQUnpqWnc5THZXUlZnSlVqcGxQbmUwc0hZcEF2VUU4RGdhMUVrIn0seyIuLi4iOiJQWFFhMVRLUjVSazAyQ3pKNzM5SGk4bFo4LUpBVDVpbThFbDVlTVVJWUxBIn1dXQ~WyI5U2xSd2k3N1lqSXRaU1l2MDhGdlZnIiwiaHR0cHM6Ly9sb2NhbGhvc3Q6OTA4MC9zZXNzaW9uX25vbmNlXzIvbnJWY1NFY0h1V3QyU0tmamtNbTZSUSJd~";
    @SuppressWarnings("checkstyle:LineLength")
    private static final String MID_SIGNING_CERTIFICATE_BASE64URL = "MIIDqDCCAy6gAwIBAgIQB9W11BzBABj-0d_AZx6UHzAKBggqhkjOPQQDAjBxMQswCQYDVQQGEwJFRTEbMBkGA1UECgwSU0sgSUQgU29sdXRpb25zIEFTMRcwFQYDVQRhDA5OVFJFRS0xMDc0NzAxMzEsMCoGA1UEAwwjVEVTVCBvZiBTSyBJRCBTb2x1dGlvbnMgRUlELVEgMjAyMUUwHhcNMjQwNjEyMDY0NTI4WhcNMjkwNjE2MDY0NTI3WjCBlTELMAkGA1UEBhMCRUUxLzAtBgNVBAMMJk1BUlkgw4ROTixPJ0NPTk5Fxb0txaBVU0xJSyBURVNUTlVNQkVSMSUwIwYDVQQEDBxPJ0NPTk5Fxb0txaBVU0xJSyBURVNUTlVNQkVSMRIwEAYDVQQqDAlNQVJZIMOETk4xGjAYBgNVBAUTEVBOT0VFLTUxMzA3MTQ5NTYwMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEWlV1aVSXw6WhagWmFmXE_oe-0R1xZzrHyoiVlgKpGiJ8cwIQLogRGQnWY7NwgQvRHCBmsl99bj57h7SWnd03m6OCAYEwggF9MAkGA1UdEwQCMAAwHwYDVR0jBBgwFoAUScfc7QYUosdtnKbP11L9aOXoBBQwcAYIKwYBBQUHAQEEZDBiMDMGCCsGAQUFBzAChidodHRwOi8vYy5zay5lZS9URVNUX0VJRC1RXzIwMjFFLmRlci5jcnQwKwYIKwYBBQUHMAGGH2h0dHA6Ly9haWEuZGVtby5zay5lZS9laWRxMjAyMWUweAYDVR0gBHEwbzAIBgYEAI96AQIwYwYJKwYBBAHOHxIBMFYwVAYIKwYBBQUHAgEWSGh0dHBzOi8vd3d3LnNraWRzb2x1dGlvbnMuZXUvcmVzb3VyY2VzL2NlcnRpZmljYXRpb24tcHJhY3RpY2Utc3RhdGVtZW50LzA0BgNVHR8ELTArMCmgJ6AlhiNodHRwOi8vYy5zay5lZS90ZXN0X2VpZC1xXzIwMjFlLmNybDAdBgNVHQ4EFgQUj8KjnXvGQJCRYOd5LVfPku7QsZwwDgYDVR0PAQH_BAQDAgeAMAoGCCqGSM49BAMCA2gAMGUCMQCocXWDbBnkM3WEyBdv9Vm0A1MNRv08WrR192dRBcX42Kz5oiH0SdHRJv2ffeuEeSwCMEw2tSA3ClJv233Dl7rIYU_T6UG2NQhvDD5FhnP0umZRmVfAUQ6eVcmU8AhFtNJjwg==";
    private static final String SESSION_NONCE_FOR_TOKEN = "nrVcSEcHuWt2SKfjkMm6RQ";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final int EXPECTED_SESSION_NONCE_BYTES = 16;
    private static final int WIREMOCK_PORT = 8090;
    private static final Instant INSTANT_NOW_SESSION_TOKEN_NOT_EXPIRED =
        Instant.parse("2026-04-22T12:30:00Z");

    @RegisterExtension
    static WireMockExtension wiremock = WireMockExtension.newInstance()
        .options(wireMockConfig()
            .httpsPort(WIREMOCK_PORT)
            .keystorePath("wiremock_keystore.p12")
            .keystorePassword("changeit")
            .keyManagerPassword("changeit")
            .keystoreType("PKCS12")
        )
        .build();

    @Autowired
    private SessionNonceJpaRepository sessionNonceJpaRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ResourceLoader resourceLoader;

    @MockitoBean
    private SiDClient sidClient;
    @MockitoBean
    private MiDClient miDClient;
    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUp() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:auth-server-well-known.json");
        String keysResponseBody = resource.getContentAsString(StandardCharsets.UTF_8);

        wiremock.stubFor(
            WireMock.get(urlEqualTo("/.well-known/jwks.jws"))
                .willReturn(aResponse()
                    .withStatus(HttpStatus.OK.value())
                    .withBody(keysResponseBody)
                )
        );

        sessionNonceJpaRepository.deleteAll();
        saveNonceForSessionToken();

        when(clock.instant()).thenReturn(INSTANT_NOW_SESSION_TOKEN_NOT_EXPIRED);
    }

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

        var nonceFromDb = sessionNonceJpaRepository.findByNonce(decodedNonce);
        assertTrue(nonceFromDb.isPresent());
        assertArrayEquals(decodedNonce, nonceFromDb.get().getNonce());
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
        var request = createSidAuthenticateRequest();
        var sessionId = UUID.randomUUID();

        when(sidClient.authenticate(EE_SEMANTICS_IDENTIFIER_OK, request))
            .thenReturn(sessionId);

        // When
        MockHttpServletResponse response = mockMvc.perform(
                post(URI.create("/sid/authenticate"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(OBJECT_MAPPER.writeValueAsString(request))
                    .headers(createHeadersForSid())
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
                    .headers(createHeadersForSid())
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

    @Test
    void shouldStartMidAuthentication() throws Exception {
        // Given
        var request = createMidAuthenticateRequest();
        var sessionId = UUID.randomUUID();

        when(miDClient.authenticate(
            request.getNationalIdentityNumber(),
            request
        )).thenReturn(sessionId);

        // When
        MockHttpServletResponse response = mockMvc.perform(
                post(URI.create("/mid/authenticate"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(OBJECT_MAPPER.writeValueAsString(request))
                    .headers(createHeadersForMid())
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
    void shouldGetMidSession() throws Exception {
        // Given
        var sessionId = UUID.randomUUID();

        when(miDClient.sessionStatus(sessionId)).thenReturn(createMidSessionStatus());

        // When
        MockHttpServletResponse response = mockMvc.perform(
                get(URI.create("/mid/session/" + sessionId))
                    .headers(createHeadersForMid())
            ).andExpect(status().isOk())
            .andExpect(header().exists("x-rp-signed-hash"))
            .andExpect(header().exists("x-rp-name"))
            .andExpect(header().exists("Signature"))
            .andExpect(header().exists("Signature-Input"))
            .andReturn().getResponse();

        // Then
        MidSessionStatusResponse sessionStatusResponse = OBJECT_MAPPER.readValue(
            response.getContentAsString(),
            MidSessionStatusResponse.class
        );

        assertEquals(MidSessionStatusResponse.StateEnum.COMPLETE, sessionStatusResponse.getState());
        assertEquals(MidSessionStatusResponse.ResultEnum.OK, sessionStatusResponse.getResult());
    }

    private void saveNonceForSessionToken() {
        SessionNonceEntity entity = new SessionNonceEntity();
        entity.setNonce(Base64.getUrlDecoder().decode(SESSION_NONCE_FOR_TOKEN));
        entity.setCreatedAt(Instant.now());
        sessionNonceJpaRepository.save(entity);
    }

    private HttpHeaders createHeadersForSid() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-cdoc2-session-token", SID_SESSION_TOKEN_WITH_FILTERED_DISCLOSURES_BASE64URL);
        headers.add("x-cdoc2-session-x5c", SID_SIGNING_CERTIFICATE_BASE64URL);
        return headers;
    }

    private HttpHeaders createHeadersForMid() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-cdoc2-session-token", MID_SESSION_TOKEN_WITH_FILTERED_DISCLOSURES_BASE64URL);
        headers.add("x-cdoc2-session-x5c", MID_SIGNING_CERTIFICATE_BASE64URL);
        return headers;
    }

    private record GetSessionNonceResponseBody(String nonce) {
    }

    private record GetWellKnownResponseBody(
        List<WellKnownKeys> keys
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WellKnownKeys(
        String kid,
        String kty
    ) {
    }
}
