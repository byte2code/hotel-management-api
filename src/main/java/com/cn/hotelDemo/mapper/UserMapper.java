package com.cn.hotelDemo.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.cn.hotelDemo.dto.UserRequest;
import com.cn.hotelDemo.dto.UserResponse;
import com.cn.hotelDemo.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

	UserResponse toResponse(User user);

	List<UserResponse> toResponseList(List<User> users);

	@Mapping(target = "id", ignore = true)
	User toEntity(UserRequest userRequest);
}
