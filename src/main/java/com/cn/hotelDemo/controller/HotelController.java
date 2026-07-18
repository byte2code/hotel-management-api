package com.cn.hotelDemo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

	@Autowired 
	HotelService hotelService;
	
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
	@Operation(summary = "Get all hotels (Admin only)")
	public List<Hotel> getAllHotels()
	{
		return hotelService.getAllHotels();
	}
	
	@DeleteMapping("/remove/id/{id}")
	@PreAuthorize("hasRole('admin') or hasAuthority('admin')")
	@Operation(summary = "Delete a hotel by ID (Admin only)")
	@AuditLogged(action = "HOTEL_DELETED", resourceType = "HOTEL", resourceIdSpel = "#id")
	public void deleteHotelById(@PathVariable Long id, Authentication authentication)
	{
		hotelService.deleteHotelById(id);
	}
}
