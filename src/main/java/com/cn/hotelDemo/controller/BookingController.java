package com.cn.hotelDemo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.AccessDeniedException;

import com.cn.hotelDemo.dto.BookingRequest;
import com.cn.hotelDemo.dto.BookingResponse;
import com.cn.hotelDemo.dto.RejectionRequest;
import com.cn.hotelDemo.model.Booking;
import com.cn.hotelDemo.model.BookingStatus;
import com.cn.hotelDemo.annotation.AuditLogged;
import com.cn.hotelDemo.service.BookingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/hotel/bookings")
@Tag(name = "Booking Controller", description = "Endpoints for managing bookings")
@SecurityRequirement(name = "Bearer Authentication")
public class BookingController {

	private final BookingService bookingService;
	public BookingController(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	@PostMapping("/create")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin') or hasRole('NORMAL') or hasAuthority('normal')")
	@Operation(summary = "Create a new booking")
	@AuditLogged(action = "#result != null ? 'BOOKING_' + #result.body.status.name() : 'UNKNOWN'", resourceType = "BOOKING", resourceIdSpel = "#result.body.bookingId")
	public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest bookingRequest,
			Authentication authentication) {
		BookingResponse response = bookingService.createBooking(bookingRequest);
		return new ResponseEntity<>(response,
				(response.getStatus() == BookingStatus.CONFIRMED || response.getStatus() == BookingStatus.REQUESTED)
						? HttpStatus.CREATED : HttpStatus.CONFLICT);
	}

	@GetMapping("/id/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin') or hasRole('NORMAL') or hasAuthority('normal')")
	@Operation(summary = "Get a booking by its ID")
	public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
		Booking booking = bookingService.getBookingById(id);
		return ResponseEntity.ok(booking);
	}

	@GetMapping("/getAll")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	@Operation(summary = "Get all bookings with pagination (Admin only)")
	public ResponseEntity<org.springframework.data.domain.Page<Booking>> getAllBookings(
			@org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
			@org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size,
			@org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
			@org.springframework.web.bind.annotation.RequestParam(defaultValue = "asc") String direction) {
		org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("desc")
				? org.springframework.data.domain.Sort.by(sortBy).descending()
				: org.springframework.data.domain.Sort.by(sortBy).ascending();
		org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
		return ResponseEntity.ok(bookingService.getAllBookings(pageable));
	}

	@GetMapping("/user/{userId}")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin') or hasRole('NORMAL') or hasAuthority('normal')")
	@Operation(summary = "Get bookings by user ID with pagination")
	public ResponseEntity<org.springframework.data.domain.Page<Booking>> getBookingsByUser(
			@PathVariable Long userId,
			@org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
			@org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size,
			@org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
			@org.springframework.web.bind.annotation.RequestParam(defaultValue = "asc") String direction) {
		org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("desc")
				? org.springframework.data.domain.Sort.by(sortBy).descending()
				: org.springframework.data.domain.Sort.by(sortBy).ascending();
		org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
		return ResponseEntity.ok(bookingService.getBookingsByUserId(userId, pageable));
	}

	@GetMapping("/hotel/{hotelId}")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	@Operation(summary = "Get bookings by hotel ID with pagination")
	public ResponseEntity<org.springframework.data.domain.Page<Booking>> getBookingsByHotel(
			@PathVariable Long hotelId,
			@org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
			@org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size,
			@org.springframework.web.bind.annotation.RequestParam(defaultValue = "id") String sortBy,
			@org.springframework.web.bind.annotation.RequestParam(defaultValue = "asc") String direction) {
		org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("desc")
				? org.springframework.data.domain.Sort.by(sortBy).descending()
				: org.springframework.data.domain.Sort.by(sortBy).ascending();
		org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
		return ResponseEntity.ok(bookingService.getBookingsByHotelId(hotelId, pageable));
	}

	@PatchMapping("/{id}/confirm")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	@Operation(summary = "Admin confirms a REQUESTED booking")
	@AuditLogged(action = "BOOKING_CONFIRMED", resourceType = "BOOKING", resourceIdSpel = "#result.body.bookingId")
	public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long id) {
		return ResponseEntity.ok(bookingService.confirmBooking(id));
	}

	@PatchMapping("/{id}/reject")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	@Operation(summary = "Admin rejects a REQUESTED booking with a reason")
	@AuditLogged(action = "BOOKING_REJECTED", resourceType = "BOOKING", resourceIdSpel = "#result.body.bookingId")
	public ResponseEntity<BookingResponse> rejectBooking(
			@PathVariable Long id,
			@Valid @RequestBody RejectionRequest request) {
		return ResponseEntity.ok(bookingService.rejectBooking(id, request.getReason()));
	}

	@PostMapping("/cancel/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin') or hasRole('NORMAL') or hasAuthority('normal')")
	@Operation(summary = "Cancel a booking by its ID")
	@AuditLogged(action = "BOOKING_CANCELLED", resourceType = "BOOKING", resourceIdSpel = "#result.body.bookingId")
	public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id, Authentication authentication) {
		Booking booking = bookingService.getBookingById(id);
		
		boolean isAdmin = authentication.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("admin"));
		
		if (!isAdmin && !booking.getUser().getUsername().equals(authentication.getName())
				&& !booking.getUser().getEmail().equals(authentication.getName())) {
			throw new AccessDeniedException("Only the booking owner or an admin can cancel this booking.");
		}

		BookingResponse response = bookingService.cancelBooking(id);
		return ResponseEntity.ok(response);
	}
}
