package com.cn.hotelDemo.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.ServletException;

class RateLimitingFilterTest {

	private RateLimitingFilter rateLimitingFilter;

	@BeforeEach
	void setUp() {
		rateLimitingFilter = new RateLimitingFilter();
		SecurityContextHolder.clearContext();
	}

	@Test
	void bookingCreate_underLimit_passes() throws ServletException, IOException {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/hotel/bookings/create");
		request.setRemoteAddr("127.0.0.1");
		MockHttpServletResponse response = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();

		rateLimitingFilter.doFilter(request, response, filterChain);

		assertEquals(200, response.getStatus());
	}

	@Test
	void bookingCreate_exceedingLimit_returns429() throws ServletException, IOException {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken("spammerUser", "pass"));

		// 20 requests should pass
		for (int i = 0; i < 20; i++) {
			MockHttpServletRequest request = new MockHttpServletRequest("POST", "/hotel/bookings/create");
			MockHttpServletResponse response = new MockHttpServletResponse();
			MockFilterChain filterChain = new MockFilterChain();
			rateLimitingFilter.doFilter(request, response, filterChain);
			assertEquals(200, response.getStatus());
		}

		// 21st request should be rejected with 429
		MockHttpServletRequest rejectedRequest = new MockHttpServletRequest("POST", "/hotel/bookings/create");
		MockHttpServletResponse rejectedResponse = new MockHttpServletResponse();
		MockFilterChain filterChain = new MockFilterChain();
		rateLimitingFilter.doFilter(rejectedRequest, rejectedResponse, filterChain);

		assertEquals(429, rejectedResponse.getStatus());
		assertEquals("{\"error\":\"Too many booking requests. Limit: 20 per minute.\"}",
				rejectedResponse.getContentAsString());
	}
}
