package com.cn.hotelDemo.dto;

import com.cn.hotelDemo.model.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

	private String bookingReference;
	private BookingStatus status;
	private String message;
	private Long bookingId;
}
