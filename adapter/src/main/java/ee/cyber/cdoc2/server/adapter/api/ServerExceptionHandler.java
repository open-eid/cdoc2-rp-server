package ee.cyber.cdoc2.server.adapter.api;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ServerExceptionHandler {
    private static final String UNEXPECTED_ERROR_CODE = "UNEXPECTED_ERROR";

    // Catch-all for exceptions not mapped by a more specific handler (bugs, illegal states, etc.)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(
        Exception exception
    ) {
        log.error("Unexpected error: {}", exception.getMessage(), exception);

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setProperties(
            Map.of("errorCode", UNEXPECTED_ERROR_CODE)
        );

        return ResponseEntity.internalServerError().body(problem);
    }
}
