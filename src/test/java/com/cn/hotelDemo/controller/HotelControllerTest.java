package com.cn.hotelDemo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.cn.hotelDemo.dto.HotelRequest;
import com.cn.hotelDemo.model.Hotel;
import com.cn.hotelDemo.service.HotelService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(HotelController.class)
@AutoConfigureMockMvc(addFilters = false)
class HotelControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private HotelService hotelService;

	@MockBean
	private com.cn.hotelDemo.mapper.HotelMapper hotelMapper;

	@Test
	void createHotel_returnsHotel() throws Exception {
		HotelRequest request = HotelRequest.builder()
				.name("Grand Luxury")
				.city("Dubai")
				.rating(5L)
				.build();

		Hotel hotel = Hotel.builder()
				.id(1L)
				.name("Grand Luxury")
				.city("Dubai")
				.rating(5L)
				.build();

		com.cn.hotelDemo.dto.HotelResponse response = com.cn.hotelDemo.dto.HotelResponse.builder()
				.id(1L)
				.name("Grand Luxury")
				.city("Dubai")
				.rating(5L)
				.build();

		when(hotelService.createHotel(any(HotelRequest.class))).thenReturn(hotel);
		when(hotelMapper.toResponse(hotel)).thenReturn(response);

		mockMvc.perform(post("/hotel/create")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.name").value("Grand Luxury"));
	}

	@Test
	void getHotelById_returnsHotel() throws Exception {
		Hotel hotel = Hotel.builder().id(2L).name("Skyline Hotel").city("Tokyo").build();
		com.cn.hotelDemo.dto.HotelResponse response = com.cn.hotelDemo.dto.HotelResponse.builder()
				.id(2L)
				.name("Skyline Hotel")
				.city("Tokyo")
				.build();

		when(hotelService.getHotelById(2L)).thenReturn(hotel);
		when(hotelMapper.toResponse(hotel)).thenReturn(response);

		mockMvc.perform(get("/hotel/id/2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(2L))
				.andExpect(jsonPath("$.name").value("Skyline Hotel"));
	}

	@Test
	void getHotelsByCity_returnsList() throws Exception {
		Hotel hotel = Hotel.builder().id(3L).name("Tokyo Bay").city("Tokyo").build();
		com.cn.hotelDemo.dto.HotelResponse response = com.cn.hotelDemo.dto.HotelResponse.builder()
				.id(3L)
				.name("Tokyo Bay")
				.city("Tokyo")
				.build();

		when(hotelService.getHotelsByCity("Tokyo")).thenReturn(List.of(hotel));
		when(hotelMapper.toResponseList(List.of(hotel))).thenReturn(List.of(response));

		mockMvc.perform(get("/hotel/city/Tokyo"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Tokyo Bay"));
	}

	@Test
	void deleteHotelById_returnsOk() throws Exception {
		mockMvc.perform(delete("/hotel/remove/id/1"))
				.andExpect(status().isOk());

		verify(hotelService).deleteHotelById(1L);
	}

	@Test
	void getDeletedHotels_returnsList() throws Exception {
		Hotel deletedHotel = Hotel.builder().id(5L).name("Archived Hotel").deleted(true).build();
		com.cn.hotelDemo.dto.HotelResponse response = com.cn.hotelDemo.dto.HotelResponse.builder()
				.id(5L)
				.name("Archived Hotel")
				.deleted(true)
				.build();

		when(hotelService.getDeletedHotels()).thenReturn(List.of(deletedHotel));
		when(hotelMapper.toResponseList(List.of(deletedHotel))).thenReturn(List.of(response));

		mockMvc.perform(get("/hotel/deleted"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(5L))
				.andExpect(jsonPath("$[0].deleted").value(true));
	}

	@Test
	void restoreHotel_returnsRestoredHotel() throws Exception {
		Hotel restoredHotel = Hotel.builder().id(5L).name("Restored Hotel").deleted(false).build();
		com.cn.hotelDemo.dto.HotelResponse response = com.cn.hotelDemo.dto.HotelResponse.builder()
				.id(5L)
				.name("Restored Hotel")
				.deleted(false)
				.build();

		when(hotelService.restoreHotel(5L)).thenReturn(restoredHotel);
		when(hotelMapper.toResponse(restoredHotel)).thenReturn(response);

		mockMvc.perform(patch("/hotel/restore/5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(5L))
				.andExpect(jsonPath("$.deleted").value(false));
	}
}
