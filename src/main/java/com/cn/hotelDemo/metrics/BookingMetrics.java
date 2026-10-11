package com.cn.hotelDemo.metrics;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

@Component
public class BookingMetrics {

	private final Counter bookingsCreated;
	private final Counter bookingsConfirmed;
	private final Counter bookingsCancelled;
	private final Counter bookingsRejected;
	private final AtomicInteger activeBookings;

	public BookingMetrics(MeterRegistry registry) {
		this.bookingsCreated = Counter.builder("hotel.bookings.created")
				.description("Total bookings created")
				.register(registry);

		this.bookingsConfirmed = Counter.builder("hotel.bookings.confirmed")
				.description("Total bookings confirmed")
				.register(registry);

		this.bookingsCancelled = Counter.builder("hotel.bookings.cancelled")
				.description("Total bookings cancelled")
				.register(registry);

		this.bookingsRejected = Counter.builder("hotel.bookings.rejected")
				.description("Total bookings rejected")
				.register(registry);

		this.activeBookings = registry.gauge("hotel.bookings.active", new AtomicInteger(0));
	}

	public void incrementCreated() {
		bookingsCreated.increment();
		if (activeBookings != null) {
			activeBookings.incrementAndGet();
		}
	}

	public void incrementConfirmed() {
		bookingsConfirmed.increment();
	}

	public void incrementCancelled() {
		bookingsCancelled.increment();
		if (activeBookings != null && activeBookings.get() > 0) {
			activeBookings.decrementAndGet();
		}
	}

	public void incrementRejected() {
		bookingsRejected.increment();
	}

	public void decrementActive() {
		if (activeBookings != null && activeBookings.get() > 0) {
			activeBookings.decrementAndGet();
		}
	}

	public double getCreatedCount() {
		return bookingsCreated.count();
	}

	public double getConfirmedCount() {
		return bookingsConfirmed.count();
	}

	public double getCancelledCount() {
		return bookingsCancelled.count();
	}

	public double getRejectedCount() {
		return bookingsRejected.count();
	}

	public int getActiveCount() {
		return activeBookings != null ? activeBookings.get() : 0;
	}
}
