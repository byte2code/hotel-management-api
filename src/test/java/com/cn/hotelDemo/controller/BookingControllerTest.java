package com.cn.hotelDemo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.cn.hotelDemo.dto.BookingRequest;
import com.cn.hotelDemo.dto.BookingResponse;
import com.cn.hotelDemo.dto.RejectionRequest;
import com.cn.hotelDemo.model.BookingStatus;
import com.cn.hotelDemo.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(BookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private BookingService bookingService;

	@Test
	void createBooking_returnsCreated() throws Exception {
		BookingRequest request = new BookingRequest(1L, 10L, 2L,
				LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), 2, "Quiet room");

		BookingResponse response = new BookingResponse("BOOK-123", BookingStatus.REQUESTED,
				"Booking requested, awaiting confirmation", 1L, new BigDecimal("300.00"));

		when(bookingService.createBooking(any(BookingRequest.class))).thenReturn(response);

		mockMvc.perform(post("/hotel/bookings/create")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("REQUESTED"))
				.andExpect(jsonPath("$.bookingReference").value("BOOK-123"));
	}

	@Test
	void confirmBooking_returnsOk() throws Exception {
		BookingResponse response = new BookingResponse("BOOK-123", BookingStatus.CONFIRMED,
				"Booking confirmed by admin", 1L, new BigDecimal("300.00"));

		when(bookingService.confirmBooking(1L)).thenReturn(response);

		mockMvc.perform(patch("/hotel/bookings/1/confirm"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("CONFIRMED"));
	}

	@Test
	void rejectBooking_returnsOk() throws Exception {
		RejectionRequest rejection = new RejectionRequest("Overbooked");
		BookingResponse response = new BookingResponse("BOOK-123", BookingStatus.REJECTED,
				"Booking rejected: Overbooked", 1L, new BigDecimal("300.00"));

		when(bookingService.rejectBooking(eq(1L), eq("Overbooked"))).thenReturn(response);

		mockMvc.perform(patch("/hotel/bookings/1/reject")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(rejection)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REJECTED"));
	}
}
