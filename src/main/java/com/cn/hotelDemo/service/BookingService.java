package com.cn.hotelDemo.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.cn.hotelDemo.dto.BookingNotification;
import com.cn.hotelDemo.dto.BookingRequest;
import com.cn.hotelDemo.dto.BookingResponse;
import com.cn.hotelDemo.event.BookingCancelledEvent;
import com.cn.hotelDemo.event.BookingConfirmedEvent;
import com.cn.hotelDemo.metrics.BookingMetrics;
import com.cn.hotelDemo.model.Booking;
import com.cn.hotelDemo.model.BookingStatus;
import com.cn.hotelDemo.model.Hotel;
import com.cn.hotelDemo.model.Room;
import com.cn.hotelDemo.model.RoomStatus;
import com.cn.hotelDemo.model.User;
import com.cn.hotelDemo.repository.BookingRepository;
import com.cn.hotelDemo.repository.HotelRepository;
import com.cn.hotelDemo.repository.RoomRepository;
import com.cn.hotelDemo.repository.UserRepository;

@Service
public class BookingService {

	private final BookingRepository bookingRepository;
	private final UserRepository userRepository;
	private final RoomRepository roomRepository;
	private final HotelRepository hotelRepository;
	private final SimpMessagingTemplate messagingTemplate;
	private final ApplicationEventPublisher eventPublisher;
	private final BookingMetrics bookingMetrics;

	@Autowired
	public BookingService(BookingRepository bookingRepository, UserRepository userRepository,
			RoomRepository roomRepository, HotelRepository hotelRepository, SimpMessagingTemplate messagingTemplate,
			ApplicationEventPublisher eventPublisher, BookingMetrics bookingMetrics) {
		this.bookingRepository = bookingRepository;
		this.userRepository = userRepository;
		this.roomRepository = roomRepository;
		this.hotelRepository = hotelRepository;
		this.messagingTemplate = messagingTemplate;
		this.eventPublisher = eventPublisher;
		this.bookingMetrics = bookingMetrics;
	}

	public BookingService(BookingRepository bookingRepository, UserRepository userRepository,
			RoomRepository roomRepository, HotelRepository hotelRepository, SimpMessagingTemplate messagingTemplate,
			ApplicationEventPublisher eventPublisher) {
		this(bookingRepository, userRepository, roomRepository, hotelRepository, messagingTemplate, eventPublisher, null);
	}

