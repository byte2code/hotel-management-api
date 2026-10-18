package com.cn.hotelDemo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.cn.hotelDemo.dto.HotelRequest;
import com.cn.hotelDemo.exception.HotelNotFoundException;
import com.cn.hotelDemo.model.Hotel;
import com.cn.hotelDemo.repository.HotelRepository;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

	@Mock
	private HotelRepository hotelRepository;

	private HotelService hotelService;

	@BeforeEach
	void setUp() {
		hotelService = new HotelService(hotelRepository);
	}

	@Test
	void createHotel_success() {
		HotelRequest request = HotelRequest.builder()
				.name("Grand Palace")
				.rating(5L)
				.city("Paris")
				.country("France")
				.discount(10.0)
				.build();

		when(hotelRepository.save(any(Hotel.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Hotel created = hotelService.createHotel(request);

		assertNotNull(created);
		assertEquals("Grand Palace", created.getName());
		assertEquals(5L, created.getRating());
		assertEquals("Paris", created.getCity());
		assertEquals("France", created.getCountry());
		assertEquals(10.0, created.getDiscount());
		verify(hotelRepository).save(any(Hotel.class));
	}

	@Test
	void getHotelById_found() {
		Hotel hotel = Hotel.builder().id(1L).name("Hotel Mirage").city("Las Vegas").build();
		when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

		Hotel result = hotelService.getHotelById(1L);

		assertEquals("Hotel Mirage", result.getName());
		assertEquals(1L, result.getId());
	}

	@Test
	void getHotelById_notFound_throwsHotelNotFoundException() {
		when(hotelRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(HotelNotFoundException.class, () -> hotelService.getHotelById(99L));
	}

	@Test
	void updateHotel_success() {
		Hotel existing = Hotel.builder().id(1L).name("Old Name").city("Rome").build();
		when(hotelRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(hotelRepository.save(any(Hotel.class))).thenAnswer(invocation -> invocation.getArgument(0));

		HotelRequest updateRequest = HotelRequest.builder()
				.name("New Grand Rome")
				.city("Rome")
				.rating(4L)
				.build();

		Hotel updated = hotelService.updateHotel(1L, updateRequest);

		assertEquals("New Grand Rome", updated.getName());
		assertEquals(4L, updated.getRating());
	}

	@Test
	void getHotelsByCity_success() {
		Hotel hotel = Hotel.builder().id(1L).name("London Eye Hotel").city("London").build();
		when(hotelRepository.findByCityIgnoreCase("London")).thenReturn(List.of(hotel));

		List<Hotel> hotels = hotelService.getHotelsByCity("London");

		assertEquals(1, hotels.size());
		assertEquals("London", hotels.get(0).getCity());
	}

	@Test
	void getAllHotels_paged_success() {
		Hotel hotel = Hotel.builder().id(1L).name("Sea View").build();
		Pageable pageable = PageRequest.of(0, 10);
		when(hotelRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(hotel), pageable, 1));

		Page<Hotel> page = hotelService.getAllHotels(pageable);

		assertEquals(1, page.getTotalElements());
		assertEquals("Sea View", page.getContent().get(0).getName());
	}

	@Test
	void deleteHotelById_softDeletesHotelAndRooms() {
		com.cn.hotelDemo.model.Room room = com.cn.hotelDemo.model.Room.builder().id(101L).deleted(false).build();
		Hotel hotel = Hotel.builder().id(1L).name("Sea View").rooms(new java.util.ArrayList<>(List.of(room))).deleted(false).build();
		when(hotelRepository.findById(1L)).thenReturn(Optional.of(hotel));

		hotelService.deleteHotelById(1L);

		org.junit.jupiter.api.Assertions.assertTrue(hotel.isDeleted());
		org.junit.jupiter.api.Assertions.assertNotNull(hotel.getDeletedAt());
		org.junit.jupiter.api.Assertions.assertTrue(room.isDeleted());
		org.junit.jupiter.api.Assertions.assertNotNull(room.getDeletedAt());
		verify(hotelRepository).save(hotel);
	}

	@Test
	void getDeletedHotels_returnsDeletedHotels() {
		Hotel deletedHotel = Hotel.builder().id(2L).name("Old Hotel").deleted(true).deletedAt(java.time.LocalDateTime.now()).build();
		when(hotelRepository.findAllDeletedHotels()).thenReturn(List.of(deletedHotel));

		List<Hotel> result = hotelService.getDeletedHotels();

		assertEquals(1, result.size());
		org.junit.jupiter.api.Assertions.assertTrue(result.get(0).isDeleted());
		verify(hotelRepository).findAllDeletedHotels();
	}

	@Test
	void restoreHotel_restoresSoftDeletedHotel() {
		com.cn.hotelDemo.model.Room room = com.cn.hotelDemo.model.Room.builder().id(101L).deleted(true).deletedAt(java.time.LocalDateTime.now()).build();
		Hotel deletedHotel = Hotel.builder().id(2L).name("Old Hotel").rooms(new java.util.ArrayList<>(List.of(room))).deleted(true).deletedAt(java.time.LocalDateTime.now()).build();
		when(hotelRepository.findDeletedById(2L)).thenReturn(Optional.of(deletedHotel));
		when(hotelRepository.save(deletedHotel)).thenReturn(deletedHotel);

		Hotel restored = hotelService.restoreHotel(2L);

		org.junit.jupiter.api.Assertions.assertFalse(restored.isDeleted());
		org.junit.jupiter.api.Assertions.assertNull(restored.getDeletedAt());
		org.junit.jupiter.api.Assertions.assertFalse(room.isDeleted());
		org.junit.jupiter.api.Assertions.assertNull(room.getDeletedAt());
		verify(hotelRepository).save(deletedHotel);
	}

	@Test
	void restoreHotel_notFound_throwsException() {
		when(hotelRepository.findDeletedById(99L)).thenReturn(Optional.empty());

		org.junit.jupiter.api.Assertions.assertThrows(
				com.cn.hotelDemo.exception.HotelNotFoundException.class,
				() -> hotelService.restoreHotel(99L));
	}
}
