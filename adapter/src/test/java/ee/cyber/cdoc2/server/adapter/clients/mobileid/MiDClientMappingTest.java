package ee.cyber.cdoc2.server.adapter.clients.mobileid;

import ee.sk.mid.MidClient;
import ee.sk.mid.rest.MidConnector;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.request.MidSessionStatusRequest;
import ee.sk.mid.rest.dao.response.MidAuthenticationResponse;

import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ee.cyber.cdoc2.server.adapter.exception.ClientBadRequestException;
import ee.cyber.cdoc2.server.adapter.generated.model.MidDisplayTextFormat;
import ee.cyber.cdoc2.server.adapter.generated.model.MidHashType;
import ee.cyber.cdoc2.server.adapter.generated.model.MidLanguage;

import static ee.cyber.cdoc2.util.RpRequestUtil.MID_IDENTIFIER_OK;
import static ee.cyber.cdoc2.util.RpRequestUtil.MID_PHONE_NUMBER;
import static ee.cyber.cdoc2.util.RpRequestUtil.createMidAuthenticateRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class MiDClientMappingTest {

    private static final int TIMEOUT_SECONDS = 30;

    @Mock
    private MidClient midClient;

    @Mock
    private MidConnector midConnector;

    private MiDClient client;

    @BeforeEach
    void setUp() {
        lenient().when(midClient.getMobileIdConnector()).thenReturn(midConnector);

        MobileIdClientConfiguration.AppProperties props =
            new MobileIdClientConfiguration.AppProperties(
                "https://tsp.demo.sk.ee/mid-api",
                TIMEOUT_SECONDS
            );

        client = new MiDClient(midClient, props);
    }

    @Test
    void authenticateBuildsRequestFromAllFieldsAndReturnsSessionId() {
        UUID expectedSessionId = UUID.randomUUID();
        MidAuthenticationResponse sdkResponse = new MidAuthenticationResponse();
        sdkResponse.setSessionID(expectedSessionId.toString());

        ArgumentCaptor<MidAuthenticationRequest> requestCaptor =
            ArgumentCaptor.forClass(MidAuthenticationRequest.class);
        when(midConnector.authenticate(requestCaptor.capture())).thenReturn(sdkResponse);

        var openApiRequest = createMidAuthenticateRequest();
        UUID returnedSessionId = client.authenticate(MID_IDENTIFIER_OK, openApiRequest);

        assertEquals(expectedSessionId, returnedSessionId);

        MidAuthenticationRequest sdkRequest = requestCaptor.getValue();
        assertEquals(MID_PHONE_NUMBER, sdkRequest.getPhoneNumber());
        assertEquals(MID_IDENTIFIER_OK, sdkRequest.getNationalIdentityNumber(),
            "nationalIdentityNumber must come from the semanticsIdentifier method parameter, "
                + "not from the OpenAPI request object");
        assertEquals(ee.sk.mid.MidLanguage.ENG, sdkRequest.getLanguage());
        assertEquals(openApiRequest.getDisplayText(), sdkRequest.getDisplayText());
        assertEquals(ee.sk.mid.MidDisplayTextFormat.GSM7, sdkRequest.getDisplayTextFormat());
        assertEquals(ee.sk.mid.MidHashType.SHA512, sdkRequest.getHashType());
        assertEquals(Base64.getEncoder().encodeToString(openApiRequest.getHash()), sdkRequest.getHash());
    }

    @Test
    void authenticateMapsHashTypeEnumValues() {
        ArgumentCaptor<MidAuthenticationRequest> captor =
            ArgumentCaptor.forClass(MidAuthenticationRequest.class);
        when(midConnector.authenticate(captor.capture()))
            .thenReturn(new MidAuthenticationResponse(UUID.randomUUID().toString()));

        client.authenticate(MID_IDENTIFIER_OK, createMidAuthenticateRequest().hashType(MidHashType.SHA256));
        client.authenticate(MID_IDENTIFIER_OK, createMidAuthenticateRequest().hashType(MidHashType.SHA384));
        client.authenticate(MID_IDENTIFIER_OK, createMidAuthenticateRequest().hashType(MidHashType.SHA512));

        assertEquals(ee.sk.mid.MidHashType.SHA256, captor.getAllValues().get(0).getHashType());
        assertEquals(ee.sk.mid.MidHashType.SHA384, captor.getAllValues().get(1).getHashType());
        assertEquals(ee.sk.mid.MidHashType.SHA512, captor.getAllValues().get(2).getHashType());
    }

    @Test
    void authenticateMapsLanguageEnumValues() {
        ArgumentCaptor<MidAuthenticationRequest> captor =
            ArgumentCaptor.forClass(MidAuthenticationRequest.class);
        when(midConnector.authenticate(captor.capture()))
            .thenReturn(new MidAuthenticationResponse(UUID.randomUUID().toString()));

        client.authenticate(MID_IDENTIFIER_OK, createMidAuthenticateRequest().language(MidLanguage.EST));
        client.authenticate(MID_IDENTIFIER_OK, createMidAuthenticateRequest().language(MidLanguage.RUS));
        client.authenticate(MID_IDENTIFIER_OK, createMidAuthenticateRequest().language(MidLanguage.LIT));

        assertEquals(ee.sk.mid.MidLanguage.EST, captor.getAllValues().get(0).getLanguage());
        assertEquals(ee.sk.mid.MidLanguage.RUS, captor.getAllValues().get(1).getLanguage());
        assertEquals(ee.sk.mid.MidLanguage.LIT, captor.getAllValues().get(2).getLanguage());
    }

    @Test
    void authenticateMapsDisplayTextFormatEnumValues() {
        ArgumentCaptor<MidAuthenticationRequest> captor =
            ArgumentCaptor.forClass(MidAuthenticationRequest.class);
        when(midConnector.authenticate(captor.capture()))
            .thenReturn(new MidAuthenticationResponse(UUID.randomUUID().toString()));

        client.authenticate(MID_IDENTIFIER_OK,
            createMidAuthenticateRequest().displayTextFormat(MidDisplayTextFormat.GSM_7));
        client.authenticate(MID_IDENTIFIER_OK,
            createMidAuthenticateRequest().displayTextFormat(MidDisplayTextFormat.UCS_2));

        assertEquals(ee.sk.mid.MidDisplayTextFormat.GSM7, captor.getAllValues().get(0).getDisplayTextFormat());
        assertEquals(ee.sk.mid.MidDisplayTextFormat.UCS2, captor.getAllValues().get(1).getDisplayTextFormat());
    }

    @Test
    void authenticateUnknownHashTypeThrows() {
        ClientBadRequestException exception = assertThrows(ClientBadRequestException.class,
            () -> client.authenticate(MID_IDENTIFIER_OK,
                createMidAuthenticateRequest().hashType(MidHashType.UNKNOWN_DEFAULT_OPEN_API)));

        assertEquals("Unknown hash value", exception.getMessage());
    }

    @Test
    void authenticateUnknownLanguageThrows() {
        ClientBadRequestException exception = assertThrows(ClientBadRequestException.class,
            () -> client.authenticate(MID_IDENTIFIER_OK,
                createMidAuthenticateRequest().language(MidLanguage.UNKNOWN_DEFAULT_OPEN_API)));

        assertEquals("Unknown langue", exception.getMessage());
    }

    @Test
    void authenticateUnknownDisplayTextFormatThrows() {
        ClientBadRequestException exception = assertThrows(ClientBadRequestException.class,
            () -> client.authenticate(MID_IDENTIFIER_OK,
                createMidAuthenticateRequest()
                    .displayTextFormat(MidDisplayTextFormat.UNKNOWN_DEFAULT_OPEN_API)));

        assertEquals("Unknown displayText format", exception.getMessage());
    }

    @Test
    void sessionStatusAppliesConfiguredTimeoutSecondsToSdkRequest() {
        UUID sessionId = UUID.randomUUID();
        MidSessionStatus sdkStatus = new MidSessionStatus();
        sdkStatus.setState("COMPLETE");

        ArgumentCaptor<MidSessionStatusRequest> captor =
            ArgumentCaptor.forClass(MidSessionStatusRequest.class);
        when(midConnector.getAuthenticationSessionStatus(captor.capture())).thenReturn(sdkStatus);

        MidSessionStatus result = client.sessionStatus(sessionId);

        assertEquals(sdkStatus, result);

        MidSessionStatusRequest sdkRequest = captor.getValue();
        assertEquals(sessionId.toString(), sdkRequest.getSessionID());
        assertEquals(TIMEOUT_SECONDS, sdkRequest.getTimeoutMs() / 1000,
            "Configured timeoutSeconds must be applied; note the SDK stores it as millis");
    }
}
