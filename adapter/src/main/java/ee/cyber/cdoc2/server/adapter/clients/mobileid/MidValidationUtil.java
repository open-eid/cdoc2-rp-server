package ee.cyber.cdoc2.server.adapter.clients.mobileid;

import ee.sk.mid.MidInputUtil;
import ee.sk.mid.exception.MidInvalidNationalIdentityNumberException;
import ee.sk.mid.exception.MidInvalidPhoneNumberException;

import java.util.Objects;

import ee.cyber.cdoc2.server.app.exception.InputValidationException;

public final class MidValidationUtil {

    private MidValidationUtil() {
    }

    public static void validatePhoneNumberAndNationalIdentityNumber(
        String phoneNr,
        String nationalIdNumber
    ) {
        try {
            Objects.requireNonNull(phoneNr);
            MidInputUtil.getValidatedPhoneNumber(phoneNr);
        } catch (MidInvalidPhoneNumberException e) {
            throw new InputValidationException(e.getMessage(), e);
        }

        try {
            MidInputUtil.getValidatedNationalIdentityNumber(nationalIdNumber);
        } catch (MidInvalidNationalIdentityNumberException e) {
            throw new InputValidationException(e.getMessage(), e);
        }
    }
}
