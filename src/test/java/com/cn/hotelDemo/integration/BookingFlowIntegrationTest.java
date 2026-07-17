package com.cn.hotelDemo.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.cn.hotelDemo.dto.BookingRequest;
import com.cn.hotelDemo.dto.BookingResponse;
import com.cn.hotelDemo.model.AuditLog;
import com.cn.hotelDemo.model.BookingStatus;
import com.cn.hotelDemo.model.Hotel;
import com.cn.hotelDemo.model.Room;
import com.cn.hotelDemo.model.RoomStatus;
import com.cn.hotelDemo.model.User;
import com.cn.hotelDemo.repository.AuditLogRepository;
import com.cn.hotelDemo.repository.BookingRepository;
import com.cn.hotelDemo.repository.HotelRepository;
import com.cn.hotelDemo.repository.RoomRepository;
import com.cn.hotelDemo.repository.UserRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
@Import(BookingFlowIntegrationTest.TestAuthenticationConfiguration.class)
class BookingFlowIntegrationTest {

	private static final String TEST_AUTO_CONFIG_EXCLUDES = String.join(",",
			"org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration",
			"org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration",
			"org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration",
			"org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration",
			"org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration");

	@Container
	static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
			.withDatabaseName("hotel_test")
			.withUsername("test")
			.withPassword("test");

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private HotelRepository hotelRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private AuditLogRepository auditLogRepository;

	@DynamicPropertySource
	static void registerProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", mysql::getJdbcUrl);
		registry.add("spring.datasource.username", mysql::getUsername);
		registry.add("spring.datasource.password", mysql::getPassword);
		registry.add("spring.cache.type", () -> "simple");
		registry.add("app.security.enabled", () -> "false");
		registry.add("spring.autoconfigure.exclude", () -> TEST_AUTO_CONFIG_EXCLUDES);
	}

	@Test
	void bookingHappyPath() {
		User user = new User();
		user.setUsername("integration-user");
		user.setEmail("integration-user@example.com");
		user.setPassword("password");
		user.setRole("NORMAL");
		User savedUser = userRepository.save(user);

		Hotel hotel = new Hotel();
		hotel.setName("Integration Grand Hotel");
		hotel.setRating(5L);
		hotel.setCity("Bengaluru");
		hotel.setDiscount(0.0);
		Hotel savedHotel = hotelRepository.save(hotel);

		Room room = new Room();
		room.setRoomNumber("101");
		room.setRoomType("Deluxe");
		room.setCapacity(2);
		room.setNightlyRate(BigDecimal.valueOf(4500));
		room.setStatus(RoomStatus.AVAILABLE);
		room.setHotel(savedHotel);
		Room savedRoom = roomRepository.save(room);

		LocalDate checkInDate = LocalDate.now().plusDays(1);
		LocalDate checkOutDate = LocalDate.now().plusDays(3);
		BookingRequest bookingRequest = new BookingRequest(
				savedHotel.getId(),
				savedRoom.getId(),
				savedUser.getId(),
				checkInDate,
				checkOutDate,
				2,
				"Late arrival");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<BookingResponse> response = restTemplate.exchange(
				"/hotel/bookings/create",
				HttpMethod.POST,
				new HttpEntity<>(bookingRequest, headers),
				BookingResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(BookingStatus.CONFIRMED, response.getBody().getStatus());
		assertEquals(1L, bookingRepository.count());

		List<AuditLog> auditLogs = auditLogRepository.findAllByOrderByCreatedAtDesc();
		assertFalse(auditLogs.isEmpty());
		assertTrue(auditLogs.stream().anyMatch(entry -> entry.getAction() != null && entry.getAction().contains("BOOKING")));
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class TestAuthenticationConfiguration {

		@Bean
		FilterRegistrationBean<org.springframework.web.filter.OncePerRequestFilter> testAuthenticationFilter() {
			org.springframework.web.filter.OncePerRequestFilter filter = new org.springframework.web.filter.OncePerRequestFilter() {
				@Override
				protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
						FilterChain filterChain) throws ServletException, IOException {
					Authentication authentication = new UsernamePasswordAuthenticationToken(
							"integration-user",
							"N/A",
							List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

					var securityContext = SecurityContextHolder.createEmptyContext();
					securityContext.setAuthentication(authentication);
					SecurityContextHolder.setContext(securityContext);

					HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
						@Override
						public Principal getUserPrincipal() {
							return authentication;
						}

						@Override
						public String getRemoteUser() {
							return authentication.getName();
						}

						@Override
						public boolean isUserInRole(String role) {
							String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
							return authentication.getAuthorities().stream()
									.anyMatch(authority -> authority.getAuthority().equals(normalizedRole));
						}
					};

					try {
						filterChain.doFilter(wrappedRequest, response);
					} finally {
						SecurityContextHolder.clearContext();
					}
				}
			};

			FilterRegistrationBean<org.springframework.web.filter.OncePerRequestFilter> registrationBean = new FilterRegistrationBean<>(
					filter);
			registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
			return registrationBean;
		}
	}
}
