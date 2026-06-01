package com.cn.hotelDemo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import java.time.LocalDate;

import com.cn.hotelDemo.dto.RoomRequest;
import com.cn.hotelDemo.model.Room;
import com.cn.hotelDemo.service.RoomService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/hotel/rooms")
public class RoomController {

	private final RoomService roomService;

	public RoomController(RoomService roomService) {
		this.roomService = roomService;
	}

	@PostMapping("/create")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	public ResponseEntity<Room> createRoom(@Valid @RequestBody RoomRequest roomRequest) {
		return new ResponseEntity<>(roomService.createRoom(roomRequest), HttpStatus.CREATED);
	}

	@GetMapping("/hotel/{hotelId}")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin') or hasRole('NORMAL') or hasAuthority('normal')")
	public ResponseEntity<List<Room>> getRoomsByHotel(@PathVariable Long hotelId) {
		return ResponseEntity.ok(roomService.getRoomsByHotelId(hotelId));
	}

	@GetMapping("/hotel/{hotelId}/available")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin') or hasRole('NORMAL') or hasAuthority('normal')")
	public ResponseEntity<List<Room>> getAvailableRoomsByHotel(
			@PathVariable Long hotelId,
			@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate checkInDate,
			@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate checkOutDate) {
		return ResponseEntity.ok(roomService.getAvailableRoomsByHotelAndDates(hotelId, checkInDate, checkOutDate));
	}

	@GetMapping("/id/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin') or hasRole('NORMAL') or hasAuthority('normal')")
	public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
		Room room = roomService.getRoomById(id);
		if (room == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(room);
	}

	@GetMapping("/getAll")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	public ResponseEntity<List<Room>> getAllRooms() {
		return ResponseEntity.ok(roomService.getAllRooms());
	}
}
