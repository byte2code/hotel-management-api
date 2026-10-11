package com.cn.hotelDemo.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "room")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"hotel", "bookings"})
public class Room {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String roomNumber;

	@Enumerated(EnumType.STRING)
	private RoomType roomType;

	private Integer capacity;
	private BigDecimal nightlyRate;

	@Enumerated(EnumType.STRING)
	@Builder.Default
	private RoomStatus status = RoomStatus.AVAILABLE;

	@ManyToOne
	@JoinColumn(name = "hotel_id")
	@JsonIgnoreProperties({"rooms"})
	private Hotel hotel;

	@OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnoreProperties({"room", "hotel", "user"})
	@Builder.Default
	private List<Booking> bookings = new ArrayList<>();

	public void setRoomType(String roomTypeStr) {
		if (roomTypeStr != null) {
			try {
				this.roomType = RoomType.valueOf(roomTypeStr.toUpperCase());
			} catch (IllegalArgumentException e) {
				this.roomType = RoomType.DELUXE;
			}
		} else {
			this.roomType = null;
		}
	}
}
