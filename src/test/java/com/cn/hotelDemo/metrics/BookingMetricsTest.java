package com.cn.hotelDemo.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class BookingMetricsTest {

	private MeterRegistry meterRegistry;
	private BookingMetrics bookingMetrics;

	@BeforeEach
	void setUp() {
		meterRegistry = new SimpleMeterRegistry();
		bookingMetrics = new BookingMetrics(meterRegistry);
	}

	@Test
	void initialMetrics_areZero() {
		assertEquals(0.0, bookingMetrics.getCreatedCount());
		assertEquals(0.0, bookingMetrics.getConfirmedCount());
		assertEquals(0.0, bookingMetrics.getCancelledCount());
		assertEquals(0.0, bookingMetrics.getRejectedCount());
		assertEquals(0, bookingMetrics.getActiveCount());
	}

	@Test
	void incrementCreated_updatesCounterAndGauge() {
		bookingMetrics.incrementCreated();

		assertEquals(1.0, bookingMetrics.getCreatedCount());
		assertEquals(1, bookingMetrics.getActiveCount());
		assertEquals(1.0, meterRegistry.get("hotel.bookings.created").counter().count());
		assertEquals(1.0, meterRegistry.get("hotel.bookings.active").gauge().value());
	}

	@Test
	void incrementConfirmed_updatesCounter() {
		bookingMetrics.incrementConfirmed();

		assertEquals(1.0, bookingMetrics.getConfirmedCount());
		assertEquals(1.0, meterRegistry.get("hotel.bookings.confirmed").counter().count());
	}

	@Test
	void incrementCancelled_updatesCounterAndDecrementsActiveGauge() {
		bookingMetrics.incrementCreated();
		assertEquals(1, bookingMetrics.getActiveCount());

		bookingMetrics.incrementCancelled();
		assertEquals(1.0, bookingMetrics.getCancelledCount());
		assertEquals(0, bookingMetrics.getActiveCount());
		assertEquals(1.0, meterRegistry.get("hotel.bookings.cancelled").counter().count());
		assertEquals(0.0, meterRegistry.get("hotel.bookings.active").gauge().value());
	}

	@Test
	void incrementRejected_andDecrementActive_updatesCounterAndGauge() {
		bookingMetrics.incrementCreated();
		assertEquals(1, bookingMetrics.getActiveCount());

		bookingMetrics.incrementRejected();
		bookingMetrics.decrementActive();

		assertEquals(1.0, bookingMetrics.getRejectedCount());
		assertEquals(0, bookingMetrics.getActiveCount());
		assertEquals(1.0, meterRegistry.get("hotel.bookings.rejected").counter().count());
	}

	@Test
	void decrementActive_whenAlreadyZero_doesNotBecomeNegative() {
		bookingMetrics.decrementActive();
		assertEquals(0, bookingMetrics.getActiveCount());

		bookingMetrics.incrementCancelled();
		assertEquals(0, bookingMetrics.getActiveCount());
	}
}
