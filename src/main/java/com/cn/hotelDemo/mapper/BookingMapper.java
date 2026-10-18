package com.cn.hotelDemo.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cn.hotelDemo.dto.BookingDetailResponse;
import com.cn.hotelDemo.dto.BookingResponse;
import com.cn.hotelDemo.model.Booking;

@Mapper(componentModel = "spring")
public interface BookingMapper {

	@Mapping(source = "hotel.id", target = "hotelId")
	@Mapping(source = "hotel.name", target = "hotelName")
	@Mapping(source = "room.id", target = "roomId")
	@Mapping(source = "room.roomNumber", target = "roomNumber")
	@Mapping(source = "user.id", target = "userId")
	@Mapping(source = "user.username", target = "username")
	@Mapping(source = "user.email", target = "userEmail")
	BookingDetailResponse toDetailResponse(Booking booking);

	List<BookingDetailResponse> toDetailResponseList(List<Booking> bookings);

	@Mapping(source = "id", target = "bookingId")
	@Mapping(target = "message", constant = "Booking details retrieved successfully")
	BookingResponse toResponse(Booking booking);
}
