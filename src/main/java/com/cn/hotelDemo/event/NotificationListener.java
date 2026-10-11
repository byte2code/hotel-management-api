package com.cn.hotelDemo.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.cn.hotelDemo.model.Booking;
import com.cn.hotelDemo.model.BookingStatus;
import com.cn.hotelDemo.service.EmailService;

@Component
public class NotificationListener {

	private static final Logger logger = LoggerFactory.getLogger(NotificationListener.class);

	private final EmailService emailService;

	public NotificationListener(EmailService emailService) {
		this.emailService = emailService;
	}

	@EventListener
	@Async("emailTaskExecutor")
	public void handleBookingConfirmedEvent(BookingConfirmedEvent event) {
		Booking booking = event.getBooking();
		logger.info("Processing BookingConfirmedEvent for booking {}", booking.getBookingReference());
		emailService.sendBookingConfirmation(
				booking.getUser() != null ? booking.getUser().getEmail() : null,
				booking.getBookingReference(),
				booking.getCheckInDate(),
				booking.getCheckOutDate(),
				booking.getHotel() != null ? booking.getHotel().getName() : "Hotel",
				booking.getTotalPrice()
		);
	}

	@EventListener
	@Async("emailTaskExecutor")
	public void handleBookingCancelledEvent(BookingCancelledEvent event) {
		Booking booking = event.getBooking();
		logger.info("Processing BookingCancelledEvent for booking {} with status {}",
				booking.getBookingReference(), booking.getStatus());
		String userEmail = booking.getUser() != null ? booking.getUser().getEmail() : null;
		String hotelName = booking.getHotel() != null ? booking.getHotel().getName() : "Hotel";

		if (booking.getStatus() == BookingStatus.REJECTED) {
			emailService.sendBookingRejection(userEmail, booking.getBookingReference(), hotelName,
					booking.getRejectionReason());
		} else {
			emailService.sendBookingCancellation(userEmail, booking.getBookingReference(), hotelName);
		}
	}
}
