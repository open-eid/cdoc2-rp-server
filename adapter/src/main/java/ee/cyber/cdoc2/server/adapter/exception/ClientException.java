package ee.cyber.cdoc2.server.adapter.exception;

import lombok.Getter;

@Getter
public class ClientException extends RuntimeException {
    private final String code;

    public ClientException(String code, String message) {
        super(message);
        this.code = code;
    }

    public ClientException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
