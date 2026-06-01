package com.cn.hotelDemo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
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

import com.cn.hotelDemo.dto.RoomRequest;
import com.cn.hotelDemo.model.Booking;
import com.cn.hotelDemo.model.BookingStatus;
import com.cn.hotelDemo.model.Hotel;
import com.cn.hotelDemo.model.Room;
import com.cn.hotelDemo.model.RoomStatus;
import com.cn.hotelDemo.repository.BookingRepository;
import com.cn.hotelDemo.repository.HotelRepository;
import com.cn.hotelDemo.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

	@Mock
	private RoomRepository roomRepository;

	@Mock
	private HotelRepository hotelRepository;

	@Mock
	private BookingRepository bookingRepository;

	private RoomService roomService;

	@BeforeEach
	void setUp() {
		roomService = new RoomService(roomRepository, hotelRepository, bookingRepository);
	}

	@Test
	void createRoomLinksRoomToHotel() {
		Hotel hotel = new Hotel();
		hotel.setId(1L);
		hotel.setRooms(new ArrayList<>());

		RoomRequest request = new RoomRequest(1L, "101", "Deluxe", 2, new BigDecimal("4500.00"), RoomStatus.AVAILABLE);

		when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));
		when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(hotelRepository.save(any(Hotel.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Room room = roomService.createRoom(request);

		assertNotNull(room);
		assertEquals("101", room.getRoomNumber());
		assertEquals(RoomStatus.AVAILABLE, room.getStatus());
		verify(hotelRepository, times(1)).save(any(Hotel.class));
	}

	@Test
	void getAvailableRoomsByHotelAndDatesFiltersBookedRooms() {
		Hotel hotel = new Hotel();
		hotel.setId(1L);
		hotel.setRooms(new ArrayList<>());

		Room availableRoom = new Room();
		availableRoom.setId(10L);
		availableRoom.setHotel(hotel);
		availableRoom.setStatus(RoomStatus.AVAILABLE);

		Room bookedRoom = new Room();
		bookedRoom.setId(11L);
		bookedRoom.setHotel(hotel);
		bookedRoom.setStatus(RoomStatus.AVAILABLE);

		Booking conflictingBooking = new Booking();
		conflictingBooking.setCheckInDate(LocalDate.now().plusDays(2));
		conflictingBooking.setCheckOutDate(LocalDate.now().plusDays(4));
		conflictingBooking.setStatus(BookingStatus.CONFIRMED);
		conflictingBooking.setRoom(bookedRoom);

		when(roomRepository.findByHotelId(1L)).thenReturn(List.of(availableRoom, bookedRoom));
		when(bookingRepository.findByHotelIdAndStatusIn(eq(1L), anyCollection())).thenReturn(List.of(conflictingBooking));

		List<Room> rooms = roomService.getAvailableRoomsByHotelAndDates(1L, LocalDate.now().plusDays(3),
				LocalDate.now().plusDays(5));

		assertEquals(1, rooms.size());
		assertEquals(10L, rooms.get(0).getId());
	}
}
