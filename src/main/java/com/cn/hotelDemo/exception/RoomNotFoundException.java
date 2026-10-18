package com.cn.hotelDemo.exception;

public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(Long id) {
        super("Room not found with ID: " + id);
    }

    public RoomNotFoundException(String message) {
        super(message);
    }
}
