package com.cn.hotelDemo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cn.hotelDemo.dto.BookingRequest;
import com.cn.hotelDemo.dto.BookingResponse;
import com.cn.hotelDemo.model.Booking;
import com.cn.hotelDemo.model.BookingStatus;
import com.cn.hotelDemo.model.Hotel;
import com.cn.hotelDemo.model.Room;
import com.cn.hotelDemo.model.RoomStatus;
import com.cn.hotelDemo.model.User;
import com.cn.hotelDemo.repository.BookingRepository;
import com.cn.hotelDemo.repository.HotelRepository;
import com.cn.hotelDemo.repository.RoomRepository;
import com.cn.hotelDemo.repository.UserRepository;

@Service
public class BookingService {

	private final BookingRepository bookingRepository;
	private final UserRepository userRepository;
	private final RoomRepository roomRepository;
	private final HotelRepository hotelRepository;

	public BookingService(BookingRepository bookingRepository, UserRepository userRepository,
			RoomRepository roomRepository, HotelRepository hotelRepository) {
		this.bookingRepository = bookingRepository;
		this.userRepository = userRepository;
		this.roomRepository = roomRepository;
		this.hotelRepository = hotelRepository;
	}

	@Transactional
	public BookingResponse createBooking(BookingRequest bookingRequest) {
		User user = userRepository.findById(bookingRequest.getUserId())
				.orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + bookingRequest.getUserId()));
		Room room = roomRepository.findByIdForUpdate(bookingRequest.getRoomId())
				.orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + bookingRequest.getRoomId()));
		Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId())
				.orElseThrow(() -> new IllegalArgumentException("Hotel not found with ID: " + bookingRequest.getHotelId()));

		if (bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())) {
			throw new IllegalArgumentException("Check-out date must be on or after the check-in date");
		}
		if (room.getHotel() == null || !room.getHotel().getId().equals(hotel.getId())) {
			throw new IllegalArgumentException("Room does not belong to the selected hotel");
		}
		if (room.getStatus() != RoomStatus.AVAILABLE) {
			return persistBooking(bookingRequest, user, room, hotel, BookingStatus.REJECTED,
					"Room is not available for booking");
		}

		List<Booking> overlappingBookings = bookingRepository.findByRoomIdAndStatusIn(room.getId(),
				List.of(BookingStatus.CONFIRMED, BookingStatus.REQUESTED));
		boolean hasOverlap = overlappingBookings.stream().anyMatch(existing -> overlaps(existing.getCheckInDate(),
				existing.getCheckOutDate(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate()));
		if (hasOverlap) {
			return persistBooking(bookingRequest, user, room, hotel, BookingStatus.REJECTED,
					"Room already has a booking in the requested date range");
		}

		return persistBooking(bookingRequest, user, room, hotel, BookingStatus.CONFIRMED,
				"Booking confirmed successfully");
	}

	public List<Booking> getAllBookings() {
		return bookingRepository.findAll();
	}

	public Booking getBookingById(Long id) {
		return bookingRepository.findById(id).orElse(null);
	}

	public List<Booking> getBookingsByUserId(Long userId) {
		return bookingRepository.findByUserId(userId);
	}

	public List<Booking> getBookingsByHotelId(Long hotelId) {
		return bookingRepository.findByHotelId(hotelId);
	}

	private BookingResponse persistBooking(BookingRequest bookingRequest, User user, Room room, Hotel hotel,
			BookingStatus status, String message) {
		Booking booking = new Booking();
		booking.setBookingReference("BOOK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
		booking.setUser(user);
		booking.setRoom(room);
		booking.setHotel(hotel);
		booking.setCheckInDate(bookingRequest.getCheckInDate());
		booking.setCheckOutDate(bookingRequest.getCheckOutDate());
		booking.setGuestCount(bookingRequest.getGuestCount());
		booking.setSpecialRequests(bookingRequest.getSpecialRequests());
		booking.setStatus(status);

		Booking savedBooking = bookingRepository.save(booking);
		return new BookingResponse(savedBooking.getBookingReference(), savedBooking.getStatus(), message,
				savedBooking.getId());
	}

	private boolean overlaps(LocalDate firstStart, LocalDate firstEnd, LocalDate secondStart, LocalDate secondEnd) {
		return !secondEnd.isBefore(firstStart) && !secondStart.isAfter(firstEnd);
	}
}
