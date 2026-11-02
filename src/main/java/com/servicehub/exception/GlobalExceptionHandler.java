package com.servicehub.exception;

import com.servicehub.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tratamento global de erros. Todas as respostas de erro seguem o formato ErrorResponse.
 * Estende ResponseEntityExceptionHandler para cobrir também os erros padrão do Spring MVC
 * (JSON malformado, método não suportado, rota inexistente, etc.).
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------- Exceções de negócio ----------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(build(HttpStatus.CONFLICT, ex.getMessage(), request, null));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(build(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null));
    }

    // ---------- Segurança ----------
    // Sem estes handlers, o handler genérico de Exception transformaria 401/403 em 500.

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(build(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este recurso.",
                        request, null));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(build(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos.", request, null));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                             WebRequest request) {
        log.warn("Violação de integridade no banco de dados", ex);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(build(HttpStatus.CONFLICT,
                        "A operação viola uma restrição de integridade dos dados.", request, null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Erro inesperado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(build(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Erro interno do servidor. Tente novamente mais tarde.", request, null));
    }

    // ---------- Erros padrão do Spring MVC ----------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        List<ErrorResponse.FieldErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new ErrorResponse.FieldErrorDetail(
                        fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();
        ErrorResponse body = build(status, "Falha na validação dos dados enviados.", request, details);
        return new ResponseEntity<>(body, headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        ErrorResponse error = build(statusCode, messageFor(ex, statusCode), request, null);
        return new ResponseEntity<>(error, headers, statusCode);
    }

    // ---------- Auxiliares ----------

    private String messageFor(Exception ex, HttpStatusCode status) {
        if (ex instanceof HttpMessageNotReadableException) {
            return "Corpo da requisição ausente ou malformado. Verifique o JSON e os valores dos campos "
                    + "(por exemplo, uma categoria inexistente).";
        }
        if (ex instanceof MethodArgumentTypeMismatchException mismatch) {
            return "Valor inválido '" + mismatch.getValue() + "' para o parâmetro '" + mismatch.getName() + "'.";
        }
        if (ex instanceof TypeMismatchException mismatch) {
            return "Valor inválido '" + mismatch.getValue() + "' para o parâmetro '"
                    + mismatch.getPropertyName() + "'.";
        }
        return switch (status.value()) {
            case 400 -> "Requisição inválida.";
            case 404 -> "Recurso não encontrado.";
            case 405 -> "Método HTTP não suportado para este endpoint.";
            case 406 -> "Formato de resposta não suportado. Use application/json.";
            case 415 -> "Tipo de conteúdo não suportado. Envie o corpo como application/json.";
            default -> "Não foi possível processar a requisição.";
        };
    }

    private ErrorResponse build(HttpStatusCode status, String message, WebRequest request,
                                List<ErrorResponse.FieldErrorDetail> details) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        String reason = resolved != null ? resolved.getReasonPhrase() : "Erro";
        String path = request.getDescription(false).replace("uri=", "");
        return new ErrorResponse(LocalDateTime.now(), status.value(), reason, message, path, details);
    }
}
