package ee.cyber.cdoc2.server.adapter.clients.smartid;

import ee.sk.smartid.HashAlgorithm;
import ee.sk.smartid.SmartIdClient;
import ee.sk.smartid.VerificationCodeType;
import ee.sk.smartid.rest.SmartIdConnector;
import ee.sk.smartid.rest.dao.NotificationAuthenticationSessionRequest;
import ee.sk.smartid.rest.dao.NotificationAuthenticationSessionResponse;
import ee.sk.smartid.rest.dao.SemanticsIdentifier;
import ee.sk.smartid.signature.AuthenticationSignatureAlgorithm;

import java.util.Base64;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ee.cyber.cdoc2.server.adapter.conf.RelyingPartyConfImpl;
import ee.cyber.cdoc2.server.adapter.generated.model.SidAuthenticateRequest;

import static ee.cyber.cdoc2.util.RpRequestUtil.EE_SEMANTICS_IDENTIFIER_OK;
import static ee.cyber.cdoc2.util.RpRequestUtil.createSidAuthenticateRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SiDClientMappingTest {

    private static final UUID DEMO_RP_UUID = UUID.randomUUID();
    private static final String DEMO_RP_NAME = "DEMO";

    @Mock
    private SmartIdClient smartIdClient;

    @Mock
    private SmartIdConnector smartIdConnector;

    @Mock
    private RelyingPartyConfImpl relyingPartyConf;

    private SiDClient client;

    @BeforeEach
    void setUp() {
        lenient().when(smartIdClient.getSmartIdConnector()).thenReturn(smartIdConnector);
        lenient().when(relyingPartyConf.getSidUuid()).thenReturn(DEMO_RP_UUID);
        lenient().when(relyingPartyConf.getSidName()).thenReturn(DEMO_RP_NAME);

        client = new SiDClient(smartIdClient, relyingPartyConf);
    }

    @Test
    void authenticateForwardsInteractionsStringUntouched() throws JsonProcessingException {
        SidAuthenticateRequest request = createSidAuthenticateRequest();
        String interactionsFromCaller = request.getInteractions();

        NotificationAuthenticationSessionRequest sdkRequest = captureSdkRequest(request);

        assertSame(interactionsFromCaller, sdkRequest.interactions());
    }

    @Test
    void authenticatePinsFixedRequestFields() throws JsonProcessingException {
        NotificationAuthenticationSessionRequest sdkRequest =
            captureSdkRequest(createSidAuthenticateRequest());

        assertEquals(DEMO_RP_UUID.toString(), sdkRequest.relyingPartyUUID());
        assertEquals(DEMO_RP_NAME, sdkRequest.relyingPartyName());
        assertEquals("QUALIFIED", sdkRequest.certificateLevel());
        assertEquals("ACSP_V2", sdkRequest.signatureProtocol());
        assertEquals(VerificationCodeType.NUMERIC4.getValue(), sdkRequest.vcType());
        assertNull(sdkRequest.requestProperties());
        assertNull(sdkRequest.capabilities());
    }

    @Test
    void authenticateMapsSignatureProtocolParameters() throws JsonProcessingException {
        SidAuthenticateRequest request = createSidAuthenticateRequest();
        byte[] rpChallengeBytes = request.getSignatureProtocolParameters().getRpChallenge();

        NotificationAuthenticationSessionRequest sdkRequest = captureSdkRequest(request);

        assertEquals(Base64.getEncoder().encodeToString(rpChallengeBytes),
            sdkRequest.signatureProtocolParameters().rpChallenge());
        assertEquals(AuthenticationSignatureAlgorithm.RSASSA_PSS.getAlgorithmName(),
            sdkRequest.signatureProtocolParameters().signatureAlgorithm());
        assertEquals(HashAlgorithm.SHA_512.getAlgorithmName(),
            sdkRequest.signatureProtocolParameters().signatureAlgorithmParameters()
                .hashAlgorithm());
    }

    @Test
    void authenticatePassesSemanticsIdentifierAndReturnsSessionId() throws JsonProcessingException {
        UUID expectedSessionId = UUID.randomUUID();
        ArgumentCaptor<SemanticsIdentifier> captor =
            ArgumentCaptor.forClass(SemanticsIdentifier.class);
        when(smartIdConnector.initNotificationAuthentication(
            any(NotificationAuthenticationSessionRequest.class), captor.capture()
        )).thenReturn(new NotificationAuthenticationSessionResponse(expectedSessionId.toString()));

        UUID sessionId =
            client.authenticate(EE_SEMANTICS_IDENTIFIER_OK, createSidAuthenticateRequest());

        assertEquals(expectedSessionId, sessionId);
        assertEquals(EE_SEMANTICS_IDENTIFIER_OK, captor.getValue().getIdentifier());
    }

    private NotificationAuthenticationSessionRequest captureSdkRequest(
        SidAuthenticateRequest request
    ) {
        ArgumentCaptor<NotificationAuthenticationSessionRequest> captor =
            ArgumentCaptor.forClass(NotificationAuthenticationSessionRequest.class);
        when(smartIdConnector.initNotificationAuthentication(
            captor.capture(), any(SemanticsIdentifier.class)
        )).thenReturn(new NotificationAuthenticationSessionResponse(UUID.randomUUID().toString()));

        client.authenticate(EE_SEMANTICS_IDENTIFIER_OK, request);

        return captor.getValue();
    }
}
