package ee.cyber.cdoc2.server.adapter.clients.mobileid;

import ee.sk.mid.MidClient;
import ee.sk.mid.exception.MidDeliveryException;
import ee.sk.mid.exception.MidException;
import ee.sk.mid.exception.MidInternalErrorException;
import ee.sk.mid.exception.MidInvalidNationalIdentityNumberException;
import ee.sk.mid.exception.MidInvalidPhoneNumberException;
import ee.sk.mid.exception.MidInvalidUserConfigurationException;
import ee.sk.mid.exception.MidMissingOrInvalidParameterException;
import ee.sk.mid.exception.MidNotMidClientException;
import ee.sk.mid.exception.MidPhoneNotAvailableException;
import ee.sk.mid.exception.MidServiceUnavailableException;
import ee.sk.mid.exception.MidSessionNotFoundException;
import ee.sk.mid.exception.MidSessionTimeoutException;
import ee.sk.mid.exception.MidSslException;
import ee.sk.mid.exception.MidUnauthorizedException;
import ee.sk.mid.exception.MidUserCancellationException;
import ee.sk.mid.rest.MidConnector;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.request.MidSessionStatusRequest;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ee.cyber.cdoc2.server.adapter.exception.ClientBadRequestException;

import static ee.cyber.cdoc2.util.RpRequestUtil.MID_IDENTIFIER_OK;
import static ee.cyber.cdoc2.util.RpRequestUtil.createMidAuthenticateRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MiDClientExceptionTests {
    private static final String MID_CLIENT_ERROR_CODE = "MID_CLIENT_ERROR";

    private static final MobileIdClientConfiguration.AppProperties MID_CLIENT_PROPS =
        new MobileIdClientConfiguration.AppProperties("https://mid.example.test", 5);

    @Mock
    private MidClient mockMidClient;
    @Mock
    private MidConnector mockMidConnector;

    private static Stream<MidException> midExceptions() {
        return Stream.of(
            new MidDeliveryException(),
            new MidInternalErrorException("internal error"),
            new MidInvalidNationalIdentityNumberException("bad national identity number"),
            new MidInvalidPhoneNumberException("bad phone number"),
            new MidInvalidUserConfigurationException(),
            new MidMissingOrInvalidParameterException("missing parameter"),
            new MidNotMidClientException(),
            new MidPhoneNotAvailableException(),
            new MidServiceUnavailableException("service unavailable"),
            new MidSessionNotFoundException(),
            new MidSessionTimeoutException(),
            new MidSslException("ssl error"),
            new MidUnauthorizedException("unauthorized"),
            new MidUserCancellationException()
        );
    }

    @ParameterizedTest
    @MethodSource("midExceptions")
    void authenticateWrapsMidExceptionAsClientBadRequestException(MidException exception) {
        var request = createMidAuthenticateRequest();
        var client = stubAuthenticateToThrow(exception);

        assertWrappedAsClientBadRequestException(
            () -> client.authenticate(MID_IDENTIFIER_OK, request), exception);
    }

    @ParameterizedTest
    @MethodSource("midExceptions")
    void sessionStatusWrapsMidExceptionAsClientBadRequestException(MidException exception) {
        var sessionId = UUID.randomUUID();
        var client = stubSessionStatusToThrow(exception);

        assertWrappedAsClientBadRequestException(() -> client.sessionStatus(sessionId), exception);
    }

    private MiDClient miDClientWithMocks() {
        return new MiDClient(mockMidClient, MID_CLIENT_PROPS);
    }

    /**
     * Stubs the connector call made by {@code authenticate()} to throw the given exception.
     */
    private MiDClient stubAuthenticateToThrow(RuntimeException exception) {
        when(mockMidClient.getMobileIdConnector()).thenReturn(mockMidConnector);
        when(mockMidConnector.authenticate(any(MidAuthenticationRequest.class))).thenThrow(exception);

        return miDClientWithMocks();
    }

    /**
     * Stubs the connector call made by {@code sessionStatus()} to throw the given exception.
     */
    private MiDClient stubSessionStatusToThrow(RuntimeException exception) {
        when(mockMidClient.getMobileIdConnector()).thenReturn(mockMidConnector);
        when(mockMidConnector.getAuthenticationSessionStatus(any(MidSessionStatusRequest.class)))
            .thenThrow(exception);

        return miDClientWithMocks();
    }

    private void assertWrappedAsClientBadRequestException(
        Executable executable, RuntimeException exception
    ) {
        var thrown = assertThrows(ClientBadRequestException.class, executable);

        assertEquals(MID_CLIENT_ERROR_CODE, thrown.getCode());
        assertEquals(exception.getMessage(), thrown.getMessage());
    }
}
