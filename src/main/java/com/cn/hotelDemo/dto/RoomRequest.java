package com.cn.hotelDemo.dto;

import java.math.BigDecimal;

import com.cn.hotelDemo.model.RoomStatus;
import com.cn.hotelDemo.model.RoomType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomRequest {

	@NotNull
	private Long hotelId;

	@NotBlank
	private String roomNumber;

	@NotBlank
	private String roomType;

	@NotNull
	@Min(1)
	private Integer capacity;

	@NotNull
	@Positive
	private BigDecimal nightlyRate;

	@Builder.Default
	private RoomStatus status = RoomStatus.AVAILABLE;

	public void setRoomType(RoomType type) {
		this.roomType = type != null ? type.name() : null;
	}

	public void setRoomType(String type) {
		this.roomType = type;
	}
}
