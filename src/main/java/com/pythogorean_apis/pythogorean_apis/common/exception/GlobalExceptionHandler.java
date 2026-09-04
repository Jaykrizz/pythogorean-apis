package com.pythogorean_apis.pythogorean_apis.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(
            ApiException exception,
            HttpServletRequest request) {

        return build(exception.getStatus(), exception.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableBody(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {

        return build(HttpStatus.BAD_REQUEST, "Request body is missing or malformed", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {

        log.warn("Database constraint rejected {} {}", request.getMethod(), request.getRequestURI(), exception);

        return build(HttpStatus.CONFLICT, "That change conflicts with existing data", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception exception,
            HttpServletRequest request) {

        if (exception instanceof ErrorResponse errorResponse) {

            HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());

            log.warn("{} on {} {}", status.value(), request.getMethod(), request.getRequestURI());

            return build(status, describe(status, request), request);
        }

        log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), exception);

        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong", request);
    }

    private String describe(HttpStatus status, HttpServletRequest request) {
        return switch (status) {
            case NOT_FOUND -> "No endpoint for " + request.getMethod() + " " + request.getRequestURI();
            case METHOD_NOT_ALLOWED -> request.getMethod() + " is not allowed on " + request.getRequestURI();
            default -> status.getReasonPhrase();
        };
    }

    private ResponseEntity<ApiError> build(
            HttpStatus status,
            String message,
            HttpServletRequest request) {

        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }
}
