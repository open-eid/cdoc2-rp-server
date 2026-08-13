package ee.cyber.cdoc2.server.adapter.clients.smartid;

import ee.sk.smartid.SmartIdClient;
import ee.sk.smartid.exception.UserAccountException;
import ee.sk.smartid.exception.UserActionException;
import ee.sk.smartid.exception.permanent.RelyingPartyAccountConfigurationException;
import ee.sk.smartid.exception.permanent.SmartIdClientException;
import ee.sk.smartid.exception.permanent.SmartIdRequestSetupException;
import ee.sk.smartid.exception.useraccount.CertificateLevelMismatchException;
import ee.sk.smartid.exception.useraccount.DocumentUnusableException;
import ee.sk.smartid.exception.useraccount.NoSuitableAccountOfRequestedTypeFoundException;
import ee.sk.smartid.exception.useraccount.PersonShouldViewSmartIdPortalException;
import ee.sk.smartid.exception.useraccount.RequiredInteractionNotSupportedByAppException;
import ee.sk.smartid.exception.useraccount.UserAccountNotFoundException;
import ee.sk.smartid.exception.useraccount.UserAccountUnusableException;
import ee.sk.smartid.exception.useraction.SessionTimeoutException;
import ee.sk.smartid.exception.useraction.UserRefusedCertChoiceException;
import ee.sk.smartid.exception.useraction.UserRefusedConfirmationMessageException;
import ee.sk.smartid.exception.useraction.UserRefusedConfirmationMessageWithVerificationChoiceException;
import ee.sk.smartid.exception.useraction.UserRefusedDisplayTextAndPinException;
import ee.sk.smartid.exception.useraction.UserRefusedException;
import ee.sk.smartid.exception.useraction.UserSelectedWrongVerificationCodeException;
import ee.sk.smartid.rest.SessionStatusPoller;
import ee.sk.smartid.rest.SmartIdConnector;
import ee.sk.smartid.rest.dao.NotificationAuthenticationSessionRequest;
import ee.sk.smartid.rest.dao.SemanticsIdentifier;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ee.cyber.cdoc2.server.adapter.conf.RelyingPartyConfImpl;
import ee.cyber.cdoc2.server.adapter.exception.ClientBadRequestException;

