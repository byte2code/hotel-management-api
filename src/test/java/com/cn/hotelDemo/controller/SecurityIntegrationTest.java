package com.cn.hotelDemo.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "app.security.enabled=true",
    "spring.autoconfigure.exclude=",
    "app.security.jwt-issuer-uri=https://accounts.google.com"
})
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @Test
    public void unauthenticatedAccess_shouldReturnUnauthorized() throws Exception {
        // Expect 401 for protected endpoints when no authentication is provided
        mockMvc.perform(get("/hotel/getAll"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void publicEndpoint_shouldReturnOk() throws Exception {
        // Expect 200 for public endpoints like /login
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    public void swaggerDocs_shouldReturnOk() throws Exception {
        // Expect 200 for public swagger endpoints
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "NORMAL")
    public void normalUser_accessingAdminEndpoint_shouldReturnForbidden() throws Exception {
        // Expect 403 when a NORMAL role tries to access an ADMIN-only endpoint
        mockMvc.perform(get("/hotel/getAll"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void adminUser_accessingAdminEndpoint_shouldReturnOk() throws Exception {
        // Expect 200 when an ADMIN role accesses an ADMIN-only endpoint
        mockMvc.perform(get("/hotel/getAll"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "NORMAL")
    public void normalUser_accessingNormalEndpoint_shouldPassSecurity() throws Exception {
        // Expect 404 (Not Found) instead of 401/403 because the user passes security
        // but the requested hotel ID (1) does not exist in the database.
        mockMvc.perform(get("/hotel/id/1"))
                .andExpect(status().isNotFound());
    }
}
