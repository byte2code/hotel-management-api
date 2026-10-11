package com.cn.hotelDemo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cn.hotelDemo.dto.UserRequest;
import com.cn.hotelDemo.exception.UserNotFoundException;
import com.cn.hotelDemo.model.User;
import com.cn.hotelDemo.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	private UserService userService;

	@BeforeEach
	void setUp() {
		userService = new UserService(userRepository, passwordEncoder);
	}

	@Test
	void createUser_success_encodesPassword() {
		UserRequest request = new UserRequest();
		request.setUsername("johndoe");
		request.setEmail("john@example.com");
		request.setPassword("plainPassword123");
		request.setRole("NORMAL");

		when(passwordEncoder.encode("plainPassword123")).thenReturn("hashedPassword123");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		User created = userService.createUser(request);

		assertNotNull(created);
		assertEquals("johndoe", created.getUsername());
		assertEquals("john@example.com", created.getEmail());
		assertEquals("hashedPassword123", created.getPassword());
		assertEquals("NORMAL", created.getRole());
		verify(passwordEncoder).encode("plainPassword123");
		verify(userRepository).save(any(User.class));
	}

	@Test
	void getUserById_found() {
		User user = User.builder().id(5L).username("alice").email("alice@test.com").build();
		when(userRepository.findById(5L)).thenReturn(Optional.of(user));

		User found = userService.getUserById(5L);

		assertEquals(5L, found.getId());
		assertEquals("alice", found.getUsername());
	}

	@Test
	void getUserById_notFound_throwsUserNotFoundException() {
		when(userRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> userService.getUserById(999L));
	}

	@Test
	void getUsers_paged_success() {
		User user = User.builder().id(1L).username("bob").build();
		Pageable pageable = PageRequest.of(0, 10);
		when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user), pageable, 1));

		Page<User> page = userService.getUsers(pageable);

		assertEquals(1, page.getTotalElements());
		assertEquals("bob", page.getContent().get(0).getUsername());
	}

	@Test
	void deleteUserById_success() {
		userService.deleteUserById(10L);
		verify(userRepository).deleteById(10L);
	}
}
