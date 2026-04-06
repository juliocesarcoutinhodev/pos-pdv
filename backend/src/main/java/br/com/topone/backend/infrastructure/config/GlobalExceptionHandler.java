package br.com.topone.backend.infrastructure.config;

import br.com.topone.backend.domain.exception.EmailAlreadyExistsException;
import br.com.topone.backend.interfaces.dto.ConflictResponse;
import br.com.topone.backend.interfaces.dto.ValidationError;
import br.com.topone.backend.interfaces.dto.ValidationErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ConflictResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        log.warn("Attempt to register existing email | email={}", ex.getMessage());
        var body = new ConflictResponse(
                HttpStatus.CONFLICT.value(),
                "Conflito",
                "E-mail já cadastrado",
                Instant.now().toString()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ValidationError(e.getField(), e.getDefaultMessage()))
                .toList();

        log.warn("Validation error | details={}", errors);

        var body = new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Requisição inválida",
                errors,
                Instant.now().toString()
        );
        return ResponseEntity.badRequest().body(body);
    }
}
