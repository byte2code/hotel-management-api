package com.cn.hotelDemo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelRequest {

	@NotBlank(message = "Hotel name is required", groups = com.cn.hotelDemo.dto.validation.OnCreate.class)
	private String name;

	@NotNull(message = "Rating is required", groups = com.cn.hotelDemo.dto.validation.OnCreate.class)
	@Min(value = 1, message = "Rating must be at least 1")
	private Long rating;

	@NotBlank(message = "City is required", groups = com.cn.hotelDemo.dto.validation.OnCreate.class)
	private String city;

	@Min(0)
	@Max(100)
	@Builder.Default
	private Double discount = 0.0;

	private String address;
	private String state;
	private String country;

	@Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Must be a valid phone number")
	private String phoneNumber;

	@Email(message = "Must be a valid email")
	private String email;

	private String website;

	@Size(max = 500, message = "Description cannot exceed 500 characters")
	private String description;

	private Integer totalRooms;

	@Pattern(regexp = "^[1-5]-star$|^boutique$", message = "Must be 1-star to 5-star or boutique")
	private String starCategory;

	public HotelRequest(String name, Long rating, String city, Double discount) {
		this.name = name;
		this.rating = rating;
		this.city = city;
		this.discount = discount;
	}
}