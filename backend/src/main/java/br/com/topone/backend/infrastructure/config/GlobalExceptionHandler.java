package br.com.topone.backend.infrastructure.config;

import br.com.topone.backend.domain.exception.*;
import br.com.topone.backend.interfaces.dto.ErrorResponse;
import br.com.topone.backend.interfaces.dto.ValidationError;
import br.com.topone.backend.interfaces.dto.ValidationErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        log.warn("Attempt to register existing email");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflito",
                "E-mail já cadastrado",
                Instant.now().toString()
        ));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Invalid login attempt");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Não autorizado",
                "E-mail ou senha inválidos",
                Instant.now().toString()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ValidationError(e.getField(), e.getDefaultMessage()))
                .toList();

        log.warn("Validation error | details={}", errors);

        return ResponseEntity.badRequest().body(new ValidationErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Requisição inválida",
                errors,
                Instant.now().toString()
        ));
    }

    @ExceptionHandler(RefreshTokenNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenNotFound(RefreshTokenNotFoundException ex) {
        log.warn("Refresh token not found");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Não autorizado",
                "Refresh token inválido",
                Instant.now().toString()
        ));
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenExpired(RefreshTokenExpiredException ex) {
        log.warn("Refresh token expired");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Não autorizado",
                "Refresh token expirado",
                Instant.now().toString()
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied | message={}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "Proibido",
                "Acesso negado. Você não tem permissão para acessar este recurso.",
                Instant.now().toString()
        ));
    }

    @ExceptionHandler(RefreshTokenRevokedException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenRevoked(RefreshTokenRevokedException ex) {
        log.warn("Refresh token was revoked");
        return ResponseEntity.status(HttpStatus.GONE).body(new ErrorResponse(
                HttpStatus.GONE.value(),
                "Tokens revogados",
                "Refresh token foi revogado, faça login novamente",
                Instant.now().toString()
        ));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Invalid parameter | name={} | value={}", ex.getName(), ex.getValue());
        return ResponseEntity.badRequest().body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Requisição inválida",
                "Parâmetro inválido: " + ex.getName(),
                Instant.now().toString()
        ));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("Missing parameter | name={}", ex.getParameterName());
        return ResponseEntity.badRequest().body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Requisição inválida",
                "Parâmetro obrigatório ausente: " + ex.getParameterName(),
                Instant.now().toString()
        ));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        log.warn("User not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Não encontrado",
                "Usuário não encontrado",
                Instant.now().toString()
        ));
    }

    @ExceptionHandler(InvalidCnpjException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCnpj(InvalidCnpjException ex) {
        log.warn("Invalid CNPJ informed");
        return ResponseEntity.badRequest().body(new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Requisição inválida",
                "CNPJ inválido. Informe 14 dígitos.",
                Instant.now().toString()
        ));
    }

    @ExceptionHandler(CnpjNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCnpjNotFound(CnpjNotFoundException ex) {
        log.warn("CNPJ not found in external provider");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Não encontrado",
                "CNPJ não encontrado",
                Instant.now().toString()
        ));
    }

    @ExceptionHandler(CnpjaIntegrationException.class)
    public ResponseEntity<ErrorResponse> handleCnpjaIntegration(CnpjaIntegrationException ex) {
        log.error("CNPJA integration failure | message={}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(new ErrorResponse(
                HttpStatus.BAD_GATEWAY.value(),
                "Falha de integração",
                "Não foi possível consultar o CNPJ no provedor externo",
                Instant.now().toString()
        ));
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoleNotFound(RoleNotFoundException ex) {
        log.warn("Role not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Não encontrado",
                "Perfil não encontrado",
                Instant.now().toString()
        ));
    }

    @ExceptionHandler(RoleAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleRoleAlreadyExists(RoleAlreadyExistsException ex) {
        log.warn("Role already exists");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflito",
                "Perfil já cadastrado",
                Instant.now().toString()
        ));
    }

    @ExceptionHandler(RoleInUseException.class)
    public ResponseEntity<ErrorResponse> handleRoleInUse(RoleInUseException ex) {
        log.warn("Role is assigned to users");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflito",
                "Perfil está vinculado a usuários e não pode ser removido",
                Instant.now().toString()
        ));
    }

    @ExceptionHandler(SystemRoleDeletionNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleSystemRoleDeletionNotAllowed(SystemRoleDeletionNotAllowedException ex) {
        log.warn("System role deletion blocked");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflito",
                "Perfis do sistema não podem ser removidos",
                Instant.now().toString()
        ));
    }
}
