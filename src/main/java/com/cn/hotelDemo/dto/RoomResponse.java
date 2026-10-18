package com.cn.hotelDemo.dto;

import java.math.BigDecimal;

import com.cn.hotelDemo.model.RoomStatus;
import com.cn.hotelDemo.model.RoomType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
	private Long id;
	private String roomNumber;
	private RoomType roomType;
	private Integer capacity;
	private BigDecimal nightlyRate;
	private RoomStatus status;
	private Long hotelId;
	private String hotelName;
}
