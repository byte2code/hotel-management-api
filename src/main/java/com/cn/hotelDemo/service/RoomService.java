package com.cn.hotelDemo.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import com.cn.hotelDemo.model.Booking;
import com.cn.hotelDemo.model.BookingStatus;
import com.cn.hotelDemo.dto.RoomRequest;
import com.cn.hotelDemo.model.Hotel;
import com.cn.hotelDemo.model.Room;
import com.cn.hotelDemo.model.RoomStatus;
import com.cn.hotelDemo.repository.HotelRepository;
import com.cn.hotelDemo.repository.BookingRepository;
import com.cn.hotelDemo.repository.RoomRepository;

@Service
public class RoomService {

	private final RoomRepository roomRepository;
	private final HotelRepository hotelRepository;
	private final BookingRepository bookingRepository;

	public RoomService(RoomRepository roomRepository, HotelRepository hotelRepository,
			BookingRepository bookingRepository) {
		this.roomRepository = roomRepository;
		this.hotelRepository = hotelRepository;
		this.bookingRepository = bookingRepository;
	}

	@Transactional
	@CacheEvict(cacheNames = "roomAvailability", allEntries = true)
	public Room createRoom(RoomRequest roomRequest) {
		Hotel hotel = hotelRepository.findById(roomRequest.getHotelId())
				.orElseThrow(() -> new IllegalArgumentException("Hotel not found with ID: " + roomRequest.getHotelId()));

		Room room = new Room();
		room.setHotel(hotel);
		room.setRoomNumber(roomRequest.getRoomNumber());
		room.setRoomType(roomRequest.getRoomType());
		room.setCapacity(roomRequest.getCapacity());
		room.setNightlyRate(roomRequest.getNightlyRate());
		room.setStatus(roomRequest.getStatus() != null ? roomRequest.getStatus() : room.getStatus());

		Room savedRoom = roomRepository.save(room);
		hotel.getRooms().add(savedRoom);
		hotelRepository.save(hotel);
		return savedRoom;
	}

	@Cacheable(cacheNames = "roomAvailability", key = "#hotelId + ':' + #checkInDate + ':' + #checkOutDate")
	@Transactional(readOnly = true)
	public List<Room> getAvailableRoomsByHotelAndDates(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate) {
		if (checkOutDate.isBefore(checkInDate)) {
			throw new IllegalArgumentException("Check-out date must be on or after the check-in date");
		}

		List<Room> rooms = roomRepository.findByHotelId(hotelId);
		Set<Long> unavailableRoomIds = bookingRepository.findByHotelIdAndStatusIn(hotelId,
				List.of(BookingStatus.REQUESTED, BookingStatus.CONFIRMED)).stream()
				.filter(booking -> overlaps(booking, checkInDate, checkOutDate))
				.map(booking -> booking.getRoom().getId())
				.collect(Collectors.toSet());

		return rooms.stream()
				.filter(room -> room.getStatus() == null || room.getStatus() == RoomStatus.AVAILABLE)
				.filter(room -> !unavailableRoomIds.contains(room.getId()))
				.collect(Collectors.toList());
	}

	public List<Room> getRoomsByHotelId(Long hotelId) {
		return roomRepository.findByHotelId(hotelId);
	}

	public Room getRoomById(Long id) {
		return roomRepository.findById(id).orElse(null);
	}

	public List<Room> getAllRooms() {
		return roomRepository.findAll();
	}

	private boolean overlaps(Booking booking, LocalDate checkInDate, LocalDate checkOutDate) {
		return !checkOutDate.isBefore(booking.getCheckInDate()) && !checkInDate.isAfter(booking.getCheckOutDate());
	}
}
