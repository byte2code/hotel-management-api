package com.cn.hotelDemo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cn.hotelDemo.dto.BookingRequest;
import com.cn.hotelDemo.dto.BookingResponse;
import com.cn.hotelDemo.model.Booking;
import com.cn.hotelDemo.model.BookingStatus;
import com.cn.hotelDemo.service.BookingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/hotel/bookings")
public class BookingController {

	private final BookingService bookingService;

	public BookingController(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	@PostMapping("/create")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin') or hasRole('NORMAL') or hasAuthority('normal')")
	public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
		BookingResponse response = bookingService.createBooking(bookingRequest);
		return new ResponseEntity<>(response,
				response.getStatus() == BookingStatus.CONFIRMED ? HttpStatus.CREATED : HttpStatus.CONFLICT);
	}

	@GetMapping("/id/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin') or hasRole('NORMAL') or hasAuthority('normal')")
	public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
		Booking booking = bookingService.getBookingById(id);
		if (booking == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(booking);
	}

	@GetMapping("/getAll")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	public ResponseEntity<List<Booking>> getAllBookings() {
		return ResponseEntity.ok(bookingService.getAllBookings());
	}

	@GetMapping("/user/{userId}")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin') or hasRole('NORMAL') or hasAuthority('normal')")
	public ResponseEntity<List<Booking>> getBookingsByUser(@PathVariable Long userId) {
		return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
	}

	@GetMapping("/hotel/{hotelId}")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	public ResponseEntity<List<Booking>> getBookingsByHotel(@PathVariable Long hotelId) {
		return ResponseEntity.ok(bookingService.getBookingsByHotelId(hotelId));
	}
}
