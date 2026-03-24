package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.dto.LoginDto;
import com.Tithaal.Wallet.dto.RegisterDto;
import com.Tithaal.Wallet.entity.Organization;
import com.Tithaal.Wallet.entity.OrganizationStatus;
import com.Tithaal.Wallet.redis.IdempotencyRecordRepository;
import com.Tithaal.Wallet.repository.OrganizationRepository;
import com.Tithaal.Wallet.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Disabled("Requires real Redis instance")
public class IdempotencyFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrganizationRepository organizationRepository;

    private String orgCode = "AUTHORD03";
    private LoginDto loginDto;

    @BeforeEach
    void setUp() throws Exception {
        organizationRepository.save(Organization.builder()
                .name("IdempotentOrg2")
                .orgCode(orgCode)
                .status(OrganizationStatus.ACTIVE)
                .createdAt(Instant.now())
                .build());

        RegisterDto registerDto = new RegisterDto();
        registerDto.setUsername("idempuser3");
        registerDto.setEmail("idempuser3@example.com");
        registerDto.setPassword("password123");
        registerDto.setCity("Pune");
        registerDto.setPhoneNumber("1234567893");
        registerDto.setOrgCode(orgCode);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isCreated());

        loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("idempuser3");
        loginDto.setPassword("password123");
    }

    @Test
    void shouldReturnCachedResponseForDuplicateIdempotencyKey() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();

        MvcResult firstResponse = mockMvc.perform(post("/api/auth/login")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult secondResponse = mockMvc.perform(post("/api/auth/login")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(firstResponse.getResponse().getContentAsString(), secondResponse.getResponse().getContentAsString());
    }

}
