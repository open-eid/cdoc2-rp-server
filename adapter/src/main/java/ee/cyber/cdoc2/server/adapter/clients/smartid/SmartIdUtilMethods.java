package ee.cyber.cdoc2.server.adapter.clients.smartid;

import ee.sk.smartid.AuthenticationCertificateLevel;
import ee.sk.smartid.HashAlgorithm;
import ee.sk.smartid.common.notification.interactions.NotificationInteraction;
import ee.sk.smartid.rest.dao.Interaction;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ee.cyber.cdoc2.server.adapter.generated.model.AuthCertificateLevel;

public final class SmartIdUtilMethods {

    private SmartIdUtilMethods() {
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static AuthenticationCertificateLevel mapCertificateLevel(AuthCertificateLevel certificateLevelEnum) {
        return switch (certificateLevelEnum) {
            case ADVANCED -> AuthenticationCertificateLevel.ADVANCED;
            case QUALIFIED -> AuthenticationCertificateLevel.QUALIFIED;
            case UNKNOWN_DEFAULT_OPEN_API ->
                throw new RuntimeException("Unsupported certificate level");
        };
    }

    public static HashAlgorithm mapHashAlgorithm(String hashAlgorithmStr) {
        return HashAlgorithm.fromString(
            hashAlgorithmStr
        ).orElseThrow(() -> new IllegalArgumentException("Unsupported hash algorithm: "
            + hashAlgorithmStr
        ));
    }

    public static List<NotificationInteraction> decodeFromBase64(byte[] interactions)
        throws JsonProcessingException {
        List<Interaction> parsedInteractions = MAPPER.readValue(
            new String(Base64.getDecoder().decode(interactions), StandardCharsets.UTF_8),
            new TypeReference<>() {
            }
        );

        return parsedInteractions.stream()
            .map(SmartIdUtilMethods::toNotificationInteraction)
            .toList();
    }

    public static NotificationInteraction toNotificationInteraction(Interaction interaction) {
        return switch (interaction.type()) {
            case "displayTextAndPIN" ->
                NotificationInteraction.displayTextAndPin(interaction.displayText60());
            case "confirmationMessage" ->
                NotificationInteraction.confirmationMessage(interaction.displayText200());
            case "confirmationMessageAndVerificationCodeChoice" ->
                NotificationInteraction.confirmationMessageAndVerificationCodeChoice(interaction.displayText200());
            default ->
                throw new IllegalArgumentException("Unsupported interaction type: " + interaction.type());
        };
    }
}
