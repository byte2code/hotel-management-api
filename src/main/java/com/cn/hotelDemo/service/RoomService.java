package com.cn.hotelDemo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.cn.hotelDemo.dto.RoomRequest;
import com.cn.hotelDemo.model.Hotel;
import com.cn.hotelDemo.model.Room;
import com.cn.hotelDemo.repository.HotelRepository;
import com.cn.hotelDemo.repository.RoomRepository;

@Service
public class RoomService {

	private final RoomRepository roomRepository;
	private final HotelRepository hotelRepository;

	public RoomService(RoomRepository roomRepository, HotelRepository hotelRepository) {
		this.roomRepository = roomRepository;
		this.hotelRepository = hotelRepository;
	}

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

	public List<Room> getRoomsByHotelId(Long hotelId) {
		return roomRepository.findByHotelId(hotelId);
	}

	public Room getRoomById(Long id) {
		return roomRepository.findById(id).orElse(null);
	}

	public List<Room> getAllRooms() {
		return roomRepository.findAll();
	}
}
