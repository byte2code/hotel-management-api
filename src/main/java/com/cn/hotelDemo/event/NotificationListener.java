package com.cn.hotelDemo.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationListener.class);

    @EventListener
    public void handleBookingConfirmedEvent(BookingConfirmedEvent event) {
        logger.info("Notification Stub: Send email to user {} for confirmed booking {}", 
            event.getBooking().getUser().getEmail(), 
            event.getBooking().getBookingReference());
    }

    @EventListener
    public void handleBookingCancelledEvent(BookingCancelledEvent event) {
        logger.info("Notification Stub: Send email to user {} for cancelled booking {}", 
            event.getBooking().getUser().getEmail(), 
            event.getBooking().getBookingReference());
    }
}
