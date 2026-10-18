package com.cn.hotelDemo.exception;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

	private static final String ERROR_BASE_URL = "https://api.hotel.com/errors/";

	private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail, ErrorCode errorCode, HttpServletRequest request) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setTitle(title);
		problem.setType(URI.create(ERROR_BASE_URL + errorCode.name().toLowerCase().replace('_', '-')));
		if (request != null && request.getRequestURI() != null) {
			problem.setInstance(URI.create(request.getRequestURI()));
		}
		problem.setProperty("errorCode", errorCode.name());
		problem.setProperty("timestamp", Instant.now().toString());

		String traceId = MDC.get("traceId");
		if (traceId != null && !traceId.isBlank()) {
			problem.setProperty("traceId", traceId);
		}
		return problem;
	}

	private ResponseEntity<ProblemDetail> toResponse(ProblemDetail problem) {
		return ResponseEntity.status(problem.getStatus())
				.contentType(MediaType.APPLICATION_PROBLEM_JSON)
				.body(problem);
	}

	@ExceptionHandler(HotelNotFoundException.class)
	public ResponseEntity<ProblemDetail> handleHotelNotFound(HotelNotFoundException ex, HttpServletRequest request) {
		ProblemDetail problem = createProblemDetail(HttpStatus.NOT_FOUND, "Hotel Not Found", ex.getMessage(), ErrorCode.HOTEL_NOT_FOUND, request);
		return toResponse(problem);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ProblemDetail> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
		ProblemDetail problem = createProblemDetail(HttpStatus.NOT_FOUND, "User Not Found", ex.getMessage(), ErrorCode.USER_NOT_FOUND, request);
		return toResponse(problem);
	}

	@ExceptionHandler(RoomNotFoundException.class)
	public ResponseEntity<ProblemDetail> handleRoomNotFound(RoomNotFoundException ex, HttpServletRequest request) {
		ProblemDetail problem = createProblemDetail(HttpStatus.NOT_FOUND, "Room Not Found", ex.getMessage(), ErrorCode.ROOM_NOT_FOUND, request);
		return toResponse(problem);
	}

	@ExceptionHandler(BookingNotFoundException.class)
	public ResponseEntity<ProblemDetail> handleBookingNotFound(BookingNotFoundException ex, HttpServletRequest request) {
		ProblemDetail problem = createProblemDetail(HttpStatus.NOT_FOUND, "Booking Not Found", ex.getMessage(), ErrorCode.BOOKING_NOT_FOUND, request);
		return toResponse(problem);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
		ProblemDetail problem = createProblemDetail(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), ErrorCode.BAD_REQUEST, request);
		return toResponse(problem);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
				.collect(Collectors.toMap(
						FieldError::getField,
						error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
						(existing, replacement) -> existing
				));
		String message = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining(", "));

		ProblemDetail problem = createProblemDetail(HttpStatus.BAD_REQUEST, "Validation Failed", message, ErrorCode.VALIDATION_FAILED, request);
		problem.setProperty("invalidFields", fieldErrors);
		return toResponse(problem);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
		ProblemDetail problem = createProblemDetail(HttpStatus.FORBIDDEN, "Access Denied", ex.getMessage(), ErrorCode.ACCESS_DENIED, request);
		return toResponse(problem);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ProblemDetail> handleGenericException(Exception ex, HttpServletRequest request) {
		ProblemDetail problem = createProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", ErrorCode.INTERNAL_SERVER_ERROR, request);
		return toResponse(problem);
	}
}
