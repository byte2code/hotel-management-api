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
import com.cn.hotelDemo.dto.RoomResponse;
import com.cn.hotelDemo.mapper.RoomMapper;
import com.cn.hotelDemo.model.Room;
import com.cn.hotelDemo.annotation.AuditLogged;
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
	private final RoomMapper roomMapper;

	public RoomController(RoomService roomService, RoomMapper roomMapper) {
		this.roomService = roomService;
		this.roomMapper = roomMapper;
	}

	@PostMapping("/create")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	@Operation(summary = "Create a new room in a hotel")
	@AuditLogged(action = "ROOM_CREATED", resourceType = "ROOM", resourceIdSpel = "#result.body.id")
	public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest roomRequest, Authentication authentication) {
		Room room = roomService.createRoom(roomRequest);
		return new ResponseEntity<>(roomMapper.toResponse(room), HttpStatus.CREATED);
	}

	@GetMapping("/hotel/{hotelId}")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin') or hasRole('NORMAL') or hasAuthority('normal')")
	@Operation(summary = "Get all rooms for a specific hotel")
	public ResponseEntity<List<RoomResponse>> getRoomsByHotel(@PathVariable Long hotelId) {
		return ResponseEntity.ok(roomMapper.toResponseList(roomService.getRoomsByHotelId(hotelId)));
	}

	@GetMapping("/hotel/{hotelId}/available")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin') or hasRole('NORMAL') or hasAuthority('normal')")
	@Operation(summary = "Check room availability between dates for a hotel")
	@AuditLogged(action = "ROOM_AVAILABILITY_SEARCHED", resourceType = "ROOM", resourceIdSpel = "#hotelId")
	public ResponseEntity<List<RoomResponse>> getAvailableRoomsByHotel(
			@PathVariable Long hotelId,
			Authentication authentication,
			@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate checkInDate,
			@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate checkOutDate) {
		List<Room> rooms = roomService.getAvailableRoomsByHotelAndDates(hotelId, checkInDate, checkOutDate);
		return ResponseEntity.ok(roomMapper.toResponseList(rooms));
	}

	@GetMapping("/id/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin') or hasRole('NORMAL') or hasAuthority('normal')")
	@Operation(summary = "Get a room by its ID")
	public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long id) {
		Room room = roomService.getRoomById(id);
		return ResponseEntity.ok(roomMapper.toResponse(room));
	}

	@GetMapping("/getAll")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	@Operation(summary = "Get all rooms across all hotels with pagination (Admin only)")
	public ResponseEntity<org.springframework.data.domain.Page<RoomResponse>> getAllRooms(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "id") String sortBy,
			@RequestParam(defaultValue = "asc") String direction) {
		org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("desc")
				? org.springframework.data.domain.Sort.by(sortBy).descending()
				: org.springframework.data.domain.Sort.by(sortBy).ascending();
		org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
		org.springframework.data.domain.Page<Room> roomPage = roomService.getAllRooms(pageable);
		org.springframework.data.domain.Page<RoomResponse> responsePage = roomPage != null
				? roomPage.map(roomMapper::toResponse)
				: org.springframework.data.domain.Page.empty();
		return ResponseEntity.ok(responsePage);
	}

	@org.springframework.web.bind.annotation.DeleteMapping("/remove/id/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	@Operation(summary = "Delete a room by ID (Admin only)")
	@AuditLogged(action = "ROOM_DELETED", resourceType = "ROOM", resourceIdSpel = "#id")
	public ResponseEntity<Void> deleteRoomById(@PathVariable Long id) {
		roomService.deleteRoomById(id);
		return ResponseEntity.noContent().build();
	}
}
