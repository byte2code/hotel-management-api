package com.cn.hotelDemo.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import com.cn.hotelDemo.dto.HotelRequest;
import com.cn.hotelDemo.dto.HotelResponse;
import com.cn.hotelDemo.model.Hotel;
import com.cn.hotelDemo.model.Room;
import com.cn.hotelDemo.model.RoomStatus;
import com.cn.hotelDemo.model.RoomType;

class HotelMapperTest {

	private HotelMapper hotelMapper;

	@BeforeEach
	void setUp() {
		hotelMapper = Mappers.getMapper(HotelMapper.class);
		ReflectionTestUtils.setField(hotelMapper, "roomMapper", Mappers.getMapper(RoomMapper.class));
	}

	@Test
	void toResponse_mapsEntityToDto() {
		Hotel hotel = Hotel.builder()
				.id(10L)
				.name("Grand Palace")
				.city("Paris")
				.rating(5L)
				.discount(15.0)
				.address("Champs-Élysées")
				.country("France")
				.createdAt(LocalDateTime.now())
				.rooms(List.of(
						Room.builder()
								.id(101L)
								.roomNumber("101")
								.roomType(RoomType.SUITE)
								.status(RoomStatus.AVAILABLE)
								.build()
				))
				.build();

		HotelResponse response = hotelMapper.toResponse(hotel);

		assertNotNull(response);
		assertEquals(10L, response.getId());
		assertEquals("Grand Palace", response.getName());
		assertEquals("Paris", response.getCity());
		assertEquals(5L, response.getRating());
		assertEquals(15.0, response.getDiscount());
		assertEquals("Champs-Élysées", response.getAddress());
		assertEquals("France", response.getCountry());
	}

	@Test
	void toEntity_mapsRequestToEntity() {
		HotelRequest request = HotelRequest.builder()
				.name("Seaside Resort")
				.city("Nice")
				.rating(4L)
				.discount(5.0)
				.country("France")
				.build();

		Hotel hotel = hotelMapper.toEntity(request);

		assertNotNull(hotel);
		assertEquals("Seaside Resort", hotel.getName());
		assertEquals("Nice", hotel.getCity());
		assertEquals(4L, hotel.getRating());
		assertEquals(5.0, hotel.getDiscount());
		assertNull(hotel.getId());
	}

	@Test
	void toResponseList_mapsListSuccessfully() {
		Hotel hotel1 = Hotel.builder().id(1L).name("Hotel A").city("City A").build();
		Hotel hotel2 = Hotel.builder().id(2L).name("Hotel B").city("City B").build();

		List<HotelResponse> responses = hotelMapper.toResponseList(List.of(hotel1, hotel2));

		assertEquals(2, responses.size());
		assertEquals("Hotel A", responses.get(0).getName());
		assertEquals("Hotel B", responses.get(1).getName());
	}
}
