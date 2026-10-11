package com.cn.hotelDemo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.cn.hotelDemo.dto.BookingNotification;
import com.cn.hotelDemo.dto.BookingRequest;
import com.cn.hotelDemo.dto.BookingResponse;
import com.cn.hotelDemo.metrics.BookingMetrics;
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

	@Mock
	private org.springframework.context.ApplicationEventPublisher eventPublisher;

	@Mock
	private SimpMessagingTemplate messagingTemplate;

	@Mock
	private BookingMetrics bookingMetrics;

	private BookingService bookingService;

	@BeforeEach
	void setUp() {
		bookingService = new BookingService(bookingRepository, userRepository, roomRepository, hotelRepository,
				messagingTemplate, eventPublisher, bookingMetrics);
	}

	@Test
	void createBookingRequestsWhenRoomIsAvailable() {
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
		when(roomRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(room));
		when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
		when(bookingRepository.findByRoomIdAndStatusIn(eq(10L), anyCollection())).thenReturn(List.of());
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		BookingResponse response = bookingService.createBooking(request);

		assertEquals(BookingStatus.REQUESTED, response.getStatus());
		assertTrue(response.getMessage().contains("requested") || response.getMessage().contains("awaiting"));
		verify(roomRepository).findByIdForUpdate(10L);
		verify(bookingMetrics).incrementCreated();
		verify(messagingTemplate).convertAndSend(eq("/topic/bookings/1"),
				argThat((BookingNotification notification) -> "REQUESTED".equals(notification.getStatus())
						&& notification.getHotelId().equals(1L) && notification.getRoomId().equals(10L)));
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
		when(roomRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(room));
		when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
		when(bookingRepository.findByRoomIdAndStatusIn(eq(10L), anyCollection())).thenReturn(List.of(existing));
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		BookingResponse response = bookingService.createBooking(request);

		assertEquals(BookingStatus.REJECTED, response.getStatus());
		assertTrue(response.getMessage().contains("requested date range"));
		verify(roomRepository).findByIdForUpdate(10L);
		verify(bookingMetrics).incrementRejected();
		verify(messagingTemplate).convertAndSend(eq("/topic/bookings/1"),
				argThat((BookingNotification notification) -> "REJECTED".equals(notification.getStatus())
						&& notification.getHotelId().equals(1L) && notification.getRoomId().equals(10L)
						&& notification.getMessage().contains("requested date range")));
	}

	@Test
	void confirmBooking_byAdmin_confirmsSuccessfully() {
		Hotel hotel = new Hotel();
		hotel.setId(1L);

		Room room = new Room();
		room.setId(10L);

		Booking booking = new Booking();
		booking.setId(100L);
		booking.setBookingReference("BOOK-12345678");
		booking.setStatus(BookingStatus.REQUESTED);
		booking.setHotel(hotel);
		booking.setRoom(room);
		booking.setTotalPrice(new BigDecimal("9000.00"));

		when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		BookingResponse response = bookingService.confirmBooking(100L);

		assertEquals(BookingStatus.CONFIRMED, response.getStatus());
		assertTrue(response.getMessage().contains("confirmed"));
		verify(bookingRepository).save(booking);
		verify(bookingMetrics).incrementConfirmed();
		verify(messagingTemplate).convertAndSend(eq("/topic/bookings/1"),
				argThat((BookingNotification notification) -> "CONFIRMED".equals(notification.getStatus())));
	}

	@Test
	void rejectBooking_byAdmin_rejectsWithReason() {
		Hotel hotel = new Hotel();
		hotel.setId(1L);

		Room room = new Room();
		room.setId(10L);

		Booking booking = new Booking();
		booking.setId(100L);
		booking.setBookingReference("BOOK-12345678");
		booking.setStatus(BookingStatus.REQUESTED);
		booking.setHotel(hotel);
		booking.setRoom(room);
		booking.setTotalPrice(new BigDecimal("9000.00"));

		when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		BookingResponse response = bookingService.rejectBooking(100L, "Payment failed");

		assertEquals(BookingStatus.REJECTED, response.getStatus());
		assertEquals("Payment failed", booking.getRejectionReason());
		verify(bookingRepository).save(booking);
		verify(bookingMetrics).incrementRejected();
		verify(bookingMetrics).decrementActive();
	}

	@Test
	void cancelBooking_requestedOrConfirmed_cancelsSuccessfully() {
		Hotel hotel = new Hotel();
		hotel.setId(1L);

		Booking booking = new Booking();
		booking.setId(100L);
		booking.setBookingReference("BOOK-12345678");
		booking.setStatus(BookingStatus.CONFIRMED);
		booking.setHotel(hotel);
		booking.setTotalPrice(new BigDecimal("9000.00"));

		when(bookingRepository.findById(100L)).thenReturn(Optional.of(booking));
		when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

		BookingResponse response = bookingService.cancelBooking(100L);

		assertEquals(BookingStatus.CANCELLED, response.getStatus());
		verify(bookingRepository).save(booking);
		verify(bookingMetrics).incrementCancelled();
		verify(eventPublisher).publishEvent(any(com.cn.hotelDemo.event.BookingCancelledEvent.class));
	}
}
