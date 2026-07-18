package com.cn.hotelDemo.exception;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(Long id) {
        super("Booking not found with ID: " + id);
    }
}
