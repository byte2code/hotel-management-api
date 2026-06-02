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

import com.cn.hotelDemo.dto.HotelRequest;
import com.cn.hotelDemo.model.Hotel;
import com.cn.hotelDemo.service.HotelService;
import com.cn.hotelDemo.service.AuditService;

@RestController
@RequestMapping("/hotel")
public class HotelController {

	@Autowired 
	HotelService hotelService;

	@Autowired
	AuditService auditService;
	
	@GetMapping("/userDetail")
	public String getDetails(@AuthenticationPrincipal OidcUser oidcUser) {
		String details = "User name: %s, email: %s".formatted(oidcUser.getFullName(), oidcUser.getEmail());
		auditService.record("OIDC_PROFILE_VIEWED", oidcUser.getEmail(), "AUTH", oidcUser.getEmail(), "SUCCESS",
				"Authenticated profile details were requested");
		return details;
	}
	
	@PostMapping("/create")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	public void createHotel(@RequestBody HotelRequest hotelRequest, Authentication authentication)
	{
		Hotel createdHotel = hotelService.createHotel(hotelRequest);
		auditService.record("HOTEL_CREATED", authentication.getName(), "HOTEL", String.valueOf(createdHotel.getId()),
				"SUCCESS", "Hotel created with name=%s, city=%s".formatted(createdHotel.getName(),
						createdHotel.getCity()));
	} 
	
	@GetMapping("/id/{id}")
	@PreAuthorize("hasRole('NORMAL') or hasAuthority('normal')")
	public Hotel getHotelById(@PathVariable Long id)
	{
		return hotelService.getHotelById(id);
	}
	
	@GetMapping("/getAll")
	@PreAuthorize("hasRole('ADMIN') or hasAuthority('admin')")
	public List<Hotel> getAllHotels()
	{
		return hotelService.getAllHotels();
	}
	
	@DeleteMapping("/remove/id/{id}")
	@PreAuthorize("hasRole('admin') or hasAuthority('admin')")
	public void deleteHotelById(@PathVariable Long id, Authentication authentication)
	{
		hotelService.deleteHotelById(id);
		auditService.record("HOTEL_DELETED", authentication.getName(), "HOTEL", String.valueOf(id), "SUCCESS",
				"Hotel delete requested for id=%s".formatted(id));
		
	}
}
