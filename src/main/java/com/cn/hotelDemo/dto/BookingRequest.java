package com.cn.hotelDemo.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

	@NotNull
	private Long hotelId;

	@NotNull
	private Long roomId;

	@NotNull
	private Long userId;

	@NotNull
	@FutureOrPresent
	private LocalDate checkInDate;

	@NotNull
	@FutureOrPresent
	private LocalDate checkOutDate;

	@NotNull
	@Min(1)
	private Integer guestCount;

	private String specialRequests;
}
