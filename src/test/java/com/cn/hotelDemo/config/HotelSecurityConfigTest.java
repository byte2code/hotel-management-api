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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.core.convert.converter.Converter;

import com.cn.hotelDemo.controller.HotelController;
import com.cn.hotelDemo.controller.AuditController;
import com.cn.hotelDemo.controller.LoginController;
import com.cn.hotelDemo.repository.UserRepository;
import com.cn.hotelDemo.service.AuditService;
import com.cn.hotelDemo.service.HotelService;
import com.cn.hotelDemo.service.UserService;

import org.springframework.context.annotation.Import;
import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;
import com.cn.hotelDemo.model.User;

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

    @Test
    public void testJwtAuthenticationConverter_WithRealmAccessRoles() {
        HotelSecurityConfig config = new HotelSecurityConfig(userRepository, null);
        JwtAuthenticationConverter converter = config.jwtAuthenticationConverter();
        
        Map<String, Object> claims = new HashMap<>();
        LinkedTreeMap<String, List<String>> realmAccess = new LinkedTreeMap<>();
        realmAccess.put("roles", List.of("admin", "user"));
        claims.put("realm_access", realmAccess);
        
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaims()).thenReturn(claims);
        
        @SuppressWarnings("unchecked")
        Converter<Jwt, Collection<GrantedAuthority>> authoritiesConverter = 
            (Converter<Jwt, Collection<GrantedAuthority>>) (Object) 
            org.springframework.test.util.ReflectionTestUtils.getField(converter, "jwtGrantedAuthoritiesConverter");
            
        Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);
        
        assertEquals(2, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("admin")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("user")));
    }

    @Test
    public void testUserAuthoritiesMapper_WithOAuth2UserAuthority() {
        HotelSecurityConfig config = new HotelSecurityConfig(userRepository, null);
        GrantedAuthoritiesMapper mapper = config.userAuthoritiesMapper();
        
        Map<String, Object> attributes = new HashMap<>();
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", List.of("admin", "manager"));
        attributes.put("realm_access", realmAccess);
        
        OAuth2UserAuthority authority = new OAuth2UserAuthority(attributes);
        
        Collection<? extends GrantedAuthority> mappedAuthorities = mapper.mapAuthorities(Set.of(authority));
        
        assertEquals(2, mappedAuthorities.size());
        assertTrue(mappedAuthorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(mappedAuthorities.contains(new SimpleGrantedAuthority("ROLE_MANAGER")));
    }

    @Test
    public void testUserAuthoritiesMapper_WithOidcUserAuthority_KnownUser() throws Exception {
        HotelSecurityConfig config = new HotelSecurityConfig(userRepository, null);
        GrantedAuthoritiesMapper mapper = config.userAuthoritiesMapper();
        
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "test@example.com");
        
        OidcIdToken idToken = mock(OidcIdToken.class);
        when(idToken.getIssuer()).thenReturn(java.net.URI.create("https://accounts.google.com").toURL());
        
        OidcUserAuthority mockAuthority = mock(OidcUserAuthority.class);
        when(mockAuthority.getAttributes()).thenReturn(attributes);
        when(mockAuthority.getIdToken()).thenReturn(idToken);
        
        User dbUser = new User();
        dbUser.setRole("ADMIN");
        when(userRepository.findByEmail("test@example.com")).thenReturn(dbUser);
        
        Collection<? extends GrantedAuthority> mappedAuthorities = mapper.mapAuthorities(Set.of(mockAuthority));
        
        assertEquals(1, mappedAuthorities.size());
        assertTrue(mappedAuthorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }
}
