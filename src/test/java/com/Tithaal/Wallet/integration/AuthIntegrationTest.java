package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.dto.RegisterDto;
import com.Tithaal.Wallet.entity.Organization;
import com.Tithaal.Wallet.entity.OrganizationStatus;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String orgCode = "AUTHORD01";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        organizationRepository.deleteAll();

        organizationRepository.save(Organization.builder()
                .name("AuthOrg")
                .orgCode(orgCode)
                .status(OrganizationStatus.ACTIVE)
                .createdAt(Instant.now())
                .build());
    }

    @Test
    void shouldRegisterLoginAndRefreshAndLogout() throws Exception {
        // 1. Success Registration
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("testuser");
        registerDto.setEmail("testuser@example.com");
        registerDto.setPassword("password123");
        registerDto.setCity("Pune");
        registerDto.setPhoneNumber("1234567890");
        registerDto.setOrgCode(orgCode);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated());

        // 2. Success Login
        LoginDto loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("testuser");
        loginDto.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("accessToken").asText();
        Cookie refreshTokenCookie = loginResult.getResponse().getCookie("refreshToken");

        // 3. Use Access Token (Corrected endpoint /api/users/{id})
        com.Tithaal.Wallet.entity.User user = userRepository.findByUsername("testuser").orElseThrow();
        mockMvc.perform(get("/api/users/" + user.getId())
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));

        // 4. Refresh Token
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refreshtoken")
                .cookie(refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        String newAccessToken = objectMapper.readTree(refreshResult.getResponse().getContentAsString()).get("accessToken").asText();
        Cookie newRefreshTokenCookie = refreshResult.getResponse().getCookie("refreshToken");

        // 5. Logout
        mockMvc.perform(post("/api/auth/logout")
                .cookie(newRefreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("refreshToken", 0));

        // 6. Refresh after logout should fail (depending on implementation, usually invalidates refresh token in DB)
        mockMvc.perform(post("/api/auth/refreshtoken")
                .cookie(newRefreshTokenCookie))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailOnDuplicateEmail() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("user1");
        registerDto.setEmail("duplicate@example.com");
        registerDto.setPassword("password123");
        registerDto.setCity("Pune");
        registerDto.setPhoneNumber("1234567890");
        registerDto.setOrgCode(orgCode);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated());

        RegisterDto registerDto2 = new RegisterDto();
        registerDto2.setUsername("user2");
        registerDto2.setEmail("duplicate@example.com");
        registerDto2.setPassword("password123");
        registerDto2.setCity("Mumbai");
        registerDto2.setOrgCode(orgCode);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto2)))
                .andExpect(status().isBadRequest()); // Or 409 depending on GlobalExceptionHandler
    }
}
