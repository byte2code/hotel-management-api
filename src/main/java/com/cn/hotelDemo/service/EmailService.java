package com.cn.hotelDemo.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender mailSender;

	public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
		this.mailSender = mailSenderProvider.getIfAvailable();
	}

	public void sendBookingConfirmation(String toEmail, String bookingRef,
			LocalDate checkIn, LocalDate checkOut, String hotelName, BigDecimal totalPrice) {
		if (toEmail == null || toEmail.isBlank()) {
			logger.warn("Skipping booking confirmation email: recipient email is missing for booking {}", bookingRef);
			return;
		}
		String body = """
				Dear Guest,

				Your booking has been confirmed!

				Booking Reference : %s
				Hotel             : %s
				Check-in          : %s
				Check-out         : %s
				Total Price       : %s

				Thank you for choosing us!
				""".formatted(bookingRef, hotelName, checkIn, checkOut,
				totalPrice != null ? "$" + totalPrice : "N/A");

		sendEmail(toEmail, "Booking Confirmed — " + bookingRef, body);
	}

	public void sendBookingCancellation(String toEmail, String bookingRef, String hotelName) {
		if (toEmail == null || toEmail.isBlank()) {
			logger.warn("Skipping booking cancellation email: recipient email is missing for booking {}", bookingRef);
			return;
		}
		String body = """
				Dear Guest,

				Your booking %s at %s has been cancelled.

				If you did not request this cancellation or have questions, please contact our support team.
				""".formatted(bookingRef, hotelName);

		sendEmail(toEmail, "Booking Cancelled — " + bookingRef, body);
	}

	public void sendBookingRejection(String toEmail, String bookingRef, String hotelName, String reason) {
		if (toEmail == null || toEmail.isBlank()) {
			logger.warn("Skipping booking rejection email: recipient email is missing for booking {}", bookingRef);
			return;
		}
		String body = """
				Dear Guest,

				We regret to inform you that your booking request %s at %s could not be accepted.

				Reason: %s

				Please feel free to search for alternative dates or rooms on our platform.
				""".formatted(bookingRef, hotelName, reason != null ? reason : "Not specified");

		sendEmail(toEmail, "Booking Request Update — " + bookingRef, body);
	}

	private void sendEmail(String to, String subject, String text) {
		if (mailSender == null) {
			logger.info("Email service disabled (no JavaMailSender bean available). Simulated email to {}: [{}] {}", to, subject, text.replace('\n', ' '));
			return;
		}
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(to);
			message.setSubject(subject);
			message.setText(text);
			mailSender.send(message);
			logger.info("Email successfully sent to {} with subject [{}]", to, subject);
		} catch (Exception e) {
			logger.error("Failed to send email to {} with subject [{}]: {}", to, subject, e.getMessage(), e);
		}
	}
}