import static ee.cyber.cdoc2.util.RpRequestUtil.createSidAuthenticateRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SiDClientExceptionTests {
    private static final String SID_CLIENT_ERROR_CODE = "SID_CLIENT_ERROR";
    public static final String EE_SEMANTICS_IDENTIFIER = "PNOEE-40504040001";

    @Mock
    private SmartIdClient mockSmartIdClient;
    @Mock
    private SmartIdConnector mockSmartIdConnector;
    @Mock
    private SessionStatusPoller mockSessionStatusPoller;
    @Mock
    private RelyingPartyConfImpl mockRelyingPartyConf;

    private static Stream<UserAccountException> userAccountExceptions() {
        return Stream.of(
            new CertificateLevelMismatchException(),
            new DocumentUnusableException(),
            new NoSuitableAccountOfRequestedTypeFoundException(),
            new PersonShouldViewSmartIdPortalException(),
            new RequiredInteractionNotSupportedByAppException(),
            new UserAccountNotFoundException(),
            new UserAccountUnusableException()
        );
    }

    private static Stream<UserActionException> userActionExceptions() {
        return Stream.of(
            new SessionTimeoutException(),
            new UserRefusedCertChoiceException(),
            new UserRefusedConfirmationMessageException(),
            new UserRefusedConfirmationMessageWithVerificationChoiceException(),
            new UserRefusedDisplayTextAndPinException(),
            new UserRefusedException("user refused"),
            new UserSelectedWrongVerificationCodeException()
        );
    }

    private static Stream<SmartIdClientException> smartIdClientExceptions() {
        return Stream.of(
            new SmartIdClientException("client setup error"),
            new SmartIdRequestSetupException("bad request setup"),
            new RelyingPartyAccountConfigurationException("bad rp config", new Exception("cause"))
        );
    }

    @ParameterizedTest
    @MethodSource("userAccountExceptions")
    void authenticateWrapsUserAccountExceptionAsClientBadRequestException(
        UserAccountException exception
    ) throws Exception {
        var rpRequest = createSidAuthenticateRequest();
        var client = stubAuthenticateToThrow(exception);

        assertWrappedAsClientBadRequestException(
            () -> client.authenticate("", rpRequest), exception);
    }

    @ParameterizedTest
    @MethodSource("userActionExceptions")
    void authenticateWrapsUserActionExceptionAsClientBadRequestException(
        UserActionException exception
    ) throws Exception {
        var rpRequest = createSidAuthenticateRequest();
        var client = stubAuthenticateToThrow(exception);

        assertWrappedAsClientBadRequestException(
            () -> client.authenticate(EE_SEMANTICS_IDENTIFIER, rpRequest), exception);
    }

    @ParameterizedTest
    @MethodSource("smartIdClientExceptions")
    void authenticateWrapsSmartIdClientExceptionAsClientBadRequestException(
        SmartIdClientException exception
    ) throws Exception {
        var rpRequest = createSidAuthenticateRequest();
        var client = stubAuthenticateToThrow(exception);

        assertWrappedAsClientBadRequestException(
            () -> client.authenticate(EE_SEMANTICS_IDENTIFIER, rpRequest), exception);
    }

    @ParameterizedTest
    @MethodSource("userAccountExceptions")
    void sessionStatusWrapsUserAccountExceptionAsClientBadRequestException(
        UserAccountException exception
    ) {
        var sessionId = UUID.randomUUID();
        var client = stubSessionStatusToThrow(exception);

        assertWrappedAsClientBadRequestException(() -> client.sessionStatus(sessionId), exception);
    }

    @ParameterizedTest
    @MethodSource("userActionExceptions")
    void sessionStatusWrapsUserActionExceptionAsClientBadRequestException(
        UserActionException exception
    ) {
        var sessionId = UUID.randomUUID();
        var client = stubSessionStatusToThrow(exception);

        assertWrappedAsClientBadRequestException(() -> client.sessionStatus(sessionId), exception);
    }

    @ParameterizedTest
    @MethodSource("smartIdClientExceptions")
    void sessionStatusWrapsSmartIdClientExceptionAsClientBadRequestException(
        SmartIdClientException exception
    ) {
        var sessionId = UUID.randomUUID();
        var client = stubSessionStatusToThrow(exception);

        assertWrappedAsClientBadRequestException(() -> client.sessionStatus(sessionId), exception);
    }

    private SiDClient siDClientWithMocks() {
        return new SiDClient(mockSmartIdClient, mockRelyingPartyConf);
    }

    private SiDClient stubAuthenticateToThrow(RuntimeException exception) {
        when(mockSmartIdClient.getSmartIdConnector()).thenReturn(mockSmartIdConnector);
        when(mockRelyingPartyConf.getSidUuid()).thenReturn(UUID.randomUUID());
        when(mockRelyingPartyConf.getSidName()).thenReturn("rp-name");
        when(mockSmartIdConnector.initNotificationAuthentication(
            any(NotificationAuthenticationSessionRequest.class), any(SemanticsIdentifier.class)
        )).thenThrow(exception);

        return siDClientWithMocks();
    }

    private SiDClient stubSessionStatusToThrow(RuntimeException exception) {
        when(mockSmartIdClient.getSessionStatusPoller()).thenReturn(mockSessionStatusPoller);
        when(mockSessionStatusPoller.getSessionStatus(any())).thenThrow(exception);

        return siDClientWithMocks();
    }

    private void assertWrappedAsClientBadRequestException(
        Executable executable, RuntimeException exception
    ) {
        var thrown = assertThrows(ClientBadRequestException.class, executable);

        assertEquals(SID_CLIENT_ERROR_CODE, thrown.getCode());
        assertEquals(exception.getMessage(), thrown.getMessage());
    }
}
