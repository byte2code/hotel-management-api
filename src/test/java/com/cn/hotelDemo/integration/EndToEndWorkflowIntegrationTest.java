package com.cn.hotelDemo.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.cn.hotelDemo.dto.BookingRequest;
import com.cn.hotelDemo.dto.BookingResponse;
import com.cn.hotelDemo.dto.HotelRequest;
import com.cn.hotelDemo.dto.RoomRequest;
import com.cn.hotelDemo.dto.UserRequest;
import com.cn.hotelDemo.metrics.BookingMetrics;
import com.cn.hotelDemo.model.AuditLog;
import com.cn.hotelDemo.model.Booking;
import com.cn.hotelDemo.model.BookingStatus;
import com.cn.hotelDemo.model.Hotel;
import com.cn.hotelDemo.model.Room;
import com.cn.hotelDemo.model.RoomStatus;
import com.cn.hotelDemo.model.RoomType;
import com.cn.hotelDemo.model.User;
import com.cn.hotelDemo.repository.AuditLogRepository;
import com.cn.hotelDemo.repository.BookingRepository;
import com.cn.hotelDemo.repository.HotelRepository;
import com.cn.hotelDemo.repository.RoomRepository;
import com.cn.hotelDemo.repository.UserRepository;
import com.cn.hotelDemo.service.AuditService;
import com.cn.hotelDemo.service.BookingService;
import com.cn.hotelDemo.service.HotelService;
import com.cn.hotelDemo.service.RoomService;
import com.cn.hotelDemo.service.UserService;

import io.micrometer.core.instrument.MeterRegistry;

@SpringBootTest
class EndToEndWorkflowIntegrationTest {

	@Autowired
	private HotelService hotelService;

	@Autowired
	private RoomService roomService;

	@Autowired
	private UserService userService;

	@Autowired
	private BookingService bookingService;

	@Autowired
	private AuditService auditService;

	@Autowired
	private BookingMetrics bookingMetrics;

	@Autowired
	private MeterRegistry meterRegistry;

	@Autowired
	private HotelRepository hotelRepository;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private AuditLogRepository auditLogRepository;

	@MockBean
	private SimpMessagingTemplate messagingTemplate;

	@BeforeEach
	void cleanUp() {
		bookingRepository.deleteAll();
		roomRepository.deleteAll();
		hotelRepository.deleteAll();
		userRepository.deleteAll();
		auditLogRepository.deleteAll();
	}

