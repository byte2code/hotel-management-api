package com.cn.hotelDemo.dto;

import java.math.BigDecimal;
import com.cn.hotelDemo.model.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

	private String bookingReference;
	private BookingStatus status;
	private String message;
	private Long bookingId;
	private BigDecimal totalPrice;
}
