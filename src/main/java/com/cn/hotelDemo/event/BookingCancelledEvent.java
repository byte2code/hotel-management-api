package com.cn.hotelDemo.event;

import org.springframework.context.ApplicationEvent;
import com.cn.hotelDemo.model.Booking;

public class BookingCancelledEvent extends ApplicationEvent {
    private final Booking booking;

    public BookingCancelledEvent(Object source, Booking booking) {
        super(source);
        this.booking = booking;
    }

    public Booking getBooking() {
        return booking;
    }
}
