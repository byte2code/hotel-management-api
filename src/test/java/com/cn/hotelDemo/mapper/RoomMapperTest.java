package com.cn.hotelDemo.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.cn.hotelDemo.dto.RoomResponse;
import com.cn.hotelDemo.model.Hotel;
import com.cn.hotelDemo.model.Room;
import com.cn.hotelDemo.model.RoomStatus;
import com.cn.hotelDemo.model.RoomType;

class RoomMapperTest {

	private final RoomMapper roomMapper = Mappers.getMapper(RoomMapper.class);

	@Test
	void toResponse_mapsRoomWithHotelInfo() {
		Hotel hotel = Hotel.builder().id(5L).name("Ritz").build();
		Room room = Room.builder()
				.id(20L)
				.roomNumber("502")
				.roomType(RoomType.DELUXE)
				.capacity(2)
				.nightlyRate(new BigDecimal("250.00"))
				.status(RoomStatus.AVAILABLE)
				.hotel(hotel)
				.build();

		RoomResponse response = roomMapper.toResponse(room);

		assertNotNull(response);
		assertEquals(20L, response.getId());
		assertEquals("502", response.getRoomNumber());
		assertEquals(RoomType.DELUXE, response.getRoomType());
		assertEquals(2, response.getCapacity());
		assertEquals(new BigDecimal("250.00"), response.getNightlyRate());
		assertEquals(RoomStatus.AVAILABLE, response.getStatus());
		assertEquals(5L, response.getHotelId());
		assertEquals("Ritz", response.getHotelName());
	}

	@Test
	void toResponseList_mapsListSuccessfully() {
		Room r1 = Room.builder().id(1L).roomNumber("101").build();
		Room r2 = Room.builder().id(2L).roomNumber("102").build();

		List<RoomResponse> responses = roomMapper.toResponseList(List.of(r1, r2));

		assertEquals(2, responses.size());
		assertEquals("101", responses.get(0).getRoomNumber());
		assertEquals("102", responses.get(1).getRoomNumber());
	}
}
