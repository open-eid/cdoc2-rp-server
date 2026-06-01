package ee.cyber.cdoc2.server.app.exception;

public class Cdoc2RpValidationException extends RuntimeException {
    private final String code;

    public Cdoc2RpValidationException(String code, String message, Throwable throwable) {
        super(message, throwable);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
