package com.cn.hotelDemo.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.cn.hotelDemo.dto.UserRequest;
import com.cn.hotelDemo.dto.UserResponse;
import com.cn.hotelDemo.model.User;

class UserMapperTest {

	private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

	@Test
	void toResponse_mapsUserWithoutPassword() {
		User user = User.builder()
				.id(1L)
				.username("alice")
				.email("alice@example.com")
				.password("secretHash123")
				.role("ADMIN")
				.build();

		UserResponse response = userMapper.toResponse(user);

		assertNotNull(response);
		assertEquals(1L, response.getId());
		assertEquals("alice", response.getUsername());
		assertEquals("alice@example.com", response.getEmail());
		assertEquals("ADMIN", response.getRole());
	}

	@Test
	void toEntity_mapsRequestToEntity() {
		UserRequest request = new UserRequest("bob", "pass456", "NORMAL", "bob@example.com");

		User user = userMapper.toEntity(request);

		assertNotNull(user);
		assertEquals("bob", user.getUsername());
		assertEquals("pass456", user.getPassword());
		assertEquals("NORMAL", user.getRole());
		assertEquals("bob@example.com", user.getEmail());
		assertNull(user.getId());
	}

	@Test
	void toResponseList_mapsListSuccessfully() {
		User u1 = User.builder().id(1L).username("u1").build();
		User u2 = User.builder().id(2L).username("u2").build();

		List<UserResponse> responses = userMapper.toResponseList(List.of(u1, u2));

		assertEquals(2, responses.size());
		assertEquals("u1", responses.get(0).getUsername());
		assertEquals("u2", responses.get(1).getUsername());
	}
}
