package com.Tithaal.Wallet.controller;

import com.Tithaal.Wallet.dto.RegisterDto;
import com.Tithaal.Wallet.security.JwtTokenProvider;
import com.Tithaal.Wallet.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    public void register_Success() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("validUser");
        registerDto.setEmail("valid@example.com");
        registerDto.setPassword("password123");
        registerDto.setCity("Test City");
        registerDto.setPhoneNumber("1234567890");

        when(userService.registerUser(any(RegisterDto.class))).thenReturn("User Registered Successfully!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated());
    }

    @Test
    public void register_Fail_InvalidUsername() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("invalid user"); // invalid characters (space)
        registerDto.setEmail("valid@example.com");
        registerDto.setPassword("password123");
        registerDto.setCity("Test City");
        registerDto.setPhoneNumber("1234567890");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void register_Fail_InvalidEmail() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("validUser");
        registerDto.setEmail("invalid-email");
        registerDto.setPassword("password123");
        registerDto.setCity("Test City");
        registerDto.setPhoneNumber("1234567890");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void register_Fail_InvalidPhone() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("validUser");
        registerDto.setEmail("valid@example.com");
        registerDto.setPassword("password123");
        registerDto.setCity("Test City");
        registerDto.setPhoneNumber("12345"); // Too short

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void register_Fail_EmptyCity() throws Exception {
        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("validUser");
        registerDto.setEmail("valid@example.com");
        registerDto.setPassword("password123");
        registerDto.setCity(""); // Empty
        registerDto.setPhoneNumber("1234567890");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest());
    }
}
