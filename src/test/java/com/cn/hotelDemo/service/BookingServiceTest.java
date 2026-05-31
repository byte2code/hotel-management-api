package com.cn.hotelDemo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

	@Mock
	private BookingRepository bookingRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private RoomRepository roomRepository;

	@Mock
	private HotelRepository hotelRepository;

	private BookingService bookingService;

	@BeforeEach
	void setUp() {
		bookingService = new BookingService(bookingRepository, userRepository, roomRepository, hotelRepository);
	}

	@Test
	void createBookingConfirmsWhenRoomIsAvailable() {
		User user = new User();
		user.setId(2L);

		Hotel hotel = new Hotel();
		hotel.setId(1L);
		hotel.setRooms(new ArrayList<>());

		Room room = new Room();
		room.setId(10L);
		room.setHotel(hotel);
		room.setStatus(RoomStatus.AVAILABLE);
		room.setNightlyRate(new BigDecimal("4500.00"));

		BookingRequest request = new BookingRequest(1L, 10L, 2L, LocalDate.now().plusDays(1),
				LocalDate.now().plusDays(3), 2, "Late check-in");

		when(userRepository.findById(2L)).thenReturn(Optional.of(user));
		when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
		when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
		when(bookingRepository.findByRoomIdAndStatusIn(eq(10L), anyCollection())).thenReturn(List.of());
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		BookingResponse response = bookingService.createBooking(request);

		assertEquals(BookingStatus.CONFIRMED, response.getStatus());
		assertTrue(response.getMessage().contains("confirmed"));
	}

	@Test
	void createBookingRejectsWhenDatesOverlap() {
		User user = new User();
		user.setId(2L);

		Hotel hotel = new Hotel();
		hotel.setId(1L);
		hotel.setRooms(new ArrayList<>());

		Room room = new Room();
		room.setId(10L);
		room.setHotel(hotel);
		room.setStatus(RoomStatus.AVAILABLE);

		Booking existing = new Booking();
		existing.setCheckInDate(LocalDate.now().plusDays(2));
		existing.setCheckOutDate(LocalDate.now().plusDays(4));
		existing.setStatus(BookingStatus.CONFIRMED);

		BookingRequest request = new BookingRequest(1L, 10L, 2L, LocalDate.now().plusDays(3),
				LocalDate.now().plusDays(5), 2, null);

		when(userRepository.findById(2L)).thenReturn(Optional.of(user));
		when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
		when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
		when(bookingRepository.findByRoomIdAndStatusIn(eq(10L), anyCollection())).thenReturn(List.of(existing));
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		BookingResponse response = bookingService.createBooking(request);

		assertEquals(BookingStatus.REJECTED, response.getStatus());
		assertTrue(response.getMessage().contains("requested date range"));
	}
}
