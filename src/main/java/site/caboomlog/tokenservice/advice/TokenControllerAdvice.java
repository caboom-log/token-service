package site.caboomlog.tokenservice.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.caboomlog.tokenservice.dto.ErrorResponse;
import site.caboomlog.tokenservice.exception.BadRequestException;
import site.caboomlog.tokenservice.exception.InvalidTokenException;

@RestControllerAdvice
public class TokenControllerAdvice {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException e) {
        return ResponseEntity.status(400)
                .body(new ErrorResponse(400, e.getMessage()));
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException e) {
        return ResponseEntity.status(401)
                .body(new ErrorResponse(401, e.getMessage()));
    }
}
