package com.kitchen.sink.exception;

import com.kitchen.sink.dto.APIResponseDTO;
import com.kitchen.sink.dto.ErrorDTO;
import com.kitchen.sink.utils.UniversalConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.ExpressionAuthorizationDecision;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.NoHandlerFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Autowired
    private UniversalConverter universalConverter;

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<APIResponseDTO<?>> handleValidationException(ValidationException ex) {
        return buildErrorResponse(ex, ex.getStatus(), "Validation Exception");
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<APIResponseDTO<?>> handleNotFoundException(NotFoundException ex) {
        return buildErrorResponse(ex, ex.getStatus(), "Not Found Exception");
    }

    @ExceptionHandler(ObjectMappingException.class)
    public ResponseEntity<APIResponseDTO<?>> handleObjectMappingException(ObjectMappingException ex) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Object Mapping Exception");
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<APIResponseDTO<?>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, "Username Not Found Exception");
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<APIResponseDTO<?>> handleNoHandlerFound(NoHandlerFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, "No Handler Found Exception", "API path is not registered");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<APIResponseDTO<?>> handleDuplicateKeyException(DuplicateKeyException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Duplicate Key Exception", "email already used");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<APIResponseDTO<?>> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access Denied Exception", ex);
        if (ex instanceof AuthorizationDeniedException authorizationDeniedException) {
            if (authorizationDeniedException.getAuthorizationResult() instanceof ExpressionAuthorizationDecision expressionAuthorizationDecision) {
                if (expressionAuthorizationDecision.getExpression().getExpressionString().contains("hasAnyAuthority")) {
                    String expression = expressionAuthorizationDecision.getExpression().getExpressionString();
                    String[] roles = expression.replace("hasAnyAuthority('", "").replace("')", "").split("', '");
                    return new ResponseEntity<>(buildErrorResponse("You do not have permission to access this functionality. Please contact the Administrator. Required roles: " + String.join(",", roles)), HttpStatus.FORBIDDEN);
                }
            }
        }
        return new ResponseEntity<>(buildErrorResponse("You do not have permission to access this functionality. Please contact the Administrator."), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<APIResponseDTO<?>> handleLockedException(LockedException ex) {
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, "Locked Exception");
    }

    @ExceptionHandler(SessionAuthenticationException.class)
    public ResponseEntity<APIResponseDTO<?>> handleSessionAuthenticationException(SessionAuthenticationException ex) {
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, "Session Authentication Exception");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponseDTO<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.add(fieldError.getDefaultMessage());
        }
        return new ResponseEntity<>(buildErrorResponse(universalConverter.writeValueAsString(errors)), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<APIResponseDTO<?>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Http Message Not Readable Exception", "Invalid Request");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponseDTO<?>> handleException(Exception ex) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, "Exception");
    }

    private ResponseEntity<APIResponseDTO<?>> buildErrorResponse(Exception ex, HttpStatus status, String logMessage) {
        return buildErrorResponse(ex, status, logMessage, ex.getMessage());
    }

    private ResponseEntity<APIResponseDTO<?>> buildErrorResponse(Exception ex, HttpStatus status, String logMessage, String errorMessage) {
        log.error(logMessage, ex);
        return new ResponseEntity<>(buildErrorResponse(errorMessage), status);
    }

    private APIResponseDTO<?> buildErrorResponse(String message) {
        return APIResponseDTO.builder()
                .status(false)
                .error(ErrorDTO.builder().message(message).build())
                .build();
    }
}