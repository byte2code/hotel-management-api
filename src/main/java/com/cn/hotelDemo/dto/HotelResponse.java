package com.cn.hotelDemo.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelResponse {
	private Long id;
	private String name;
	private Long rating;
	private String city;
	private Double discount;
	private String address;
	private String state;
	private String country;
	private String phoneNumber;
	private String email;
	private String website;
	private String description;
	private Integer totalRooms;
	private String starCategory;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private List<RoomResponse> rooms;
}
