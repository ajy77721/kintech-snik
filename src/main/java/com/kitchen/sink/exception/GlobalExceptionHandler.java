package com.kitchen.sink.exception;

import com.kitchen.sink.dto.APIResponseDTO;
import com.kitchen.sink.dto.ErrorDTO;
import com.kitchen.sink.utils.ObjectConvertor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private ObjectConvertor objectConvertor;

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<APIResponseDTO<?>> handleValidationException(ValidationException ex) {
        log.error("Validation Exception", ex);
        return new ResponseEntity<>(buildErrorResponse(ex.getMessage()), ex.getStatus());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<APIResponseDTO<?>> handleNotFoundException(NotFoundException ex) {
        log.error("Not Found Exception", ex);
        return new ResponseEntity<>(buildErrorResponse(ex.getMessage()), ex.getStatus());
    }

    @ExceptionHandler(ObjectMappingException.class)
    public ResponseEntity<APIResponseDTO<?>> handleObjectMappingException(ObjectMappingException ex) {
        log.error("Object Mapping Exception", ex);
        return new ResponseEntity<>(buildErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<APIResponseDTO<?>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        log.error("Username Not Found Exception", ex);
        return new ResponseEntity<>(buildErrorResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<APIResponseDTO<?>> handleNoHandlerFound(NoHandlerFoundException ex) {
        log.error("No Handler Found Exception", ex);
        return new ResponseEntity<>(buildErrorResponse("API path is not registered"), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<APIResponseDTO<?>> handleDuplicateKeyException(DuplicateKeyException ex) {
        log.error("Duplicate Key Exception", ex);
        return new ResponseEntity<>(buildErrorResponse("Duplicate Key Exception"), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponseDTO<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.add(fieldError.getDefaultMessage());
        }
        return new ResponseEntity<>(buildErrorResponse(objectConvertor.writeValueAsString(errors)), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<APIResponseDTO<?>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("Http Message Not Readable Exception", ex);
        return new ResponseEntity<>(buildErrorResponse("Invalid Request"), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponseDTO<?>> handleException(Exception ex) {
        log.error("Exception", ex);
        return new ResponseEntity<>(buildErrorResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private APIResponseDTO<?> buildErrorResponse(String message) {
        return APIResponseDTO.builder()
                .status(false)
                .error(ErrorDTO.builder().message(message).build())
                .build();
    }

}
