package com.cn.hotelDemo.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cn.hotelDemo.dto.HotelRequest;
import com.cn.hotelDemo.dto.HotelResponse;
import com.cn.hotelDemo.model.Hotel;

@Mapper(componentModel = "spring", uses = {RoomMapper.class})
public interface HotelMapper {

	HotelResponse toResponse(Hotel hotel);

	List<HotelResponse> toResponseList(List<Hotel> hotels);

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "deleted", ignore = true)
	@Mapping(target = "deletedAt", ignore = true)
	@Mapping(target = "rooms", ignore = true)
	Hotel toEntity(HotelRequest hotelRequest);
}
