package com.cn.hotelDemo.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cn.hotelDemo.model.Booking;
import com.cn.hotelDemo.model.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking, Long> {

	List<Booking> findByUserId(Long userId);

	List<Booking> findByHotelId(Long hotelId);

	List<Booking> findByRoomIdAndStatusIn(Long roomId, Collection<BookingStatus> statuses);

	List<Booking> findByHotelIdAndStatusIn(Long hotelId, Collection<BookingStatus> statuses);
}
