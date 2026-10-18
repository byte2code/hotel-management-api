package com.cn.hotelDemo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.cn.hotelDemo.model.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailResponse {
	private Long id;
	private String bookingReference;
	private LocalDate checkInDate;
	private LocalDate checkOutDate;
	private Integer guestCount;
	private String specialRequests;
	private BigDecimal totalPrice;
	private BookingStatus status;
	private String rejectionReason;

	private Long hotelId;
	private String hotelName;

	private Long roomId;
	private String roomNumber;

	private Long userId;
	private String username;
	private String userEmail;
}
