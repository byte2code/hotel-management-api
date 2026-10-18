package com.cn.hotelDemo.service;

import java.util.List;

import com.cn.hotelDemo.exception.HotelNotFoundException;

import org.springframework.stereotype.Service;

import com.cn.hotelDemo.dto.HotelRequest;
import com.cn.hotelDemo.model.Hotel;
import com.cn.hotelDemo.repository.HotelRepository;

@Service
public class HotelService {

	private final HotelRepository hotelRepository;

	public HotelService(HotelRepository hotelRepository) {
		this.hotelRepository = hotelRepository;
	}

	public List<Hotel> getAllHotels() {
		return hotelRepository.findAll();
	}

	public org.springframework.data.domain.Page<Hotel> getAllHotels(org.springframework.data.domain.Pageable pageable) {
		return hotelRepository.findAll(pageable);
	}

	public Hotel getHotelById(Long id) {
		return hotelRepository.findById(id)
				.orElseThrow(() -> new HotelNotFoundException(id));
	}

	public Hotel createHotel(HotelRequest hotelRequest) {
		Hotel hotel = Hotel.builder()
				.city(hotelRequest.getCity())
				.name(hotelRequest.getName())
				.rating(hotelRequest.getRating())
				.discount(hotelRequest.getDiscount() != null ? hotelRequest.getDiscount() : 0.0)
				.address(hotelRequest.getAddress())
				.state(hotelRequest.getState())
				.country(hotelRequest.getCountry())
				.phoneNumber(hotelRequest.getPhoneNumber())
				.email(hotelRequest.getEmail())
				.website(hotelRequest.getWebsite())
				.description(hotelRequest.getDescription())
				.totalRooms(hotelRequest.getTotalRooms())
				.starCategory(hotelRequest.getStarCategory())
				.build();
		return hotelRepository.save(hotel);
	}

	public Hotel updateHotel(Long id, HotelRequest hotelRequest) {
		Hotel hotel = getHotelById(id);
		if (hotelRequest.getName() != null) hotel.setName(hotelRequest.getName());
		if (hotelRequest.getRating() != null) hotel.setRating(hotelRequest.getRating());
		if (hotelRequest.getCity() != null) hotel.setCity(hotelRequest.getCity());
		if (hotelRequest.getDiscount() != null) hotel.setDiscount(hotelRequest.getDiscount());
		if (hotelRequest.getAddress() != null) hotel.setAddress(hotelRequest.getAddress());
		if (hotelRequest.getState() != null) hotel.setState(hotelRequest.getState());
		if (hotelRequest.getCountry() != null) hotel.setCountry(hotelRequest.getCountry());
		if (hotelRequest.getPhoneNumber() != null) hotel.setPhoneNumber(hotelRequest.getPhoneNumber());
		if (hotelRequest.getEmail() != null) hotel.setEmail(hotelRequest.getEmail());
		if (hotelRequest.getWebsite() != null) hotel.setWebsite(hotelRequest.getWebsite());
		if (hotelRequest.getDescription() != null) hotel.setDescription(hotelRequest.getDescription());
		if (hotelRequest.getTotalRooms() != null) hotel.setTotalRooms(hotelRequest.getTotalRooms());
		if (hotelRequest.getStarCategory() != null) hotel.setStarCategory(hotelRequest.getStarCategory());
		return hotelRepository.save(hotel);
	}

	public List<Hotel> getHotelsByCity(String city) {
		return hotelRepository.findByCityIgnoreCase(city);
	}

	public List<Hotel> getHotelsByCountry(String country) {
		return hotelRepository.findByCountryIgnoreCase(country);
	}

	public List<Hotel> searchHotels(String name, String city, Long rating) {
		if (name != null && !name.isBlank() && city != null && !city.isBlank()) {
			return hotelRepository.findByNameContainingIgnoreCaseAndCityContainingIgnoreCase(name, city);
		} else if (city != null && !city.isBlank()) {
			return hotelRepository.findByCityIgnoreCase(city);
		} else if (rating != null) {
			return hotelRepository.findByRatingGreaterThanEqual(rating);
		}
		return hotelRepository.findAll();
	}

	public void deleteHotelById(Long id) {
		Hotel hotel = getHotelById(id);
		hotel.setDeleted(true);
		hotel.setDeletedAt(java.time.LocalDateTime.now());
		if (hotel.getRooms() != null) {
			for (com.cn.hotelDemo.model.Room room : hotel.getRooms()) {
				room.setDeleted(true);
				room.setDeletedAt(java.time.LocalDateTime.now());
			}
		}
		hotelRepository.save(hotel);
	}

	public List<Hotel> getDeletedHotels() {
		return hotelRepository.findAllDeletedHotels();
	}

	public Hotel restoreHotel(Long id) {
		Hotel hotel = hotelRepository.findDeletedById(id)
				.orElseThrow(() -> new HotelNotFoundException("Hotel with ID " + id + " not found or is not deleted"));
		hotel.setDeleted(false);
		hotel.setDeletedAt(null);
		if (hotel.getRooms() != null) {
			for (com.cn.hotelDemo.model.Room room : hotel.getRooms()) {
				room.setDeleted(false);
				room.setDeletedAt(null);
			}
		}
		return hotelRepository.save(hotel);
	}
}
