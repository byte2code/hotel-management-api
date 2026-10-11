package com.cn.hotelDemo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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

		when(hotelService.createHotel(any(HotelRequest.class))).thenReturn(hotel);

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
		when(hotelService.getHotelById(2L)).thenReturn(hotel);

		mockMvc.perform(get("/hotel/id/2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(2L))
				.andExpect(jsonPath("$.name").value("Skyline Hotel"));
	}

	@Test
	void getHotelsByCity_returnsList() throws Exception {
		Hotel hotel = Hotel.builder().id(3L).name("Tokyo Bay").city("Tokyo").build();
		when(hotelService.getHotelsByCity("Tokyo")).thenReturn(List.of(hotel));

		mockMvc.perform(get("/hotel/city/Tokyo"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Tokyo Bay"));
	}
}
