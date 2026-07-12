package com.cn.hotelDemo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelRequest {

	@NotBlank(message = "Hotel name is required")
	private String name;

	@NotNull(message = "Rating is required")
	@Min(value = 1, message = "Rating must be at least 1")
	private Long rating;

	@NotBlank(message = "City is required")
	private String city;

	@Min(0)
	@Max(100)
	private Double discount = 0.0;
	
}