	@Transactional
	@CacheEvict(cacheNames = "roomAvailability", allEntries = true)
	public BookingResponse createBooking(BookingRequest bookingRequest) {
		MDC.put("userId", String.valueOf(bookingRequest.getUserId()));
		MDC.put("hotelId", String.valueOf(bookingRequest.getHotelId()));
		try {
			User user = userRepository.findById(bookingRequest.getUserId())
					.orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + bookingRequest.getUserId()));
			Room room = roomRepository.findByIdForUpdate(bookingRequest.getRoomId())
					.orElseThrow(() -> new IllegalArgumentException("Room not found with ID: " + bookingRequest.getRoomId()));
			Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId())
					.orElseThrow(() -> new IllegalArgumentException("Hotel not found with ID: " + bookingRequest.getHotelId()));

			if (bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())) {
				throw new IllegalArgumentException("Check-out date must be on or after the check-in date");
			}
			if (room.getHotel() == null || !room.getHotel().getId().equals(hotel.getId())) {
				throw new IllegalArgumentException("Room does not belong to the selected hotel");
			}
			if (room.getStatus() != RoomStatus.AVAILABLE) {
				if (bookingMetrics != null) {
					bookingMetrics.incrementRejected();
				}
				return persistBooking(bookingRequest, user, room, hotel, BookingStatus.REJECTED,
						"Room is not available for booking");
			}

			List<Booking> overlappingBookings = bookingRepository.findByRoomIdAndStatusIn(room.getId(),
					List.of(BookingStatus.CONFIRMED, BookingStatus.REQUESTED));
			boolean hasOverlap = overlappingBookings.stream().anyMatch(existing -> overlaps(existing.getCheckInDate(),
					existing.getCheckOutDate(), bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate()));
			if (hasOverlap) {
				if (bookingMetrics != null) {
					bookingMetrics.incrementRejected();
				}
				return persistBooking(bookingRequest, user, room, hotel, BookingStatus.REJECTED,
						"Room already has a booking in the requested date range");
			}

			if (bookingMetrics != null) {
				bookingMetrics.incrementCreated();
			}
			return persistBooking(bookingRequest, user, room, hotel, BookingStatus.REQUESTED,
					"Booking requested, awaiting confirmation");
		} finally {
			MDC.remove("userId");
			MDC.remove("hotelId");
			MDC.remove("bookingRef");
		}
	}

	public List<Booking> getAllBookings() {
		return bookingRepository.findAll();
	}

	public org.springframework.data.domain.Page<Booking> getAllBookings(org.springframework.data.domain.Pageable pageable) {
		return bookingRepository.findAll(pageable);
	}

	public Booking getBookingById(Long id) {
		return bookingRepository.findById(id).orElseThrow(() -> new com.cn.hotelDemo.exception.BookingNotFoundException(id));
	}

	public List<Booking> getBookingsByUserId(Long userId) {
		return bookingRepository.findByUserId(userId);
	}

	public org.springframework.data.domain.Page<Booking> getBookingsByUserId(Long userId, org.springframework.data.domain.Pageable pageable) {
		return bookingRepository.findByUserId(userId, pageable);
	}

	public List<Booking> getBookingsByHotelId(Long hotelId) {
		return bookingRepository.findByHotelId(hotelId);
	}

	public org.springframework.data.domain.Page<Booking> getBookingsByHotelId(Long hotelId, org.springframework.data.domain.Pageable pageable) {
		return bookingRepository.findByHotelId(hotelId, pageable);
	}

	@Transactional
	@CacheEvict(cacheNames = "roomAvailability", allEntries = true)
	public BookingResponse confirmBooking(Long id) {
		MDC.put("bookingId", String.valueOf(id));
		try {
			Booking booking = bookingRepository.findById(id)
					.orElseThrow(() -> new com.cn.hotelDemo.exception.BookingNotFoundException(id));
			if (booking.getStatus() != BookingStatus.REQUESTED) {
				throw new IllegalArgumentException(
						"Only REQUESTED bookings can be confirmed. Current status: " + booking.getStatus());
			}
			MDC.put("bookingRef", booking.getBookingReference());
			booking.setStatus(BookingStatus.CONFIRMED);
			bookingRepository.save(booking);
			if (bookingMetrics != null) {
				bookingMetrics.incrementConfirmed();
			}
			messagingTemplate.convertAndSend("/topic/bookings/" + booking.getHotel().getId(),
					new BookingNotification(booking.getBookingReference(), booking.getStatus().name(),
							"Booking confirmed by admin", booking.getHotel().getId(), booking.getRoom().getId()));
			eventPublisher.publishEvent(new BookingConfirmedEvent(this, booking));
			return new BookingResponse(booking.getBookingReference(), booking.getStatus(),
					"Booking confirmed by admin", booking.getId(), booking.getTotalPrice());
		} finally {
			MDC.remove("bookingId");
			MDC.remove("bookingRef");
		}
	}

	@Transactional
	@CacheEvict(cacheNames = "roomAvailability", allEntries = true)
	public BookingResponse rejectBooking(Long id, String reason) {
		MDC.put("bookingId", String.valueOf(id));
		try {
			Booking booking = bookingRepository.findById(id)
					.orElseThrow(() -> new com.cn.hotelDemo.exception.BookingNotFoundException(id));
			if (booking.getStatus() != BookingStatus.REQUESTED) {
				throw new IllegalArgumentException(
						"Only REQUESTED bookings can be rejected. Current status: " + booking.getStatus());
			}
			MDC.put("bookingRef", booking.getBookingReference());
			booking.setStatus(BookingStatus.REJECTED);
			booking.setRejectionReason(reason);
			bookingRepository.save(booking);
			if (bookingMetrics != null) {
				bookingMetrics.incrementRejected();
				bookingMetrics.decrementActive();
			}
			messagingTemplate.convertAndSend("/topic/bookings/" + booking.getHotel().getId(),
					new BookingNotification(booking.getBookingReference(), booking.getStatus().name(),
							"Booking rejected: " + reason, booking.getHotel().getId(), booking.getRoom().getId()));
			eventPublisher.publishEvent(new BookingCancelledEvent(this, booking));
			return new BookingResponse(booking.getBookingReference(), booking.getStatus(),
					"Booking rejected: " + reason, booking.getId(), booking.getTotalPrice());
		} finally {
			MDC.remove("bookingId");
			MDC.remove("bookingRef");
		}
	}

	@Transactional
	@CacheEvict(cacheNames = "roomAvailability", allEntries = true)
	public BookingResponse cancelBooking(Long id) {
		MDC.put("bookingId", String.valueOf(id));
		try {
			Booking booking = bookingRepository.findById(id)
					.orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + id));

			if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.REQUESTED) {
				throw new IllegalArgumentException(
						"Only REQUESTED or CONFIRMED bookings can be cancelled. Current status: " + booking.getStatus());
			}

			MDC.put("bookingRef", booking.getBookingReference());
			booking.setStatus(BookingStatus.CANCELLED);
			bookingRepository.save(booking);
			if (bookingMetrics != null) {
				bookingMetrics.incrementCancelled();
			}
			
			eventPublisher.publishEvent(new BookingCancelledEvent(this, booking));

			return new BookingResponse(booking.getBookingReference(), booking.getStatus(),
					"Booking cancelled successfully", booking.getId(), booking.getTotalPrice());
		} finally {
			MDC.remove("bookingId");
			MDC.remove("bookingRef");
		}
	}

	private BookingResponse persistBooking(BookingRequest bookingRequest, User user, Room room, Hotel hotel,
			BookingStatus status, String message) {
		Booking booking = new Booking();
		booking.setBookingReference("BOOK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
		booking.setUser(user);
		booking.setRoom(room);
		booking.setHotel(hotel);
		booking.setCheckInDate(bookingRequest.getCheckInDate());
		booking.setCheckOutDate(bookingRequest.getCheckOutDate());
		booking.setGuestCount(bookingRequest.getGuestCount());
		booking.setSpecialRequests(bookingRequest.getSpecialRequests());
		booking.setStatus(status);
		
		if ((status == BookingStatus.CONFIRMED || status == BookingStatus.REQUESTED) && room.getNightlyRate() != null) {
			long nights = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckOutDate());
			if (nights <= 0) nights = 1;
			BigDecimal basePrice = room.getNightlyRate().multiply(BigDecimal.valueOf(nights));
			if (hotel.getDiscount() != null && hotel.getDiscount() > 0) {
				BigDecimal discountFactor = BigDecimal.valueOf(100 - hotel.getDiscount()).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
				basePrice = basePrice.multiply(discountFactor);
			}
			booking.setTotalPrice(basePrice.setScale(2, RoundingMode.HALF_UP));
		}

		Booking savedBooking = bookingRepository.save(booking);
		MDC.put("bookingRef", savedBooking.getBookingReference());

		if (status == BookingStatus.CONFIRMED || status == BookingStatus.REJECTED || status == BookingStatus.REQUESTED) {
			messagingTemplate.convertAndSend("/topic/bookings/" + hotel.getId(),
					new BookingNotification(savedBooking.getBookingReference(), savedBooking.getStatus().name(),
							message, hotel.getId(), room.getId()));
		}
		
		if (status == BookingStatus.CONFIRMED) {
			eventPublisher.publishEvent(new BookingConfirmedEvent(this, savedBooking));
		}
		
		return new BookingResponse(savedBooking.getBookingReference(), savedBooking.getStatus(), message,
				savedBooking.getId(), savedBooking.getTotalPrice());
	}

	private boolean overlaps(LocalDate firstStart, LocalDate firstEnd, LocalDate secondStart, LocalDate secondEnd) {
		return !secondEnd.isBefore(firstStart) && !secondStart.isAfter(firstEnd);
	}
}
