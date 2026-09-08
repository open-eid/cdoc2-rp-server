package ee.cyber.cdoc2.server.adapter.clients.mobileid;

import ee.sk.mid.MidInputUtil;
import ee.sk.mid.exception.MidInvalidNationalIdentityNumberException;
import ee.sk.mid.exception.MidInvalidPhoneNumberException;

import ee.cyber.cdoc2.server.app.exception.InputValidationException;

public final class MidValidationUtil {

    private MidValidationUtil() {
    }

    public static void validatePhoneNumberAndNationalIdentityNumber(
        String phoneNr,
        String nationalIdNumber
    ) {
        if (phoneNr == null) {
            throw new InputValidationException("Phone number must not be null", null);
        }

        try {
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
