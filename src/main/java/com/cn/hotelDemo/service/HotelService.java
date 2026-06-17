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

    public Hotel getHotelById(Long id) {
        return hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException(id));
    }

    public Hotel createHotel(HotelRequest hotelRequest) {
    	Hotel hotel = new Hotel();
    	hotel.setCity(hotelRequest.getCity());
    	hotel.setName(hotelRequest.getName());
    	hotel.setRating(hotelRequest.getRating());
    	
       return hotelRepository.save(hotel);
    }

    public void deleteHotelById(Long id) {
    	hotelRepository.deleteById(id);
    }

}
