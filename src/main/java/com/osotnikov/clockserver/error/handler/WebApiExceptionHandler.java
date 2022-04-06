package com.osotnikov.clockserver.error.handler;

import com.osotnikov.clockserver.error.model.ApiError;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class WebApiExceptionHandler {

    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex,
                                                            WebRequest request) {

        List<String> errors = new ArrayList<String>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.add(violation.getRootBeanClass().getName() + " " +
                    violation.getPropertyPath() + ": " + violation.getMessage());
        }

        ApiError apiError =
                new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errors);
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), apiError.getStatus());
    }

    /*@ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, WebRequest request) {

        String errorMessage = String.format("Invalid argument submitted: %s", ex.getParameter().getParameterName());
        ApiError apiError =
            new ApiError(HttpStatus.BAD_REQUEST, errorMessage, errorMessage);
        return new ResponseEntity<>(
            apiError, new HttpHeaders(), apiError.getStatus());
    }*/

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAnyUnhandledException(Exception ex, WebRequest request) {
        ApiError apiError =
                new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(), ex.getLocalizedMessage());
        return new ResponseEntity<>(
                apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        // Get the error messages for invalid fields
        List<FieldError> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fieldError -> new FieldError(fieldError.getObjectName(), fieldError.getField(), fieldError.getDefaultMessage()))
            .collect(Collectors.toList());

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Invalid request body.",
            errors.stream().map(FieldError::getDefaultMessage).collect(Collectors.toList()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }
}
