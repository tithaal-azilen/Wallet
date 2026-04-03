package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.entity.*;
import com.Tithaal.Wallet.repository.WalletRepository;
import com.Tithaal.Wallet.repository.WalletTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserTransactionHistoryIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private WalletRepository walletRepository;
    @Autowired private WalletTransactionRepository transactionRepository;

    private Wallet wallet;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();

        UUID userId = UUID.randomUUID();
        wallet = walletRepository.save(Wallet.builder()
                .userId(userId)
                .balance(new BigDecimal("500.00"))
                .createdAt(Instant.now())
                .build());

        // Seed 5 transactions
        for (int i = 0; i < 5; i++) {
            transactionRepository.save(WalletTransaction.builder()
                    .wallet(wallet)
                    .amount(new BigDecimal("10.00"))
                    .type(i % 2 == 0 ? TransactionType.CREDIT : TransactionType.DEBIT)
                    .balanceAfter(new BigDecimal("500.00"))
                    .description("Test tx " + i)
                    .referenceId(UUID.randomUUID().toString())
                    .createdAt(Instant.now().minusSeconds(3600L * i))
                    .build());
        }
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldGetPaginatedUserHistory() throws Exception {
        mockMvc.perform(get("/api/ledger/me")
                .param("page", "0").param("size", "3"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldGetWalletSpecificHistory() throws Exception {
        mockMvc.perform(get("/api/ledger/me/wallet/" + wallet.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldDownloadUserHistoryPdf() throws Exception {
        mockMvc.perform(get("/api/ledger/me/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }
}
