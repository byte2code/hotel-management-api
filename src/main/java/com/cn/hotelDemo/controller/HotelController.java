package com.cn.hotelDemo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.cn.hotelDemo.dto.HotelRequest;
import com.cn.hotelDemo.model.Hotel;
import com.cn.hotelDemo.service.HotelService;
import com.cn.hotelDemo.annotation.AuditLogged;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/hotel")
@Tag(name = "Hotel Controller", description = "Endpoints for managing hotels")
@SecurityRequirement(name = "Bearer Authentication")
public class HotelController {

	private final HotelService hotelService;

	public HotelController(HotelService hotelService) {
		this.hotelService = hotelService;
	}
	
	@GetMapping("/userDetail")
	@Operation(summary = "Get current authenticated user details from OIDC")
	@AuditLogged(action = "OIDC_PROFILE_VIEWED", resourceType = "AUTH", resourceIdSpel = "#oidcUser.email")
	public String getDetails(@AuthenticationPrincipal OidcUser oidcUser) {
		return "User name: %s, email: %s".formatted(oidcUser.getFullName(), oidcUser.getEmail());
	}
	
	@PostMapping("/create")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	@Operation(summary = "Create a new hotel (Admin only)")
	@AuditLogged(action = "HOTEL_CREATED", resourceType = "HOTEL", resourceIdSpel = "#result.id")
	public Hotel createHotel(@Valid @RequestBody HotelRequest hotelRequest, Authentication authentication)
	{
		return hotelService.createHotel(hotelRequest);
	}
	
	@GetMapping("/id/{id}")
	@PreAuthorize("hasRole('NORMAL') or hasAuthority('normal')")
	@Operation(summary = "Get hotel by ID")
	public Hotel getHotelById(@PathVariable Long id)
	{
		return hotelService.getHotelById(id);
	}
	
	@GetMapping("/getAll")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	@Operation(summary = "Get all hotels with pagination (Admin only)")
	public ResponseEntity<org.springframework.data.domain.Page<Hotel>> getAllHotels(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			@RequestParam(defaultValue = "id") String sortBy,
			@RequestParam(defaultValue = "asc") String direction) {
		org.springframework.data.domain.Sort sort = direction.equalsIgnoreCase("desc")
				? org.springframework.data.domain.Sort.by(sortBy).descending()
				: org.springframework.data.domain.Sort.by(sortBy).ascending();
		org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
		return ResponseEntity.ok(hotelService.getAllHotels(pageable));
	}

	@PutMapping("/update/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	@Operation(summary = "Update an existing hotel (Admin only)")
	@AuditLogged(action = "HOTEL_UPDATED", resourceType = "HOTEL", resourceIdSpel = "#id")
	public Hotel updateHotel(@PathVariable Long id, @Valid @RequestBody HotelRequest hotelRequest) {
		return hotelService.updateHotel(id, hotelRequest);
	}

	@GetMapping("/city/{city}")
	@PreAuthorize("hasRole('NORMAL') or hasAuthority('normal')")
	@Operation(summary = "Get hotels by city")
	public List<Hotel> getHotelsByCity(@PathVariable String city) {
		return hotelService.getHotelsByCity(city);
	}

	@GetMapping("/country/{country}")
	@PreAuthorize("hasRole('NORMAL') or hasAuthority('normal')")
	@Operation(summary = "Get hotels by country")
	public List<Hotel> getHotelsByCountry(@PathVariable String country) {
		return hotelService.getHotelsByCountry(country);
	}

	@GetMapping("/search")
	@PreAuthorize("hasRole('NORMAL') or hasAuthority('normal')")
	@Operation(summary = "Search hotels by name, city, or minimum rating")
	public List<Hotel> searchHotels(
			@RequestParam(required = false) String name,
			@RequestParam(required = false) String city,
			@RequestParam(required = false) Long rating) {
		return hotelService.searchHotels(name, city, rating);
	}
	
	@DeleteMapping("/remove/id/{id}")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	@Operation(summary = "Delete a hotel by ID (Admin only)")
	@AuditLogged(action = "HOTEL_DELETED", resourceType = "HOTEL", resourceIdSpel = "#id")
	public void deleteHotelById(@PathVariable Long id, Authentication authentication)
	{
		hotelService.deleteHotelById(id);
	}
}
