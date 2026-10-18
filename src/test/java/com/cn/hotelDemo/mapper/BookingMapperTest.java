package com.cn.hotelDemo.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.cn.hotelDemo.dto.BookingDetailResponse;
import com.cn.hotelDemo.dto.BookingResponse;
import com.cn.hotelDemo.model.Booking;
import com.cn.hotelDemo.model.BookingStatus;
import com.cn.hotelDemo.model.Hotel;
import com.cn.hotelDemo.model.Room;
import com.cn.hotelDemo.model.User;

class BookingMapperTest {

	private final BookingMapper bookingMapper = Mappers.getMapper(BookingMapper.class);

	@Test
	void toDetailResponse_mapsBookingWithNestedEntities() {
		Hotel hotel = Hotel.builder().id(1L).name("Hilton").build();
		Room room = Room.builder().id(2L).roomNumber("301").build();
		User user = User.builder().id(3L).username("charlie").email("charlie@test.com").build();

		Booking booking = Booking.builder()
				.id(100L)
				.bookingReference("REF-999")
				.checkInDate(LocalDate.of(2026, 11, 1))
				.checkOutDate(LocalDate.of(2026, 11, 5))
				.guestCount(2)
				.specialRequests("Sea view")
				.totalPrice(new BigDecimal("600.00"))
				.status(BookingStatus.CONFIRMED)
				.rejectionReason(null)
				.hotel(hotel)
				.room(room)
				.user(user)
				.build();

		BookingDetailResponse detail = bookingMapper.toDetailResponse(booking);

		assertNotNull(detail);
		assertEquals(100L, detail.getId());
		assertEquals("REF-999", detail.getBookingReference());
		assertEquals(BookingStatus.CONFIRMED, detail.getStatus());
		assertEquals(1L, detail.getHotelId());
		assertEquals("Hilton", detail.getHotelName());
		assertEquals(2L, detail.getRoomId());
		assertEquals("301", detail.getRoomNumber());
		assertEquals(3L, detail.getUserId());
		assertEquals("charlie", detail.getUsername());
		assertEquals("charlie@test.com", detail.getUserEmail());
		assertEquals(new BigDecimal("600.00"), detail.getTotalPrice());
	}

	@Test
	void toResponse_mapsBookingToBookingResponse() {
		Booking booking = Booking.builder()
				.id(55L)
				.bookingReference("REF-55")
				.status(BookingStatus.REQUESTED)
				.totalPrice(new BigDecimal("250.00"))
				.build();

		BookingResponse response = bookingMapper.toResponse(booking);

		assertNotNull(response);
		assertEquals(55L, response.getBookingId());
		assertEquals("REF-55", response.getBookingReference());
		assertEquals(BookingStatus.REQUESTED, response.getStatus());
		assertEquals(new BigDecimal("250.00"), response.getTotalPrice());
	}

	@Test
	void toDetailResponseList_mapsList() {
		Booking b1 = Booking.builder().id(1L).bookingReference("B1").build();
		Booking b2 = Booking.builder().id(2L).bookingReference("B2").build();

		List<BookingDetailResponse> list = bookingMapper.toDetailResponseList(List.of(b1, b2));

		assertEquals(2, list.size());
		assertEquals("B1", list.get(0).getBookingReference());
		assertEquals("B2", list.get(1).getBookingReference());
	}
}
