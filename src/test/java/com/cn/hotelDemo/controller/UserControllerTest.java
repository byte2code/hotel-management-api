package com.cn.hotelDemo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.cn.hotelDemo.dto.UserRequest;
import com.cn.hotelDemo.dto.UserResponse;
import com.cn.hotelDemo.mapper.UserMapper;
import com.cn.hotelDemo.model.User;
import com.cn.hotelDemo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private UserService userService;

	@MockBean
	private UserMapper userMapper;

	@Test
	void createUser_returnsUserResponse() throws Exception {
		UserRequest request = new UserRequest("john_doe", "password123", "NORMAL", "john@example.com");
		User user = User.builder().id(1L).username("john_doe").email("john@example.com").role("NORMAL").build();
		UserResponse response = UserResponse.builder().id(1L).username("john_doe").email("john@example.com").role("NORMAL").build();

		when(userService.createUser(any(UserRequest.class))).thenReturn(user);
		when(userMapper.toResponse(user)).thenReturn(response);

		mockMvc.perform(post("/user/createUser")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L))
				.andExpect(jsonPath("$.username").value("john_doe"))
				.andExpect(jsonPath("$.email").value("john@example.com"))
				.andExpect(jsonPath("$.role").value("NORMAL"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void getUserById_returnsUserResponse() throws Exception {
		User user = User.builder().id(2L).username("alice").email("alice@example.com").role("ADMIN").build();
		UserResponse response = UserResponse.builder().id(2L).username("alice").email("alice@example.com").role("ADMIN").build();

		when(userService.getUserById(2L)).thenReturn(user);
		when(userMapper.toResponse(user)).thenReturn(response);

		mockMvc.perform(get("/user/getUsers/2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(2L))
				.andExpect(jsonPath("$.username").value("alice"));
	}

	@Test
	void getUsers_returnsPagedUserResponses() throws Exception {
		User user = User.builder().id(3L).username("bob").build();
		UserResponse response = UserResponse.builder().id(3L).username("bob").build();

		when(userService.getUsers(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(user)));
		when(userMapper.toResponse(user)).thenReturn(response);

		mockMvc.perform(get("/user/getUsers"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(3L))
				.andExpect(jsonPath("$.content[0].username").value("bob"));
	}

	@Test
	void deleteUserById_returnsOk() throws Exception {
		doNothing().when(userService).deleteUserById(1L);

		mockMvc.perform(delete("/user/remove/id/1"))
				.andExpect(status().isOk());
	}
}
