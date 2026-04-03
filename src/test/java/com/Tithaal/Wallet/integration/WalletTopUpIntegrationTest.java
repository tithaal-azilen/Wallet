package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.dto.CreditRequestDto;
import com.Tithaal.Wallet.entity.*;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for wallet top-up using mock JWT (no local login endpoint).
 * Uses Spring Security's WithMockUser / JWT test support.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class WalletTopUpIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private WalletRepository walletRepository;

    private UUID userId;
    private Long walletId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        Wallet wallet = walletRepository.save(Wallet.builder()
                .userId(userId)
                .balance(new BigDecimal("100.00"))
                .createdAt(Instant.now())
                .nextDeductionDate(java.time.LocalDate.now().plusMonths(1))
                .build());
        walletId = wallet.getId();
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "testuser", roles = {"USER"})
    void shouldTopUpWalletSuccessfully() throws Exception {
        CreditRequestDto dto = new CreditRequestDto();
        dto.setCreditCardNumber("4111111111111111");
        dto.setAmount(new BigDecimal("250.00"));

        mockMvc.perform(post("/api/wallet/" + walletId + "/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        Wallet updated = walletRepository.findById(walletId).orElseThrow();
        assertEquals(0, new BigDecimal("350.00").compareTo(updated.getBalance()));
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "testuser", roles = {"USER"})
    void shouldFailTopUpWithZeroAmount() throws Exception {
        CreditRequestDto dto = new CreditRequestDto();
        dto.setCreditCardNumber("4111111111111111");
        dto.setAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/api/wallet/" + walletId + "/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "testuser", roles = {"USER"})
    void shouldFailTopUpWithNegativeAmount() throws Exception {
        CreditRequestDto dto = new CreditRequestDto();
        dto.setCreditCardNumber("4111111111111111");
        dto.setAmount(new BigDecimal("-50.00"));

        mockMvc.perform(post("/api/wallet/" + walletId + "/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "testuser", roles = {"USER"})
    void shouldFailTopUpWithMissingCardNumber() throws Exception {
        CreditRequestDto dto = new CreditRequestDto();
        dto.setAmount(new BigDecimal("50.00"));

        mockMvc.perform(post("/api/wallet/" + walletId + "/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}
