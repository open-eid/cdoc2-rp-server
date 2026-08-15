package ee.cyber.cdoc2.server.adapter.clients.mobileid;

import ee.sk.mid.MidAuthenticationHashToSign;
import ee.sk.mid.MidClient;

import ee.sk.mid.exception.MidException;
import ee.sk.mid.rest.dao.MidSessionStatus;
import ee.sk.mid.rest.dao.request.MidAuthenticationRequest;
import ee.sk.mid.rest.dao.request.MidSessionStatusRequest;
import ee.sk.mid.rest.dao.response.MidAuthenticationResponse;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.stereotype.Component;

import ee.cyber.cdoc2.server.adapter.exception.ClientBadRequestException;
import ee.cyber.cdoc2.server.adapter.generated.model.MidAuthenticateRequest;
import ee.cyber.cdoc2.server.adapter.generated.model.MidDisplayTextFormat;
import ee.cyber.cdoc2.server.adapter.generated.model.MidHashType;
import ee.cyber.cdoc2.server.adapter.generated.model.MidLanguage;

@Component
@RequiredArgsConstructor
public class MiDClient {
    private static final String MID_CLIENT_ERROR_CODE = "MID_CLIENT_ERROR";

    private final MidClient midClient;
    private final MobileIdClientConfiguration.AppProperties props;

    public UUID authenticate(
        String semanticsIdentifier,
        MidAuthenticateRequest midAuthenticateRequest
    ) {
        var authHash = MidAuthenticationHashToSign.newBuilder()
            .withHash(midAuthenticateRequest.getHash())
            .withHashType(
                mapHashType(midAuthenticateRequest.getHashType())
            ).build();

        MidAuthenticationRequest request = MidAuthenticationRequest.newBuilder()
            .withPhoneNumber(midAuthenticateRequest.getPhoneNumber())
            .withNationalIdentityNumber(semanticsIdentifier)
            .withHashToSign(authHash)
            .withLanguage(
                mapLanguage(midAuthenticateRequest.getLanguage())
            )
            .withDisplayText(midAuthenticateRequest.getDisplayText())
            .withDisplayTextFormat(
                mapDisplayTextFormat(midAuthenticateRequest.getDisplayTextFormat())
            )
            .build();

        try {
            MidAuthenticationResponse response =
                midClient.getMobileIdConnector().authenticate(request);

            return UUID.fromString(response.getSessionID());
        } catch (MidException e) {
            throw new ClientBadRequestException(MID_CLIENT_ERROR_CODE, e.getMessage());
        }
    }

    public MidSessionStatus sessionStatus(UUID sessionId) {
        MidSessionStatusRequest request = new MidSessionStatusRequest(
            sessionId.toString(),
            props.statusPollTimeoutSeconds()
        );

        try {
            return midClient.getMobileIdConnector().getAuthenticationSessionStatus(request);
        } catch (MidException e) {
            throw new ClientBadRequestException(MID_CLIENT_ERROR_CODE, e.getMessage());
        }
    }

    private static ee.sk.mid.MidHashType mapHashType(MidHashType hashType) {
        return switch (hashType) {
            case SHA256 -> ee.sk.mid.MidHashType.SHA256;
            case SHA384 -> ee.sk.mid.MidHashType.SHA384;
            case SHA512 -> ee.sk.mid.MidHashType.SHA512;
            case UNKNOWN_DEFAULT_OPEN_API -> throw new ClientBadRequestException(
                MID_CLIENT_ERROR_CODE,
                "Unknown hash value"
            );
        };
    }

    private static ee.sk.mid.MidLanguage mapLanguage(MidLanguage language) {
        return switch (language) {
            case EST -> ee.sk.mid.MidLanguage.EST;
            case ENG -> ee.sk.mid.MidLanguage.ENG;
            case RUS -> ee.sk.mid.MidLanguage.RUS;
            case LIT -> ee.sk.mid.MidLanguage.LIT;
            case UNKNOWN_DEFAULT_OPEN_API -> throw new ClientBadRequestException(
                MID_CLIENT_ERROR_CODE,
                "Unknown langue"
            );
        };
    }

    private static ee.sk.mid.MidDisplayTextFormat mapDisplayTextFormat(
        MidDisplayTextFormat displayTextFormat
    ) {
        return switch (displayTextFormat) {
            case GSM_7 -> ee.sk.mid.MidDisplayTextFormat.GSM7;
            case UCS_2 -> ee.sk.mid.MidDisplayTextFormat.UCS2;
            case UNKNOWN_DEFAULT_OPEN_API -> throw new ClientBadRequestException(
                MID_CLIENT_ERROR_CODE,
                "Unknown displayText format"
            );
        };
    }
}
