package com.cn.hotelDemo.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "booking")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"hotel", "room", "user"})
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String bookingReference;

	private LocalDate checkInDate;
	private LocalDate checkOutDate;
	private Integer guestCount;
	private String specialRequests;
	private BigDecimal totalPrice;

	@Enumerated(EnumType.STRING)
	@Builder.Default
	private BookingStatus status = BookingStatus.REQUESTED;

	@Column(length = 500)
	private String rejectionReason;

	@ManyToOne
	@JoinColumn(name = "hotel_id")
	@NotFound(action = NotFoundAction.IGNORE)
	@JsonIgnoreProperties({"rooms"})
	private Hotel hotel;

	@ManyToOne
	@JoinColumn(name = "room_id")
	@NotFound(action = NotFoundAction.IGNORE)
	@JsonIgnoreProperties({"hotel", "bookings"})
	private Room room;

	@ManyToOne
	@JoinColumn(name = "user_id")
	@JsonIgnoreProperties({"account", "subscriptionList"})
	private User user;
}
