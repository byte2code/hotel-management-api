package com.cn.hotelDemo.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.cn.hotelDemo.model.*;
import com.cn.hotelDemo.repository.*;
import com.cn.hotelDemo.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "app.security.enabled=true",
    "spring.autoconfigure.exclude=",
    "app.security.jwt-issuer-uri=https://accounts.google.com"
})
class SoftDeleteIntegrationTest extends BaseIntegrationTest {

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingService bookingService;

    @BeforeEach
    void cleanDb() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldSoftDeleteHotelAndFilterFromGetAll() throws Exception {
        Hotel hotel = Hotel.builder()
                .name("Coastal Retreat")
                .city("Goa")
                .rating(4L)
                .discount(10.0)
                .build();
        hotel = hotelRepository.save(hotel);
        Long hotelId = hotel.getId();

        // 1. Verify it exists initially
        mockMvc.perform(get("/hotel/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + hotelId + ")]").exists());

        // 2. Soft delete the hotel
        mockMvc.perform(delete("/hotel/remove/id/" + hotelId).with(csrf()))
                .andExpect(status().isOk());

        // 3. Verify it no longer appears in normal getAll
        mockMvc.perform(get("/hotel/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + hotelId + ")]").doesNotExist());

        // 4. Verify getById returns 404
        mockMvc.perform(get("/hotel/id/" + hotelId))
                .andExpect(status().isNotFound());

        // 5. Verify it appears in /hotel/deleted
        mockMvc.perform(get("/hotel/deleted"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + hotelId + ")].deleted").value(true));

        // 6. Restore the hotel
        mockMvc.perform(patch("/hotel/restore/" + hotelId).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deleted").value(false));

        // 7. Verify it is visible again in getAll
        mockMvc.perform(get("/hotel/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + hotelId + ")]").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldKeepBookingReadableWhenHotelIsSoftDeleted() throws Exception {
        Hotel hotel = Hotel.builder().name("Mountain Lodge").city("Manali").rating(5L).build();
        hotel = hotelRepository.save(hotel);

        Room room = Room.builder()
                .hotel(hotel)
                .roomNumber("M-101")
                .roomType(RoomType.DELUXE)
                .capacity(2)
                .nightlyRate(new BigDecimal("3000.00"))
                .status(RoomStatus.AVAILABLE)
                .build();
        room = roomRepository.save(room);

        User user = User.builder()
                .username("traveler")
                .password("secret")
                .email("traveler@example.com")
                .role("NORMAL")
                .build();
        user = userRepository.save(user);

        Booking booking = Booking.builder()
                .bookingReference("BOOK-SD-001")
                .hotel(hotel)
                .room(room)
                .user(user)
                .checkInDate(LocalDate.now().plusDays(10))
                .checkOutDate(LocalDate.now().plusDays(15))
                .guestCount(2)
                .status(BookingStatus.CONFIRMED)
                .totalPrice(new BigDecimal("15000.00"))
                .build();
        booking = bookingRepository.save(booking);

        // Soft delete the hotel
        mockMvc.perform(delete("/hotel/remove/id/" + hotel.getId()).with(csrf()))
                .andExpect(status().isOk());

        // Verify booking can still be fetched without throwing an error
        mockMvc.perform(get("/hotel/bookings/id/" + booking.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingReference").value("BOOK-SD-001"));
    }
}
