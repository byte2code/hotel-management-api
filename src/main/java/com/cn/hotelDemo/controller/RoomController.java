package com.cn.hotelDemo.controller;

import java.util.List;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.cn.hotelDemo.dto.RoomRequest;
import com.cn.hotelDemo.model.Room;
import com.cn.hotelDemo.service.AuditService;
import com.cn.hotelDemo.service.RoomService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/hotel/rooms")
@Tag(name = "Room Controller", description = "Endpoints for managing rooms")
@SecurityRequirement(name = "Bearer Authentication")
public class RoomController {

	private final RoomService roomService;
	private final AuditService auditService;

	public RoomController(RoomService roomService, AuditService auditService) {
		this.roomService = roomService;
		this.auditService = auditService;
	}

	@PostMapping("/create")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	@Operation(summary = "Create a new room in a hotel")
	public ResponseEntity<Room> createRoom(@Valid @RequestBody RoomRequest roomRequest, Authentication authentication) {
		Room room = roomService.createRoom(roomRequest);
		auditService.record("ROOM_CREATED", authentication.getName(), "ROOM", String.valueOf(room.getId()), "SUCCESS",
				"Room created for hotelId=%s, roomNumber=%s".formatted(roomRequest.getHotelId(),
						roomRequest.getRoomNumber()));
		return new ResponseEntity<>(room, HttpStatus.CREATED);
	}

	@GetMapping("/hotel/{hotelId}")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin') or hasRole('NORMAL') or hasAuthority('normal')")
	@Operation(summary = "Get all rooms for a specific hotel")
	public ResponseEntity<List<Room>> getRoomsByHotel(@PathVariable Long hotelId) {
		return ResponseEntity.ok(roomService.getRoomsByHotelId(hotelId));
	}

	@GetMapping("/hotel/{hotelId}/available")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin') or hasRole('NORMAL') or hasAuthority('normal')")
	@Operation(summary = "Check room availability between dates for a hotel")
	public ResponseEntity<List<Room>> getAvailableRoomsByHotel(
			@PathVariable Long hotelId,
			Authentication authentication,
			@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate checkInDate,
			@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate checkOutDate) {
		List<Room> rooms = roomService.getAvailableRoomsByHotelAndDates(hotelId, checkInDate, checkOutDate);
		auditService.record("ROOM_AVAILABILITY_SEARCHED", authentication.getName(), "ROOM", String.valueOf(hotelId),
				"SUCCESS", "Availability checked for hotelId=%s between %s and %s".formatted(hotelId, checkInDate,
						checkOutDate));
		return ResponseEntity.ok(rooms);
	}

	@GetMapping("/id/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin') or hasRole('NORMAL') or hasAuthority('normal')")
	@Operation(summary = "Get a room by its ID")
	public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
		Room room = roomService.getRoomById(id);
		if (room == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(room);
	}

	@GetMapping("/getAll")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	@Operation(summary = "Get all rooms across all hotels (Admin only)")
	public ResponseEntity<List<Room>> getAllRooms() {
		return ResponseEntity.ok(roomService.getAllRooms());
	}
}
