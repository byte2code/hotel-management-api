package com.cn.hotelDemo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cn.hotelDemo.dto.RoomRequest;
import com.cn.hotelDemo.model.Hotel;
import com.cn.hotelDemo.model.Room;
import com.cn.hotelDemo.model.RoomStatus;
import com.cn.hotelDemo.repository.HotelRepository;
import com.cn.hotelDemo.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

	@Mock
	private RoomRepository roomRepository;

	@Mock
	private HotelRepository hotelRepository;

	private RoomService roomService;

	@BeforeEach
	void setUp() {
		roomService = new RoomService(roomRepository, hotelRepository);
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
}
