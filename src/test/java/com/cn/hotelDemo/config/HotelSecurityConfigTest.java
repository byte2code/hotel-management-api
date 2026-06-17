package com.cn.hotelDemo.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.cn.hotelDemo.controller.HotelController;
import com.cn.hotelDemo.controller.AuditController;
import com.cn.hotelDemo.controller.LoginController;
import com.cn.hotelDemo.repository.UserRepository;
import com.cn.hotelDemo.service.AuditService;
import com.cn.hotelDemo.service.HotelService;
import com.cn.hotelDemo.service.UserService;

import org.springframework.context.annotation.Import;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({HotelController.class, AuditController.class, LoginController.class})
@Import(HotelSecurityConfig.class)
@TestPropertySource(properties = {
    "app.security.enabled=true",
    "spring.autoconfigure.exclude=",
    "app.security.jwt-issuer-uri=https://accounts.google.com"
})
public class HotelSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HotelService hotelService;

    @MockBean
    private UserService userService;

    @MockBean
    private AuditService auditService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    @WithMockUser(roles = "NORMAL")
    public void getAllHotels_asNormalUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/hotel/getAll"))
               .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getAllHotels_asAdminUser_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/hotel/getAll"))
               .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "NORMAL")
    public void getAllAuditLogs_asNormalUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/audit/getAll"))
               .andExpect(status().isForbidden());
    }

    @Test
    public void loginEndpoint_shouldBePermittedWithoutAuth() throws Exception {
        mockMvc.perform(get("/login"))
               .andExpect(status().isOk());
    }
}
