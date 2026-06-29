package ee.cyber.cdoc2.server.adapter.clients.mobileid;

import org.junit.jupiter.api.Test;

import ee.cyber.cdoc2.server.app.exception.InputValidationException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


class MidValidationUtilTest {

    private static final String VALID_PHONE = "+37268000769";
    private static final String VALID_NATIONAL_ID = "60001017869";

    @Test
    void happyPathValidInputsDoesNotThrow() {
        assertDoesNotThrow(() ->
            MidValidationUtil.validatePhoneNumberAndNationalIdentityNumber(
                VALID_PHONE,
                VALID_NATIONAL_ID
            )
        );
    }

    @Test
    void nullPhoneNumberThrowsNpeNotInputValidationException() {
        assertThrows(NullPointerException.class,
            () -> MidValidationUtil.validatePhoneNumberAndNationalIdentityNumber(
                null,
                VALID_NATIONAL_ID
            ));
    }

    @Test
    void invalidPhoneNumberFormatThrowsInputValidationException() {
        assertThrows(InputValidationException.class,
            () -> MidValidationUtil.validatePhoneNumberAndNationalIdentityNumber(
                "12345", // missing + and country prefix
                VALID_NATIONAL_ID
            )
        );
    }

    @Test
    void invalidNationalIdentityNumberFormatThrowsInputValidationException() {
        assertThrows(InputValidationException.class,
            () -> MidValidationUtil.validatePhoneNumberAndNationalIdentityNumber(
                VALID_PHONE,
                "abc" // not 11 digits
            )
        );
    }
}
