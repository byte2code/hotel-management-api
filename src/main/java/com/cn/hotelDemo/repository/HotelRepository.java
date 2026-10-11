package com.cn.hotelDemo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cn.hotelDemo.model.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

	List<Hotel> findByCityIgnoreCase(String city);

	List<Hotel> findByCountryIgnoreCase(String country);

	List<Hotel> findByNameContainingIgnoreCaseAndCityContainingIgnoreCase(String name, String city);

	List<Hotel> findByRatingGreaterThanEqual(Long rating);
}
