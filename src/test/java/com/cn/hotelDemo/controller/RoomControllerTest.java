package com.cn.hotelDemo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.cn.hotelDemo.dto.RoomRequest;
import com.cn.hotelDemo.dto.RoomResponse;
import com.cn.hotelDemo.mapper.RoomMapper;
import com.cn.hotelDemo.model.Room;
import com.cn.hotelDemo.model.RoomStatus;
import com.cn.hotelDemo.model.RoomType;
import com.cn.hotelDemo.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private RoomService roomService;

	@MockBean
	private RoomMapper roomMapper;

	@Test
	void createRoom_returnsCreatedRoomResponse() throws Exception {
		RoomRequest request = new RoomRequest(1L, "101", "Deluxe", 2, new BigDecimal("350.00"), RoomStatus.AVAILABLE);
		Room room = Room.builder().id(10L).roomNumber("101").build();
		RoomResponse response = RoomResponse.builder()
				.id(10L)
				.roomNumber("101")
				.roomType(RoomType.DELUXE)
				.capacity(2)
				.nightlyRate(new BigDecimal("350.00"))
				.status(RoomStatus.AVAILABLE)
				.hotelId(1L)
				.hotelName("Hilton")
				.build();

		when(roomService.createRoom(any(RoomRequest.class))).thenReturn(room);
		when(roomMapper.toResponse(room)).thenReturn(response);

		mockMvc.perform(post("/hotel/rooms/create")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(10L))
				.andExpect(jsonPath("$.roomNumber").value("101"))
				.andExpect(jsonPath("$.hotelId").value(1L));
	}

	@Test
	void getRoomsByHotel_returnsList() throws Exception {
		Room room = Room.builder().id(11L).roomNumber("102").build();
		RoomResponse response = RoomResponse.builder().id(11L).roomNumber("102").build();

		when(roomService.getRoomsByHotelId(1L)).thenReturn(List.of(room));
		when(roomMapper.toResponseList(List.of(room))).thenReturn(List.of(response));

		mockMvc.perform(get("/hotel/rooms/hotel/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(11L))
				.andExpect(jsonPath("$[0].roomNumber").value("102"));
	}

	@Test
	void getAvailableRoomsByHotel_returnsList() throws Exception {
		LocalDate checkIn = LocalDate.now().plusDays(2);
		LocalDate checkOut = LocalDate.now().plusDays(5);
		Room room = Room.builder().id(12L).roomNumber("103").build();
		RoomResponse response = RoomResponse.builder().id(12L).roomNumber("103").build();

		when(roomService.getAvailableRoomsByHotelAndDates(eq(1L), eq(checkIn), eq(checkOut))).thenReturn(List.of(room));
		when(roomMapper.toResponseList(List.of(room))).thenReturn(List.of(response));

		mockMvc.perform(get("/hotel/rooms/hotel/1/available")
				.param("checkInDate", checkIn.toString())
				.param("checkOutDate", checkOut.toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].roomNumber").value("103"));
	}

	@Test
	void getRoomById_returnsRoomResponse() throws Exception {
		Room room = Room.builder().id(15L).roomNumber("105").build();
		RoomResponse response = RoomResponse.builder().id(15L).roomNumber("105").build();

		when(roomService.getRoomById(15L)).thenReturn(room);
		when(roomMapper.toResponse(room)).thenReturn(response);

		mockMvc.perform(get("/hotel/rooms/id/15"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(15L));
	}

	@Test
	void getAllRooms_returnsPagedRoomResponse() throws Exception {
		Room room = Room.builder().id(20L).roomNumber("201").build();
		RoomResponse response = RoomResponse.builder().id(20L).roomNumber("201").build();

		when(roomService.getAllRooms(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(room)));
		when(roomMapper.toResponse(room)).thenReturn(response);

		mockMvc.perform(get("/hotel/rooms/getAll"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].roomNumber").value("201"));
	}

	@Test
	void deleteRoomById_returnsNoContent() throws Exception {
		mockMvc.perform(delete("/hotel/rooms/remove/id/20"))
				.andExpect(status().isNoContent());

		org.mockito.Mockito.verify(roomService).deleteRoomById(20L);
	}
}
