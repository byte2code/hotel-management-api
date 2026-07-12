package com.cn.hotelDemo.integration;

import com.cn.hotelDemo.dto.HotelRequest;
import com.cn.hotelDemo.model.Hotel;
import com.cn.hotelDemo.repository.HotelRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "app.security.enabled=true",
    "spring.autoconfigure.exclude=",
    "app.security.jwt-issuer-uri=https://accounts.google.com"
})
class HotelIntegrationTest extends BaseIntegrationTest {

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HotelRepository hotelRepository;

    @BeforeEach
    void cleanDb() {
        hotelRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateHotelSuccessfully() throws Exception {
        HotelRequest request = new HotelRequest("Grand Plaza", 5L, "New York", 10.0);

        mockMvc.perform(post("/hotel/create")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertEquals(1, hotelRepository.count());
        Hotel savedHotel = hotelRepository.findAll().get(0);
        assertEquals("Grand Plaza", savedHotel.getName());
        assertEquals("New York", savedHotel.getCity());
        assertEquals(5L, savedHotel.getRating());
    }
}
