package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.dto.DebitRequestDto;
import com.Tithaal.Wallet.entity.Wallet;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class WalletTransactionIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private WalletRepository walletRepository;
    @Autowired private WalletTransactionRepository transactionRepository;

    private Wallet walletA;
    private Wallet walletB;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();

        UUID userAId = UUID.randomUUID();
        UUID userBId = UUID.randomUUID();

        walletA = walletRepository.save(Wallet.builder()
                .userId(userAId)
                .balance(new BigDecimal("1000.00"))
                .createdAt(Instant.now())
                .build());

        walletB = walletRepository.save(Wallet.builder()
                .userId(userBId)
                .balance(new BigDecimal("0.00"))
                .createdAt(Instant.now())
                .build());
    }

    @Test
    @WithMockUser(username = "userA", roles = {"USER"})
    void shouldPerformSuccessfulTransfer() throws Exception {
        DebitRequestDto transferRequest = new DebitRequestDto();
        transferRequest.setSendingWalletId(walletA.getId());
        transferRequest.setReceivingWalletId(walletB.getId());
        transferRequest.setAmount(new BigDecimal("200.00"));

        mockMvc.perform(post("/api/wallet/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk());

        Wallet updatedWalletA = walletRepository.findById(walletA.getId()).orElseThrow();
        Wallet updatedWalletB = walletRepository.findById(walletB.getId()).orElseThrow();

        assert updatedWalletA.getBalance().compareTo(new BigDecimal("800.00")) == 0;
        assert updatedWalletB.getBalance().compareTo(new BigDecimal("200.00")) == 0;
    }

    @Test
    @WithMockUser(username = "userA", roles = {"USER"})
    void shouldFailOnInsufficientFunds() throws Exception {
        DebitRequestDto transferRequest = new DebitRequestDto();
        transferRequest.setSendingWalletId(walletA.getId());
        transferRequest.setReceivingWalletId(walletB.getId());
        transferRequest.setAmount(new BigDecimal("1200.00"));

        mockMvc.perform(post("/api/wallet/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient balance"));
    }
}
