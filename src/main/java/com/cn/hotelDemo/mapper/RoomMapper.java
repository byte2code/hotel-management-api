package com.cn.hotelDemo.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cn.hotelDemo.dto.RoomResponse;
import com.cn.hotelDemo.model.Room;

@Mapper(componentModel = "spring")
public interface RoomMapper {

	@Mapping(source = "hotel.id", target = "hotelId")
	@Mapping(source = "hotel.name", target = "hotelName")
	RoomResponse toResponse(Room room);

	List<RoomResponse> toResponseList(List<Room> rooms);
}
