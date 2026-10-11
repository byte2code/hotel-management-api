package com.cn.hotelDemo.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitingFilter extends OncePerRequestFilter {

	private final Map<String, Bucket> bookingBuckets = new ConcurrentHashMap<>();
	private final Map<String, Bucket> userRegistrationBuckets = new ConcurrentHashMap<>();
	private final Map<String, Bucket> roomAvailabilityBuckets = new ConcurrentHashMap<>();

	private Bucket createBookingBucket() {
		return Bucket.builder()
				.addLimit(Bandwidth.classic(20, Refill.greedy(20, Duration.ofMinutes(1))))
				.build();
	}

	private Bucket createUserRegistrationBucket() {
		return Bucket.builder()
				.addLimit(Bandwidth.classic(5, Refill.greedy(5, Duration.ofHours(1))))
				.build();
	}

	private Bucket createRoomAvailabilityBucket() {
		return Bucket.builder()
				.addLimit(Bandwidth.classic(60, Refill.greedy(60, Duration.ofMinutes(1))))
				.build();
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain filterChain) throws ServletException, IOException {

		String uri = request.getRequestURI();
		String method = request.getMethod();

		if (uri.contains("/hotel/bookings/create") && "POST".equalsIgnoreCase(method)) {
			String key = resolveClientIdentifier(request);
			Bucket bucket = bookingBuckets.computeIfAbsent(key, k -> createBookingBucket());
			if (!bucket.tryConsume(1)) {
				rejectTooManyRequests(response, "Too many booking requests. Limit: 20 per minute.");
				return;
			}
		} else if ((uri.contains("/user/createUser") || uri.equals("/user")) && "POST".equalsIgnoreCase(method)) {
			String ip = request.getRemoteAddr();
			Bucket bucket = userRegistrationBuckets.computeIfAbsent(ip, k -> createUserRegistrationBucket());
			if (!bucket.tryConsume(1)) {
				rejectTooManyRequests(response, "Too many user registration attempts. Limit: 5 per hour.");
				return;
			}
		} else if (uri.contains("/available") && "GET".equalsIgnoreCase(method)) {
			String key = resolveClientIdentifier(request);
			Bucket bucket = roomAvailabilityBuckets.computeIfAbsent(key, k -> createRoomAvailabilityBucket());
			if (!bucket.tryConsume(1)) {
				rejectTooManyRequests(response, "Too many availability queries. Limit: 60 per minute.");
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	private String resolveClientIdentifier(HttpServletRequest request) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
			return auth.getName();
		}
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isBlank()) {
			return xForwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private void rejectTooManyRequests(HttpServletResponse response, String message) throws IOException {
		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write("{\"error\":\"" + message + "\"}");
	}
}