	@Test
	@DisplayName("Full End-to-End Workflow: Hotel, Room, User, Booking Request -> Overlap Rejection -> Confirm -> Cancel -> Metrics")
	void completeBookingLifecycleWorkflow() {
		// 1. Create User
		UserRequest userReq = new UserRequest("john_doe", "password123", "NORMAL", "john@example.com");
		userService.createUser(userReq);
		User user = userRepository.findByEmail("john@example.com");
		assertNotNull(user);
		assertNotNull(user.getId());
		assertEquals("NORMAL", user.getRole());

		// 2. Create Hotel with enriched metadata
		HotelRequest hotelReq = HotelRequest.builder()
				.name("The Oberoi Grand")
				.rating(5L)
				.city("Kolkata")
				.discount(10.0)
				.address("15 Jawaharlal Nehru Road")
				.state("West Bengal")
				.country("India")
				.phoneNumber("+919876543210")
				.email("oberoi.kolkata@example.com")
				.website("https://www.oberoihotels.com")
				.description("Heritage luxury hotel")
				.totalRooms(200)
				.starCategory("5-star")
				.build();

		hotelService.createHotel(hotelReq);
		List<Hotel> hotels = hotelRepository.findByCityIgnoreCase("Kolkata");
		assertEquals(1, hotels.size());
		Hotel hotel = hotels.get(0);
		assertEquals("Kolkata", hotel.getCity());
		assertEquals("India", hotel.getCountry());
		assertEquals("5-star", hotel.getStarCategory());
		assertEquals(10.0, hotel.getDiscount());

		// 3. Search Hotel
		List<Hotel> cityHotels = hotelService.getHotelsByCity("Kolkata");
		assertEquals(1, cityHotels.size());
		List<Hotel> searchHotels = hotelService.searchHotels("Grand", "Kolkata", null);
		assertEquals(1, searchHotels.size());

		// 4. Create Room with RoomType
		RoomRequest roomReq = RoomRequest.builder()
				.hotelId(hotel.getId())
				.roomNumber("PREMIER-401")
				.roomType("DELUXE")
				.capacity(2)
				.nightlyRate(new BigDecimal("8000.00"))
				.status(RoomStatus.AVAILABLE)
				.build();

		roomService.createRoom(roomReq);
		Room room = roomRepository.findByHotelId(hotel.getId()).get(0);
		assertEquals(RoomType.DELUXE, room.getRoomType());
		assertEquals(RoomStatus.AVAILABLE, room.getStatus());

		// 5. Paginated retrieval
		Page<Hotel> hotelPage = hotelService.getAllHotels(PageRequest.of(0, 10));
		assertEquals(1, hotelPage.getTotalElements());

		Page<Room> roomPage = roomService.getAllRooms(PageRequest.of(0, 10));
		assertEquals(1, roomPage.getTotalElements());

		// Record initial metric counts
		double initialCreated = bookingMetrics.getCreatedCount();
		double initialConfirmed = bookingMetrics.getConfirmedCount();
		double initialRejected = bookingMetrics.getRejectedCount();
		double initialCancelled = bookingMetrics.getCancelledCount();

		// 6. Workflow Step 1: Request a Booking
		LocalDate checkIn = LocalDate.now().plusDays(2);
		LocalDate checkOut = LocalDate.now().plusDays(5); // 3 nights
		BookingRequest bookingReq = new BookingRequest(hotel.getId(), room.getId(), user.getId(), checkIn, checkOut, 2, "High floor");
		BookingResponse bookResp = bookingService.createBooking(bookingReq);

		assertNotNull(bookResp);
		assertNotNull(bookResp.getBookingReference());
		assertEquals(BookingStatus.REQUESTED, bookResp.getStatus());
		assertTrue(bookResp.getMessage().contains("requested"));
		// Price calculation: 8000 * 3 nights = 24000. 10% discount = 21600.00
		assertEquals(new BigDecimal("21600.00"), bookResp.getTotalPrice());
		assertEquals(initialCreated + 1, bookingMetrics.getCreatedCount());

		Long bookingId = bookResp.getBookingId();

		// 7. Workflow Step 2: Prevent Double Booking on Overlapping Dates
		BookingRequest overlapReq = new BookingRequest(hotel.getId(), room.getId(), user.getId(),
				LocalDate.now().plusDays(3), LocalDate.now().plusDays(6), 2, "Second booking");
		BookingResponse overlapResp = bookingService.createBooking(overlapReq);

		assertEquals(BookingStatus.REJECTED, overlapResp.getStatus());
		assertTrue(overlapResp.getMessage().contains("requested date range"));
		assertEquals(initialRejected + 1, bookingMetrics.getRejectedCount());

		// 8. Workflow Step 3: Admin Confirms Booking
		BookingResponse confirmResp = bookingService.confirmBooking(bookingId);
		assertEquals(BookingStatus.CONFIRMED, confirmResp.getStatus());
		assertTrue(confirmResp.getMessage().contains("confirmed"));
		assertEquals(initialConfirmed + 1, bookingMetrics.getConfirmedCount());

		// Verify cannot confirm an already confirmed booking
		assertThrows(IllegalArgumentException.class, () -> bookingService.confirmBooking(bookingId));

		// 9. Workflow Step 4: Paginated Bookings Verification
		Page<Booking> userBookingsPage = bookingService.getBookingsByUserId(user.getId(), PageRequest.of(0, 10));
		assertEquals(2, userBookingsPage.getTotalElements()); // 1 REQUESTED->CONFIRMED, 1 REJECTED

		Page<Booking> hotelBookingsPage = bookingService.getBookingsByHotelId(hotel.getId(), PageRequest.of(0, 10));
		assertEquals(2, hotelBookingsPage.getTotalElements());

		// 10. Workflow Step 5: Cancel Booking
		BookingResponse cancelResp = bookingService.cancelBooking(bookingId);
		assertEquals(BookingStatus.CANCELLED, cancelResp.getStatus());
		assertEquals(initialCancelled + 1, bookingMetrics.getCancelledCount());

		// 11. Workflow Step 6: Admin Rejection Flow with Reason
		BookingRequest booking2Req = new BookingRequest(hotel.getId(), room.getId(), user.getId(),
				LocalDate.now().plusDays(10), LocalDate.now().plusDays(12), 1, "Quiet room");
		BookingResponse book2Resp = bookingService.createBooking(booking2Req);
		assertEquals(BookingStatus.REQUESTED, book2Resp.getStatus());

		BookingResponse rejectResp = bookingService.rejectBooking(book2Resp.getBookingId(), "Card authorization failed");
		assertEquals(BookingStatus.REJECTED, rejectResp.getStatus());
		assertTrue(rejectResp.getMessage().contains("Card authorization failed"));

		Booking rejectedBooking = bookingService.getBookingById(book2Resp.getBookingId());
		assertEquals("Card authorization failed", rejectedBooking.getRejectionReason());

		// 12. Observability Check: Micrometer Meters
		assertNotNull(meterRegistry.find("hotel.bookings.created").counter());
		assertNotNull(meterRegistry.find("hotel.bookings.confirmed").counter());
		assertNotNull(meterRegistry.find("hotel.bookings.cancelled").counter());
		assertNotNull(meterRegistry.find("hotel.bookings.rejected").counter());
		assertNotNull(meterRegistry.find("hotel.bookings.active").gauge());

		// 13. Audit Logging
		auditService.record("FULL_WORKFLOW_TEST", "TEST_ADMIN", "HOTEL", String.valueOf(hotel.getId()), "SUCCESS", "All workflow assertions passed");
		List<AuditLog> logs = auditLogRepository.findAll();
		assertTrue(logs.stream().anyMatch(l -> "FULL_WORKFLOW_TEST".equals(l.getAction())));
	}
}
