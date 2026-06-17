package com.cn.hotelDemo.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HotelNotFoundException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleHotelNotFound(HotelNotFoundException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiError error = new ApiError(
            LocalDateTime.now(),
            status.value(),
            "Not Found",
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiError error = new ApiError(
            LocalDateTime.now(),
            status.value(),
            "Not Found",
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiError error = new ApiError(
            LocalDateTime.now(),
            status.value(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        ApiError error = new ApiError(
            LocalDateTime.now(),
            status.value(),
            "Bad Request",
            message,
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        ApiError error = new ApiError(
            LocalDateTime.now(),
            status.value(),
            "Forbidden",
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ApiError error = new ApiError(
            LocalDateTime.now(),
            status.value(),
            "Internal Server Error",
            "An unexpected error occurred",
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, status);
    }
}
