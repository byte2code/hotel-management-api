package com.cn.hotelDemo.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
	private HttpServletRequest request;

	@BeforeEach
	void setUp() {
		request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/api/test");
		MDC.put("traceId", "test-trace-12345");
	}

	@AfterEach
	void tearDown() {
		MDC.clear();
	}

	@Test
	void handleHotelNotFound_returnsProblemDetailWithNotFound() {
		HotelNotFoundException ex = new HotelNotFoundException("Hotel 42 not found");

		ResponseEntity<ProblemDetail> response = handler.handleHotelNotFound(ex, request);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertEquals(MediaType.APPLICATION_PROBLEM_JSON, response.getHeaders().getContentType());
		assertNotNull(response.getBody());
		assertEquals("Hotel Not Found", response.getBody().getTitle());
		assertEquals("Hotel 42 not found", response.getBody().getDetail());
		assertEquals("HOTEL_NOT_FOUND", response.getBody().getProperties().get("errorCode"));
		assertEquals("test-trace-12345", response.getBody().getProperties().get("traceId"));
	}

	@Test
	void handleRoomNotFound_returnsProblemDetail() {
		RoomNotFoundException ex = new RoomNotFoundException("Room 101 not found");

		ResponseEntity<ProblemDetail> response = handler.handleRoomNotFound(ex, request);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertEquals("ROOM_NOT_FOUND", response.getBody().getProperties().get("errorCode"));
	}

	@Test
	void handleUserNotFound_returnsProblemDetail() {
		UserNotFoundException ex = new UserNotFoundException("User 5 not found");

		ResponseEntity<ProblemDetail> response = handler.handleUserNotFound(ex, request);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertEquals("USER_NOT_FOUND", response.getBody().getProperties().get("errorCode"));
	}

	@Test
	void handleBookingNotFound_returnsProblemDetail() {
		BookingNotFoundException ex = new BookingNotFoundException("Booking 7 not found");

		ResponseEntity<ProblemDetail> response = handler.handleBookingNotFound(ex, request);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertEquals("BOOKING_NOT_FOUND", response.getBody().getProperties().get("errorCode"));
	}

	@Test
	void handleAccessDenied_returnsForbidden() {
		AccessDeniedException ex = new AccessDeniedException("Forbidden action");

		ResponseEntity<ProblemDetail> response = handler.handleAccessDenied(ex, request);

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		assertEquals("ACCESS_DENIED", response.getBody().getProperties().get("errorCode"));
	}

	@Test
	void handleIllegalArgument_returnsBadRequest() {
		IllegalArgumentException ex = new IllegalArgumentException("Invalid date range");

		ResponseEntity<ProblemDetail> response = handler.handleIllegalArgument(ex, request);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("BAD_REQUEST", response.getBody().getProperties().get("errorCode"));
	}

	@Test
	void handleValidation_returnsBadRequestWithFieldErrors() {
		MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
		BindingResult bindingResult = mock(BindingResult.class);
		FieldError fieldError = new FieldError("hotelRequest", "name", "must not be blank");

		when(ex.getBindingResult()).thenReturn(bindingResult);
		when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

		ResponseEntity<ProblemDetail> response = handler.handleValidation(ex, request);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("VALIDATION_FAILED", response.getBody().getProperties().get("errorCode"));
		assertNotNull(response.getBody().getProperties().get("invalidFields"));
	}

	@Test
	void handleGenericException_returnsInternalServerError() {
		Exception ex = new RuntimeException("Unexpected db failure");

		ResponseEntity<ProblemDetail> response = handler.handleGenericException(ex, request);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getProperties().get("errorCode"));
	}
}
