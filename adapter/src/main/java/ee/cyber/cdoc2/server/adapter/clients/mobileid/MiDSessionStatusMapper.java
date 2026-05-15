package ee.cyber.cdoc2.server.adapter.clients.mobileid;

import ee.sk.mid.rest.dao.MidSessionStatus;

import java.util.Base64;

import ee.cyber.cdoc2.server.adapter.generated.model.MidSessionStatusResponse;
import ee.cyber.cdoc2.server.adapter.generated.model.MidSignature;

public final class MiDSessionStatusMapper {

    private MiDSessionStatusMapper() {
    }

    public static MidSessionStatusResponse map(MidSessionStatus source) {
        MidSessionStatusResponse target = new MidSessionStatusResponse(
            MidSessionStatusResponse.StateEnum.fromValue(source.getState())
        );

        if ("COMPLETE".equalsIgnoreCase(source.getState())) {
            target.setResult(mapResult(source.getResult()));

            if (source.getCert() != null) {
                target.setCert(Base64.getDecoder().decode((source.getCert())));
            }

            if (source.getSignature() != null) {
                target.setSignature(new MidSignature(
                    Base64.getDecoder().decode(source.getSignature().getValue()),
                    source.getSignature().getAlgorithm()
                ));
            }
        }

        return target;
    }

    private static MidSessionStatusResponse.ResultEnum mapResult(String result) {
        return switch (result) {
            case "OK" -> MidSessionStatusResponse.ResultEnum.OK;
            case "TIMEOUT" -> MidSessionStatusResponse.ResultEnum.TIMEOUT;
            case "NOT_MID_CLIENT" -> MidSessionStatusResponse.ResultEnum.NOT_MID_CLIENT;
            case "USER_CANCELLED" -> MidSessionStatusResponse.ResultEnum.USER_CANCELLED;
            case "SIGNATURE_HASH_MISMATCH" ->
                MidSessionStatusResponse.ResultEnum.SIGNATURE_HASH_MISMATCH;
            case "PHONE_ABSENT" -> MidSessionStatusResponse.ResultEnum.PHONE_ABSENT;
            case "DELIVERY_ERROR" -> MidSessionStatusResponse.ResultEnum.DELIVERY_ERROR;
            case "SIM_ERROR" -> MidSessionStatusResponse.ResultEnum.SIM_ERROR;
            default -> throw new RuntimeException("Unknown result code:" + result);
        };
    }
}
