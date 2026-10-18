package com.cn.hotelDemo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cn.hotelDemo.model.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

	List<Hotel> findByCityIgnoreCase(String city);

	List<Hotel> findByCountryIgnoreCase(String country);

	List<Hotel> findByNameContainingIgnoreCaseAndCityContainingIgnoreCase(String name, String city);

	List<Hotel> findByRatingGreaterThanEqual(Long rating);

	@Query(value = "SELECT * FROM hotel WHERE deleted = true", nativeQuery = true)
	List<Hotel> findAllDeletedHotels();

	@Query(value = "SELECT * FROM hotel WHERE id = :id AND deleted = true", nativeQuery = true)
	Optional<Hotel> findDeletedById(@Param("id") Long id);
}
