package com.cn.hotelDemo.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingNotification {

	private String bookingReference;
	private String status;
	private String message;
	private Long hotelId;
	private Long roomId;
}
