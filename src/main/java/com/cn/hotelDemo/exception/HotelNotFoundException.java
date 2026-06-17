package com.cn.hotelDemo.exception;

public class HotelNotFoundException extends RuntimeException {
    public HotelNotFoundException(Long id) {
        super("Hotel not found with ID: " + id);
    }
}
