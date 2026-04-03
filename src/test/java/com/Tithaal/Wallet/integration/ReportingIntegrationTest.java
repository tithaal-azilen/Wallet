package com.Tithaal.Wallet.integration;

import com.Tithaal.Wallet.entity.*;
import com.Tithaal.Wallet.repository.OrganizationRepository;
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
public class ReportingIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private WalletTransactionRepository transactionRepository;

    private Organization org1;
    private Organization org2;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        organizationRepository.deleteAll();

        org1 = organizationRepository.save(Organization.builder()
                .name("Org One").orgCode("ORG1")
                .status(OrganizationStatus.ACTIVE).createdAt(Instant.now()).build());

        org2 = organizationRepository.save(Organization.builder()
                .name("Org Two").orgCode("ORG2")
                .status(OrganizationStatus.ACTIVE).createdAt(Instant.now()).build());

        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();

        Wallet wallet1 = walletRepository.save(Wallet.builder()
                .userId(user1Id)
                .balance(new BigDecimal("1000.00"))
                .createdAt(Instant.now())
                .build());

        Wallet wallet2 = walletRepository.save(Wallet.builder()
                .userId(user2Id)
                .balance(new BigDecimal("500.00"))
                .createdAt(Instant.now())
                .build());

        for (int i = 0; i < 5; i++) {
            transactionRepository.save(WalletTransaction.builder()
                    .wallet(wallet1)
                    .amount(new BigDecimal("10.00"))
                    .type(TransactionType.CREDIT)
                    .balanceAfter(new BigDecimal("1000.00").add(new BigDecimal("10.00").multiply(new BigDecimal(i + 1))))
                    .description("Test transaction " + i)
                    .referenceId(UUID.randomUUID().toString())
                    .createdAt(Instant.now().minusSeconds(3600L * i))
                    .build());
        }

        for (int i = 0; i < 3; i++) {
            transactionRepository.save(WalletTransaction.builder()
                    .wallet(wallet2)
                    .amount(new BigDecimal("20.00"))
                    .type(TransactionType.DEBIT)
                    .balanceAfter(new BigDecimal("500.00").subtract(new BigDecimal("20.00").multiply(new BigDecimal(i + 1))))
                    .description("Other org transaction " + i)
                    .referenceId(UUID.randomUUID().toString())
                    .createdAt(Instant.now())
                    .build());
        }
    }

    @Test
    @WithMockUser(username = "admin1", roles = {"ORG_ADMIN"})
    void orgAdminShouldSeeOrgTransactions() throws Exception {
        mockMvc.perform(get("/api/organizations/transactions/" + org1.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "superadmin", roles = {"SUPER_ADMIN"})
    void superAdminShouldSeeAllTransactions() throws Exception {
        mockMvc.perform(get("/api/platform/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(8));
    }

    @Test
    @WithMockUser(username = "admin1", roles = {"ORG_ADMIN"})
    void shouldDownloadOrgPdfReport() throws Exception {
        mockMvc.perform(get("/api/organizations/transactions/" + org1.getId() + "/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    @WithMockUser(username = "superadmin", roles = {"SUPER_ADMIN"})
    void shouldDownloadPlatformPdfReport() throws Exception {
        mockMvc.perform(get("/api/platform/transactions/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }
}
