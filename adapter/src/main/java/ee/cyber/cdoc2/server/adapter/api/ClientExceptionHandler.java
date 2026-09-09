package ee.cyber.cdoc2.server.adapter.api;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import ee.cyber.cdoc2.server.adapter.exception.ClientBadRequestException;
import ee.cyber.cdoc2.server.app.exception.Cdoc2RpValidationException;

@Slf4j
@RestControllerAdvice
public class ClientExceptionHandler {

    @ExceptionHandler(ClientBadRequestException.class)
    public ResponseEntity<ProblemDetail> handleClientBadRequestException(
        ClientBadRequestException exception
    ) {
        log.warn("Client bad request [{}]: {}", exception.getCode(), exception.getMessage(), exception);

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setProperties(
            Map.of(
                "errorCode", exception.getCode()
            )
        );

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(Cdoc2RpValidationException.class)
    public ResponseEntity<ProblemDetail> handleCdoc2RpValidationException(
        Cdoc2RpValidationException exception
    ) {
        log.warn("Request validation failed [{}]: {}", exception.getCode(), exception.getMessage(), exception);

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setProperties(
            Map.of(
                "errorCode", exception.getCode(),
                "errorMessage", exception.getMessage()
            )
        );

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(Exception exception) {
        log.error("Unexpected error while handling request", exception);

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);

        return ResponseEntity.internalServerError().body(problem);
    }
}
