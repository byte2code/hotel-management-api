package com.cn.hotelDemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cn.hotelDemo.model.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

}

